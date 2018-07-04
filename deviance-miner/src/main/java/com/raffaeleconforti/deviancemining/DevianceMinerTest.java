package com.raffaeleconforti.deviancemining;

import com.raffaeleconforti.log.util.LogImporter;
import org.deckfour.xes.factory.XFactory;
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

    public static void main2(String[] args) throws Exception {
        XFactory factory = new XFactoryNaiveImpl();
        String path = "/Volumes/MobileData/Logs/Deviance Mining/";
        String[] log_names = new String[]{
                "Deviance Mining - Standard",
                "Deviance Mining - Var1",
                "Deviance Mining - Var2",
                "Deviance Mining - Var3",
                "Deviance Mining - Var4"};
        XLog[] logs = new XLog[log_names.length];

        for (int i = 0; i < log_names.length; i++) {
            logs[i] = LogImporter.importFromFile(factory, path + log_names[i] + file_ext);
        }
        XLog log = factory.createLog(logs[0].getAttributes());
        for (XLog log1 : logs) {
            log.addAll(log1);
        }
        LogImporter.exportToFile(path, "Deviance Mining" + file_ext, log);
    }

    public static void main(String[] args) throws Exception {
        DevianceMiner devianceMiner = new DevianceMiner();

        String path = "/Volumes/MobileData/Logs/Deviance Mining/";
        XLog original_log = LogImporter.importFromFile(new XFactoryNaiveImpl(), path + "Deviance Mining" + file_ext);
//        XLog original_log = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/Logs/4TU Logs/Sepsis Cases" + file_ext);
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

        LogImporter.exportToFile(path, "Deviance Mining - Correct" + file_ext, devianceMiner.getNormalLog());
        LogImporter.exportToFile("Deviance Mining - Deviances" + file_ext, devianceMiner.getDeviantLog());

    }

}
