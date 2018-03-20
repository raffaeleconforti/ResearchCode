package com.raffaeleconforti.noisefiltering.timestamp;

import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.marking.MarkingDiscoverer;
import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.impl.AlignmentBasedFitness;
import com.raffaeleconforti.noisefiltering.timestamp.permutation.PermutationTechnique;
import com.raffaeleconforti.wrappers.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.acceptingpetrinet.plugins.ImportAcceptingPetriNetPlugin;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 20/4/17.
 */
public class TimestampFixerExperiments {

    private static boolean useGurobi = true;
    private static boolean debug_mode = false;
    private static boolean self_cleaning = false;

    public static void main(String[] args) throws Exception {
//        generateArtificialLogs();
        generateRealLifeLogs();
//        generateArtificialLogTable();
    }

    public static void generateArtificialLogTable() throws Exception {
        String path = "/Volumes/Data/Dropbox/LaTex/2018/Timestamp Repair/Logs/Experiments copy/";
        String logExtension = ".xes.gz";

        String[] typeLogs = new String[] {"Event", "Trace", "UniqueTrace"};
        String[] typeFilters = new String[] {"ILP", "N", "R"};

        String[] typeExperiments = new String[] {"0.05", "0.10", "0.15", "0.20", "0.25", "0.30", "0.35", "0.40"};

        TimeStampFilterChecker timeStampFilterChecker = new TimeStampFilterChecker();

        UIPluginContext context = new FakePluginContext();
        ImportAcceptingPetriNetPlugin importAcceptingPetriNetPlugin = new ImportAcceptingPetriNetPlugin();
        Petrinet petrinet = ((AcceptingPetriNetImpl) importAcceptingPetriNetPlugin.importFile(context, "/Volumes/Data/Dropbox/LaTex/2018/Timestamp Repair/Logs/Experiments/TimeExperiments.pnml")).getNet();
        Marking initialMarking = MarkingDiscoverer.constructInitialMarking(context, petrinet);
        Marking finalMarking = MarkingDiscoverer.constructFinalMarking(context, petrinet);
        PetrinetWithMarking petrinetWithMarking = new PetrinetWithMarking(petrinet, initialMarking, finalMarking);

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

                        AlignmentBasedFitness alignmentBasedFitness = new AlignmentBasedFitness();
                        Measure measure = alignmentBasedFitness.computeMeasurement(context, new XEventNameClassifier(), petrinetWithMarking, null, noisyLog);

                        System.out.println("\\hline");
                        System.out.println(f + " & " + numbers[4] + " & " + numbers[0] + " & " + numbers[1] + " & " + numbers[2] + " & " + numbers[3] + " & " + numbers[5] + " & " + numbers[6] + " & " + numbers[8] + " & " + new BigDecimal(String.valueOf(measure.getValue())).setScale(3, BigDecimal.ROUND_HALF_EVEN).toString() + "\\\\");
                        System.out.println("\\cline{2-11}");
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

                    AlignmentBasedFitness alignmentBasedFitness = new AlignmentBasedFitness();
                    Measure measure = alignmentBasedFitness.computeMeasurement(context, new XEventNameClassifier(), petrinetWithMarking, null, filteredLog);

                    if(algo.equals("Our")) {
                        System.out.println(f + " & \\bf{" + numbers[4] + "} & \\bf{" + numbers[0] + "} & \\bf{" + numbers[1] + "} & \\bf{" + numbers[2] + "} & \\bf{" + numbers[3] + "} & \\bf{" + numbers[5] + "} & \\bf{" + numbers[6] + "} & \\bf{" + numbers[8] + "} & \\bf{" + new BigDecimal(String.valueOf(measure.getValue())).setScale(3, BigDecimal.ROUND_HALF_EVEN).toString() + "}\\\\");
                    }else {
                        System.out.println(f + " & " + numbers[4] + " & " + numbers[0] + " & " + numbers[1] + " & " + numbers[2] + " & " + numbers[3] + " & " + numbers[5] + " & " + numbers[6] + " & " + numbers[8] + " & " + new BigDecimal(String.valueOf(measure.getValue())).setScale(3, BigDecimal.ROUND_HALF_EVEN).toString() + " \\\\");
                    }
                    if(algo.equals("Random")) {
                        System.out.println("\\hline");
                    }else {
                        System.out.println("\\cline{2-11}");
                    }
                }
            }
        }
    }

    public static void checkModelWithFilteredLogs() throws Exception {
        String path = "/Volumes/Data/Dropbox/LaTex/2018/Timestamp Repair/Logs/Experiments/";
        String logExtension = ".xes.gz";

        String[] typeLogs = new String[] {"Event", "Trace", "UniqueTrace"};
        String[] typeFilters = new String[] {"", "ILP", "N", "R"};

        String[] typeExperiments = new String[] {"0.05", "0.10", "0.15", "0.20", "0.25", "0.30", "0.35", "0.40"};

        UIPluginContext context = new FakePluginContext();
        ImportAcceptingPetriNetPlugin importAcceptingPetriNetPlugin = new ImportAcceptingPetriNetPlugin();
        Petrinet petrinet = ((AcceptingPetriNetImpl) importAcceptingPetriNetPlugin.importFile(context, "/Volumes/Data/Dropbox/LaTex/2018/Timestamp Repair/Logs/Experiments/TimeExperiments.pnml")).getNet();
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
                    System.out.println(typeLog + "/" + typeLog + typeExperiment + typeFilter + " " + measure.getValue());
                }
            }
        }
    }

    public static void generateRealLifeLogs() throws Exception {
        String path = "/Volumes/Data/Dropbox/LaTex/2018/Timestamp Repair/Logs/Experiments/";
        String logExtension = ".xes.gz";

        String[] typeLogs = new String[] {"RealLife"};
        String[] typeFilters = new String[] {"ILP"};//, "N", "R"};

        String[] typeExperiments = new String[] {"BPI2014", "Hitachi"};

        for(String typeLog : typeLogs) {
            for(String typeExperiment : typeExperiments) {
                XLog log = LogImporter.importFromFile(new XFactoryNaiveImpl(), path + typeLog + "/" + typeExperiment + logExtension);
                for(String typeFilter : typeFilters) {
                    XLog filteredLog = null;
                    if(typeFilter.equals("ILP")) {
                        System.out.println("ILP " + typeLog + " " + typeExperiment);
                        filteredLog = logGenerationSmart(log, useGurobi);
                    }else if(typeFilter.equals("N")) {
                        filteredLog = logGenerationNaive(log, useGurobi);
                    }else {
                        filteredLog = logGenerationRandom(log, useGurobi);
                    }
                    LogImporter.exportToFile(path + typeLog + "/" + typeExperiment + typeFilter + logExtension, filteredLog);
                }
            }
        }
    }

    public static void generateArtificialLogs() throws Exception {
        String path = "/Volumes/Data/Dropbox/LaTex/2018/Timestamp Repair/Logs/Experiments/";
        String logExtension = ".xes.gz";

        String[] typeLogs = new String[] {"Event", "Trace", "UniqueTrace"};
//        String[] typeFilters = new String[] {"ILP", "N", "R"};
        String[] typeFilters = new String[] {"ILP"};

        String[] typeExperiments = new String[] {"0.05", "0.10", "0.15", "0.20", "0.25", "0.30", "0.35", "0.40"};

        for(String typeLog : typeLogs) {
            for(String typeExperiment : typeExperiments) {
                XLog log = LogImporter.importFromFile(new XFactoryNaiveImpl(), path + typeLog + "/" + typeLog + typeExperiment + logExtension);
                for(String typeFilter : typeFilters) {
                    XLog filteredLog = null;
                    if(typeFilter.equals("ILP")) {
                        System.out.println("ILP " + typeLog + " " + typeExperiment);
                        filteredLog = logGenerationSmart(log, useGurobi);
                    }else if(typeFilter.equals("N")) {
                        filteredLog = logGenerationNaive(log, useGurobi);
                    }else {
                        filteredLog = logGenerationRandom(log, useGurobi);
                    }
                    LogImporter.exportToFile(path + typeLog + "/" + typeLog + typeExperiment + typeFilter + logExtension, filteredLog);
                }
            }
        }
    }

    public static XLog logGenerationNaive(XLog log, boolean useGurobi) {
        TimeStampFixerDummyExecutor timeStampFixerDummyExecutor = new TimeStampFixerDummyExecutor(useGurobi, false);
        XLog filtered1 = timeStampFixerDummyExecutor.filterLog(log);
        return filtered1;
    }

    public static XLog logGenerationRandom(XLog log, boolean useGurobi) {
        TimeStampFixerRandomExecutor timeStampFixerRandomExecutor = new TimeStampFixerRandomExecutor(useGurobi, false);
        XLog filtered1 = timeStampFixerRandomExecutor.filterLog(log);
        return filtered1;
    }

    public static XLog logGenerationSmart(XLog log, boolean useGurobi) {
        TimeStampFixerSmartExecutor timeStampFixerSmartExecutor = new TimeStampFixerSmartExecutor(useGurobi, false, false);
        XLog filtered1 = null;
        if(useGurobi) filtered1 = timeStampFixerSmartExecutor.filterLog(log, 11, PermutationTechnique.ILP_GUROBI, debug_mode, self_cleaning);
        else filtered1 = timeStampFixerSmartExecutor.filterLog(log, 11, PermutationTechnique.ILP_LPSOLVE, debug_mode, self_cleaning);
        return filtered1;
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

}
