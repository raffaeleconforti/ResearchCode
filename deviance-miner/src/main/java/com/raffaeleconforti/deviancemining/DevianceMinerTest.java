/*
 *  Copyright (C) 2018 Raffaele Conforti (www.raffaeleconforti.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.raffaeleconforti.deviancemining;

import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.memorylog.XLogImpl;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import java.util.*;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 20/4/17.
 */
public class DevianceMinerTest {

    private final static String file_ext = ".xes.gz";

    public static void main2(String[] args) throws Exception {

        String path = "/Volumes/MobileData/Logs/Deviance Mining/Reduced Logs/";
        XLog original_log = LogImporter.importFromFile(new XFactoryNaiveImpl(), path + "Deviance Mining" + file_ext);
        int i = 1;
        for (XTrace trace : original_log) {
            XConceptExtension.instance().assignName(trace, "" + i);
            i++;
        }
        LogImporter.exportToFile(path, "Deviance Mining 1" + file_ext, original_log);
    }

    public static void main1(String[] args) throws Exception {
        XFactory factory = new XFactoryNaiveImpl();
        String path = "/Volumes/MobileData/Logs/Deviance Mining/Reduced Logs/";
        String[] log_names = new String[]{
                "Deviance Mining - Standard",
                "Deviance Mining - Var1",
                "Deviance Mining - Var2",
                "Deviance Mining - Var3",
                "Deviance Mining - Var4"
        };
//        log_names = new String[]{
//                "Deviance Mining - Standard.1",
//                "Deviance Mining - Standard.2",
////                "Deviance Mining - Standard.3",
//        };

        XLog[] logs = new XLog[log_names.length];

        for (int i = 0; i < log_names.length; i++) {
            logs[i] = LogImporter.importFromFile(factory, path + log_names[i] + file_ext);
        }
        XLog log = factory.createLog(logs[0].getAttributes());
        for (XLog log1 : logs) {
            log.addAll(log1);
        }
//        LogImporter.exportToFile(path, "Deviance Mining - Standard" + file_ext, log);
        LogImporter.exportToFile(path, "Deviance Mining" + file_ext, log);
        main1(null);
    }

