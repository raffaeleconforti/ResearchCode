package com.raffaeleconforti.noisefiltering.label.logic;

import com.raffaeleconforti.benchmark.logic.MeasurementAlgorithmDiscoverer;
import com.raffaeleconforti.benchmark.logic.MiningAlgorithmDiscoverer;
import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.log.util.LogAnalyser;
import com.raffaeleconforti.log.util.LogCloner;
import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.MeasurementAlgorithm;
import com.raffaeleconforti.measurements.impl.AlignmentBasedFMeasure;
import com.raffaeleconforti.measurements.impl.ProjectedFMeasure;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import com.raffaeleconforti.wrapper.impl.SplitMinerWrapper;
import com.raffaeleconforti.wrapper.impl.heuristics.HeuristicsAlgorithmWrapper;
import com.raffaeleconforti.wrapper.impl.inductive.InductiveMinerIMfWrapper;
import com.raffaeleconforti.wrapper.settings.MiningSettings;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.model.XLog;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.acceptingpetrinet.plugins.ExportAcceptingPetriNetPlugin;
import org.processmining.plugins.InductiveMiner.plugins.IMProcessTree;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

import static com.raffaeleconforti.log.util.LogImporter.importFromFile;
import static com.raffaeleconforti.log.util.LogImporter.importFromInputStream;
import static org.processmining.models.causalnet.CausalNetAnnotations.parameters;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 18/10/2016.
 */
public class Experiment {
    private String extLocation;
    private Map<String, XLog> logs;
    private Map<String, Object> logsInput;
    private XLog refLog;
    private String refLogName;

    private Set<String> packages = new UnifiedSet<>();

    /* this is a multidimensional cube containing all the measures.
    for each log, each mining algorithm and each metric we have a resulting metric value */
    private HashMap<String, HashMap<String, HashMap<String, String>>> measures;

    public static void main(String[] args) throws Exception {
        Experiment experiment = new Experiment();
        experiment.performBenchmark();
    }

    public Experiment() throws Exception {
        this.extLocation = "/Volumes/Data/SharedFolder/Logs/Label";
    }

    public void performBenchmark() throws Exception {
        System.out.println("DEBUG - running benchmark ...");
        loadLogs();
        performBenchmarkFromLogInput(packages, logsInput);
    }

