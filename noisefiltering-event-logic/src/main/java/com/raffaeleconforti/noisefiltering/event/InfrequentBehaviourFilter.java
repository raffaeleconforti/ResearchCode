package com.raffaeleconforti.noisefiltering.event;

import com.raffaeleconforti.automaton.Automaton;
import com.raffaeleconforti.automaton.AutomatonFactory;
import com.raffaeleconforti.automaton.Edge;
import com.raffaeleconforti.automaton.Node;
import com.raffaeleconforti.automaton.exception.HighThresholdException;
import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.log.util.LogModifier;
import com.raffaeleconforti.log.util.LogOptimizer;
import com.raffaeleconforti.noisefiltering.event.infrequentbehaviour.automaton.AutomatonInfrequentBehaviourDetector;
import com.raffaeleconforti.noisefiltering.event.infrequentbehaviour.automaton.AutomatonInfrequentBehaviourRemover;
import com.raffaeleconforti.noisefiltering.event.selection.NoiseFilterResult;
import com.raffaeleconforti.outliers.statistics.boxplot.Percentile;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import com.raffaeleconforti.memorylog.XFactoryMemoryImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;

import java.util.*;

/**
 * Created by conforti on 7/02/15.
 */

public class InfrequentBehaviourFilter {

    private final XEventClassifier xEventClassifier;
    private final AutomatonFactory automatonFactory;

    private double finalThreshold = 0.0;
    private XLog finalLog = null;
    private final Map<Double, Boolean> upperbounds = new UnifiedMap<Double, Boolean>();

    private AutomatonInfrequentBehaviourDetector automatonInfrequentBehaviourDetector = new AutomatonInfrequentBehaviourDetector(AutomatonInfrequentBehaviourDetector.MAX);

    public InfrequentBehaviourFilter(XEventClassifier xEventClassifier) {
        this.xEventClassifier = xEventClassifier;
        automatonFactory = new AutomatonFactory(xEventClassifier);
    }

    public double[] discoverArcs(Automaton<String> automatonOriginal, double finalUpperBound) {
        List<Double> listArcs = new ArrayList<Double>();

        for(Edge<String> edge : automatonOriginal.getEdges()) {
            double value = automatonInfrequentBehaviourDetector.getFrequency(automatonOriginal, edge);
            if(value <= finalUpperBound) {
                listArcs.add(value);
            }
        }

        double[] arcs = new double[listArcs.size()];

        for(int i = 0; i < listArcs.size(); i++) {
            arcs[i] = listArcs.get(i);
        }

        Arrays.sort(arcs);

        System.out.println(Arrays.toString(arcs));
        return arcs;

    }

    public double discoverThreshold(double[] arcs, double percentile) {
        double upper_half_iqr = Percentile.evaluate(0.75, arcs) - Percentile.evaluate(0.5, arcs);
        double lower_half_iqr = Percentile.evaluate(0.5, arcs) - Percentile.evaluate(0.25, arcs);

        double limit = Percentile.evaluate(percentile, arcs);

        System.out.println("Percentile " + percentile + " FinalLimit " + limit + " Arcs " + arcs.length);

        double value = roundNumber(upper_half_iqr / lower_half_iqr, 3, false);
        while (value > 1 && arcs[0] < limit) {
            arcs = Arrays.copyOfRange(arcs, 1, arcs.length);
            upper_half_iqr = Percentile.evaluate(0.75, arcs) - Percentile.evaluate(0.5, arcs);
            lower_half_iqr = Percentile.evaluate(0.5, arcs) - Percentile.evaluate(0.25, arcs);
            value = roundNumber(upper_half_iqr / lower_half_iqr, 3, false);
        }

        double res = roundNumber(arcs[0], 3, true);
        if (res < roundNumber(res, 2, true)) {
            res = roundNumber(res, 2, true);
        }

        return res;
    }

