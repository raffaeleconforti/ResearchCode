package com.raffaeleconforti.noisefiltering.timestamp.permutation;

import com.raffaeleconforti.datastructures.cache.Cache;
import com.raffaeleconforti.datastructures.cache.impl.ManualCache;
import com.raffaeleconforti.kernelestimation.distribution.EventDistributionCalculator;
import com.raffaeleconforti.log.util.LogCloner;
import com.raffaeleconforti.log.util.NameExtractor;
import com.raffaeleconforti.log.util.TraceToString;
import com.raffaeleconforti.noisefiltering.timestamp.check.TimeStampChecker;
import com.raffaeleconforti.noisefiltering.timestamp.collections.Collections2;
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
public class EventPermutatorSmart implements EventPermutator {

    private final boolean debug_mode;
    private final EventDistributionCalculator eventDistributionCalculator;
    private final LogCloner logCloner;
    private final Map<String, Set<String>> duplicatedEvents = new UnifiedMap<>();
    private final NameExtractor nameExtractor;
    private final Set<String> duplicatedTraces = new UnifiedSet<>();
    private final Set<String> sequences;
    private final Map<List<String>, Set<List<String>>> patternsMap;
    private final Cache<List<String>, Set<List<String>>> discoveredPatternsMap = new ManualCache<>();//SelfCleaningCache<>();
    private final TimeStampChecker timeStampChecker;
    private final XFactory factory;
    private final int limitExtensive;

    private final String fixed = "fixed";
    private final XAttribute fixedAttribute = new XAttributeBooleanImpl(fixed, true);

    private int approach;
    private boolean verbose;

    public EventPermutatorSmart(LogCloner logCloner, XFactory factory, XEventClassifier xEventClassifier, EventDistributionCalculator eventDistributionCalculator,
                                TimeStampChecker timeStampChecker, Set<String> sequences, Map<List<String>,
                            Set<List<String>>> patternsMap, int limitExtensive, int approach, boolean debug_mode) {
        this.debug_mode = debug_mode;
        this.factory = factory;
        this.logCloner = logCloner;
        this.eventDistributionCalculator = eventDistributionCalculator;
        this.timeStampChecker = timeStampChecker;
        this.sequences = sequences;
        this.patternsMap = patternsMap;
        this.nameExtractor = new NameExtractor(xEventClassifier);
        this.limitExtensive = limitExtensive;
        this.approach = approach;
        this.verbose = debug_mode;
    }

    private String getEventName(XEvent event) {
        return nameExtractor.getEventName(event);
    }

    private String getTraceName(XTrace trace) {
        return nameExtractor.getTraceName(trace);
    }

