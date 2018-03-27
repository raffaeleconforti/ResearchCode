package com.raffaeleconforti.noisefiltering.timestamp;

import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.log.util.NameExtractor;
import com.raffaeleconforti.log.util.TraceToString;
import com.raffaeleconforti.marking.MarkingDiscoverer;
import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.impl.AlignmentBasedFitness;
import com.raffaeleconforti.noisefiltering.timestamp.check.TimeStampChecker;
import com.raffaeleconforti.statistics.StatisticsSelector;
import com.raffaeleconforti.wrappers.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.acceptingpetrinet.plugins.ImportAcceptingPetriNetPlugin;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by conforti on 7/02/15.
 */

public class TimeStampFilterChecker {

    XConceptExtension xce = XConceptExtension.instance();
    XTimeExtension xte = XTimeExtension.instance();

    //Graph
    public static void logGeneration(String[] args) throws Exception {
        String path = "/Volumes/Data/Dropbox/LaTex/2017/Timestamp Repair/Logs/Experiments/";
        String logExtension = ".xes.gz";

        String[] typeLogs = new String[] {"Event", "Trace", "UniqueTrace"};
        String[] typeFilters = new String[] {"", " ILP", "D", "R"};

        String[] typeExperiments = new String[] {"0.05", "0.10", "0.15", "0.20", "0.25", "0.30", "0.35", "0.40"};

        TimeStampFilterChecker timeStampFilterChecker = new TimeStampFilterChecker();

        UIPluginContext context = new FakePluginContext();
        ImportAcceptingPetriNetPlugin importAcceptingPetriNetPlugin = new ImportAcceptingPetriNetPlugin();
        Petrinet petrinet = ((AcceptingPetriNetImpl) importAcceptingPetriNetPlugin.importFile(context, "/Volumes/Data/Dropbox/LaTex/2017/Timestamp Repair/Logs/Experiments/TimeExperiments.pnml")).getNet();
        Marking initialMarking = MarkingDiscoverer.constructInitialMarking(context, petrinet);
        Marking finalMarking = MarkingDiscoverer.constructFinalMarking(context, petrinet);
        PetrinetWithMarking petrinetWithMarking = new PetrinetWithMarking(petrinet, initialMarking, finalMarking);

        for(String typeLog : typeLogs) {
            for(String typeExperiment : typeExperiments) {
                String n = "";
                for(String typeFilter : typeFilters) {
                    XLog filteredLog = LogImporter.importFromFile(new XFactoryNaiveImpl(), path + typeLog + "/" + typeLog + typeExperiment + typeFilter + logExtension);

                    AlignmentBasedFitness alignmentBasedFitness = new AlignmentBasedFitness();
                    Measure measure = alignmentBasedFitness.computeMeasurement(context, new XEventNameClassifier(), petrinetWithMarking, null, filteredLog);
                    System.out.println(typeLog + "/" + typeLog + typeExperiment + typeFilter +" " + measure.getValue());
                }
            }
        }
    }