    public XLog filterLog(final UIPluginContext context, XLog rawlog, NoiseFilterResult result) {
        XLog log = rawlog;
        LogOptimizer logOptimizer = new LogOptimizer();
        log = logOptimizer.optimizeLog(log);

        LogModifier logModifier = new LogModifier(new XFactoryMemoryImpl(), XConceptExtension.instance(), XTimeExtension.instance(), logOptimizer);
        logModifier.insertArtificialStartAndEndEvent(log);

        automatonInfrequentBehaviourDetector = new AutomatonInfrequentBehaviourDetector(result.getApproach());
        boolean repeated = result.isRepeated();
        XLog log2 = rawlog;
        int events;
        int newEvents = countEvents(log2);
        do {
            events = newEvents;
            XLog log3 = filter(context, log2, result.getRequiredStates(), result.isFixLevel(), result.getNoiseLevel(), true, result.getPercentile());
            newEvents = countEvents(log3);
            if(newEvents == 0) return log2;
            else log2 = log3;
            System.out.println("Removed " + (countEvents(rawlog) - newEvents) + " events");
        }while (newEvents < events && repeated);

        return log2;
    }

    private XLog filter(final UIPluginContext context, XLog rawlog, Set<Node<String>> requiredStates, boolean isFixLevel, double noiseLevel, boolean excludeTraces, double percentile) {

        XLog log2;
        XLog log = rawlog;
        LogOptimizer logOptimizer = new LogOptimizer();
        log = logOptimizer.optimizeLog(log);

        LogModifier logModifier = new LogModifier(new XFactoryMemoryImpl(), XConceptExtension.instance(), XTimeExtension.instance(), logOptimizer);
        logModifier.insertArtificialStartAndEndEvent(log);

        Automaton<String> automatonOriginal = automatonFactory.generate(log);
        Automaton<String> lastAutomaton = null;
        Automaton<String> automaton;

        double[] arcs = discoverArcs(automatonOriginal, 1.0);
        double noiseThreshold = discoverThreshold(arcs, percentile);

        double lowerbound = 0.0;
        double upperbound = noiseThreshold;

        if(isFixLevel) {
            try {
                noiseThreshold = noiseLevel;
                automaton = getFilteredAutomaton(automatonOriginal, requiredStates, noiseThreshold);
                log2 = AutomatonInfrequentBehaviourRemover.removeInfrequentBehaviour(context, xEventClassifier, log, automaton, lowerbound, upperbound, false, excludeTraces);
            }catch(HighThresholdException hte) {
                log.clear();
                return log;
            }
        }else {
            System.out.println("Threshold " + noiseThreshold);
            automaton = getFilteredAutomaton(automatonOriginal, requiredStates, noiseThreshold);

            System.out.println("Automaton " + lastAutomaton);
            if (lastAutomaton == null || !automaton.equals(lastAutomaton)) {
                try {
                    finalLog = AutomatonInfrequentBehaviourRemover.removeInfrequentBehaviour(context, xEventClassifier, log, automaton, lowerbound, upperbound, true, excludeTraces);
                    finalThreshold = noiseThreshold;
                } catch (HighThresholdException e) {
                    System.out.println("Identifying best upperbound...");
                    noiseThreshold = roundNumber(findBestUpperbound(context, log, requiredStates, upperbound/2, upperbound, excludeTraces));
                    System.out.println("Best upperbound " + noiseThreshold);
                }
            }

            if(finalThreshold == noiseThreshold && finalLog != null) {
                log2 = finalLog;
            }else {
                try {
                    automaton = getFilteredAutomaton(automatonOriginal, requiredStates, noiseThreshold);
                    log2 = AutomatonInfrequentBehaviourRemover.removeInfrequentBehaviour(context, xEventClassifier, log, automaton, 0, noiseThreshold, true, excludeTraces);
                } catch (HighThresholdException hte) {
                    log.clear();
                    return log;
                }
            }
        }

        logModifier.removeArtificialStartAndEndEvent(log2);

        if(log2.size() == 0) {
            log2 = log;
        }

        return log2;
    }

    private Automaton<String> getFilteredAutomaton(Automaton<String> automatonOriginal, Set<Node<String>> requiredStates, double threshold) {
        Automaton<String> automaton = (Automaton<String>) automatonOriginal.clone();
        return automatonInfrequentBehaviourDetector.removeInfrequentBehaviour(automaton, requiredStates, threshold);
    }

