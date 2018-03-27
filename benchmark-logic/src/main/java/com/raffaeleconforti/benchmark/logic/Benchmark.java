package com.raffaeleconforti.benchmark.logic;

import au.edu.qut.processmining.log.LogParser;
import au.edu.qut.processmining.log.SimpleLog;
import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.log.util.LogCloner;
import com.raffaeleconforti.marking.MarkingDiscoverer;
import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.MeasurementAlgorithm;
import com.raffaeleconforti.measurements.impl.*;
import com.raffaeleconforti.wrappers.MiningAlgorithm;
import com.raffaeleconforti.wrappers.PetrinetWithMarking;
import com.raffaeleconforti.wrappers.StructuredMinerAlgorithmWrapperHM52;
import com.raffaeleconforti.wrappers.impl.heuristics.Heuristics52AlgorithmWrapper;
import com.raffaeleconforti.wrappers.impl.inductive.InductiveMinerIMfWrapper;
import hub.top.petrinet.PetriNet;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XesXmlSerializer;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.acceptingpetrinet.plugins.ExportAcceptingPetriNetPlugin;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.bpmn.plugins.BpmnExportPlugin;
import org.processmining.plugins.pnml.importing.PnmlImportNet;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.raffaeleconforti.log.util.LogImporter.importFromFile;
import static com.raffaeleconforti.log.util.LogImporter.importFromInputStream;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 18/10/2016.
 */
public class Benchmark {

    private static final long MAX_TIME = 3600000;

    private boolean defaultLogs;
    private String extLocation;
    private Map<String, Object> inputLogs;
    private LogCloner logCloner;
    private static XEventClassifier xEventClassifier = new XEventNameClassifier();

    private Set<String> packages = new UnifiedSet<>();

    /* this is a multidimensional cube containing all the measures.
    for each log, each mining algorithm and each metric we have a resulting metric value */
    private HashMap<String, HashMap<String, HashMap<String, String>>> measures;

    private Benchmark() {}

    public Benchmark(boolean defaultLogs, String extLocation, Set<String> packages) {
        this.defaultLogs = defaultLogs;
        this.extLocation = extLocation;
        this.packages = packages;
        loadLogs(extLocation);
    }

