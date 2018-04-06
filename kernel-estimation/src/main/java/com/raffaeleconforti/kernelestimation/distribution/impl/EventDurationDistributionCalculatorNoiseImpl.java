package com.raffaeleconforti.kernelestimation.distribution.impl;

import com.raffaeleconforti.automaton.Automaton;
import com.raffaeleconforti.automaton.Edge;
import com.raffaeleconforti.automaton.Node;
import com.raffaeleconforti.kernelestimation.distribution.EventDurationDistributionCalculator;
import com.raffaeleconforti.kernelestimation.distribution.mixturemodel.NormalMixtureDistribution;
import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.log.util.NameExtractor;
import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.math.BigInteger;
import java.util.*;

/**
 * Created by conforti on 28/01/2016.
 */
public class EventDurationDistributionCalculatorNoiseImpl implements EventDurationDistributionCalculator {

    private final Map<String, AbstractRealDistribution> distributionsMap = new UnifiedMap<>();
    private final Map<String, Map<String, List<Long>>> durations = new UnifiedMap<>(); //Previous Event Duration

    private final Map<String, Set<String>> duplicatedEvents;
    private final NameExtractor nameExtractor;
    private final XLog log;
    private final XTimeExtension xte = XTimeExtension.instance();
    private final boolean debug_mode;

    public static void main(String[] args) throws Exception {
        XLog log = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/SharedFolder/Logs/TimeNoise/LoanApplication.xes.gz");
        EventDistributionCalculatorNoiseImpl dc = new EventDistributionCalculatorNoiseImpl(log, new XEventNameClassifier(), null, false);
        dc.analyseLog();
        System.out.println(dc.computeLikelihood(log.get(100)));
    }

    public EventDurationDistributionCalculatorNoiseImpl(XLog log, Map<String, Set<String>> duplicatedEvents, XEventClassifier xEventClassifier, boolean debug_mode) {
        this.log = log;
        this.nameExtractor = new NameExtractor(xEventClassifier);
        this.duplicatedEvents = duplicatedEvents;
        this.debug_mode = debug_mode;
    }

    public void analyseLog() {
        for(XTrace trace : log) {
            XEvent previous = null;
            String previousName;
            for(XEvent event : trace) {
                if(previous != null) {
                    previousName = nameExtractor.getEventName(previous);
                    String eventName = nameExtractor.getEventName(event);

                    if((duplicatedEvents.get(nameExtractor.getTraceName(trace)) == null || !duplicatedEvents.get(nameExtractor.getTraceName(trace)).contains(previousName)) &&
                            (duplicatedEvents.get(nameExtractor.getTraceName(trace)) == null || !duplicatedEvents.get(nameExtractor.getTraceName(trace)).contains(eventName))) {
                        Map<String, List<Long>> map;
                        if ((map = durations.get(previousName)) == null) {
                            map = new UnifiedMap<>();
                        }
                        List<Long> list;
                        if ((list = map.get(eventName)) == null) {
                            list = new ArrayList<>();
                        }
                        Date timeLast = xte.extractTimestamp(previous);
                        Date timeEvent = xte.extractTimestamp(event);

                        Long time = timeEvent.getTime() - timeLast.getTime();

                        list.add(time);

                        map.put(eventName, list);
                        durations.put(previousName, map);

                    }
                }
                previous = event;
            }
        }
    }