    public static void main4(String[] args) throws Exception {
        String path = "/Volumes/Data/Dropbox/LaTex/2016/Timestamp Repair/Logs/Experiments/RealLife/";
        String logExtension = ".xes.gz";

        String[] typeLogs = new String[] {"BPI2014"};
        String[] typeFilters = new String[] {" ILP", "D", "R"};

        TimeStampFilterChecker timeStampFilterChecker = new TimeStampFilterChecker();

        XLog correctLog = LogImporter.importFromFile(new XFactoryNaiveImpl(), path + "BPI2014 (Incident Activity Sorted).xes.gz");
        for(String typeLog : typeLogs) {
            Set<String> done = new HashSet<>();
            for(String typeFilter : typeFilters) {
                XLog filteredLog = LogImporter.importFromFile(new XFactoryNaiveImpl(), path +  typeLog + typeFilter + logExtension);
                XLog noisyLog = LogImporter.importFromFile(new XFactoryNaiveImpl(), path + typeLog + logExtension);

                String base = "\\multirow{4}{*}{BPI2014}";

                if(!done.contains(base)) {
                    done.add(base);
                    String res = timeStampFilterChecker.check(noisyLog, noisyLog, correctLog).replaceAll("<html><p>", "").replaceAll("<br>", "\n").replaceAll("</p></html>", "");

                    FileWriter fileWriter = new FileWriter(path + typeLog + ".txt");
                    fileWriter.write(res);
                    fileWriter.close();

                    res = res.replaceAll("\nMin Levenshtein Distance : 0.0", "");
                    res = res.substring(res.indexOf("Levenshtein Distance"), res.indexOf("Total time error"));

                    String f = base + "} & " + "Affected";
                    String[] numbers = getNumbers(res);

                    System.out.println("\\hline");
                    System.out.println(f + " & " + numbers[4] + " & " + numbers[0] + " & " + numbers[1] + " & " + numbers[2] + " & " + numbers[3] + " & " + numbers[5] + " & " + numbers[6] + " & " + numbers[8] + " \\\\");
                    System.out.println("\\cline{2-10}");
                }

                String res = timeStampFilterChecker.check(filteredLog, noisyLog, correctLog).replaceAll("<html><p>", "").replaceAll("<br>", "\n").replaceAll("</p></html>", "");

                FileWriter fileWriter = new FileWriter(path + typeLog + typeFilter + ".txt");
                fileWriter.write(res);
                fileWriter.close();

                res = res.replaceAll("\nMin Levenshtein Distance : 0.0", "");
                res = res.substring(res.indexOf("Levenshtein Distance"), res.indexOf("Total time error"));

                String algo = "";
                if(typeFilter.equals(" ILP")) {
                    algo = "Our";
                }else if(typeFilter.equals("D")) {
                    algo = "Naive";
                }else {
                    algo = "Random";
                }

                String f = " & " + algo;
                String[] numbers = getNumbers(res);

                if(algo.equals("Our")) {
                    System.out.println(f + " & \\bf{" + numbers[4] + "} & \\bf{" + numbers[0] + "} & \\bf{" + numbers[1] + "} & \\bf{" + numbers[2] + "} & \\bf{" + numbers[3] + "} & \\bf{" + numbers[5] + "} & \\bf{" + numbers[6] + "} & \\bf{" + numbers[8] + "}\\\\");
                }else {
                    System.out.println(f + " & " + numbers[4] + " & " + numbers[0] + " & " + numbers[1] + " & " + numbers[2] + " & " + numbers[3] + " & " + numbers[5] + " & " + numbers[6] + " & " + numbers[8] + " \\\\");
                }
                if(algo.equals("Random")) {
                    System.out.println("\\hline");
                }else {
                    System.out.println("\\cline{2-10}");
                }
            }
        }
    }

    //Graph
    public static void main(String[] args) throws Exception {
        String path = "/Volumes/Data/Dropbox/LaTex/2017/Timestamp Repair/Logs/Experiments/";
        String logExtension = ".xes.gz";

        String[] typeLogs = new String[] {"Event", "Trace", "UniqueTrace"};
        String[] typeFilters = new String[] {"ILP", "N", "R"};

        String[] typeExperiments = new String[] {"0.05", "0.10", "0.15", "0.20", "0.25", "0.30", "0.35", "0.40"};

        TimeStampFilterChecker timeStampFilterChecker = new TimeStampFilterChecker();

        XLog correctLog = LogImporter.importFromFile(new XFactoryNaiveImpl(), path + "TimeExperimentSimulation.xes.gz");
        for(String typeLog : typeLogs) {
            Set<String> done = new HashSet<>();
            for(String typeExperiment : typeExperiments) {
                String[] values = new String[4];
                String n = "";
                for(String typeFilter : typeFilters) {
                    XLog filteredLog = LogImporter.importFromFile(new XFactoryNaiveImpl(), path + typeLog + "/" + typeLog + typeExperiment + typeFilter + logExtension);
                    XLog noisyLog = LogImporter.importFromFile(new XFactoryNaiveImpl(), path + typeLog + "/" + typeLog + typeExperiment + logExtension);

                    String base = "N$";
                    if(typeExperiment.equals("0.05")) {
                        n = "5";
                        base += "5";
                    }else {
                        n = typeExperiment.substring(2);
                        base += typeExperiment.substring(2);
                    }

                    if(typeLog.equals("Event")) {
                        base += "_e$";
                    }else if(typeLog.equals("Trace")) {
                        base += "_t$";
                    }else {
                        base += "_{\\textit{ut}}$";
                    }

                    if(!done.contains(base)) {
                        done.add(base);
                        String res = timeStampFilterChecker.check(noisyLog, noisyLog, correctLog).replaceAll("<html><p>", "").replaceAll("<br>", "\n").replaceAll("</p></html>", "");

                        FileWriter fileWriter = new FileWriter(path + typeLog + "/" + typeLog + typeExperiment + ".txt");
                        fileWriter.write(res);
                        fileWriter.close();

                        res = res.replaceAll("\nMin Levenshtein Distance : 0.0", "");
                        res = res.substring(res.indexOf("Levenshtein Distance"), res.indexOf("Total time error"));

                        String[] numbers = getNumbers(res);

                        values[0] = numbers[8];
                    }

                    String res = timeStampFilterChecker.check(filteredLog, noisyLog, correctLog).replaceAll("<html><p>", "").replaceAll("<br>", "\n").replaceAll("</p></html>", "");

                    FileWriter fileWriter = new FileWriter(path + typeLog + "/" + typeLog + typeExperiment + typeFilter + ".txt");
                    fileWriter.write(res);
                    fileWriter.close();

                    res = res.replaceAll("\nMin Levenshtein Distance : 0.0", "");
                    res = res.substring(res.indexOf("Levenshtein Distance"), res.indexOf("Total time error"));

                    String[] numbers = getNumbers(res);

                    if(typeFilter.equals("ILP")) {
                        values[3] = numbers[8];
                    }else if(typeFilter.equals("N")) {
                        values[2] = numbers[8];
                    }else {
                        values[1] = numbers[8];
                    }
                }

//                System.out.println(n + "\\%" + " " + values[0] + " " + values[1] + " " + values[2] + " " + values[3]);
                System.out.println(values[2]);
            }
        }
    }

