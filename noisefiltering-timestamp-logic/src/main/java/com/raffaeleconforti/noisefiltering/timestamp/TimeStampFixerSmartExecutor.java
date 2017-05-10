package com.raffaeleconforti.noisefiltering.timestamp;

import com.raffaeleconforti.automaton.Automaton;
import com.raffaeleconforti.automaton.AutomatonFactory;
import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.log.util.LogCloner;
import com.raffaeleconforti.log.util.LogModifier;
import com.raffaeleconforti.log.util.LogOptimizer;
import com.raffaeleconforti.memorylog.XFactoryMemoryImpl;
import com.raffaeleconforti.noisefiltering.timestamp.traceselector.AutomatonBestTraceMatchSelector;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
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

    private final boolean useGurobi;
    private final boolean useArcsFrequency;

    public TimeStampFixerSmartExecutor(boolean useGurobi, boolean useArcsFrequency) {
        this.useGurobi = useGurobi;
        this.useArcsFrequency = useArcsFrequency;
    }

    public XLog filterLog(XLog log, int limitExtensive, int approach) {

        XFactory factory = new XFactoryMemoryImpl();
        LogCloner logCloner = new LogCloner(factory);

        XLog res = logCloner.cloneLog(log);

        LogOptimizer logOptimizer = new LogOptimizer();
        LogModifier logModifier = new LogModifier(factory, XConceptExtension.instance(), XTimeExtension.instance(), logOptimizer);

        int[] fix = new int[] {0};

        res = logModifier.sortLog(res);
        XLog optimizedLog = logOptimizer.optimizeLog(res);
        optimizedLog = logModifier.insertArtificialStartAndEndEvent(optimizedLog);

        System.out.println("Permutations discovery started");
        TimeStampFixer timeStampFixerSmart = new TimeStampFixerSmart(factory, logCloner, optimizedLog, xEventClassifier, dateFormatSeconds, limitExtensive, approach, useGurobi, useArcsFrequency);

        XLog permutedLog = timeStampFixerSmart.obtainPermutedLog();
//        permutedLog = logModifier.insertArtificialStartAndEndEvent(permutedLog);
        System.out.println("Permutations discovery ended");

        System.out.println(timeStampFixerSmart.getDuplicatedTraces());
        Automaton<String> automaton = automatonFactory.generateForTimeFilter(permutedLog, timeStampFixerSmart.getDuplicatedEvents());

        System.out.println();

        System.out.println("Selection best permutation started");
        AutomatonBestTraceMatchSelector automatonBestTraceMatchSelector = new AutomatonBestTraceMatchSelector(permutedLog, xEventClassifier, automaton, timeStampFixerSmart.getDuplicatedTraces(), timeStampFixerSmart.getPossibleTraces(), timeStampFixerSmart.getFaultyEvents(), log.size());
        List<String> fixedTraces = new ArrayList<String>();

        res = automatonBestTraceMatchSelector.selectBestMatchingTraces(new FakePluginContext(), fix, fixedTraces, approach);
        res = logModifier.removeArtificialStartAndEndEvent(res);

        System.out.println("Selection best permutation completed");

        System.out.println();

        System.out.println("Assignation timestamps started");
        TimestampsAssigner timestampsAssigner = new TimestampsAssigner(res, xEventClassifier, dateFormatSeconds, timeStampFixerSmart.getDuplicatedTraces(), timeStampFixerSmart.getDuplicatedEvents(), useGurobi, useArcsFrequency);
        boolean result = timestampsAssigner.assignTimestamps(fixedTraces);

        if(!result) {
            timestampsAssigner.assignTimestamps();
        }
        res = logModifier.sortLog(res);

        System.out.println("Assignation timestamps completed");

        return res;
    }

}
