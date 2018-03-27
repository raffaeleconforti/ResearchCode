package com.raffaeleconforti.noisefiltering.timestamp;

import com.raffaeleconforti.automaton.Automaton;
import com.raffaeleconforti.automaton.AutomatonFactory;
import com.raffaeleconforti.kernelestimation.distribution.impl.EventDistributionCalculatorNoiseImpl;
import com.raffaeleconforti.log.util.LogCloner;
import com.raffaeleconforti.log.util.NameExtractor;
import com.raffaeleconforti.log.util.TraceToString;
import com.raffaeleconforti.noisefiltering.event.InfrequentBehaviourFilter;
import com.raffaeleconforti.noisefiltering.event.infrequentbehaviour.automaton.AutomatonInfrequentBehaviourDetector;
import com.raffaeleconforti.noisefiltering.timestamp.check.TimeStampChecker;
import com.raffaeleconforti.noisefiltering.timestamp.permutation.EventPermutator;
import com.raffaeleconforti.noisefiltering.timestamp.permutation.EventPermutatorSmart;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by conforti on 14/01/2016.
 */
public class TimeStampFixerSmart implements TimeStampFixer {

    private AutomatonInfrequentBehaviourDetector automatonInfrequentBehaviourDetector = new AutomatonInfrequentBehaviourDetector(AutomatonInfrequentBehaviourDetector.MAX);
    private final AutomatonFactory automatonFactory;

    private final boolean debug_mode;

    private final LogCloner logCloner;
    private final NameExtractor nameExtractor;
    private final TimeStampChecker timeStampChecker;
    private final XEventClassifier xEventClassifier;
    private final XLog log;
    private final XFactory factory;
    private final boolean useGurobi;
    private final boolean useArcsFrequency;

    private EventDistributionCalculatorNoiseImpl eventDistributionCalculator;
    private EventPermutator eventPermutator;

    private final Map<String, Set<XTrace>> possibleTraces = new UnifiedMap<>();
    private final Map<String, Set<String>> faultyEvents = new UnifiedMap<>();
    private final Map<String, XTrace> originalTraces = new UnifiedMap<>();

    private final Set<String> sequences = new UnifiedSet<>();
    private final Map<List<String>, Set<List<String>>> patternsMap = new UnifiedMap<>();

    private XLog duplicatedLog;
    private XLog noiseFreeLog;

    private int approach;
    private boolean self_cleaning;

    public TimeStampFixerSmart(XFactory factory, LogCloner logCloner, XLog rawlog, XEventClassifier xEventClassifier, SimpleDateFormat dateFormatSeconds, int limitExtensive, int approach, boolean useGurobi, boolean useArcsFrequency, boolean debug_mode, boolean self_cleaning) {
        this.debug_mode = debug_mode;
        this.factory = factory;
        this.logCloner = logCloner;
        log = rawlog;
        this.approach = approach;
        this.useGurobi = useGurobi;
        this.useArcsFrequency = useArcsFrequency;

        this.xEventClassifier = xEventClassifier;
        this.automatonFactory = new AutomatonFactory(xEventClassifier);
        this.nameExtractor = new NameExtractor(xEventClassifier);
        this.timeStampChecker = new TimeStampChecker(xEventClassifier, dateFormatSeconds);

        this.self_cleaning = self_cleaning;
        initialize(limitExtensive);
    }

    private String getEventName(XEvent event) {
        return nameExtractor.getEventName(event);
    }

    private String getTraceName(XTrace trace) {
        return nameExtractor.getTraceName(trace);
    }

    private void initialize(int limitExtensive) {
        noiseFreeLog = factory.createLog(log.getAttributes());
        discoverSequences();

        for(XTrace t : log) {
            if(!timeStampChecker.containsSameTimestamps(t)) {
                noiseFreeLog.add(logCloner.getXTrace(t));
            }else {
                Set<Set<XEvent>> setXEvents = timeStampChecker.findEventsSameTimeStamp(t);
                Set<String> stringSet = new UnifiedSet<>();
                for(Set<XEvent> set : setXEvents) {
                    for(XEvent e : set) {
                        stringSet.add(getEventName(e));
                    }
                    faultyEvents.put(getTraceName(t), stringSet);
                }
            }
        }


        eventDistributionCalculator = new EventDistributionCalculatorNoiseImpl(log, xEventClassifier, faultyEvents, self_cleaning);
        eventDistributionCalculator.analyseLog();
        eventPermutator = new EventPermutatorSmart(logCloner, factory, xEventClassifier, eventDistributionCalculator, timeStampChecker, sequences, patternsMap, limitExtensive, approach, debug_mode);

        if(true) {
            Automaton<String> automatonOriginal = automatonFactory.generate(log);
            InfrequentBehaviourFilter infrequentBehaviourFilter = new InfrequentBehaviourFilter(xEventClassifier);

            double[] arcs = infrequentBehaviourFilter.discoverArcs(automatonOriginal, 1.0);

            Automaton<String> automatonClean = automatonInfrequentBehaviourDetector.removeInfrequentBehaviour(automatonOriginal, automatonOriginal.getNodes(), infrequentBehaviourFilter.discoverThreshold(arcs, 0.125), useGurobi, useArcsFrequency);

            eventDistributionCalculator.filter(automatonClean);
        }
    }

    private XLog permuteLog() {
        duplicatedLog = factory.createLog(log.getAttributes());
        for(XTrace t : log) {
            originalTraces.put(getTraceName(t), t);

            if(timeStampChecker.containsSameTimestamps(t)) {
                Set<XTrace> traces = eventPermutator.duplicatesTrace(t);
                if(traces.size() > 0) {
                    duplicatedLog.addAll(traces);
                    possibleTraces.put(getTraceName(t), traces);
                }else {
                    duplicatedLog.add(logCloner.getXTrace(t));
                }
            }else {
                duplicatedLog.add(logCloner.getXTrace(t));
            }
        }
        return  duplicatedLog;
    }

    public XLog obtainPermutedLog() {
        if(duplicatedLog == null) {
            permuteLog();
        }
        return  duplicatedLog;
    }

    public Set<String> getDuplicatedTraces() {
        return  eventPermutator.getDuplicatedTraces();
    }

    private void discoverSequences() {
        for(XTrace trace : log) {
            if(!timeStampChecker.containsSameTimestamps(trace)) {
                for (int i = 0; i < trace.size(); i++) {
                    List<String> labels = new ArrayList<>(trace.size());
                    labels.add(getEventName(trace.get(i)));
                    for (int j = i + 1; j < trace.size(); j++) {
                        labels.add(getEventName(trace.get(j)));
                        sequences.add(TraceToString.listToString(labels));

                        List<String> pattern = new ArrayList<>(labels);
                        Collections.sort(pattern);
                        Set<List<String>> patterns;
                        if((patterns = patternsMap.get(pattern)) == null) {
                            patterns = new UnifiedSet<>();
                            patternsMap.put(pattern, patterns);
                        }
                        patterns.add(pattern);
                    }
                }
            }
        }
    }

    public Map<String, XTrace> getOriginalTraces() {
        return originalTraces;
    }

    public Map<String, Set<String>> getDuplicatedEvents() {
        return eventPermutator.getDuplicatedEvents();
    }

    public XLog getNoiseFreeLog() {
        return noiseFreeLog;
    }

    public Map<String,Set<XTrace>> getPossibleTraces() {
        return possibleTraces;
    }

    public Map<String, Set<String>> getFaultyEvents() {
        return faultyEvents;
    }
}