    public void performBenchmark(ArrayList<Integer> selectedMiners, ArrayList<Integer> selectedMetrics) {

        hub.top.petrinet.PetriNet petriNet = new PetriNet();
        petriNet.getPlaces();

        FakePluginContext fakePluginContext = new FakePluginContext();

        /* retrieving all the mining algorithms */
        List<MiningAlgorithm> miningAlgorithms = MiningAlgorithmDiscoverer.discoverAlgorithms(packages);
        Collections.sort(miningAlgorithms, new Comparator<MiningAlgorithm>() {
            @Override
            public int compare(MiningAlgorithm o1, MiningAlgorithm o2) {
                return o2.getAlgorithmName().compareTo(o1.getAlgorithmName());
            }
        });

        // pruning the list of miners
        if( selectedMiners != null && !selectedMiners.isEmpty() ) {
//            System.out.println("DEBUG - pruning miners");
            Collections.sort(selectedMiners);
            Collections.reverse(selectedMiners);
            for(int i = miningAlgorithms.size()-1; i >= 0; i--) {
                if( selectedMiners.isEmpty() || (i != selectedMiners.get(0)) ) miningAlgorithms.remove(i);
                else selectedMiners.remove(0);
            }
        }
        System.out.println("DEBUG - total miners: " + miningAlgorithms.size());

        /* retrieving all the measuring algorithms */
        List<MeasurementAlgorithm> measurementAlgorithms = MeasurementAlgorithmDiscoverer.discoverAlgorithms(packages);
        Collections.sort(measurementAlgorithms, new Comparator<MeasurementAlgorithm>() {
            @Override
            public int compare(MeasurementAlgorithm o1, MeasurementAlgorithm o2) {
                return o2.getMeasurementName().compareTo(o1.getMeasurementName());
            }
        });

        // pruning the list of metrics
        if( selectedMetrics != null && !selectedMetrics.isEmpty() ) {
//            System.out.println("DEBUG - pruning metrics");
            Collections.sort(selectedMetrics);
            Collections.reverse(selectedMetrics);
            for(int i = measurementAlgorithms.size()-1; i >= 0; i--) {
                if( selectedMetrics.isEmpty() || (i != selectedMetrics.get(0)) ) measurementAlgorithms.remove(i);
                else selectedMetrics.remove(0);
            }
        }

        System.out.println("DEBUG - total metrics: " + measurementAlgorithms.size());

        measures = new HashMap<>();
        System.out.println("DEBUG - total logs: " + inputLogs.keySet().size());

        /* creating the directory for the results*/
        File resultDir = new File("./results");
        if( !resultDir.exists() && resultDir.mkdir() );


        /* populating measurements results */
        XLog log;
        File old;
        String pathname;
        long eTime;
        for( MiningAlgorithm miningAlgorithm : miningAlgorithms ) {
            old = null;

            String miningAlgorithmName = miningAlgorithm.getAcronym();
            String measurementAlgorithmName = "NULL";
            System.out.println("DEBUG - mining with algorithm: " + miningAlgorithmName);

            /* creating the directory for the results*/
            File maDir = new File("./results/" + miningAlgorithmName);
            if( !maDir.exists() && maDir.mkdir() );

            for( String logName : inputLogs.keySet() ) {
                log = loadLog(inputLogs.get(logName));
                System.out.println("DEBUG - log: " + logName);
                // adding an entry on the measures table for this miner
                if( !measures.containsKey(miningAlgorithmName) )measures.put(miningAlgorithmName, new HashMap<>());
                measures.get(miningAlgorithmName).put(logName, new HashMap<>());

                try {
                    // mining the petrinet
                    eTime = System.currentTimeMillis();
                    logCloner = new LogCloner(new XFactoryNaiveImpl());
                    XLog miningLog = logCloner.cloneLog(log);
                    PetrinetWithMarking petrinetWithMarking = miningAlgorithm.minePetrinet(fakePluginContext, miningLog, false, null, xEventClassifier);
                    eTime = System.currentTimeMillis() - eTime;
                    measures.get(miningAlgorithmName).get(logName).put("mining-time", Long.toString(eTime));
                    System.out.println("DEBUG - mining time: " + eTime + "ms");

                    String bpmnpath = "./results/" + miningAlgorithmName + "/" + logName + "_" + Long.toString(System.currentTimeMillis()) + ".bpmn";
                    BPMNDiagram bpmnModel = PetriNetToBPMNConverter.convert(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking(), false);
                    exportBPMN(bpmnModel, bpmnpath);


                    Measure measure;
                    // computing metrics on the output petrinet
                    for( MeasurementAlgorithm measurementAlgorithm : measurementAlgorithms ) {
                        measurementAlgorithmName = measurementAlgorithm.getAcronym();

                        try {
                            XLog measuringLog = logCloner.cloneLog(log);
                            eTime = System.currentTimeMillis();
                            if( measurementAlgorithm instanceof BPMNComplexity ) {
                                BPMNDiagram diagram = miningAlgorithm.mineBPMNDiagram(fakePluginContext, miningLog, false, null, xEventClassifier);
                                eTime = System.currentTimeMillis();
                                measure = ((BPMNComplexity) measurementAlgorithm).computeMeasurementBPMN(diagram);
                            } else measure = measurementAlgorithm.computeMeasurement(fakePluginContext, xEventClassifier, petrinetWithMarking, miningAlgorithm, measuringLog);

                            eTime = System.currentTimeMillis() - eTime;
                            if (measurementAlgorithm.isMultimetrics()) {
                                for (String metric : measure.getMetrics()) {
                                    measures.get(miningAlgorithmName).get(logName).put(metric, measure.getMetricValue(metric));
                                    System.out.println("DEBUG - " + metric + " : " + measure.getMetricValue(metric));
                                }
                            } else {
                                measures.get(miningAlgorithmName).get(logName).put(measurementAlgorithmName, String.format("%.2f", measure.getValue()));
                                System.out.println("DEBUG - " + measurementAlgorithmName + " : " + measure.getValue());
                            }

//                            if( execTime > MAX_TIME)
                                measures.get(miningAlgorithmName).get(logName).put(measurementAlgorithmName + "-time", Long.toString(eTime));
                        } catch (Error e) {
                            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
                            e.printStackTrace();
                            measures.get(miningAlgorithmName).get(logName).put(measurementAlgorithmName, "-ERR");
                            System.out.println("ERROR - measuring: " + miningAlgorithmName + " : " + logName + " : " + measurementAlgorithmName);
                        } catch(Exception e) {
                            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
                            System.out.println("ERROR - mining: " + miningAlgorithmName + " - " + measurementAlgorithmName);
//                            e.printStackTrace();
                            measures.get(miningAlgorithmName).remove(logName);
                        }
                    }

                } catch(Error e) {
                    System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
                    System.out.println("ERROR - mining: " + miningAlgorithmName + " - " + measurementAlgorithmName);
                    e.printStackTrace();
                    measures.get(miningAlgorithmName).remove(logName);
                } catch(Exception e) {
                    System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
                    System.out.println("ERROR - mining: " + miningAlgorithmName + " - " + measurementAlgorithmName);
                    e.printStackTrace();
                    measures.get(miningAlgorithmName).remove(logName);
                }

                if(old != null) old.delete();
                pathname = "./results/" + miningAlgorithmName + "/upto_" + logName + "_" + Long.toString(System.currentTimeMillis()) + ".xls";
                old = new File(pathname);
                publishResults(pathname);
            }
        }
        publishResults("./results/" + "benchmark_" + Long.toString(System.currentTimeMillis()) + ".xls");
    }

