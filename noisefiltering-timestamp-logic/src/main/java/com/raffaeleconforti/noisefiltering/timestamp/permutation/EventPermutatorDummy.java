package com.raffaeleconforti.noisefiltering.timestamp.permutation;

import com.raffaeleconforti.datastructures.cache.Cache;
import com.raffaeleconforti.datastructures.cache.impl.SelfCleaningCache;
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
public class EventPermutatorDummy implements EventPermutator {

    private final EventDistributionCalculator eventDistributionCalculator;
    private final LogCloner logCloner;
    private final Map<String, Set<String>> duplicatedEvents = new UnifiedMap<>();
    private final NameExtractor nameExtractor;
    private final Set<String> duplicatedTraces = new UnifiedSet<>();
    private final Set<String> sequences;
    private final Map<List<String>, Set<List<String>>> patternsMap;
    private final Cache<List<String>, Set<List<String>>> discoveredPatternsMap;
    private final TimeStampChecker timeStampChecker;
    private final XFactory factory;
    private final int limitExtensive;

    private final String fixed = "fixed";
    private final XAttribute fixedAttribute = new XAttributeBooleanImpl(fixed, true);

    private int approach;

    public EventPermutatorDummy(LogCloner logCloner, XFactory factory, XEventClassifier xEventClassifier, EventDistributionCalculator eventDistributionCalculator,
                                TimeStampChecker timeStampChecker, Set<String> sequences, Map<List<String>,
                            Set<List<String>>> patternsMap, int limitExtensive, int approach, boolean self_cleaning) {
        this.factory = factory;
        this.logCloner = logCloner;
        this.eventDistributionCalculator = eventDistributionCalculator;
        this.timeStampChecker = timeStampChecker;
        this.sequences = sequences;
        this.patternsMap = patternsMap;
        this.nameExtractor = new NameExtractor(xEventClassifier);
        this.limitExtensive = limitExtensive;
        this.approach = approach;
        discoveredPatternsMap = new SelfCleaningCache<>(self_cleaning);
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
                traces2.addAll(permuteXEventsDummy(trace, events));
                first = false;
            }

            for (XTrace t : traces) {
                traces2.addAll(permuteXEventsDummy(t, events));
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

    private Set<List<XEvent>> createPatternAndGetExistingTraceBasedOnPattern(Set<XEvent> events) {
        ArrayList<String> pattern = new ArrayList<>(events.size());
        for(XEvent event : events) {
            pattern.add(nameExtractor.getEventName(event));
        }
        Collections.sort(pattern);

        return getExistingTraceBasedOnPattern(events, pattern, false);
    }

    private Set<List<XEvent>> getExistingTraceBasedOnPattern(Set<XEvent> events, List<String> pattern, boolean discoveredPatterns) {
        Set<List<String>> matchingPatterns = discoveredPatterns?discoveredPatternsMap.get(pattern):patternsMap.get(pattern);

        Set<List<XEvent>> existingPatterns = new UnifiedSet<>();
        Set<XEvent> tmpEvents;
        List<XEvent> list;
        if(matchingPatterns != null) {
            for (List<String> matchingPattern : matchingPatterns) {
                list = new ArrayList<>(matchingPattern.size());

                tmpEvents = new UnifiedSet<>(events);
                for (String eventName : matchingPattern) {
                    Iterator<XEvent> iterator = tmpEvents.iterator();
                    while (iterator.hasNext()) {
                        XEvent event = iterator.next();
                        if (eventName.equals(nameExtractor.getEventName(event))) {
                            list.add(event);
                            iterator.remove();
                            break;
                        }
                    }
                }
                existingPatterns.add(list);
            }
        }

        return existingPatterns;
    }

    private Set<XTrace> permuteXEventsDummy(XTrace trace, Set<XEvent> events) {
        Set<XTrace> traces = new UnifiedSet<>();

        Collection<List<XEvent>> permutations = createPatternAndGetExistingTraceBasedOnPattern(events);

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

    public Set<String> getDuplicatedTraces() {
        return duplicatedTraces;
    }

    public Map<String,Set<String>> getDuplicatedEvents() {
        return duplicatedEvents;
    }
}
