package com.raffaeleconforti.deviancemining;

import com.raffaeleconforti.log.util.LogImporter;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 20/4/17.
 */
public class DevianceMinerTest {

    private final static String file_ext = ".xes.gz";

    public static void main(String[] args) throws Exception {

    }

    public static void main1(String[] args) throws Exception {
        DevianceMiner devianceMiner = new DevianceMiner();

//        XLog original_log = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Users/raffaele/Downloads/Deviance Mining - performance and consequences.xes");
        XLog original_log = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/Logs/4TU Logs/Sepsis Cases" + file_ext);
//        XLog original_log = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/Logs/4TU Logs/RTFMP" + file_ext);
//        XLog original_log = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/Logs/4TU Logs/BPIC 2012 - Loan Application" + file_ext);

        List<Deviance> relevant_deviances = devianceMiner.mineDeviances(original_log);

        List<Deviance> toPrint = new ArrayList<>();
        for (Deviance deviance : relevant_deviances) {
            if (deviance.isTopDeviance()) toPrint.add(deviance);
        }
        Collections.sort(toPrint);

        for (Deviance deviance : toPrint) {
            System.out.println(deviance.toFullString(0.01));
        }
        for (Deviance deviance : relevant_deviances) {
//            deviance.serialize("/Users/raffaele/Downloads/Test/");
        }

        LogImporter.exportToFile("/Users/raffaele/Downloads/Deviance Mining - Correct" + file_ext, devianceMiner.getNormalLog());
        LogImporter.exportToFile("/Users/raffaele/Downloads/Deviance Mining - Deviances" + file_ext, devianceMiner.getDeviantLog());

    }

}
