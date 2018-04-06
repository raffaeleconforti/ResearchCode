package com.raffaeleconforti.noisefiltering.timestamp.traceselector;

import com.raffaeleconforti.automaton.Automaton;
import com.raffaeleconforti.kernelestimation.distribution.impl.EventDistributionCalculatorNoiseImpl;
import com.raffaeleconforti.log.util.NameExtractor;
import com.raffaeleconforti.log.util.TraceToString;
import com.raffaeleconforti.memorylog.XFactoryMemoryImpl;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

import java.util.*;

/**
 * Created by conforti on 14/01/2016.
 */
public class AutomatonBestTraceMatchSelector {

    private final int originalSize;
    private final XFactory factory = new XFactoryMemoryImpl();
    private final NameExtractor nameExtractor;
    private final XEventClassifier xEventClassifier;
    private final XLog log;
    private final Automaton<String> automaton;
    private final Set<String> duplicatedTraces;
    private final Map<String, Set<XTrace>> possibleTraces;
    private final Map<String, Set<String>> duplicatedEvents;
    private Map<String, Double> likelihoodMap = new UnifiedMap<>();
    private Map<String, Double> likelihoodWithoutZeroMap = new UnifiedMap<>();

    public AutomatonBestTraceMatchSelector(XLog log, XEventClassifier xEventClassifier, Automaton<String> automaton, Set<String> duplicatedTraces, Map<String, Set<XTrace>> possibleTraces, Map<String, Set<String>> duplicatedEvents, int originalSize) {
        this.log = log;
        this.automaton = automaton;
        this.duplicatedTraces = duplicatedTraces;
        this.possibleTraces = possibleTraces;
        this.duplicatedEvents = duplicatedEvents;
        this.xEventClassifier = xEventClassifier;
        this.originalSize = originalSize;
        this.nameExtractor = new NameExtractor(xEventClassifier);
    }

    private String getTraceName(XTrace trace) {
        return nameExtractor.getTraceName(trace);
    }

    public XLog selectBestMatchingTraces(PluginContext context, int[] fixed, List<String> fixedTraces, int approach, boolean self_cleaning) {
        XLog result = filter(context, fixed, fixedTraces, approach, self_cleaning);

        return result;
    }