    //Table
    public static void main2(String[] args) throws Exception {
        String path = "/Volumes/Data/Dropbox/LaTex/2017/Timestamp Repair/Logs/Experiments/";
        String logExtension = ".xes.gz";

        String[] typeLogs = new String[] {"Event", "Trace", "UniqueTrace"};
        String[] typeFilters = new String[] {"ILP", "N", "R"};

        String[] typeExperiments = new String[] {"0.05", "0.10", "0.15", "0.20", "0.25", "0.30", "0.35", "0.40"};

        TimeStampFilterChecker timeStampFilterChecker = new TimeStampFilterChecker();

        XLog correctLog = LogImporter.importFromFile(new XFactoryNaiveImpl(), path + "TimeExperimentSimulation.xes.gz");
        for(String typeLog : typeLogs) {
            Set<String> done = new HashSet<>();
            for(String typeExperiment : typeExperiments) {
                for(String typeFilter : typeFilters) {
                    XLog filteredLog = LogImporter.importFromFile(new XFactoryNaiveImpl(), path + typeLog + "/" + typeLog + typeExperiment + typeFilter + logExtension);
                    XLog noisyLog = LogImporter.importFromFile(new XFactoryNaiveImpl(), path + typeLog + "/" + typeLog + typeExperiment + logExtension);

                    String base = "\\multirow{4}{*}{N$";
                    if(typeExperiment.equals("0.05")) {
                        base += "5";
                    }else {
                        base += typeExperiment.substring(2);
                    }

                    if(typeLog.equals("Event")) {
                        base += "_e$";
                    }else if(typeLog.equals("Trace")) {
                        base += "_t$";
                    }else {
                        base += "_{\\textit{ut}}$";
                    }

                    if(!done.contains(base)) {
                        done.add(base);
                        String res = timeStampFilterChecker.check(noisyLog, noisyLog, correctLog).replaceAll("<html><p>", "").replaceAll("<br>", "\n").replaceAll("</p></html>", "");

                        FileWriter fileWriter = new FileWriter(path + typeLog + "/" + typeLog + typeExperiment + ".txt");
                        fileWriter.write(res);
                        fileWriter.close();

                        res = res.replaceAll("\nMin Levenshtein Distance : 0.0", "");
                        res = res.substring(res.indexOf("Levenshtein Distance"), res.indexOf("Total time error"));

                        String f = base + "} & " + "Affected";
                        String[] numbers = getNumbers(res);

                        System.out.println("\\hline");
                        System.out.println(f + " & " + numbers[4] + " & " + numbers[0] + " & " + numbers[1] + " & " + numbers[2] + " & " + numbers[3] + " & " + numbers[5] + " & " + numbers[6] + " & " + numbers[8] + " \\\\");
                        System.out.println("\\cline{2-10}");
                    }

                    String res = timeStampFilterChecker.check(filteredLog, noisyLog, correctLog).replaceAll("<html><p>", "").replaceAll("<br>", "\n").replaceAll("</p></html>", "");

                    FileWriter fileWriter = new FileWriter(path + typeLog + "/" + typeLog + typeExperiment + typeFilter + ".txt");
                    fileWriter.write(res);
                    fileWriter.close();

                    res = res.replaceAll("\nMin Levenshtein Distance : 0.0", "");
                    res = res.substring(res.indexOf("Levenshtein Distance"), res.indexOf("Total time error"));

                    String algo = "";
                    if(typeFilter.equals("ILP")) {
                        algo = "Our";
                    }else if(typeFilter.equals("N")) {
                        algo = "Naive";
                    }else {
                        algo = "Random";
                    }

                    String f = " & " + algo;
                    String[] numbers = getNumbers(res);

                    if(algo.equals("Our")) {
                        System.out.println(f + " & \\bf{" + numbers[4] + "} & \\bf{" + numbers[0] + "} & \\bf{" + numbers[1] + "} & \\bf{" + numbers[2] + "} & \\bf{" + numbers[3] + "} & \\bf{" + numbers[5] + "} & \\bf{" + numbers[6] + "} & \\bf{" + numbers[8] + "}\\\\");
                    }else {
                        System.out.println(f + " & " + numbers[4] + " & " + numbers[0] + " & " + numbers[1] + " & " + numbers[2] + " & " + numbers[3] + " & " + numbers[5] + " & " + numbers[6] + " & " + numbers[8] + " \\\\");
                    }
                    if(algo.equals("Random")) {
                        System.out.println("\\hline");
                    }else {
                        System.out.println("\\cline{2-10}");
                    }
                }
            }
        }
    }