    private double findBestUpperbound(PluginContext context, XLog log, Set<Node<String>> requiredStates, double upperbound, double oldUpperbound, boolean excludeTraces) {

        Automaton<String> automatonOriginal = automatonFactory.generate(log);
        Automaton<String> lastAutomaton = null;
        Automaton<String> automaton;

        do {
            double noiseThreshold = roundNumber((upperbound + oldUpperbound)/2);

            System.out.println("NewUpperbound " + noiseThreshold);
            try {
                if(upperbound != oldUpperbound) {
                    automaton = getFilteredAutomaton(automatonOriginal, requiredStates, noiseThreshold);

                    if(lastAutomaton == null || !automaton.equals(lastAutomaton)) {
                        if(!upperbounds.containsKey(noiseThreshold)) {
                            lastAutomaton = automaton;
                            upperbounds.put(noiseThreshold, false);
                            finalLog = AutomatonInfrequentBehaviourRemover.removeInfrequentBehaviour(context, xEventClassifier, log, automaton, upperbound, oldUpperbound, true, excludeTraces);
                            finalThreshold = noiseThreshold;
                            upperbounds.put(noiseThreshold, true);
                        }else {
                            if(!upperbounds.get(noiseThreshold)) {
                                throw new HighThresholdException();
                            }
                        }
                    }else {
                        throw new HighThresholdException();
                    }

                    upperbound = noiseThreshold;

                }else {
                    return noiseThreshold;
                }
            } catch (HighThresholdException e) {
                double tmpNoiseThreshold = roundNumber((upperbound + noiseThreshold)/2);

                if(tmpNoiseThreshold != noiseThreshold) {
                    oldUpperbound = noiseThreshold;
                }else {
                    oldUpperbound = upperbound;
                }

                if(upperbound > oldUpperbound) {
                    oldUpperbound = upperbound;
                }
            }
        }while(true);
    }

    private double roundNumber(double number) {
        return (double) Math.round(number * 100) / 100;
    }

    private double roundNumber(double number, int decimal, boolean ceil) {
        double power = Math.pow(10, decimal);
        if (ceil) {
            return Math.ceil(number * power) / power;
        }else {
            return (double) Math.round(number * power) / power;
        }
    }

    public XLog filterLog(XLog rawlog) {

        XLog log = rawlog;
        LogOptimizer logOptimizer = new LogOptimizer();
        log = logOptimizer.optimizeLog(log);

        LogModifier logModifier = new LogModifier(new XFactoryMemoryImpl(), XConceptExtension.instance(), XTimeExtension.instance(), logOptimizer);
        logModifier.insertArtificialStartAndEndEvent(log);

        Automaton<String> automatonOriginal = automatonFactory.generate(log);
        Automaton<String> lastAutomaton = null;
        Automaton<String> automaton;

        double[] arcs = discoverArcs(automatonOriginal, 1.0);
        double noiseThreshold = discoverThreshold(arcs, 0.125);
        Set<Node<String>> requiredStates = automatonOriginal.getNodes();

        double lowerbound = 0.0;
        double upperbound = noiseThreshold;

        XLog log2 = rawlog;
        int events;
        int newEvents = countEvents(log2);

        UIPluginContext context = new FakePluginContext();
        do {
            automaton = getFilteredAutomaton(automatonOriginal, requiredStates, noiseThreshold);

            if (lastAutomaton == null || !automaton.equals(lastAutomaton)) {
                try {
                    finalLog = AutomatonInfrequentBehaviourRemover.removeInfrequentBehaviour(context, xEventClassifier, log, automaton, lowerbound, upperbound, true, true);
                    finalThreshold = noiseThreshold;
                } catch (HighThresholdException e) {
                    noiseThreshold = roundNumber(findBestUpperbound(context, log, requiredStates, upperbound/2, upperbound, true));
                }
            }

            if(finalThreshold == noiseThreshold && finalLog != null) {
                log2 = finalLog;
            }else {
                try {
                    automaton = getFilteredAutomaton(automatonOriginal, requiredStates, noiseThreshold);
                    log2 = AutomatonInfrequentBehaviourRemover.removeInfrequentBehaviour(context, xEventClassifier, log, automaton, 0, noiseThreshold, true, true);
                } catch (HighThresholdException hte) {
                    log.clear();
                    return log;
                }
            }
            events = newEvents;
            newEvents = countEvents(log2);
        }while (newEvents < events);

        logModifier.removeArtificialStartAndEndEvent(log2);

        return log2;
    }

    private static int countEvents(XLog log) {
        int count = 0;
        for(XTrace trace : log) {
            if(trace.size() > 1) {
                count += (trace.size() - 2);
            }
        }
        return count;
    }

}
