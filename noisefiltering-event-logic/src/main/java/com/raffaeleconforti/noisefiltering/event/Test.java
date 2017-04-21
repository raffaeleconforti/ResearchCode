package com.raffaeleconforti.noisefiltering.event;

import com.raffaeleconforti.log.util.LogImporter;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 20/4/17.
 */
public class Test {

    public static void main(String[] args) throws Exception {
        XLog log = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/SharedFolder/Logs/ArtificialLess.xes.gz");

        test(log, true);
        test(log, false);
    }

    private static void test(XLog log, boolean useGurobi) {
        InfrequentBehaviourFilter filter = new InfrequentBehaviourFilter(new XEventNameClassifier(), useGurobi);
        filter.filterLog(log);
    }

}
