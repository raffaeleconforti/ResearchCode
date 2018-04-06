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
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by conforti on 7/02/15.
 */

public class TimeStampFixerRandomExecutor {

    private final XEventClassifier xEventClassifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());

    private final AutomatonFactory automatonFactory = new AutomatonFactory(xEventClassifier);

    private final SimpleDateFormat dateFormatSeconds = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private final boolean useGurobi;
    private final boolean useArcsFrequency;

    public TimeStampFixerRandomExecutor(boolean useGurobi, boolean useArcsFrequency) {
        this.useGurobi = useGurobi;
        this.useArcsFrequency = useArcsFrequency;
    }

    public XLog filterLog(XLog log) {

        LogCloner logCloner = new LogCloner();

        XLog res = logCloner.cloneLog(log);

        LogOptimizer logOptimizer = new LogOptimizer();
        LogModifier logModifier = new LogModifier(new XFactoryMemoryImpl(), XConceptExtension.instance(), XTimeExtension.instance(), logOptimizer);

        int[] fix = new int[] {1};

        boolean doneOnce = false;
        int count = 0;
//        while(fix[0] > 0 && count < res.size() / 4) {
            count++;
            fix[0] = 0;

            res = logModifier.sortLog(res);
            XLog optimizedLog = logOptimizer.optimizeLog(res);

            System.out.println("Start permutations");
            TimeStampFixer timeStampFixerRandom = new TimeStampFixerRandom(new XFactoryNaiveImpl(), logCloner, optimizedLog, xEventClassifier, dateFormatSeconds, 0, 0, useGurobi, useArcsFrequency);

            XLog permutedLog = timeStampFixerRandom.obtainPermutedLog();
            permutedLog = logModifier.insertArtificialStartAndEndEvent(permutedLog);
            System.out.println("Permutations ended");

//            try {
//                LogImporter.exportToFile("", "PermutedDummyLog" + count + ".xes.gz", permutedLog);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            System.out.println(timeStampFixerRandom.getDuplicatedTraces());
            Automaton<String> automaton = automatonFactory.generateForTimeFilter(permutedLog, timeStampFixerRandom.getDuplicatedEvents());

            System.out.println("Start filtering");
            AutomatonBestTraceMatchSelector automatonBestTraceMatchSelector = new AutomatonBestTraceMatchSelector(permutedLog, xEventClassifier, automaton, timeStampFixerRandom.getDuplicatedTraces(), timeStampFixerRandom.getPossibleTraces(), timeStampFixerRandom.getFaultyEvents(), log.size());
            List<String> fixedTraces = new ArrayList<String>();

            res = automatonBestTraceMatchSelector.selectBestMatchingTraces(new FakePluginContext(), fix, fixedTraces, 0, false);
            res = logModifier.removeArtificialStartAndEndEvent(res);
            res = logModifier.sortLog(res);

            TimestampsAssigner timestampsAssigner = new TimestampsAssigner(res, xEventClassifier, dateFormatSeconds, timeStampFixerRandom.getDuplicatedTraces(), timeStampFixerRandom.getDuplicatedEvents(), useGurobi, useArcsFrequency, false);
            boolean result = timestampsAssigner.assignTimestampsDummy(fixedTraces);

            System.out.println();

            if(!result) {
                fix[0] = 0;
            }

            if(fix[0] > 0) {
                System.out.println(fix[0]);
                doneOnce = false;
            }

            if(fix[0] == 0 && !doneOnce) {
                timestampsAssigner.assignTimestampsDummy();
                fix[0] = 1;
                doneOnce = true;
            }
//        }

        System.out.println("Filtering completed");

        return res;
    }

}
