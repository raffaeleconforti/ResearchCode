package com.raffaeleconforti.noisefiltering.timestamp;

import com.raffaeleconforti.automaton.Automaton;
import com.raffaeleconforti.automaton.AutomatonFactory;
import com.raffaeleconforti.kernelestimation.distribution.impl.EventDurationDistributionCalculatorNoiseImpl;
import com.raffaeleconforti.log.util.NameExtractor;
import com.raffaeleconforti.noisefiltering.event.InfrequentBehaviourFilter;
import com.raffaeleconforti.noisefiltering.event.infrequentbehaviour.automaton.AutomatonInfrequentBehaviourDetector;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by conforti on 28/01/2016.
 */
public class TimestampsAssigner {

    private AutomatonInfrequentBehaviourDetector automatonInfrequentBehaviourDetector = new AutomatonInfrequentBehaviourDetector(AutomatonInfrequentBehaviourDetector.MAX);
    private final AutomatonFactory automatonFactory;

    private final boolean debug_mode;

    private static NameExtractor nameExtractor;
    private static final XTimeExtension xte = XTimeExtension.instance();
    private final XLog log;
    private final Set<String> duplicatedTraces;
    private final EventDurationDistributionCalculatorNoiseImpl eventDurationDistributionCalculator;
    private final SimpleDateFormat dateFormatSeconds;

    public TimestampsAssigner(XLog log, XEventClassifier xEventClassifier, SimpleDateFormat dateFormatSeconds, Set<String> duplicatedTraces, Map<String, Set<String>> duplicatedEvents, boolean useGurobi, boolean useArcsFrequency, boolean debug_mode) {
        this.debug_mode = debug_mode;

        this.log = log;
        nameExtractor = new NameExtractor(xEventClassifier);
        this.automatonFactory = new AutomatonFactory(xEventClassifier);

        this.dateFormatSeconds = dateFormatSeconds;
        this.duplicatedTraces = duplicatedTraces;
        eventDurationDistributionCalculator = new EventDurationDistributionCalculatorNoiseImpl(log, duplicatedEvents, xEventClassifier, debug_mode);

        eventDurationDistributionCalculator.analyseLog();

        if(true) {
            Automaton<String> automatonOriginal = automatonFactory.generate(log);
            InfrequentBehaviourFilter infrequentBehaviourFilter = new InfrequentBehaviourFilter(xEventClassifier);

            double[] arcs = infrequentBehaviourFilter.discoverArcs(automatonOriginal, 1.0);

            Automaton<String> automatonClean = automatonInfrequentBehaviourDetector.removeInfrequentBehaviour(automatonOriginal, automatonOriginal.getNodes(), infrequentBehaviourFilter.discoverThreshold(arcs, 0.125), useGurobi, useArcsFrequency);

            eventDurationDistributionCalculator.filter(automatonClean);
        }
    }

    public boolean assignTimestamps(List<String> fixedTraces) {
        boolean result = false;
        for(XTrace trace : log) {
            if(duplicatedTraces.contains(getTraceName(trace)) && fixedTraces.contains(getTraceName(trace))) {
                Set<int[]> sameTimeStamps = findEventsSameTimeStamp(trace);
                for(int[] couple : sameTimeStamps) {
                    XEvent first = trace.get(couple[0]);

                    long diff;
                    long[] distribution;
                    if (couple[1] < trace.size()) {
                        XEvent last = trace.get(couple[1]);
                        diff = xte.extractTimestamp(last).getTime() - xte.extractTimestamp(first).getTime();
                        distribution = eventDurationDistributionCalculator.estimateDuration(diff, trace.subList(couple[0], couple[1] + 1));
                    }else {
                        diff = Long.MAX_VALUE;
                        List<XEvent> list = new ArrayList<XEvent>(trace.subList(couple[0], couple[1]));
                        list.add(null);
                        distribution = eventDurationDistributionCalculator.estimateDuration(diff, list);
                    }

                    Date date = xte.extractTimestamp(first);
                    if(distribution.length == 1) {
                        date = new Date(date.getTime() + distribution[0]);
                        if (!xte.extractTimestamp(trace.get(couple[0] + 1)).equals(date)) {
                            result = true;
                        } else {
                            if(debug_mode) System.out.println("Unable to fix timestamp for trace " + getTraceName(trace));
                        }

                        xte.assignTimestamp(trace.get(couple[0] + 1), date);
                    }else {
                        for (int i = 1; i < distribution.length; i++) {
                            date = new Date(date.getTime() + distribution[i]);
                            if (!xte.extractTimestamp(trace.get(couple[0] + i)).equals(date)) {
                                result = true;
                            } else {
                                if(debug_mode) System.out.println("Unable to fix timestamp for trace " + getTraceName(trace));
                            }

                            xte.assignTimestamp(trace.get(couple[0] + i), date);
                        }
                    }
                }
            }
        }
        return result;
    }