    private static String[] getNumbers(String res) {
        StringTokenizer stringTokenizer = new StringTokenizer(res, " \n");
        String[] numbers = new String[9];
        int pos = 0;
        while (stringTokenizer.hasMoreElements()) {
            String s = stringTokenizer.nextToken();
            try {
                Double.parseDouble(s);
                numbers[pos] = s;
                pos++;
            }catch (NumberFormatException nfe) {

            }
        }
        return numbers;
    }

    public String check(XLog filteredLog, XLog noisyLog, XLog correctLog) {
        int numberOfTracesAffected = 0;
        int numberOfEventsAffected = 0;

        int eventErrorIntroduced = 0;
        int traceErrorIntroduced = 0;

        int numberOfTracesAttempted = 0;

        int numberOfTracesCorrectlyChanged = 0;
        int numberOfEventsCorrectlyChanged = 0;

        int numberOfTracesWronglyChanged = 0;
        int numberOfEventsWronglyChanged = 0;

        Map<Integer, Integer> mapGapsPerTrace = new UnifiedMap<>();
        XEventClassifier xEventClassifier = new XEventAndClassifier(new XEventNameClassifier());
        TimeStampChecker timeStampChecker = new TimeStampChecker(xEventClassifier, new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"));

        List<Long> errors = new ArrayList<>();

        for(XTrace trace : noisyLog) {
            if (trace.getAttributes().get("change") != null) {
                numberOfTracesAffected++;
                for(XEvent event : trace) {
                    if (event.getAttributes().get("change") != null) {
                        numberOfEventsAffected++;
                    }
                }
            }
        }

        for(XTrace trace1 : filteredLog) {
            String traceID = xce.extractName(trace1);
            boolean matches = true;
            if (trace1.getAttributes().get("change") != null || numberOfTracesAffected == 0) {
                if (trace1.getAttributes().get("fixed") != null) {
                    numberOfTracesAttempted++;
                }

                for (XTrace trace2 : correctLog) {
                    if (xce.extractName(trace2).equals(traceID)) {

                        for (int i = 0; i < trace1.size(); i++) {
                            if (trace1.get(i).getAttributes().get("change") != null || numberOfTracesAffected == 0) {

                                if (!xce.extractName(trace1.get(i)).equals(xce.extractName(trace2.get(i)))) {
                                    matches = false;
                                    numberOfEventsWronglyChanged++;
                                }else {
                                    numberOfEventsCorrectlyChanged++;
                                }

                                if(trace1.get(i).getAttributes().get("originalTimeStamp") != null) {
                                    Date originalTimeStamp = ((XAttributeTimestamp) trace1.get(i).getAttributes().get("originalTimeStamp")).getValue();
                                    Date timestamp = xte.extractTimestamp(trace1.get(i));
                                    errors.add(timestamp.getTime() - originalTimeStamp.getTime());
                                }
                            }
                        }

                        if (matches) {
                            numberOfTracesCorrectlyChanged++;
                        } else {
                            numberOfTracesWronglyChanged++;

                            for (XTrace trace : noisyLog) {
                                if (xce.extractName(trace).equals(traceID)) {
                                    if (timeStampChecker.containsSameTimestamps(trace)) {
                                        Set<Set<XEvent>> sets = timeStampChecker.findEventsSameTimeStamp(trace);

                                        Integer count;
                                        for (Set<XEvent> set : sets) {
                                            if ((count = mapGapsPerTrace.get(set.size())) == null) {
                                                count = 0;
                                            }
                                            count++;
                                            mapGapsPerTrace.put(set.size(), count);
                                        }
                                    }
                                    break;
                                }
                            }
                        }

                        break;
                    }
                }
            }else if (trace1.getAttributes().get("fixed") != null) {
                for (XTrace trace2 : correctLog) {
                    if (xce.extractName(trace2).equals(traceID)) {
                        for (int i = 0; i < trace1.size(); i++) {
                            if (!xce.extractName(trace1.get(i)).equals(xce.extractName(trace2.get(i))) && !xte.extractTimestamp(trace1.get(i)).equals(xte.extractTimestamp(trace2.get(i)))) {
                                matches = false;
                                eventErrorIntroduced++;
                            }
                        }
                        if (!matches) {
                            traceErrorIntroduced++;
                        }
                        break;
                    }
                }
            }
        }

        NameExtractor nameExtractor = new NameExtractor(new XEventNameClassifier());
        double[] partialDistances = new double[filteredLog.size()];
        int totalDistance = 0;
        int counter = 0;
        int pos = 0;
        Map<String, String> nameMapper = new UnifiedMap<>();
        for(XTrace trace1 : filteredLog) {
            String traceID = xce.extractName(trace1);
            for (XTrace trace2 : correctLog) {
                if (xce.extractName(trace2).equals(traceID)) {
                    for(XEvent event : trace1) {
                        if(!nameMapper.containsKey(event)) {
                            nameMapper.put(nameExtractor.getEventName(event), "" + counter);
                            counter++;
                        }
                    }
                    String[] string1 = TraceToString.convertXTraceToListOfString(trace1, nameMapper, nameExtractor);
                    String[] string2 = TraceToString.convertXTraceToListOfString(trace2, nameMapper, nameExtractor);
                    partialDistances[pos] = getLevenshteinDistanceLinearSpace(string1, string2);
                    totalDistance += partialDistances[pos];
                    pos++;
                }
            }
        }

        double percentageOfTracesAttempted = (double) numberOfTracesAttempted/ (double) numberOfTracesAffected;

        double percentageOfTracesCorrectlyChanged = (double) numberOfTracesCorrectlyChanged/ (double) numberOfTracesAffected;
        double percentageOfTracesWronglyChanged = (double) numberOfTracesWronglyChanged/ (double) numberOfTracesAffected;

        double percentageOfEventsCorrectlyChanged = (double) numberOfEventsCorrectlyChanged/ (double) numberOfEventsAffected;
        double percentageOfEventsWronglyChanged = (double) numberOfEventsWronglyChanged/ (double) numberOfEventsAffected;


        double mse = 0;
        double totalError = 0;
        for(Long error : errors) {
            mse += Math.pow(error, 2);
            totalError += Math.abs(error);
        }
        mse = mse / errors.size();
        double rmse = Math.sqrt(mse);
        double averageError = totalError / errors.size();

        double totalSquareDifferenced = 0;
        for(Long error : errors) {
            totalSquareDifferenced += Math.pow(averageError - error, 2);
        }
        double stdDeviation = Math.sqrt(totalSquareDifferenced/ errors.size());

        StatisticsSelector statisticsSelector = new StatisticsSelector();

        double ave = 0;
        double sd = 0;
        int size = 0;
        for(double d : partialDistances) {
            if(d > 0) {
                ave += d;
                size++;
            }
        }
        ave = ave / size;

        for(double d : partialDistances) {
            if(d > 0) {
                sd += Math.pow(d - ave, 2);
            }
        }
        sd = Math.sqrt(sd / size);

        String s =  "<html><p>Number of Noisy Traces: " + numberOfTracesAffected +
                    "<br>Number of Attempted Noisy Traces: " + numberOfTracesAttempted + //" (" + format(percentageOfTracesAttempted) + "%) " +
                    "<br>Number of Fixed Noisy Traces: " + numberOfTracesCorrectlyChanged + //" (" + format(percentageOfTracesCorrectlyChanged) + "%) " +
                    "<br>Number of Not Fixed Noisy Traces: " + numberOfTracesWronglyChanged + //" (" + format(percentageOfTracesWronglyChanged) + "%) " +
                    "<br>Number of Wrongly Fixed Traces: " + traceErrorIntroduced+ //" (" + format(percentageOfTracesWronglyChanged) + "%) " +

                    "<br>Number of Noisy Events: " + numberOfEventsAffected +
                    "<br>Number of Fixed Noisy Events: " + numberOfEventsCorrectlyChanged + //" (" + format(percentageOfEventsCorrectlyChanged) + "%) " +
                    "<br>Number of Not Fixed Noisy Events: " + numberOfEventsWronglyChanged + //" (" + format(percentageOfEventsWronglyChanged) + "%) " +
                    "<br>Number of Wrongly Fixed Events: " + eventErrorIntroduced +
                    "<br>Levenshtein Distance : " + totalDistance +
                    "<br>Min Levenshtein Distance : " + statisticsSelector.evaluate(StatisticsSelector.StatisticsMeasures.MIN, null, partialDistances) +
                    "<br>Max Levenshtein Distance : " + truncate(statisticsSelector.evaluate(StatisticsSelector.StatisticsMeasures.MAX, null, partialDistances)) +
                    "<br>Average Levenshtein Distance : " + truncate(statisticsSelector.evaluate(StatisticsSelector.StatisticsMeasures.MEAN, null, partialDistances)) +
                    "<br>Sd Levenshtein Distance : " + truncate(statisticsSelector.evaluate(StatisticsSelector.StatisticsMeasures.SD, null, partialDistances)) +
                    "<br>Exclusive : " + size +
                    "<br>Average Exclusive Levenshtein Distance : " + truncate(ave) +
                    "<br>Sd Exclusive Levenshtein Distance : " + truncate(sd) +

                    "<br>MSE: " + format2(mse / (1000 * 60 * 60 * 24)) +
                    "<br>RMSE: " + format2(rmse / (1000 * 60 * 60 * 24)) +
                    "<br>Total time error: " + format(totalError) +
                    "<br>Average time error: " + format(averageError) +
                    "<br>Std Deviation time error: " + format(stdDeviation);

        s += "<br>GapSize and Frequencies:";
        for(Map.Entry<Integer, Integer> entry : mapGapsPerTrace.entrySet()) {
            s += "<br>" + entry.getKey() + " : " + entry.getValue();
        }

        s += "</p></html>";

        return s;
    }

    private String truncate(double value) {
        DecimalFormat df = new DecimalFormat("#0.000");
        String s = df.format(value);
        if(value > 10 && s.endsWith(".000")) {
            return s.substring(0, s.length() - 4);
        }
        return s;
    }

    private String format2(double value) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(value);
    }

