package com.raffaeleconforti.kernelestimation.distribution.impl;

import com.raffaeleconforti.automaton.Automaton;
import com.raffaeleconforti.automaton.Edge;
import com.raffaeleconforti.automaton.Node;
import com.raffaeleconforti.datastructures.cache.Cache;
import com.raffaeleconforti.datastructures.cache.impl.SelfCleaningCache;
import com.raffaeleconforti.kernelestimation.distribution.EventDistributionCalculator;
import com.raffaeleconforti.log.util.NameExtractor;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by conforti on 21/01/2016.
 */
public class EventDistributionCalculatorImpl implements EventDistributionCalculator {

    protected int initiator = -1;
    protected int terminator = -1;
    protected final NameExtractor nameExtractor;
    protected final XLog log;
    protected final Map<String, Integer> startEvent = new UnifiedMap<>();
    protected final Map<String, Integer> endEvent = new UnifiedMap<>();
    protected final Map<String, Map<String, Integer>> distribution = new UnifiedMap<>();
    protected final Map<String, Map<String, Integer>> distributionReverse = new UnifiedMap<>();

    protected final Map<String, Double> distributionCache = new UnifiedMap<>();
    protected final Map<String, Double> distributionReverseCache = new UnifiedMap<>();
    protected final Map<String, Double> distributionZeroCache = new UnifiedMap<>();
    protected final Map<String, Double> distributionReverseZeroCache = new UnifiedMap<>();

    protected Map<String, Double> likelihood = new UnifiedMap<>();
    protected Cache<XTrace, Double> likelihoodCache;

    protected Map<String, Map<String, Integer>> enrichedDistribution = new UnifiedMap<>();
    protected Map<String, Map<String, Integer>> enrichedDistributionReverse = new UnifiedMap<>();

    public EventDistributionCalculatorImpl(XLog log, XEventClassifier xEventClassifier, boolean self_cleaning) {
        this.log = log;
        this.nameExtractor = new NameExtractor(xEventClassifier);
        likelihoodCache = new SelfCleaningCache(self_cleaning);
    }

    protected String getEventName(XEvent event) {
        return nameExtractor.getEventName(event);
    }

    protected String getTraceName(XTrace trace) {
        return nameExtractor.getTraceName(trace);
    }

    public void analyseLog() {
        for(XTrace trace : log) {
            XEvent previous = null;
            String previousName;
            for(XEvent event : trace) {
                if(previous != null) {
                    previousName = getEventName(previous);
                    String eventName = getEventName(event);

                    Map<String, Integer> map;
                    if ((map = distribution.get(previousName)) == null) {
                        map = new UnifiedMap<>();
                    }
                    Integer i;
                    if ((i = map.get(eventName)) == null) {
                        i = 0;
                    }
                    i++;
                    map.put(eventName, i);
                    distribution.put(previousName, map);

                    if ((map = distributionReverse.get(eventName)) == null) {
                        map = new UnifiedMap<>();
                    }
                    if ((i = map.get(previousName)) == null) {
                        i = 0;
                    }
                    i++;
                    map.put(previousName, i);
                    distributionReverse.put(eventName, map);
                }
                previous = event;
            }

            Integer likelihood;
            if((likelihood = startEvent.get(getEventName(trace.get(0)))) == null) {
                likelihood = 0;
            }
            likelihood++;
            startEvent.put(getEventName(trace.get(0)), likelihood);

            if((likelihood = endEvent.get(getEventName(trace.get(trace.size() - 1)))) == null) {
                likelihood = 0;
            }
            likelihood++;
            endEvent.put(getEventName(trace.get(trace.size() - 1)), likelihood);
        }
    }

    public double computeLikelihood(XTrace trace) {
        double likelihood = 1;
        XEvent last = null;
        String lastName;
        Double res;
        if((res = likelihoodCache.get(trace)) == null) {
            for (int i = 0; i < trace.size(); i++) {
                XEvent event = trace.get(i);
                if (last != null) {
                    lastName = getEventName(last);
                    String eventName = getEventName(event);
                    likelihood *= computeLikelihood(lastName, eventName);
                }
                last = event;
            }
            likelihoodCache.put(trace, likelihood);
        }
        if(res != null) {
            likelihood = res;
        }

        return likelihood;
    }

    public double computeLikelihoodWithoutZero(XTrace trace) {
        double likelihood = 1;
        XEvent last = null;
        String lastName;
        double scaleFactor = 1 / trace.size();
        for(int i = 0; i < trace.size(); i++) {
            XEvent event = trace.get(i);
            if(last != null) {
                lastName = getEventName(last);
                String eventName = getEventName(event);
                likelihood += computeLikelihood(lastName, eventName) * scaleFactor;
            }
            last = event;
        }

        return likelihood;
    }