    public boolean assignTimestampsDummy(List<String> fixedTraces) {
        boolean result = false;
        for(XTrace trace : log) {
            if(duplicatedTraces.contains(getTraceName(trace)) && fixedTraces.contains(getTraceName(trace))) {
                Set<int[]> sameTimeStamps = findEventsSameTimeStamp(trace);
                for(int[] couple : sameTimeStamps) {
                    XEvent first = trace.get(couple[0]);

                    long diff;
                    if (couple[1] < trace.size()) {
                        XEvent last = trace.get(couple[1]);
                        diff = xte.extractTimestamp(last).getTime() - xte.extractTimestamp(first).getTime();
                    }else {
                        diff = Long.MAX_VALUE;
                    }

                    Date date = xte.extractTimestamp(first);
                    long increment = diff / (couple[1] - couple[0]);
                    for (int i = 1; i < couple[1] - couple[0]; i++) {
                        date = new Date(date.getTime() + increment);
                        if (!xte.extractTimestamp(trace.get(couple[0] + i)).equals(date)) {
                            result = true;
                        } else {
                            if(debug_mode) System.out.println("Unable to fix timestamp for trace " + getTraceName(trace));
                        }

                        xte.assignTimestamp(trace.get(couple[0] + i), date);
                    }
                }
            }
        }
        return result;
    }

    public void assignTimestamps() {
        for(XTrace trace : log) {
            if(duplicatedTraces.contains(getTraceName(trace))) {
                Set<int[]> sameTimeStamps = findEventsSameTimeStamp(trace);
                for(int[] couple : sameTimeStamps) {
                    XEvent first = trace.get(couple[0]);

                    long diff;
                    long[] distribution;
                    if (couple[1] < trace.size()) {
                        XEvent last = trace.get(couple[1]);
                        diff = xte.extractTimestamp(last).getTime() - xte.extractTimestamp(first).getTime();
                        distribution = eventDurationDistributionCalculator.estimateDuration(diff, trace.subList(couple[0], couple[1] + 1));
                    }else {
                        diff = Long.MAX_VALUE;
                        List<XEvent> list = new ArrayList<XEvent>(trace.subList(couple[0], couple[1]));
                        list.add(null);
                        distribution = eventDurationDistributionCalculator.estimateDuration(diff, list);
                    }

                    Date date = xte.extractTimestamp(first);
                    if(distribution.length == 1) {
                        date = new Date(date.getTime() + distribution[0]);
                        if(debug_mode && xte.extractTimestamp(trace.get(couple[0] + 1)).equals(date)) System.out.println("Unable to fix timestamp for trace " + getTraceName(trace));

                        xte.assignTimestamp(trace.get(couple[0] + 1), date);
                    }else {
                        for (int i = 1; i < distribution.length; i++) {
                            date = new Date(date.getTime() + distribution[i]);
                            if(debug_mode && xte.extractTimestamp(trace.get(couple[0] + i)).equals(date)) System.out.println("Unable to fix timestamp for trace " + getTraceName(trace));

                            xte.assignTimestamp(trace.get(couple[0] + i), date);
                        }
                    }
                }
            }
        }
    }

    public void assignTimestampsDummy() {
        for(XTrace trace : log) {
            if(duplicatedTraces.contains(getTraceName(trace))) {
                Set<int[]> sameTimeStamps = findEventsSameTimeStamp(trace);
                for(int[] couple : sameTimeStamps) {
                    XEvent first = trace.get(couple[0]);

                    long diff;
                    if (couple[1] < trace.size()) {
                        XEvent last = trace.get(couple[1]);
                        diff = xte.extractTimestamp(last).getTime() - xte.extractTimestamp(first).getTime();
                    }else {
                        diff = Long.MAX_VALUE;
                    }

                    Date date = xte.extractTimestamp(first);
                    long increment = diff / (couple[1] - couple[0]);
                    for (int i = 1; i < couple[1] - couple[0]; i++) {
                        date = new Date(date.getTime() + increment);
                        if(debug_mode && xte.extractTimestamp(trace.get(couple[0] + i)).equals(date)) System.out.println("Unable to fix timestamp for trace " + getTraceName(trace));

                        xte.assignTimestamp(trace.get(couple[0] + i), date);
                    }
                }
            }
        }
    }

    private String getTraceName(XTrace trace) {
        return nameExtractor.getTraceName(trace);
    }

    private Set<int[]> findEventsSameTimeStamp(XTrace trace) {
        Set<int[]> setXEvents = new UnifiedSet<>();
        Map<String, int[]> times = new UnifiedMap<>();
        Map<String, Set<String>> labels = new UnifiedMap<>();

        for(int i = 0; i < trace.size(); i++) {
            XEvent event = trace.get(i);
            String time = dateFormatSeconds.format(xte.extractTimestamp(event));

            int[] set;
            Set<String> setLabels;
            if((set = times.get(time)) == null) {
                set = new int[] {i, -1};
                setLabels = new UnifiedSet<>();
                labels.put(time, setLabels);
                times.put(time, set);
            }else {
                setLabels = labels.get(time);
                set[1] = i + 1;
            }
            setLabels.add(nameExtractor.getEventName(event));

        }

        for(Map.Entry<String, int[]> entry : times.entrySet()) {

            if(entry.getValue()[0] == entry.getValue()[1] - 1) {
                if(debug_mode) System.out.println("");
            }
            if(entry.getValue()[1] > -1) setXEvents.add(entry.getValue());
        }

        return setXEvents;
    }

}