    private String format(double value) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(value * 100);
    }

    public int getLevenshteinDistanceLinearSpace(String[] seq1, String[] seq2) {
        int lengthSeq1 = seq1.length;
        int lengthSeq2 = seq2.length;
        int[] S = new int[lengthSeq2 + 1];
        S[0] = 0;
        S[1] = S[0] + 1;

        int s;
        for(s = 2; s <= lengthSeq2; ++s) {
            S[s] = S[s - 1] + 1;
        }

        for(int i = 1; i <= lengthSeq1; ++i) {
            s = S[0];
            int c;
            S[0] = c = S[0] + 1;
            String[] sI = Arrays.copyOfRange(seq1, (i - 1), i);

            for(int j = 1; j <= lengthSeq2; ++j) {
                String[] tJ = Arrays.copyOfRange(seq2, (j - 1), j);
                byte cost = 0;
                if(!Arrays.equals(sI, tJ)) {
                    cost = 1;
                }

                c = this.Minimum(S[j] + 1, s + cost, c + 1);
                s = S[j];
                S[j] = c;
            }
        }

        return S[lengthSeq2];
    }

    private int Minimum(int a, int b, int c) {
        int mi = a;
        if(b < a) {
            mi = b;
        }

        if(c < mi) {
            mi = c;
        }

        return mi;
    }


}