    private void performBenchmarkFromLogInput(Set<String> packages, Map<String, Object> logsInput) throws Exception {
        XEventClassifier xEventClassifier = new XEventAndClassifier(new XEventNameClassifier());
        FakePluginContext fakePluginContext = new FakePluginContext();

        /* retrieving all the mining algorithms */
        List<MiningAlgorithm> miningAlgorithms = MiningAlgorithmDiscoverer.discoverAlgorithms(packages);
        Collections.sort(miningAlgorithms, new Comparator<MiningAlgorithm>() {
            @Override
            public int compare(MiningAlgorithm o1, MiningAlgorithm o2) {
                return o1.getAlgorithmName().compareTo(o2.getAlgorithmName());
            }
        });

        /* retrieving all the measuring algorithms */
        List<MeasurementAlgorithm> measurementAlgorithms = MeasurementAlgorithmDiscoverer.discoverAlgorithms(packages);
        Collections.sort(measurementAlgorithms, new Comparator<MeasurementAlgorithm>() {
            @Override
            public int compare(MeasurementAlgorithm o1, MeasurementAlgorithm o2) {
                return o2.getMeasurementName().compareTo(o1.getMeasurementName());
            }
        });

        measures = new HashMap<>();

        System.out.println("DEBUG - total logs: " + logsInput.keySet().size());


        /* populating measurements results */
        XLog log;
        LogCloner logCloner = new LogCloner(new XFactoryNaiveImpl());
        String[] lognames = logsInput.keySet().toArray(new String[logsInput.size()]);
        Arrays.sort(lognames);
        for(int i = lognames.length - 1; i >= 0; i--) {
//        for(int i = 0; i < lognames.length; i++) {
            String logName = lognames[i];
            if(logName.endsWith(LabelFilter.testName + ".xes.gz")) continue;
//            if(!logName.endsWith("BPI2011 (Label CoV CS).xes.gz")) continue;
//            if(logName.contains("Road")) continue;
//            if(logName.contains("2015-5")) continue;
//            if(logName.contains("2015-4")) continue;
//            if(logName.contains("2015-3")) continue;
//            if(logName.contains("2015-2")) continue;
//            if(logName.contains("2015-1")) continue;
//            if(logName.contains("2014")) continue;
//            if(logName.contains("2012")) continue;
//            if(logName.contains("2011")) continue;
            XLog rawlog = loadLog(logsInput.get(logName));
//            rawlog.getClassifiers().clear();
//            LogImporter.exportToFile(logName, rawlog);

            String original = null;
            if(logName.contains(" ")) original = logName.substring(0, logName.indexOf(" ")) + ".xes.gz";
            else original = logName;

            String newRefLogName = extLocation + "/" + original;
            if(refLogName == null || !refLogName.equals(newRefLogName)) {
                this.refLog = LogImporter.importFromFile(new XFactoryNaiveImpl(), extLocation + "/" + original);
            }

            measures.put(logName, new HashMap<>());
            int labels = LogAnalyser.getUniqueActivities(rawlog, xEventClassifier).size();
            System.out.println("DEBUG - measuring on log: " + logName + " " + labels);

            for (MiningAlgorithm miningAlgorithm : miningAlgorithms) {
                if(miningAlgorithm instanceof InductiveMinerIMfWrapper) {
//                if(miningAlgorithm instanceof HeuristicsAlgorithmWrapper) {
//                if(miningAlgorithm instanceof SplitMinerWrapper) {

                    log = logCloner.cloneLog(rawlog);
                    String miningAlgorithmName = miningAlgorithm.getAlgorithmName();

                    String measurementAlgorithmName = "NULL";
                    measures.get(logName).put(miningAlgorithmName, new HashMap<>());

                    try {
                        System.out.println("DEBUG - measuring on mining algorithm: " + miningAlgorithmName);
                        long sTime = System.currentTimeMillis();

                        MiningSettings miningSettings = new MiningSettings();
                        miningSettings.setParam("noiseThresholdIMf", 0F);

                        PetrinetWithMarking petrinetWithMarking = null;
                        ProcessTree processTree = null;
                        if(miningAlgorithm.canMineProcessTree()) {
                            processTree = miningAlgorithm.mineProcessTree(fakePluginContext, log, false, miningSettings);
//                            ProcessTree2Petrinet.PetrinetWithMarkings pn = null;
//
//                            try {
//                                pn = ProcessTree2Petrinet.convert(processTree);
//                            } catch (ProcessTree2Petrinet.NotYetImplementedException var6) {
//                                var6.printStackTrace();
//                            } catch (ProcessTree2Petrinet.InvalidProcessTreeException var7) {
//                                var7.printStackTrace();
//                            }
//
//                            petrinetWithMarking = new PetrinetWithMarking(pn.petrinet, pn.initialMarking, pn.finalMarking);
                        } else{
                            petrinetWithMarking = miningAlgorithm.minePetrinet(fakePluginContext, log, false, miningSettings);
                        }

//                        ExportAcceptingPetriNetPlugin exportAcceptingPetriNetPlugin = new ExportAcceptingPetriNetPlugin();
//                        exportAcceptingPetriNetPlugin.export(
//                                fakePluginContext,
//                                new AcceptingPetriNetImpl(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking()),
//                                new File(extLocation + "/" + logName + "-" + miningAlgorithmName + ".pnml"));

                        long execTime = System.currentTimeMillis() - sTime;
                        measures.get(logName).get(miningAlgorithmName).put("ExecTime", Long.toString(execTime));

                        for (MeasurementAlgorithm measurementAlgorithm : measurementAlgorithms) {
                            if (measurementAlgorithm instanceof ProjectedFMeasure) {
//                            if (measurementAlgorithm instanceof AlignmentBasedFMeasure) {
                                measurementAlgorithmName = measurementAlgorithm.getMeasurementName();
                                System.out.println("DEBUG - measuring: " + measurementAlgorithmName);

                                Measure measure;
                                if(miningAlgorithm.canMineProcessTree()) {
                                    measure = measurementAlgorithm.computeMeasurement(fakePluginContext, xEventClassifier,
                                            processTree, miningAlgorithm, refLog);
                                }else {
                                    measure = measurementAlgorithm.computeMeasurement(fakePluginContext, xEventClassifier,
                                            petrinetWithMarking, miningAlgorithm, refLog);
                                }

                                String fmeasure = measure.getMetricValue("Projected FMeasure");
                                String fitness = measure.getMetricValue("Projected Fitness");
                                String precision = measure.getMetricValue("Projected Precision");

                                measures.get(logName).get(miningAlgorithmName).put("Projected FMeasure", fmeasure);
                                measures.get(logName).get(miningAlgorithmName).put("Projected Fitness", fitness);
                                measures.get(logName).get(miningAlgorithmName).put("Projected Precision", precision);

                                System.out.println("DEBUG - " + measurementAlgorithmName + measure);
                            }
                        }

                    } catch (Exception e) {
                        System.out.println("ERROR - [mining algorithm : measurement algorithm] > [" + miningAlgorithmName + " : " + measurementAlgorithmName + "]");
                        e.printStackTrace();
                        measures.get(logName).remove(miningAlgorithmName);
                    }
                }
            }
        }
    }