    public static void main(String[] args) throws Exception {

        String path = "/Volumes/MobileData/Logs/Deviance Mining/Reduced Logs/";
        XLog original_log = LogImporter.importFromFile(new XFactoryNaiveImpl(), path + "Deviance Mining" + file_ext);
//        XLog original_log = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/Logs/4TU Logs/Sepsis Cases" + file_ext);
//        XLog original_log = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/Logs/4TU Logs/RTFMP" + file_ext);
//        XLog original_log = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/Logs/4TU Logs/BPIC 2012 - Loan Application" + file_ext);

        DevianceMiner devianceMiner = new DevianceMiner();
        devianceMiner.mineDeviances(original_log);
        XLog normal = devianceMiner.getNormalLog();

        LogImporter.exportToFile(path, "Deviance Mining - Correct" + file_ext, normal);
        LogImporter.exportToFile(path, "Deviance Mining - Deviant" + file_ext, devianceMiner.getDeviantLog());

        List<XLog> deviants = splitLog(devianceMiner.getDeviantLog());
//        List<XLog> deviants = new ArrayList<>();
//        deviants.add(devianceMiner.getDeviantLog());

        List<String> messages = new ArrayList<>();
        Set<Deviance> toPrint = new HashSet<>();
        Map<String, Set<XTrace>> map = new HashMap<>();
        int dev = 0;

        List<Deviance> list = new ArrayList<>();
        for (XLog deviant : deviants) {
            dev++;
            normal = LogImporter.importFromFile(new XFactoryNaiveImpl(), path + "Deviance Mining - Correct" + file_ext);

            devianceMiner = new DevianceMiner();
            Set<Deviance> deviances = devianceMiner.generateStatementsFromTriples(normal, "normal behaviour", deviant, "deviant behaviour");
            List<Deviance> relevant_deviances = devianceMiner.retainRelevantDeviances(deviances, normal.size() + deviant.size());
            list.addAll(relevant_deviances);
        }

        removeRedundant(list);

        for (Deviance deviance : list) {
            String d = deviance.getNormal_statement();
            if (d.equals("") || d.equals("does not")) d = "D+" + deviance.getDeviant_statement();
            Set<XTrace> set = map.get(d);
            if (set == null) {
                map.put(d, deviance.getDeviantTraces());
            } else {
                set.addAll(deviance.getDeviantTraces());
            }
        }

        list.clear();
        for (String key : map.keySet()) {
            normal = LogImporter.importFromFile(new XFactoryNaiveImpl(), path + "Deviance Mining - Correct" + file_ext);

            XLog deviant = new XFactoryNaiveImpl().createLog();
            deviant.addAll(map.get(key));
            devianceMiner = new DevianceMiner();
            Set<Deviance> deviances = devianceMiner.generateStatementsFromTriples(normal, "normal behaviour", deviant, "deviant behaviour");
            List<Deviance> relevant_deviances = devianceMiner.retainRelevantDeviances(deviances, normal.size() + deviant.size());
            list.addAll(relevant_deviances);
        }

        removeRedundant(list);

        devianceMiner = new DevianceMiner();
        List<Deviance> relevant_deviances = devianceMiner.retainRelevantDeviances(new HashSet<>(list), 0);
        devianceMiner.determineCausalityBetweenDeviances(relevant_deviances);

        for (Deviance deviance : relevant_deviances) {
            deviance.setOriginalLogSize(original_log.size());
            if (deviance.isTopDeviance()) {
                boolean found = false;
                Deviance replace = null;
                for (Deviance old : toPrint) {
                    if (old.equals(deviance)) {
                        if (old.getRelevance() > deviance.getRelevance()) {
                            old.addAllConsequentDeviations(deviance.getConsequentDeviations());
                        } else {
                            replace = old;
                        }
                        found = true;
                    }
                }
                if (replace != null) {
                    toPrint.remove(replace);
                    deviance.addAllConsequentDeviations(replace.getConsequentDeviations());
                    toPrint.add(deviance);
                }
                if (!found) {
                    toPrint.add(deviance);
                }
            }
        }

//        for(XLog deviant : deviants) {
//            dev++;
//            normal = LogImporter.importFromFile(new XFactoryNaiveImpl(), path + "Deviance Mining - Correct" + file_ext);
//
//            devianceMiner = new DevianceMiner();
//            Set<Deviance> deviances = devianceMiner.generateStatementsFromTriples(normal, "normal behaviour", deviant, "deviant behaviour");
//            List<Deviance> relevant_deviances = devianceMiner.retainRelevantDeviances(deviances, normal.size() + deviant.size());
//            devianceMiner.determineCausalityBetweenDeviances(relevant_deviances);
//
//            for (Deviance deviance : relevant_deviances) {
//                deviance.setOriginalLogSize(original_log.size());
//                if (deviance.isTopDeviance()) {
//                    boolean found = false;
//                    Deviance replace = null;
//                    for(Deviance old : toPrint) {
//                        if(old.equals(deviance)) {
//                            if(old.getRelevance() > deviance.getRelevance()) {
//                                old.addAllConsequentDeviations(deviance.getConsequentDeviations());
//                            }else {
//                                replace = old;
//                            }
//                            found = true;
//                        }
//                    }
//                    if(replace != null) {
//                        toPrint.remove(replace);
//                        deviance.addAllConsequentDeviations(replace.getConsequentDeviations());
//                        toPrint.add(deviance);
//                    }
//                    if(!found) {
//                        toPrint.add(deviance);
//                    }
//                }
//            }
//        }
        list = new ArrayList<>(toPrint);
        Collections.sort(list);
        removeRedundant(list);

        dev = 0;
        for (Deviance deviance : list) {
            dev++;
            messages.add(dev + " - " + deviance.toFullString(2, 0.005));

            XLog log = new XFactoryNaiveImpl().createLog();
            log.addAll(deviance.getNormalTraces());
            LogImporter.exportToFile(path, "cor." + dev + file_ext, log);

            log = new XFactoryNaiveImpl().createLog();
            log.addAll(deviance.getDeviantTraces());
            LogImporter.exportToFile(path, "dev." + dev + file_ext, log);
        }
        for (String deviance : messages) {
            System.out.println(deviance);
        }
    }

    private static List<XLog> splitLog(XLog log) {
        DevianceMiner devianceMiner = new DevianceMiner();
        devianceMiner.mineDeviances(log);
        List<XLog> logs = new ArrayList<>();
        if (devianceMiner.getNormalLog().size() > 0 && devianceMiner.getDeviantLog().size() > 0) {
            logs.addAll(splitLog(devianceMiner.getNormalLog()));
            logs.addAll(splitLog(devianceMiner.getDeviantLog()));
        } else {
            if (devianceMiner.getNormalLog().size() > 0) logs.add(devianceMiner.getNormalLog());
            else if (devianceMiner.getDeviantLog().size() > 0) logs.add(devianceMiner.getDeviantLog());
        }
        return logs;
    }

    private static void removeRedundant(List<Deviance> list) {
        Collections.sort(list);
        DevianceMiner devianceMiner = new DevianceMiner();
        boolean cycle = true;
        while (cycle) {
            cycle = false;
            loop:
            for (Deviance deviance1 : list) {
                if (deviance1.getRelevance() == 0) {
                    list.remove(deviance1);
                    cycle = true;
                    break;
                }
                for (Deviance deviance2 : list) {
                    if (!deviance1.equals(deviance2) && devianceMiner.checkInclusion(
                            deviance1.getNormal_statement(),
                            deviance2.getNormal_statement(),
                            deviance1.getDeviant_statement(),
                            deviance2.getDeviant_statement())) {
                        list.remove(deviance2);
                        cycle = true;
                        break loop;
                    }
                }
            }
        }
    }
}