    private XLog filter(PluginContext context, int[] fixed, List<String> fixedTraces, int approach, boolean self_cleaning) {
        XLog finalLog = factory.createLog(log.getAttributes());
//        if(approach == PermutationTechnique.HEURISTICS_SET) {
//            Map<String, Integer> removed = new UnifiedMap<>();
//            Set<String> ok = new UnifiedSet<>();
//
//            PetrinetReplayerWithILP replayer = new PetrinetReplayerWithILP();
//
//            Petrinet petrinet = automaton.getPetrinet();
//
//            Map<Transition, Integer> transitions2costs = new UnifiedMap<Transition, Integer>();
//            for (Transition t : petrinet.getTransitions()) {
//                if (t.isInvisible()) {
//                    transitions2costs.put(t, 0);
//                } else {
//                    transitions2costs.put(t, 100000000);
//                }
//            }
//
//            XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
//
//            Map<XEventClass, Integer> events2costs = constructMOTCostFunction(log, dummyEvClass);
//            IPNReplayParameter parameters = new CostBasedCompleteParam(events2costs, transitions2costs);
//
//            Marking initialMarking = new Marking();
//            Marking finalMarking = new Marking();
//            for (Place p : petrinet.getPlaces()) {
//                if (p.getLabel().startsWith("source")) {
//                    initialMarking.add(p);
//                }
//                if (p.getLabel().startsWith("sink")) {
//                    finalMarking.add(p);
//                }
//            }
//
//            parameters.setInitialMarking(initialMarking);
//            parameters.setFinalMarkings(finalMarking);
//            parameters.setGUIMode(false);
//            parameters.setCreateConn(false);
//            ((CostBasedCompleteParam) parameters).setMaxNumOfStates(Integer.MAX_VALUE);
//
//            PNRepResult replayResults = null;
//
//            try {
//                replayResults = replayer.replayLog(context, petrinet, log, constructMapping(petrinet, log, dummyEvClass), parameters);
//            } catch (AStarException e) {
//                e.printStackTrace();
//            }
//
//            for (SyncReplayResult replayResult : replayResults) {
//                List<Object> nodeInstance = replayResult.getNodeInstance();
//                List<StepTypes> stepTypes = replayResult.getStepTypes();
//
//                for (Integer tracePos : replayResult.getTraceIndex()) {
//                    if (duplicatedTraces.contains(getTraceName(log.get(tracePos)))) {
//                        boolean remove = false;
//
//                        for (int i = 0; i < nodeInstance.size(); i++) {
//                            StepTypes type = stepTypes.get(i);
//                            if (type != StepTypes.LMGOOD && type != StepTypes.MINVI) {
//                                remove = true;
//                                break;
//                            }
//                        }
//
//                        if (!remove) {
//                            ok.add(getTraceName(log.get(tracePos)));
//                            finalLog.add(log.get(tracePos));
//                        } else {
//                            Integer i;
//                            if ((i = removed.get(getTraceName(log.get(tracePos)))) == null) {
//                                i = 0;
//                            }
//                            i++;
//                            removed.put(getTraceName(log.get(tracePos)), i);
//                        }
//                    } else {
//                        finalLog.add(log.get(tracePos));
//                    }
//
//                }
//            }
//        }else {
            for(XTrace trace : log) {
                finalLog.add(trace);
            }
//        }

        EventDistributionCalculatorNoiseImpl dc = null;
        if(log.size() > originalSize) {
            dc = new EventDistributionCalculatorNoiseImpl(log, xEventClassifier, duplicatedEvents, self_cleaning);
            dc.analyseLog();
        }

        for(String traceID : duplicatedTraces) {
            int count = 0;

            if(log.size() == originalSize) {
                count = 1;
            }else {
                for (XTrace trace : finalLog) {
                    if (getTraceName(trace).equals(traceID)) {
                        count++;
                    }
                }
            }

            if(count > 0) {
                fixedTraces.add(traceID);
                fixed[0]++;

                if(count > 1) {
                    List<XTrace> possibleTraces2 = new ArrayList<>(count);
                    Iterator<XTrace> iterator = finalLog.iterator();
                    while (iterator.hasNext()) {
                        XTrace trace = iterator.next();
                        if (getTraceName(trace).equals(traceID)) {
                            possibleTraces2.add(trace);
                            iterator.remove();
                        }
                    }

                    double max = -1;
                    XTrace best = null;
                    for (XTrace possible : possibleTraces2) {
                        Double likelihood;
                        String trace = TraceToString.convertXTraceToString(possible, nameExtractor);
                        if((likelihood = likelihoodMap.get(trace)) == null) {
                            likelihood = dc.computeLikelihood(possible);
                            likelihoodMap.put(trace, likelihood);
                        }

                        if (likelihood > max) {
                            max = likelihood;
                            best = possible;
                        }
                    }

                    if(max == 0) {
                        max = -1;
                        best = null;
                        for (XTrace possible : possibleTraces.get(traceID)) {
                            Double likelihood;
                            String trace = TraceToString.convertXTraceToString(possible, nameExtractor);
                            if((likelihood = likelihoodWithoutZeroMap.get(trace)) == null) {
                                likelihood = dc.computeLikelihoodWithoutZero(possible);
                                likelihoodWithoutZeroMap.put(trace, likelihood);
                            }

                            if (likelihood > max) {
                                max = likelihood;
                                best = possible;
                            }
                        }
                    }

                    finalLog.add(best);
                }
            }else if(count == 0){
                System.out.println("Unable to fix events order for trace " + traceID + ", selecting best match out of " + possibleTraces.get(traceID).size());

                double max = -1;
                XTrace best = null;
                for (XTrace possible : possibleTraces.get(traceID)) {
                    Double likelihood;
                    String trace = TraceToString.convertXTraceToString(possible, nameExtractor);
                    if((likelihood = likelihoodMap.get(trace)) == null) {
                        likelihood = dc.computeLikelihood(possible);
                        likelihoodMap.put(trace, likelihood);
                    }

                    if (likelihood > max) {
                        max = likelihood;
                        best = possible;
                    }
                }

                if(max == 0) {
                    max = -1;
                    best = null;
                    for (XTrace possible : possibleTraces.get(traceID)) {
                        Double likelihood;
                        String trace = TraceToString.convertXTraceToString(possible, nameExtractor);
                        if((likelihood = likelihoodWithoutZeroMap.get(trace)) == null) {
                            likelihood = dc.computeLikelihoodWithoutZero(possible);
                            likelihoodWithoutZeroMap.put(trace, likelihood);
                        }

                        if (likelihood > max) {
                            max = likelihood;
                            best = possible;
                        }
                    }
                }

                finalLog.add(best);
            }
        }

        return finalLog;
    }

    private Map<XEventClass, Integer> constructMOTCostFunction(XLog log, XEventClass dummyEvClass) {
        Map<XEventClass,Integer> costMOT = new UnifiedMap<XEventClass,Integer>();
        XLogInfo summary = XLogInfoFactory.createLogInfo(log, xEventClassifier);

        for (XEventClass evClass : summary.getEventClasses().getClasses()) {
            costMOT.put(evClass, 1);
        }

        costMOT.put(dummyEvClass, 1);

        return costMOT;
    }

    private TransEvClassMapping constructMapping(Petrinet net, XLog log, XEventClass dummyEvClass) {
        TransEvClassMapping mapping = new TransEvClassMapping(xEventClassifier, dummyEvClass);

        XLogInfo summary = XLogInfoFactory.createLogInfo(log, xEventClassifier);

        for (Transition t : net.getTransitions()) {
            boolean mapped = false;

            for (XEventClass evClass : summary.getEventClasses().getClasses()) {
                String id = evClass.getId();

                if (t.getLabel().equals(id)) {
                    mapping.put(t, evClass);
                    mapped = true;
                    break;
                }
            }

            if (!mapped) {
                mapping.put(t, dummyEvClass);
            }

        }

        return mapping;
    }

}