    public Set<XTrace> duplicatesTrace(XTrace trace) {
        if (verbose) {
            System.out.println("Starting new Trace!");
        }
        Set<XTrace> traces = new UnifiedSet();
        Set<Set<XEvent>> setXEvents = timeStampChecker.findEventsSameTimeStamp(trace);
        boolean first = true;

        double best = 0;
        double likelihood;
        Set<XTrace> traces2;
        Set<String> duplicated;
        for(Set<XEvent> events : setXEvents) {
            traces2 = new UnifiedSet<>();

            if(first) {
                try {
                    traces2.addAll(permuteXEvents(trace, events));
                } catch (PermutationLimitException e) {
                    if (verbose) {
                        System.out.println("Permutation Limit Exceeded!");
                    }
                    return traces;
                }
                first = false;
            }

            for (XTrace t : traces) {
                try {
                    traces2.addAll(permuteXEvents(t, events));
                } catch (PermutationLimitException e) {
                    if (verbose) {
                        System.out.println("Permutation Limit Exceeded!");
                    }
                    return traces;
                }
            }

            if((duplicated = duplicatedEvents.get(getTraceName(trace))) == null) {
                duplicated = new UnifiedSet<>();
            }
            
            for(XEvent event : events) {
                duplicated.add(getEventName(event));
            }

//            for(String key : duplicatedEvents.keySet()) {
//                if(duplicatedEvents.get(key).equals(duplicated)) {
//                    duplicated = duplicatedEvents.get(key);
//                    System.out.println("Found Duplicate saving space");
//                    break;
//                }
//            }
            duplicatedEvents.put(getTraceName(trace), duplicated);

            for(XTrace t : traces2) {
                likelihood = eventDistributionCalculator.computeLikelihood(t);
                if (likelihood > best) {
                    traces.add(t);
                    best = likelihood;
                }
            }

            if(traces.size() > 100) {
                if(best > 0) {
                    Iterator<XTrace> iterator = traces.iterator();
                    while (iterator.hasNext()) {
                        XTrace t = iterator.next();
                        likelihood = eventDistributionCalculator.computeLikelihood(t);
                        if (likelihood < best) {
                            iterator.remove();
                            if (verbose) {
                                System.out.println("Removing unlikely traces!");
                            }
                        }
                    }
                }else {
                    double secondBest = -1;
                    for(XTrace t : traces) {
                        likelihood = eventDistributionCalculator.computeLikelihoodWithoutZero(t);
                        if (likelihood > secondBest) {
                            best = likelihood;
                        }
                    }
                    Iterator<XTrace> iterator = traces.iterator();
                    while (iterator.hasNext()) {
                        XTrace t = iterator.next();
                        likelihood = eventDistributionCalculator.computeLikelihoodWithoutZero(t);
                        if (likelihood < best) {
                            iterator.remove();
                            if (verbose) {
                                System.out.println("Removing unlikely traces!");
                            }
                        }
                    }
                }
            }
        }

        if(traces.size() > 0) {
            duplicatedTraces.add(getTraceName(trace));
        }else if(setXEvents.size() > 0) {
            if (verbose) {
                System.out.println("No Permutation Found!");
            }
            traces.add(trace);
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

    private Set<XTrace> permuteXEvents(XTrace trace, Set<XEvent> events) throws PermutationLimitException {
        if(eventDistributionCalculator == null && events.size() > limitExtensive) throw new PermutationLimitException();

        Collection<List<XEvent>> permutations;
        if (eventDistributionCalculator == null) {
            if (verbose) {
                System.out.println("Extensive Approach!");
            }
            permutations = Collections2.permutations(events);
        }else {
            XEvent start = findStart(trace, events);
            XEvent end = findEnd(trace, start, events);
//            if((permutations = reusePermutations(start, end, events)).size() == 0) {
                if(verbose) {
                    System.out.println(PermutationTechniqueFactory.getPermutationTechniqueName(approach) + " Approach All!");
                    System.out.println("Start " + nameExtractor.getEventName(start));
                    System.out.println("End " + nameExtractor.getEventName(end));
                }
                PermutationTechnique permutationTechnique = PermutationTechniqueFactory.getPermutationTechnique(approach, events, eventDistributionCalculator, start, end, debug_mode);
                permutations = permutationTechnique.findBestStartEnd();
                populateDiscoveredPatterns(createPatternToReusePermutations(start, end, events), permutations);

                if(discoveredPatternsMap.size() > 100) {
                    if (verbose) {
                        System.out.println("Cleaning memory!");
                    }
                    discoveredPatternsMap.free();
                }
//            }else {
//                System.out.println("Reuse Pattern!");
//            }
        }

        return computeFinalTraces(trace, events, permutations);
    }

    private void populateDiscoveredPatterns(List<String> patternToReusePermutations, Collection<List<XEvent>> permutations) {
        Set<List<String>> patterns = new UnifiedSet<>();
        ArrayList<String> pattern;
        for(List<XEvent> permutation : permutations) {
            pattern = new ArrayList<>(permutation.size());
            for(XEvent event : permutation) {
                pattern.add(nameExtractor.getEventName(event));
            }
            patterns.add(pattern);
        }
        discoveredPatternsMap.put(patternToReusePermutations, patterns);
    }

    private XEvent findEnd(XTrace trace, XEvent start, Set<XEvent> events) {
        boolean foundStart = false;
//        for(int i = 0; i < trace.size(); i++) {
//            if(!foundStart && start == trace.get(i)) {
//                foundStart = true;
//                continue;
//            }
//            if(foundStart && !events.contains(trace.get(i))) return trace.get(i);
//        }
        for(XEvent event : trace) {
            if(!foundStart && start == event) {
                foundStart = true;
                continue;
            }
            if(foundStart && !events.contains(event)) return event;
        }
        return null;
    }

    private XEvent findStart(XTrace trace, Set<XEvent> events) {
//        for(int i = 0; i < trace.size(); i++) {
//            if(events.contains(trace.get(i))) return trace.get(i - 1);
//        }
        XEvent last = null;
        for(XEvent event : trace) {
            if(events.contains(event)) return last;
            last = event;
        }
        return null;
    }

    private Set<XTrace> computeFinalTraces(XTrace trace, Set<XEvent> events, Collection<List<XEvent>> permutations) {
        Set<XTrace> traces = new UnifiedSet<>();
        Set<XTrace> backupTraces = new UnifiedSet<>();
        Set<List<String>> processedSequences = new UnifiedSet<>();
        double likelihoodBest = 0;

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

            String seqCorr = TraceToString.listToString(sequence);


            XTrace t = factory.createTrace();
//            for(Map.Entry<String, XAttribute> entry : trace.getAttributes().entrySet()) {
////                t.getAttributes().put(entry.getKey(), logCloner.getXAttribute(entry.getKey(), entry.getValue()));
//                t.getAttributes().put(entry.getKey(), entry.getValue());
//            }

            t.setAttributes(trace.getAttributes());
            t.getAttributes().put(fixed, fixedAttribute);

            int position = 0;
//            for (int i = 0; i < trace.size(); i++) {
//                if (!events.contains(trace.get(i))) {
//                    t.add(trace.get(i));
//                } else {
////                    XEvent e = logCloner.getXEvent(permutation.get(position));
////                    t.add(e);
//                    t.add(permutation.get(position));
//                    position++;
//                }
//            }

            for (XEvent event : trace) {
                if (!events.contains(event)) {
                    t.add(event);
                } else {
                    t.add(permutation.get(position));
                    position++;
                }
            }

            if (sequences.contains(seqCorr)) {
                traces.add(t);
            }else if(traces.size() == 0){
                if(permutations.size() < 100) {
                    backupTraces.add(t);
                }else {
                    double likelihood = eventDistributionCalculator.computeLikelihood(t);
                    if (likelihood >= likelihoodBest && likelihood > 0) {
                        if (likelihood > likelihoodBest) {
                            double oldBest = likelihoodBest;
                            likelihoodBest = likelihood;
                            if (likelihood > oldBest && backupTraces.size() > 100) {
                                Iterator<XTrace> iterator = backupTraces.iterator();
                                if (verbose) {
                                    System.out.println("Cleaning " + backupTraces.size());
                                }
                                while (iterator.hasNext()) {
                                    likelihood = eventDistributionCalculator.computeLikelihood(t);
                                    if (likelihood < likelihoodBest / 2) {
                                        iterator.remove();
                                    }
                                }
                            }
                        }
                        backupTraces.add(t);
                    }
                }
            }
        }

        if(traces.size() == 0) {
            if (verbose) {
                System.out.println("Using Backup Traces!");
            }
            traces.addAll(backupTraces);
        }

        return traces;
    }

    private List<String> createPatternToReusePermutations(XEvent start, XEvent end, Set<XEvent> events) {
        ArrayList<String> pattern = new ArrayList<>(events.size());
        for(XEvent event : events) {
            pattern.add(nameExtractor.getEventName(event));
        }
        Collections.sort(pattern);

        pattern.add(0, nameExtractor.getEventName(start));
        pattern.add(nameExtractor.getEventName(end));

        return pattern;
    }

    private Set<List<XEvent>> reusePermutations(XEvent start, XEvent end, Set<XEvent> events) {
        List<String> pattern = createPatternToReusePermutations(start, end, events);
        return getExistingTraceBasedOnPattern(events, pattern, true);
    }

    public Set<String> getDuplicatedTraces() {
        return duplicatedTraces;
    }

    public Map<String,Set<String>> getDuplicatedEvents() {
        return duplicatedEvents;
    }
}
