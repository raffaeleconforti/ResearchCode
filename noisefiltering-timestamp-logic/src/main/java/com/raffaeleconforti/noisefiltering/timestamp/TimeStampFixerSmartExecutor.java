package com.raffaeleconforti.noisefiltering.timestamp;

import com.raffaeleconforti.automaton.Automaton;
import com.raffaeleconforti.automaton.AutomatonFactory;
import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.log.util.LogCloner;
import com.raffaeleconforti.log.util.LogModifier;
import com.raffaeleconforti.log.util.LogOptimizer;
import com.raffaeleconforti.noisefiltering.timestamp.traceselector.AutomatonBestTraceMatchSelector;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by conforti on 7/02/15.
 */

public class TimeStampFixerSmartExecutor {

    private final XEventClassifier xEventClassifier = new XEventAndClassifier(new XEventNameClassifier());

//    private AutomatonInfrequentBehaviourDetector automatonInfrequentBehaviourDetector = new AutomatonInfrequentBehaviourDetector(AutomatonInfrequentBehaviourDetector.MAX);

    private final AutomatonFactory automatonFactory = new AutomatonFactory(xEventClassifier);

    private final SimpleDateFormat dateFormatSeconds = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private final boolean debug_mode;
    private final boolean useGurobi;
    private final boolean useArcsFrequency;

    public TimeStampFixerSmartExecutor(boolean useGurobi, boolean useArcsFrequency, boolean debug_mode) {
        this.debug_mode = debug_mode;
        this.useGurobi = useGurobi;
        this.useArcsFrequency = useArcsFrequency;
    }

//    public XLog filterLog(XLog log, int limitExtensive, int approach, boolean debug_mode) {
//
//        XFactory factory = new XFactoryNaiveImpl();//XFactoryMemoryImpl();
//        LogCloner logCloner = new LogCloner(factory);
//
//        XLog res = logCloner.cloneLog(log);
//
//        LogOptimizer logOptimizer = new LogOptimizer();
//        LogModifier logModifier = new LogModifier(factory, XConceptExtension.instance(), XTimeExtension.instance(), logOptimizer);
//
//        int[] fix = new int[]{0};
//
//        res = logModifier.sortLog(res);
//        XLog optimizedLog = logOptimizer.optimizeLog(res);
//        optimizedLog = logModifier.insertArtificialStartAndEndEvent(optimizedLog);
//
//        if (debug_mode) {
//            System.out.println("Permutations discovery started");
//        }
//        TimeStampFixer timeStampFixerSmart = new TimeStampFixerSmart(factory, logCloner, optimizedLog, xEventClassifier, dateFormatSeconds, limitExtensive, approach, useGurobi, useArcsFrequency, debug_mode);
//
//        XLog permutedLog = timeStampFixerSmart.obtainPermutedLog();
////        permutedLog = logModifier.insertArtificialStartAndEndEvent(permutedLog);
//
//        if (debug_mode) {
//            System.out.println("Permutations discovery ended");
//            System.out.println(timeStampFixerSmart.getDuplicatedTraces());
//        }
//
//        Automaton<String> automaton = automatonFactory.generateForTimeFilter(permutedLog, timeStampFixerSmart.getDuplicatedEvents());
//
////        InfrequentBehaviourFilter infrequentBehaviourFilter = new InfrequentBehaviourFilter(xEventClassifier);
////        double[] arcs = infrequentBehaviourFilter.discoverArcs(automaton, 1.0);
////        AutomatonInfrequentBehaviourDetector automatonInfrequentBehaviourDetector = new AutomatonInfrequentBehaviourDetector(AutomatonInfrequentBehaviourDetector.MAX);
////        Automaton<String> automatonClean = automatonInfrequentBehaviourDetector.removeInfrequentBehaviour(automaton, automaton.getNodes(), infrequentBehaviourFilter.discoverThreshold(arcs, 0.125), useGurobi, useArcsFrequency);
//
//        if (debug_mode) {
//            System.out.println();
//            System.out.println("Selection best permutation started");
//        }
//        AutomatonBestTraceMatchSelector automatonBestTraceMatchSelector = new AutomatonBestTraceMatchSelector(permutedLog, xEventClassifier, automaton, timeStampFixerSmart.getDuplicatedTraces(), timeStampFixerSmart.getPossibleTraces(), timeStampFixerSmart.getFaultyEvents(), log.size());
////        AutomatonBestTraceMatchSelector automatonBestTraceMatchSelector = new AutomatonBestTraceMatchSelector(permutedLog, xEventClassifier, automatonClean, timeStampFixerSmart.getDuplicatedTraces(), timeStampFixerSmart.getPossibleTraces(), timeStampFixerSmart.getFaultyEvents(), log.size());
//        List<String> fixedTraces = new ArrayList<String>();
//
//        res = automatonBestTraceMatchSelector.selectBestMatchingTraces(new FakePluginContext(), fix, fixedTraces, approach);
//        res = logModifier.removeArtificialStartAndEndEvent(res);
//
//        if (debug_mode) {
//            System.out.println("Selection best permutation completed");
//            System.out.println();
//            System.out.println("Timestamps disambiguation started");
//        }
//
//        TimestampsAssigner timestampsAssigner = new TimestampsAssigner(res, xEventClassifier, dateFormatSeconds, timeStampFixerSmart.getDuplicatedTraces(), timeStampFixerSmart.getDuplicatedEvents(), useGurobi, useArcsFrequency, debug_mode);
//        boolean result = timestampsAssigner.assignTimestamps(fixedTraces);
//
//        if (!result) {
//            timestampsAssigner.assignTimestamps();
//        }
//        res = logModifier.sortLog(res);
//
//        if (debug_mode) {
//            System.out.println("Timestamps disambiguation completed");
//        }
//
//        return res;
//    }