    public long[] estimateDuration(long maxTimeFrame, List<XEvent> events) {
        long totalDuration = 0;
        long[] durations = new long[events.size() - 1];

        boolean positive = true;
        for (int i = 1; i < events.size() - 1; i++) {
            XEvent previousEvent = events.get(i - 1);
            XEvent event = events.get(i);

            long estimate = 0;
            try {
                estimate = estimateDuration(event, previousEvent);
                if(estimate < 0) estimate = -estimate;
            }catch (NoDataAvailableException ndae) {
                if(debug_mode) {
                    System.out.println("Data not available for sequence " + nameExtractor.getEventName(previousEvent) + " " + nameExtractor.getEventName(event));
                }
            }

            try {
                if(estimate == 0) estimate = getAverageDuration(nameExtractor.getEventName(event), nameExtractor.getEventName(previousEvent));
            }catch (NoDataAvailableException ndae) {
                if(debug_mode) {
                    System.out.println("Data not available for sequence " + nameExtractor.getEventName(previousEvent) + " " + nameExtractor.getEventName(event));
                }
            }

            try {
                if(estimate == 0) estimate = estimateDuration(event);
            }catch (NoDataAvailableException ndae) {
                if(debug_mode) {
                    System.out.println("Data not available for sequence " + nameExtractor.getEventName(previousEvent) + " " + nameExtractor.getEventName(event));
                }
            }

            if(estimate < 0) estimate = -estimate;

            try {
                if(estimate == 0) estimate = getAverageDuration(nameExtractor.getEventName(event));
            }catch (NoDataAvailableException ndae) {
                if(debug_mode) {
                    System.out.println("Data not available for sequence " + nameExtractor.getEventName(previousEvent) + " " + nameExtractor.getEventName(event));
                }
            }

            durations[i] = estimate;
            totalDuration += durations[i];
            if(durations[i] <= 0) positive = false;
        }

        if(!positive) {
            if(debug_mode) {
                System.out.println("Error in the distribution");
            }
            for (int i = 1; i < events.size() - 1; i++) {
                if(durations[i] <= 0) {
                    durations[i] = 1000;
                }
            }
        }

        long totalDuration2 = totalDuration;
        if(totalDuration > maxTimeFrame) {
            totalDuration = 0;
            BigInteger max = BigInteger.valueOf(maxTimeFrame - 1000);
            BigInteger dur = BigInteger.valueOf(totalDuration2);
            for (int i = 1; i < events.size() - 1; i++) {
                BigInteger d = BigInteger.valueOf(durations[i]);
                BigInteger bigInteger = d.multiply(max).divide(dur);
                durations[i] = bigInteger.longValue();
                totalDuration += durations[i];
            }
        }

        if(totalDuration > maxTimeFrame) {
            System.out.println("EXCEEDING");
        }

        if(durations.length == 1) durations[0] = 1000;

        return durations;
    }

    public long estimateDuration(XEvent event, XEvent preceedingEventName) throws NoDataAvailableException {
        return estimateDuration(nameExtractor.getEventName(event), nameExtractor.getEventName(preceedingEventName));
    }

    public long estimateDuration(XEvent event) throws NoDataAvailableException {
        return estimateDuration(nameExtractor.getEventName(event));
    }

    private long estimateDuration(String eventName, String preceedingEventName) throws NoDataAvailableException {
        AbstractRealDistribution d = guessDistribution(eventName, preceedingEventName);
        return (long) d.sample();
    }

    private AbstractRealDistribution guessDistribution(String eventName, String preceedingEventName) throws NoDataAvailableException {
        AbstractRealDistribution distribution = null;
        String key = preceedingEventName + "+|+" + eventName;
        if((distribution = distributionsMap.get(key)) == null) {
            KolmogorovSmirnovTest kolmogorovSmirnovTest = new KolmogorovSmirnovTest();
            MannWhitneyUTest mannWhitneyUTest = new MannWhitneyUTest();

            if(durations.get(preceedingEventName) == null) throw new NoDataAvailableException();
            List<Long> list = durations.get(preceedingEventName).get(eventName);

            if(list == null || list.size() == 0) throw new NoDataAvailableException();
            double[] data = new double[list.size()];
            for (int i = 0; i < list.size(); i++) {
                data[i] = list.get(i);
            }

            NormalDistribution normalDistribution;
            ExponentialDistribution exponentialDistribution = null;
            UniformRealDistribution uniformRealDistribution = null;
            NormalMixtureDistribution normalMixtureDistribution;

            if(data.length > 100) {
                normalDistribution = new NormalDistribution(getAverageDuration(eventName, preceedingEventName), getStdDeviationDuration(eventName, preceedingEventName));

                try {
                    exponentialDistribution = new ExponentialDistribution(getAverageDuration(eventName, preceedingEventName));
                } catch (NotStrictlyPositiveException nspe) { }

                try {
                    uniformRealDistribution = new UniformRealDistribution(getMinDuration(eventName, preceedingEventName), getMaxDuration(eventName, preceedingEventName));
                } catch (NumberIsTooLargeException nitle) { }

                try {
                    normalMixtureDistribution = new NormalMixtureDistribution(data);
                    kolmogorovSmirnovTest.kolmogorovSmirnovTest(normalMixtureDistribution, data);
                    mannWhitneyUTest.mannWhitneyUTest(normalMixtureDistribution.sample(data.length), data);
                } catch (NotStrictlyPositiveException nspe) {
                    normalMixtureDistribution = null;
                }

                AbstractRealDistribution[] distributions = new AbstractRealDistribution[]{
                        normalDistribution,
                        exponentialDistribution,
                        uniformRealDistribution,
                        normalMixtureDistribution
                };

                Double[] significances = new Double[4];
                for (int i = 0; i < significances.length; i++) {
                    if (distributions[i] != null) {
                        significances[i] = Math.max(kolmogorovSmirnovTest.kolmogorovSmirnovTest(distributions[i], data), mannWhitneyUTest.mannWhitneyUTest(distributions[i].sample(data.length), data));
                    } else {
                        significances[i] = null;
                    }
                }

                double max = -1;
                for (int i = 0; i < distributions.length; i++) {
                    if (significances[i] != null && max < significances[i]) {
                        distribution = distributions[i];
                        max = significances[i];
                    }
                }
            }else {
                distribution = new NormalDistribution(getAverageDuration(eventName, preceedingEventName), getStdDeviationDuration(eventName, preceedingEventName));
            }

            distributionsMap.put(key, distribution);

        }

        return distribution;
    }

