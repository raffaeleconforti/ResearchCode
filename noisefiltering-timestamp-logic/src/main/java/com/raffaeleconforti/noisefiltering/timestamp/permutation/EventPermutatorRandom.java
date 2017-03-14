package com.raffaeleconforti.noisefiltering.timestamp.permutation;

import com.raffaeleconforti.kernelestimation.distribution.EventDistributionCalculator;
import com.raffaeleconforti.log.util.LogCloner;
import com.raffaeleconforti.log.util.NameExtractor;
import com.raffaeleconforti.log.util.TraceToString;
import com.raffaeleconforti.noisefiltering.timestamp.check.TimeStampChecker;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeBooleanImpl;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.*;

/**
 * Created by conforti on 5/02/2016.
 */
public class EventPermutatorRandom implements EventPermutator {

    private final EventDistributionCalculator eventDistributionCalculator;
    private final LogCloner logCloner;
    private final Map<String, Set<String>> duplicatedEvents = new UnifiedMap<>();
    private final NameExtractor nameExtractor;
    private final Set<String> duplicatedTraces = new UnifiedSet<>();
    private final Set<String> sequences;
    private final TimeStampChecker timeStampChecker;
    private final XFactory factory;
    private final Random random = new Random(123456789);

    private final String fixed = "fixed";
    private final XAttribute fixedAttribute = new XAttributeBooleanImpl(fixed, true);

    private int approach;

    public EventPermutatorRandom(LogCloner logCloner, XFactory factory, XEventClassifier xEventClassifier, EventDistributionCalculator eventDistributionCalculator,
                                 TimeStampChecker timeStampChecker, Set<String> sequences, Map<List<String>,
                            Set<List<String>>> patternsMap, int limitExtensive, int approach) {
        this.factory = factory;
        this.logCloner = logCloner;
        this.eventDistributionCalculator = eventDistributionCalculator;
        this.timeStampChecker = timeStampChecker;
        this.sequences = sequences;
        this.nameExtractor = new NameExtractor(xEventClassifier);
        this.approach = approach;
    }

    private String getEventName(XEvent event) {
        return nameExtractor.getEventName(event);
    }

    private String getTraceName(XTrace trace) {
        return nameExtractor.getTraceName(trace);
    }

    public Set<XTrace> duplicatesTrace(XTrace trace) {
        Set<XTrace> traces = new UnifiedSet<>();
        Set<Set<XEvent>> setXEvents = timeStampChecker.findEventsSameTimeStamp(trace);
        boolean first = true;

        double best = 0;
        double likelihood;
        Set<XTrace> traces2;
        for(Set<XEvent> events : setXEvents) {
            traces2 = new UnifiedSet<>();

            if(first) {
                traces2.addAll(permuteXEventsRandom(trace, events));
                first = false;
            }

            for (XTrace t : traces) {
                traces2.addAll(permuteXEventsRandom(t, events));
            }

            Set<String> duplicated;
            if((duplicated = duplicatedEvents.get(getTraceName(trace))) == null) {
                duplicated = new UnifiedSet<>();
            }

            for(XEvent event : events) {
                duplicated.add(getEventName(event));
            }

            duplicatedEvents.put(getTraceName(trace), duplicated);

            for(XTrace t : traces2) {
                likelihood = eventDistributionCalculator.computeLikelihood(t);
                if (likelihood > best) {
                    best = likelihood;
                }
                if(likelihood > 0) {
                    traces.add(t);
                }
            }

            if(traces.size() > 100) {
                Iterator<XTrace> iterator = traces.iterator();
                while(iterator.hasNext()) {
                    XTrace t = iterator.next();
                    likelihood = eventDistributionCalculator.computeLikelihood(t);
                    if (likelihood < best / 2) {
                        iterator.remove();
                        System.out.println("Removing unlikely traces!");
                    }
                }
            }
        }

        if(traces.size() > 0) {
            duplicatedTraces.add(getTraceName(trace));
        }

        return traces;
    }

    private Set<XTrace> permuteXEventsRandom(XTrace trace, Set<XEvent> events) {
        Set<XTrace> traces = new UnifiedSet<>();

        Collection<List<XEvent>> permutations = createRandomPermutation(events);

        Set<List<String>> processedSequences = new UnifiedSet<>();
        List<String> sequence;
        for(List<XEvent> permutation : permutations) {
            sequence = new ArrayList<>(permutation.size());

            for (int i = 0; i < permutation.size(); i++) {
                sequence.add(getEventName(permutation.get(i)));
            }

            if (!processedSequences.contains(sequence)) {
                processedSequences.add(sequence);
            } else {
                continue;
            }

            XTrace t = factory.createTrace();
            for(Map.Entry<String, XAttribute> entry : trace.getAttributes().entrySet()) {
                t.getAttributes().put(entry.getKey(), logCloner.getXAttribute(entry.getKey(), entry.getValue()));
            }

            t.getAttributes().put(fixed, fixedAttribute);

            int position = 0;
            for (int i = 0; i < trace.size(); i++) {
                if (!events.contains(trace.get(i))) {
                    t.add(trace.get(i));
                } else {
                    XEvent e = logCloner.getXEvent(permutation.get(position));
                    t.add(e);
                    position++;
                }
            }

            String seqCorr = TraceToString.convertXTraceToString(t, nameExtractor);
            if (sequences.contains(seqCorr)) {
                traces.add(t);
            }
        }

        return traces;
    }

    private Collection<List<XEvent>> createRandomPermutation(Set<XEvent> events) {
        Set<List<XEvent>> set = new UnifiedSet<>();
        Set<XEvent> destroySet = new UnifiedSet<>(events);

        List<XEvent> list = new ArrayList<>(destroySet.size());
        while(destroySet.size() > 0) {
            int pos = random.nextInt(destroySet.size());
            XEvent event = destroySet.toArray(new XEvent[destroySet.size()])[pos];
            list.add(event);
            destroySet.remove(event);
        }
        set.add(list);
        return set;
    }

    public Set<String> getDuplicatedTraces() {
        return duplicatedTraces;
    }

    public Map<String,Set<String>> getDuplicatedEvents() {
        return duplicatedEvents;
    }
}