    private void loadLogs(String extLocation) {
        inputLogs = new UnifiedMap<>();
        String logName;
        InputStream in;

        try {
            /* Loading first the logs inside the resources folder (default logs) */
            if( defaultLogs ) {
                System.out.println("DEBUG - importing internal logs.");
                ClassLoader classLoader = getClass().getClassLoader();
                String path = "logs/";
                File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

                if( jarFile.isFile() ) {
                    JarFile jar = new JarFile(jarFile);
                    Enumeration<JarEntry> entries = jar.entries();
                    while( entries.hasMoreElements() ) {
                        logName = entries.nextElement().getName();
                        if( logName.startsWith(path) && !logName.equalsIgnoreCase(path) ) {
                            System.out.println("DEBUG - found log: " + logName);
                            in = classLoader.getResourceAsStream(logName);
//                            System.out.println("DEBUG - stream size: " + in.available());
                            inputLogs.put(logName.replaceAll(".*/", ""), in);
                        }
                    }
                    jar.close();
                }
            }

            /* checking if the user wants to upload also external logs */
            if( extLocation != null ) {
                System.out.println("DEBUG - importing external logs.");
                File folder = new File(extLocation);
                File[] listOfFiles = folder.listFiles();
                if( folder.isDirectory() ) {
                    for( File file : listOfFiles )
                        if( file.isFile() ) {
                            logName = file.getPath();
                            System.out.println("DEBUG - found log: " + logName);
                            inputLogs.put(file.getName(), logName);
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
                return importFromFile(new XFactoryNaiveImpl(), (String) o);
            }else if(o instanceof InputStream){
                return importFromInputStream((InputStream) o, new XesXmlGZIPParser(new XFactoryNaiveImpl()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void exportBPMN(BPMNDiagram diagram, String path) {
        BpmnExportPlugin bpmnExportPlugin = new BpmnExportPlugin();
        UIContext context = new UIContext();
        UIPluginContext uiPluginContext = context.getMainPluginContext();
        try {
            bpmnExportPlugin.export(uiPluginContext, diagram, new File(path));
        } catch (Exception e) { System.out.println("ERROR - impossible to export .bpmn result of split-miner"); }
    }

    private void exportPetrinet(UIPluginContext context, PetrinetWithMarking petrinetWithMarking, String path) {
        ExportAcceptingPetriNetPlugin exportAcceptingPetriNetPlugin = new ExportAcceptingPetriNetPlugin();
        try {
            exportAcceptingPetriNetPlugin.export(
                    context,
                    new AcceptingPetriNetImpl(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking()),
                    new File(path));
        } catch (Exception e) {
            System.out.println("ERROR - impossible to export the petrinet to: " + path);
            return;
        }
    }

    private void publishResults(String filename) {
//        System.out.println("DEBUG - starting generation of the excel file.");
        try {
            HSSFWorkbook workbook = new HSSFWorkbook();
            int rowCounter;
            int cellCounter;
            boolean generateHead;

            /* generating one sheet for each log */
            for( String miningAlgorithmName : measures.keySet() ) {
                generateHead = true;
                rowCounter = 0;

                HSSFSheet sheet = workbook.createSheet(miningAlgorithmName);
                sheet.setDefaultColumnWidth(12);
                HSSFRow rowhead = sheet.createRow((short) rowCounter);
                rowCounter++;

                for( String logName : measures.get(miningAlgorithmName).keySet() ) {
                    /* creating the row for this mining algorithm */
                    HSSFRow row = sheet.createRow((short) rowCounter);
                    rowCounter++;

                    cellCounter = 0;
                    if( generateHead ) rowhead.createCell(cellCounter).setCellValue("Log");
                    row.createCell(cellCounter).setCellValue(logName);
                    cellCounter++;

                    ArrayList<String> metrics = new ArrayList<>(measures.get(miningAlgorithmName).get(logName).keySet());
                    Collections.sort(metrics);

                    for( int i = 0; i < metrics.size(); i++ ) {
                        String metricName = metrics.get(i);
                        if( generateHead ) rowhead.createCell(cellCounter).setCellValue(metricName);
                        row.createCell(cellCounter).setCellValue(measures.get(miningAlgorithmName).get(logName).get(metricName));
                        cellCounter++;
                    }
                    generateHead = false;
                }
            }

            FileOutputStream fileOut = new FileOutputStream(filename);
            workbook.write(fileOut);
            fileOut.close();
            System.out.println("DEBUG - results exported to: " + filename);
        } catch ( Exception e ) {
            System.out.println("ERROR - something went wrong while writing the excel sheet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void computeFScoreFromPetrinet(String modelPath, String logPath) {
        FakePluginContext fakePluginContext = new FakePluginContext();
        Petrinet net = null;

        AlignmentBasedFMeasure alignmentBasedFMeasure = new AlignmentBasedFMeasure();
        AlignmentBasedPrecision alignmentBasedPrecision = new AlignmentBasedPrecision();
        ProjectedPrecision projprecision = new ProjectedPrecision();
        Benchmark benchmark = new Benchmark();
        PnmlImportNet pnmli = new PnmlImportNet();

        try {
            Object o = pnmli.importFile(fakePluginContext, modelPath);
            if(o instanceof Object[] && (((Object[])o)[0] instanceof Petrinet) ) net = (Petrinet)((Object[])o)[0];
            else {
                System.out.println("DEBUG - class: " + o.getClass().getSimpleName());
                return;
            }

            Marking initMarking = MarkingDiscoverer.constructInitialMarking(fakePluginContext, net);
            Marking finalMarking = MarkingDiscoverer.constructFinalMarking(fakePluginContext, net);
            PetrinetWithMarking petrinet = new PetrinetWithMarking(net, initMarking, finalMarking);
            XLog log = benchmark.loadLog(logPath);

            Measure measure = alignmentBasedPrecision.computeMeasurement(fakePluginContext, xEventClassifier, petrinet, null, log);
            for( String metric : measure.getMetrics() ) System.out.println("RESULT - " + metric + " : " + measure.getMetricValue(metric));
            measure = projprecision.computeMeasurement(fakePluginContext, xEventClassifier, petrinet, null, log);
            for( String metric : measure.getMetrics() ) System.out.println("RESULT - " + metric + " : " + measure.getMetricValue(metric));
        } catch ( Exception e ) {
            System.out.println("ERROR - " + e.getMessage());
            e.printStackTrace();
            return;
        }

    }


    public static void computeFitnessNPrecision(String mLogPath, String eLogPath) {
        FakePluginContext fakePluginContext = new FakePluginContext();

        AlignmentBasedFMeasure alignmentBasedFMeasure = new AlignmentBasedFMeasure();
        Benchmark benchmark = new Benchmark();

        HashSet<MiningAlgorithm> miningAlgorithms = new HashSet<>();
        miningAlgorithms.add(new Heuristics52AlgorithmWrapper());
        miningAlgorithms.add(new InductiveMinerIMfWrapper());
        miningAlgorithms.add(new StructuredMinerAlgorithmWrapperHM52());

        XLog mLog = benchmark.loadLog(mLogPath);
        XLog eLog = benchmark.loadLog(eLogPath);

        try {
            for( MiningAlgorithm ma : miningAlgorithms ) {

                PetrinetWithMarking petrinet = ma.minePetrinet(fakePluginContext, mLog, false, null, xEventClassifier);
                benchmark.exportPetrinet(fakePluginContext, petrinet, "./" + ma.getAcronym() + "_pn.pnml");
                Measure measure = alignmentBasedFMeasure.computeMeasurement(fakePluginContext, xEventClassifier, petrinet, ma, eLog);

                System.out.println("DEBUG - results for: " + ma.getAlgorithmName());
                for( String metric : measure.getMetrics() )
                    System.out.println("RESULT - " + metric + " : " + measure.getMetricValue(metric));
            }
        } catch ( Exception e ) {
            System.out.println("ERROR - " + e.getMessage());
            e.printStackTrace();
            return;
        }

    }

    public static void logsAnalysis(String path) {
        ArrayList<SimpleLog> sLogs = new ArrayList<>();
        Benchmark benchmark = new Benchmark();
        XLog log;

        int totalLogs;

        int avgDistinctTraces = 0;
        int avgDistinctEvents = 0;
        int avgTraces = 0;
        long avgEvents = 0;

        int avgLongestTrace = 0;
        int avgShortestTrace = 0;
        int avgAvgTrace = 0;

        int maxTraces = Integer.MIN_VALUE;
        int minTraces = Integer.MAX_VALUE;

        long minEvents = Long.MAX_VALUE;
        long maxEvents = Long.MIN_VALUE;

        int maxDistinctTraces = Integer.MIN_VALUE;
        int minDistinctTraces = Integer.MAX_VALUE;

        int minDistinctEvents = Integer.MAX_VALUE;
        int maxDistinctEvents = Integer.MIN_VALUE;

        System.out.println("LOGSA - starting analysis ... ");

        benchmark.loadLogs(path);
        for( String logName : benchmark.inputLogs.keySet() ) {
            log = benchmark.loadLog(benchmark.inputLogs.get(logName));
            sLogs.add(LogParser.getSimpleLog(log, xEventClassifier));
        }

        totalLogs = sLogs.size();


        for( SimpleLog l : sLogs ) {
            avgTraces += l.size();
            avgEvents += l.getTotalEvents();

            if( minTraces > l.size() ) minTraces = l.size();
            if( maxTraces < l.size() ) maxTraces = l.size();

            if( minEvents > l.getTotalEvents() ) minEvents = l.getTotalEvents();
            if( maxEvents < l.getTotalEvents() ) maxEvents = l.getTotalEvents();

            avgDistinctTraces += l.getDistinctTraces();
            if( minDistinctTraces > l.getDistinctTraces() ) minDistinctTraces = l.getDistinctTraces();
            if( maxDistinctTraces < l.getDistinctTraces() ) maxDistinctTraces = l.getDistinctTraces();

            avgDistinctEvents += l.getDistinctEvents();
            if( minDistinctEvents > l.getDistinctEvents() ) minDistinctEvents = l.getDistinctEvents();
            if( maxDistinctEvents < l.getDistinctEvents() ) maxDistinctEvents = l.getDistinctEvents();

            avgShortestTrace += l.getShortestTrace();
            avgAvgTrace += l.getAvgTraceLength();
            avgLongestTrace += l.getLongestTrace();
        }

        System.out.println("LOGSA - analysis result of " + totalLogs + " logs: ");

        System.out.println("LOGSA - avg traces: " + avgTraces/totalLogs);
        System.out.println("LOGSA - avg events: " + avgEvents/totalLogs);

        System.out.println("LOGSA - avg distinct traces: " + avgDistinctTraces/totalLogs);
        System.out.println("LOGSA - avg distinct events: " + avgDistinctEvents/totalLogs);

        System.out.println("LOGSA - avg shortest trace: " + avgShortestTrace/totalLogs);
        System.out.println("LOGSA - avg avg trace: " + avgAvgTrace/totalLogs);
        System.out.println("LOGSA - avg longest trace: " + avgLongestTrace/totalLogs);

        System.out.println("LOGSA - max traces: " + maxTraces);
        System.out.println("LOGSA - min traces: " + minTraces);

        System.out.println("LOGSA - max events: " + maxEvents);
        System.out.println("LOGSA - min events: " + minEvents);

        System.out.println("LOGSA - max distinct traces: " + maxDistinctTraces);
        System.out.println("LOGSA - min distinct traces: " + minDistinctTraces);

        System.out.println("LOGSA - max distinct events: " + maxDistinctTraces);
        System.out.println("LOGSA - min distinct events: " + minDistinctTraces);
    }

    public static void getLogFolds(String logPath, String sfold) {
        Benchmark benchmark = new Benchmark();
        XLog evalLog;
        XLog log = benchmark.loadLog(logPath);
        Map<XLog, XLog> crossValidationLogs;
        int k;
        int i;

        String folder = "./";

        FileOutputStream fos;
        XesXmlSerializer serializer = new XesXmlSerializer();

        try {
            k = Integer.valueOf(sfold);
            crossValidationLogs = XFoldAlignmentBasedFMeasure.getCrossValidationLogs(log, k);

            /* saving the folds */
            i=0;
            for( XLog miningLog : crossValidationLogs.keySet() ) {
                evalLog = crossValidationLogs.get(miningLog);
                i++;

                fos = new FileOutputStream( new String(folder + i + "_mining_fold.xes") );
                serializer.serialize(miningLog, fos);
                fos.close();

                fos = new FileOutputStream( new String(folder + i + "_eval_fold.xes") );
                serializer.serialize(evalLog, fos);
                fos.close();
            }

        } catch(Exception e) {
            System.out.println("ERROR - something went wrong while folding the log.");
            e.printStackTrace();
        }
    }

}