    public XLog filterLog(XLog log, int limitExtensive, int approach, boolean debug_mode, boolean self_cleaning) {

        XFactory factory = new XFactoryNaiveImpl();//XFactoryMemoryImpl();
        LogCloner logCloner = new LogCloner(factory);

        XLog res = logCloner.cloneLog(log);

        LogOptimizer logOptimizer = new LogOptimizer();
        LogModifier logModifier = new LogModifier(factory, XConceptExtension.instance(), XTimeExtension.instance(), logOptimizer);

        res = logModifier.sortLog(res);
        XLog optimizedLog = logOptimizer.optimizeLog(log);
        optimizedLog = logModifier.insertArtificialStartAndEndEvent(optimizedLog);

        if (debug_mode) {
            System.out.println("Permutations discovery started");
        }
        TimeStampFixer timeStampFixerSmart = new TimeStampFixerSmart(factory, logCloner, optimizedLog, xEventClassifier, dateFormatSeconds, limitExtensive, approach, useGurobi, useArcsFrequency, debug_mode, self_cleaning);
        List<String> fixedTraces = new ArrayList<String>();

        long start = System.currentTimeMillis();
        res = sortLog(res, logModifier, timeStampFixerSmart, fixedTraces, approach, debug_mode, self_cleaning);
        long middle = System.currentTimeMillis();
        res = assignTimestamp(res, logModifier, timeStampFixerSmart, fixedTraces, debug_mode);
        long end = System.currentTimeMillis();

        System.out.println("Ordering: " + (middle - start) + " Assignment: " + (end - middle) + " Total: " + (end - start));

        return res;
    }

    public XLog sortLog(XLog log, LogModifier logModifier, TimeStampFixer timeStampFixerSmart, List<String> fixedTraces, int approach, boolean debug_mode, boolean self_cleaning) {
        int[] fix = new int[]{0};

        XLog permutedLog = timeStampFixerSmart.obtainPermutedLog();
//        permutedLog = logModifier.insertArtificialStartAndEndEvent(permutedLog);

        if (debug_mode) {
            System.out.println("Permutations discovery ended");
            System.out.println(timeStampFixerSmart.getDuplicatedTraces());
        }

        Automaton<String> automaton = automatonFactory.generateForTimeFilter(permutedLog, timeStampFixerSmart.getDuplicatedEvents());

//        InfrequentBehaviourFilter infrequentBehaviourFilter = new InfrequentBehaviourFilter(xEventClassifier);
//        double[] arcs = infrequentBehaviourFilter.discoverArcs(automaton, 1.0);
//        AutomatonInfrequentBehaviourDetector automatonInfrequentBehaviourDetector = new AutomatonInfrequentBehaviourDetector(AutomatonInfrequentBehaviourDetector.MAX);
//        Automaton<String> automatonClean = automatonInfrequentBehaviourDetector.removeInfrequentBehaviour(automaton, automaton.getNodes(), infrequentBehaviourFilter.discoverThreshold(arcs, 0.125), useGurobi, useArcsFrequency);

        if (debug_mode) {
            System.out.println();
            System.out.println("Selection best permutation started");
        }
        AutomatonBestTraceMatchSelector automatonBestTraceMatchSelector = new AutomatonBestTraceMatchSelector(permutedLog, xEventClassifier, automaton, timeStampFixerSmart.getDuplicatedTraces(), timeStampFixerSmart.getPossibleTraces(), timeStampFixerSmart.getFaultyEvents(), log.size());
//        AutomatonBestTraceMatchSelector automatonBestTraceMatchSelector = new AutomatonBestTraceMatchSelector(permutedLog, xEventClassifier, automatonClean, timeStampFixerSmart.getDuplicatedTraces(), timeStampFixerSmart.getPossibleTraces(), timeStampFixerSmart.getFaultyEvents(), log.size());

        log = automatonBestTraceMatchSelector.selectBestMatchingTraces(new FakePluginContext(), fix, fixedTraces, approach, self_cleaning);
        log = logModifier.removeArtificialStartAndEndEvent(log);

        if (debug_mode) {
            System.out.println("Selection best permutation completed");
            System.out.println();
            System.out.println("Timestamps disambiguation started");
        }

        return log;
    }

    public XLog assignTimestamp(XLog log, LogModifier logModifier, TimeStampFixer timeStampFixerSmart, List<String> fixedTraces, boolean debug_mode) {
        TimestampsAssigner timestampsAssigner = new TimestampsAssigner(log, xEventClassifier, dateFormatSeconds, timeStampFixerSmart.getDuplicatedTraces(), timeStampFixerSmart.getDuplicatedEvents(), useGurobi, useArcsFrequency, debug_mode);
        boolean result = timestampsAssigner.assignTimestamps(fixedTraces);

        if(!result) {
            timestampsAssigner.assignTimestamps();
        }
        log = logModifier.sortLog(log);

        if(debug_mode) {
            System.out.println("Timestamps disambiguation completed");
        }

        return log;
    }

}