    private void loadLogs() {
        logsInput = new UnifiedMap<>();
        String logName;
        InputStream in;

        try {
            /* checking if the user wants to upload also external logs */
            if( extLocation != null ) {
                System.out.println("DEBUG - importing external logs.");
                File folder = new File(extLocation);
                File[] listOfFiles = folder.listFiles();
                if( folder.isDirectory() ) {
                    for( File file : listOfFiles )
                        if( file.isFile() ) {
                            logName = file.getPath();
                            System.out.println("DEBUG - name: " + logName);
                            logsInput.put(file.getName(), logName);
                        }
                } else {
                    System.out.println("ERROR - external logs loading failed, input path is not a folder.");
                }
            }
        } catch( Exception e ) {
            System.out.println("ERROR - something went wrong reading the resource folder: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private XLog loadLog(Object o) {
        try {
            if(o instanceof String) {
                return LogImporter.importFromFile(new XFactoryNaiveImpl(), (String) o);
            }else if(o instanceof InputStream){
                return LogImporter.importFromInputStream((InputStream) o, new XesXmlGZIPParser(new XFactoryNaiveImpl()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void publishResults() {
        System.out.println("DEBUG - starting generation of the excel file.");
        try {
            String filename = "./benchmark_result_" + Long.toString(System.currentTimeMillis()) + ".xls" ;
            HSSFWorkbook workbook = new HSSFWorkbook();
            int rowCounter;
            int cellCounter;
            boolean generateHead = true;
            HSSFSheet sheet = workbook.createSheet("Results");

            /* generating one sheet for each log */
            for( String logName : measures.keySet() ) {
                rowCounter = 0;

                HSSFRow rowhead = sheet.createRow((short) rowCounter);
                rowCounter++;

                for( String miningAlgorithmName : measures.get(logName).keySet() ) {
                    /* creating the row for this mining algorithm */
                    HSSFRow row = sheet.createRow((short) rowCounter);
                    rowCounter++;

                    cellCounter = 0;
                    if( generateHead ) rowhead.createCell(cellCounter).setCellValue("Log");
                    row.createCell(cellCounter).setCellValue(logName);
                    cellCounter++;

                    if( generateHead ) rowhead.createCell(cellCounter).setCellValue("Mining Algorithm");
                    row.createCell(cellCounter).setCellValue(miningAlgorithmName);
                    cellCounter++;

                    for( String metricName : measures.get(logName).get(miningAlgorithmName).keySet() ) {
                        if( generateHead ) rowhead.createCell(cellCounter).setCellValue(metricName);
                        row.createCell(cellCounter).setCellValue(measures.get(logName).get(miningAlgorithmName).get(metricName));
                        cellCounter++;
                    }
                    generateHead = false;
                }
            }

            FileOutputStream fileOut = new FileOutputStream(filename);
            workbook.write(fileOut);
            fileOut.close();
            System.out.println("DEBUG - generation of the excel sheet completed.");
        } catch ( Exception e ) {
            System.out.println("ERROR - something went wrong while writing the excel sheet: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