    private long estimateDuration(String eventName) throws NoDataAvailableException {
        NormalDistribution d = new NormalDistribution(getAverageDuration(eventName), getStdDeviationDuration(eventName));
        return (long) d.sample();
    }

    private long getMaxDuration(String eventName, String preceedingEventName) throws NoDataAvailableException {
        long max = Long.MIN_VALUE;

        if(durations.get(preceedingEventName) == null) throw new NoDataAvailableException();
        List<Long> list = durations.get(preceedingEventName).get(eventName);

        if(list == null || list.size() == 0) throw new NoDataAvailableException();
        for(Long partialDuration : list) {
            max = Math.max(max, partialDuration);
        }

        return max;
    }

    private long getMinDuration(String eventName, String preceedingEventName) throws NoDataAvailableException {
        long min = Long.MAX_VALUE;

        if(durations.get(preceedingEventName) == null) throw new NoDataAvailableException();
        List<Long> list = durations.get(preceedingEventName).get(eventName);

        if(list == null || list.size() == 0) throw new NoDataAvailableException();
        for(Long partialDuration : list) {
            min = Math.min(min, partialDuration);
        }

        return min;
    }

    private long getAverageDuration(String eventName, String preceedingEventName) throws NoDataAvailableException {
        long duration = 0;
        int count = 0;

        if(durations.get(preceedingEventName) == null) throw new NoDataAvailableException();
        List<Long> list = durations.get(preceedingEventName).get(eventName);

        if(list == null || list.size() == 0) throw new NoDataAvailableException();
        for(Long partialDuration : list) {
            duration += partialDuration;
            count++;
        }

        if(count == 0) throw new NoDataAvailableException();
        return duration / count;
    }

    private long getAverageDuration(String eventName) throws NoDataAvailableException {
        long duration = 0;
        int count = 0;

        for(Map.Entry<String, Map<String, List<Long>>> entry1 : durations.entrySet()) {
            if(entry1.getValue().get(eventName) != null) {
                for (Long partialDuration : entry1.getValue().get(eventName)) {
                    duration += partialDuration;
                    count++;
                }
            }
        }

        if(count == 0) throw new NoDataAvailableException();
        return duration / count;
    }

    private long getStdDeviationDuration(String eventName, String preceedingEventName) throws NoDataAvailableException {
        long average = getAverageDuration(eventName);
        long diff = 0;
        int count = 0;

        if(durations.get(preceedingEventName) == null) throw new NoDataAvailableException();
        List<Long> list = durations.get(preceedingEventName).get(eventName);

        if(list == null || list.size() == 0) throw new NoDataAvailableException();
        for(Long partialDuration : list) {
            diff += Math.pow((partialDuration - average), 2);
            count++;
        }

        if(diff == 0 || count == 0) throw new NoDataAvailableException();
        return (long) Math.sqrt(diff / count);
    }

    private long getStdDeviationDuration(String eventName) throws NoDataAvailableException {
        long average = getAverageDuration(eventName);
        long diff = 0;
        int count = 0;

        for(Map.Entry<String, Map<String, List<Long>>> entry1 : durations.entrySet()) {
            if(entry1.getValue().get(eventName) != null) {
                for (Long partialDuration : entry1.getValue().get(eventName)) {
                    diff += Math.pow((partialDuration - average), 2);
                    count++;
                }
            }
        }

        if(diff == 0 || count == 0) throw new NoDataAvailableException();
        return (long) Math.sqrt(diff / count);
    }

    @Override
    public void filter(Automaton<String> automatonClean) {
        for(Map.Entry<String, Map<String, List<Long>>> entry1 : durations.entrySet()) {
            Node source = new Node<Object>(entry1.getKey());
            Set<String> remove = new UnifiedSet<>();
            for(Map.Entry<String, List<Long>> entry2 : entry1.getValue().entrySet()) {
                Node target = new Node<Object>(entry2.getKey());
                if(!automatonClean.getEdges().contains(new Edge<>(source, target))) {
                    remove.add(entry2.getKey());
                }
            }
            for(String toRemove : remove) {
                entry1.getValue().remove(toRemove);
            }
        }
    }

}