    private double computeLikelihoodStartEvent(String eventName) {
        Integer likelihood = startEvent.get(eventName);
        if(likelihood == null) return 0;
        else return (double) likelihood / (double) log.size();
    }

    private double computeLikelihoodEndEvent(String eventName) {
        Integer likelihood = endEvent.get(eventName);
        if(likelihood == null) return 0;
        else return (double) likelihood / (double) log.size();
    }

    @Override
    public Map<String, Double> computeLikelihoodNextEvent(String originator) {
        Map<String, Double> res = new UnifiedMap<>();
        double total = 0;
        Map<String, Integer> tmp = distribution.get(originator);
        if(tmp != null) {
            for (Integer i : tmp.values()) {
                total += i;
            }
            for (Map.Entry<String, Integer> entry : tmp.entrySet()) {
                res.put(entry.getKey(), entry.getValue() / total);
            }
        }
        return res;
    }

    @Override
    public Map<String, Double> computeLikelihoodPreviousEvent(String follower) {
        Map<String, Double> res = new UnifiedMap<>();
        double total = 0;
        Map<String, Integer> tmp = distributionReverse.get(follower);
        if(tmp != null) {
            for (Integer i : tmp.values()) {
                total += i;
            }
            for (Map.Entry<String, Integer> entry : tmp.entrySet()) {
                res.put(entry.getKey(), entry.getValue() / total);
            }
        }
        return res;
    }

    public double computeLikelihood(List<XEvent> trace) {
        double likelihood = 1;
        XEvent last = null;
        String lastName;
        for(int i = 0; i < trace.size(); i++) {
            XEvent event = trace.get(i);
            if(last != null) {
                lastName = getEventName(last);
                String eventName = getEventName(event);
                double tmp = computeLikelihoodAcceptZero(lastName, eventName);
                if(tmp == 0) {
                    initiator = i-1;
                    terminator = i;
                    return 0;
                }
                likelihood *= tmp;
            }
            last = event;
        }

        return likelihood;
    }

    public double computeEnrichedLikelihood(List<XEvent> trace) {
        double likelihood = computeLikelihood(trace);
        if(likelihood == 0.0) {
            XEvent last = null;
            String lastName;
            for(int i = 0; i < trace.size(); i++) {
                XEvent event = trace.get(i);
                if(last != null) {
                    lastName = getEventName(last);
                    String eventName = getEventName(event);
                    double tmp = computeLikelihoodAcceptZero(lastName, eventName);
                    if(tmp == 0) {
                        initiator = i-1;
                        terminator = i;
                        return 0;
                    }
                    likelihood *= tmp;
                }
                last = event;
            }
        }
        return likelihood;
    }

    public void updateEnrichedLikelihood(XEvent event1, XEvent event2) {
        String eventName1 = getEventName(event1);
        String eventName2 = getEventName(event2);

        Map<String, Integer> map;
        if ((map = enrichedDistribution.get(eventName1)) == null) {
            map = new UnifiedMap<>();
        }
        Integer i;
        if ((i = map.get(eventName2)) == null) {
            i = 0;
        }
        i++;
        map.put(eventName2, i);
        enrichedDistribution.put(eventName1, map);

        if ((map = enrichedDistributionReverse.get(eventName2)) == null) {
            map = new UnifiedMap<>();
        }
        if ((i = map.get(eventName1)) == null) {
            i = 0;
        }
        i++;
        map.put(eventName1, i);
        enrichedDistributionReverse.put(eventName2, map);
    }

    public double computeLikelihood(List<XEvent> trace, double limit) {
        double likelihood = 1;
        XEvent previous = null;
        String previousName;
        for(int i = 0; i < trace.size(); i++) {
            XEvent event = trace.get(i);
            if(previous != null) {
                previousName = getEventName(previous);
                String eventName = getEventName(event);
                likelihood *= computeLikelihoodAcceptZero(previousName, eventName);
                if(likelihood <= limit) {
                    initiator = i-1;
                    terminator = i;
                    return 0;
                }
            }
            previous = event;
        }

        return likelihood;
    }

    protected double computeLikelihood(String originator, String follower) {
        String name = originator + "+|+" + follower;
        Double d;
        if((d = likelihood.get(name)) == null) {
            d = computeNormalLikelihood(originator, follower) * computeReverseLikelihood(follower, originator);
            likelihood.put(name, d);
        }
        return d;
    }

