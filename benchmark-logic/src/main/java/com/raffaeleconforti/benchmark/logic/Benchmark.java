package com.raffaeleconforti.benchmark.logic;

import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.MeasurementAlgorithm;
import com.raffaeleconforti.memorylog.XFactoryMemoryImpl;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.model.XLog;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.raffaeleconforti.log.util.LogImporter.importFromFile;
import static com.raffaeleconforti.log.util.LogImporter.importFromInputStream;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 18/10/2016.
 */
public class Benchmark {
    private boolean defaultLogs;
    private String extLocation;
    private Map<String, XLog> logs;
    private Map<String, Object> logsInput;

    private Set<String> packages = new UnifiedSet<>();

    /* this is a multidimensional cube containing all the measures.
    for each log, each mining algorithm and each metric we have a resulting metric value */
    private HashMap<String, HashMap<String, HashMap<String, String>>> measures;

    public Benchmark(boolean defaultLogs, String extLocation, Set<String> packages) {
        this.defaultLogs = defaultLogs;
        this.extLocation = extLocation;
        this.packages = packages;
        loadLogs();
    }

    public void performBenchmark(ArrayList<Integer> selectedMiners, ArrayList<Integer> selectedMetrics) {
        XEventClassifier xEventClassifier = new XEventAndClassifier(new XEventNameClassifier());
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
            Collections.sort(selectedMetrics);
            Collections.reverse(selectedMetrics);
            for(int i = measurementAlgorithms.size()-1; i >= 0; i--) {
                if( selectedMetrics.isEmpty() || (i != selectedMetrics.get(0)) ) measurementAlgorithms.remove(i);
                else selectedMetrics.remove(0);
            }
        }

        System.out.println("DEBUG - total metrics: " + measurementAlgorithms.size());

        measures = new HashMap<>();
        System.out.println("DEBUG - total logs: " + logs.keySet().size());

        /* populating measurements results */
        XLog log;
        for( String logName : logsInput.keySet() ) {
            log = loadLog(logsInput.get(logName));
            measures.put(logName, new HashMap<>());
            System.out.println("DEBUG - measuring on log: " + logName);

            for( MiningAlgorithm miningAlgorithm : miningAlgorithms ) {
                // adding an entry on the measures table for this miner
                String miningAlgorithmName = miningAlgorithm.getAlgorithmName();
                String measurementAlgorithmName = "NULL";
                measures.get(logName).put(miningAlgorithmName, new HashMap<>());

                try {
                    System.out.println("DEBUG - measuring on mining algorithm: " + miningAlgorithmName);

                    // mining the petrinet
                    long sTime = System.currentTimeMillis();
                    PetrinetWithMarking petrinetWithMarking = miningAlgorithm.minePetrinet(fakePluginContext, log, false);
                    long execTime = System.currentTimeMillis() - sTime;
                    measures.get(logName).get(miningAlgorithmName).put("exec-time", Long.toString(execTime));

                    // computing metrics on the output petrinet
                    for( MeasurementAlgorithm measurementAlgorithm : measurementAlgorithms ) {
                            measurementAlgorithmName = measurementAlgorithm.getMeasurementName();
                            Measure measure = measurementAlgorithm.computeMeasurement(fakePluginContext, xEventClassifier, petrinetWithMarking, miningAlgorithm, log);

                            if( measurementAlgorithm.isMultimetrics() ) {
                                for(String metric : measure.getMetrics() ) {
                                    measures.get(logName).get(miningAlgorithmName).put(metric, measure.getMetricValue(metric));
                                    System.out.println("DEBUG - " + metric + " : " + measure.getMetricValue(metric));
                                }
                            } else {
                                measures.get(logName).get(miningAlgorithmName).put(measurementAlgorithmName, Double.toString(measure.getValue()));
                                System.out.println("DEBUG - " + measurementAlgorithmName + " : " + measure.getValue());
                            }
                    }
                } catch(Exception e) {
                    System.out.println("ERROR - for: " + miningAlgorithmName + " - " + measurementAlgorithmName);
                    e.printStackTrace();
                    measures.get(logName).remove(miningAlgorithmName);
                }
            }
        }

        publishResults();
    }

    private void loadLogs() {
        logsInput = new UnifiedMap<>();
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
                            System.out.println("DEBUG - name: " + logName);
                            in = classLoader.getResourceAsStream(logName);
                            System.out.println("DEBUG - stream size: " + in.available());
                            logsInput.put(logName.replaceAll(".*/", ""), in);
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
                return importFromFile(new XFactoryMemoryImpl(), (String) o);
            }else if(o instanceof InputStream){
                return importFromInputStream((InputStream) o, new XesXmlGZIPParser(new XFactoryMemoryImpl()));
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

            /* generating one sheet for each log */
            for( String logName : measures.keySet() ) {
                rowCounter = 0;

                HSSFSheet sheet = workbook.createSheet(logName);
                HSSFRow rowhead = sheet.createRow((short) rowCounter);
                rowCounter++;

                for( String miningAlgorithmName : measures.get(logName).keySet() ) {
                    /* creating the row for this mining algorithm */
                    HSSFRow row = sheet.createRow((short) rowCounter);
                    rowCounter++;

                    cellCounter = 0;
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