    protected double computeEnrichedLikelihood(String originator, String follower) {
        return computeEnrichedLikelihood(originator, follower) * computeEnrichedReverseLikelihood(follower, originator);
    }

    protected double computeLikelihoodAcceptZero(String originator, String follower) {
        return computeNormalLikelihoodAcceptZero(originator, follower) * computeReverseLikelihoodAcceptZero(follower, originator);
    }

    protected double computeEnrichedLikelihoodAcceptZero(String originator, String follower) {
        return computeEnrichedNormalLikelihoodAcceptZero(originator, follower) * computeEnrichedReverseLikelihoodAcceptZero(follower, originator);
    }

    protected double computeNormalLikelihood(String originator, String follower) {
        Double res;
        String s = originator + "+|+" + follower;
        if((res = distributionCache.get(s)) == null) {
            Map<String, Integer> map = distribution.get(originator);
            res = computeLikelihood(map, follower);
            distributionCache.put(s, res);
        }
        return res;
    }

    protected double computeEnrichedNormalLikelihood(String originator, String follower) {
        Map<String, Integer> map = enrichedDistribution.get(originator);
        return computeLikelihood(map, follower);
    }

    protected double computeReverseLikelihood(String originator, String follower) {
        Double res;
        String s = originator + "+|+" + follower;
        if((res = distributionReverseCache.get(s)) == null) {
            Map<String, Integer> map = distributionReverse.get(originator);
            res = computeLikelihood(map, follower);
            distributionReverseCache.put(s, res);
        }
        return res;
    }

    protected double computeEnrichedReverseLikelihood(String originator, String follower) {
        Map<String, Integer> map = enrichedDistributionReverse.get(originator);
        return computeLikelihood(map, follower);
    }

    protected double computeNormalLikelihoodAcceptZero(String originator, String follower) {
        Double res;
        String s = originator + "+|+" + follower;
        if((res = distributionZeroCache.get(s)) == null) {
            Map<String, Integer> map = distribution.get(originator);
            res = computeLikelihoodAcceptZero(map, follower);
            distributionZeroCache.put(s, res);
        }
        return res;
    }

    protected double computeEnrichedNormalLikelihoodAcceptZero(String originator, String follower) {
        Map<String, Integer> map = enrichedDistribution.get(originator);
        return computeLikelihoodAcceptZero(map, follower);
    }

    protected double computeReverseLikelihoodAcceptZero(String originator, String follower) {
        Double res;
        String s = originator + "+|+" + follower;
        if((res = distributionReverseZeroCache.get(s)) == null) {
            Map<String, Integer> map = distributionReverse.get(originator);
            res = computeLikelihoodAcceptZero(map, follower);
            distributionReverseZeroCache.put(s, res);
        }
        return res;
    }

    protected double computeEnrichedReverseLikelihoodAcceptZero(String originator, String follower) {
        Map<String, Integer> map = enrichedDistributionReverse.get(originator);
        return computeLikelihoodAcceptZero(map, follower);
    }

    protected double computeLikelihood(Map<String, Integer> map, String follower) {
        if(map != null) {
            double total = 0;
            for(Integer i : map.values()) {
                total += i;
            }
            return map.get(follower) == null ? 1 / total : map.get(follower) / total;
        }
        return 0;
    }

    protected double computeLikelihoodAcceptZero(Map<String, Integer> map, String follower) {
        if(map != null) {
            double total = 0;
            for(Integer i : map.values()) {
                total += i;
            }
            return map.get(follower) == null ? 0 : map.get(follower) / total;
        }
        return 0;
    }

    public int getInitiator() {
        return initiator;
    }

    public int getTerminator() {
        return terminator;
    }

    @Override
    public void filter(Automaton<String> automatonClean) {
        for(Map.Entry<String, Map<String, Integer>> entry1 : distribution.entrySet()) {
            Node source = new Node<Object>(entry1.getKey());
            Set<String> remove = new UnifiedSet<>();
            for(Map.Entry<String, Integer> entry2 : entry1.getValue().entrySet()) {
                Node target = new Node<Object>(entry2.getKey());
                if(!automatonClean.getEdges().contains(new Edge<>(source, target))) {
                    remove.add(entry2.getKey());
                }
            }
            for(String toRemove : remove) {
                entry1.getValue().remove(toRemove);
            }
        }

        for(Map.Entry<String, Map<String, Integer>> entry1 : distributionReverse.entrySet()) {
            Node target = new Node<Object>(entry1.getKey());
            Set<String> remove = new UnifiedSet<>();
            for(Map.Entry<String, Integer> entry2 : entry1.getValue().entrySet()) {
                Node source = new Node<Object>(entry2.getKey());
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
