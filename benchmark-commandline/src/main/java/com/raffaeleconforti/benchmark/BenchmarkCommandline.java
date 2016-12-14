package com.raffaeleconforti.benchmark;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import com.raffaeleconforti.benchmark.logic.Benchmark;
import com.raffaeleconforti.benchmark.logic.MeasurementAlgorithmDiscoverer;
import com.raffaeleconforti.benchmark.logic.MiningAlgorithmDiscoverer;
import com.raffaeleconforti.measurements.MeasurementAlgorithm;
import com.raffaeleconforti.noisefiltering.event.InfrequentBehaviourFilter;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import org.apache.commons.io.FileUtils;
import org.apache.poi.util.IOUtils;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;


/**
 * Created by Adriano on 12/10/2016.
 */
public class BenchmarkCommandline {

    private final static String LPSOLVE55 = "lpsolve55";
    private final static String LPSOLVE55J = "lpsolve55j";
    private final static String LIBLPSOLVE55 = "liblpsolve55";
    private final static String LIBLPSOLVE55J = "liblpsolve55j";

//    static {
//        try {
//            if(System.getProperty("os.name").startsWith("Windows")) {
//                System.loadLibrary(LPSOLVE55);
//                System.loadLibrary(LPSOLVE55J);
//            }else {
//                System.loadLibrary(LIBLPSOLVE55);
//                System.loadLibrary(LIBLPSOLVE55J);
//            }
//        } catch (UnsatisfiedLinkError e) {
//            loadFromJar();
//        }
//    }

    private static void loadFromJar() {
        // we need to put both DLLs to temp dir
        String path = "AC_" + new Date().getTime();
        if(System.getProperty("os.name").startsWith("Windows")) {
            loadLibWin(path, LPSOLVE55);
            loadLibWin(path, LPSOLVE55J);
        }else {
            loadLibMac(path, LIBLPSOLVE55);
            loadLibMac(path, LIBLPSOLVE55J);
        }
    }

    private static void loadLibWin(String path, String name) {
        name = name + ".dll";
        try {
            // have to use a stream
            InputStream in = InfrequentBehaviourFilter.class.getResourceAsStream("/" + name);
            // always write to different location
            File fileOut = new File(name);
            OutputStream out = FileUtils.openOutputStream(fileOut);
            IOUtils.copy(in, out);
            in.close();
            out.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load required DLL", e);
        }
    }

    private static void loadLibMac(String path, String name) {
        name = name + ".jnilib";
        try {
            // have to use a stream
            InputStream in = InfrequentBehaviourFilter.class.getResourceAsStream("/" + name);
            // always write to different location
            File fileOut = new File(name);
            OutputStream out = FileUtils.openOutputStream(fileOut);
            IOUtils.copy(in, out);
            in.close();
            out.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load required JNILIB", e);
        }
    }

    public static void main(String[] args) throws Exception {
        boolean defaultLogs = true;
        String extLoc = null;
        Set<String> packages = new UnifiedSet<>();
        Set<Integer> selectedMiners = null;
        Set<Integer> selectedMetrics = null;
        Benchmark benchmark;
        boolean timeout = false;

        long miningTimeout = 3600000;
        long measurementTimeout = 3600000;

        int icmd = 0;

        if( (args.length != 0) && (args[icmd].equalsIgnoreCase("-help"))) {
            showHelp();
            return;
        }

        if( (args.length != 0) && (args[icmd].equalsIgnoreCase("-algorithms"))) {
            icmd++;

            if( (icmd < args.length) && args[icmd].equalsIgnoreCase("-p") ) {
                icmd++;
                do{
                    packages.add(args[icmd]);
                    icmd++;
                }while( icmd < args.length );
            }

            showMiningAlgorithms(packages);
            return;
        }

        if( (args.length != 0) && args[icmd].equalsIgnoreCase("-ext") ) {
            defaultLogs = false;
            icmd++;
            extLoc = args[icmd];
            icmd++;
        }

            if( (icmd < args.length) && args[icmd].equalsIgnoreCase("-timeout") ) {
                timeout = true;
                icmd++;
                miningTimeout = Long.valueOf(args[icmd]);
                icmd++;
                measurementTimeout = Long.valueOf(args[icmd]);
                icmd++;
            }

            selectedMiners = new HashSet<>();
            if( (icmd < args.length) && args[icmd].equalsIgnoreCase("-miners") ) {
                icmd++;
                do{
                    try { selectedMiners.add(Integer.valueOf(args[icmd])); }
                    catch( NumberFormatException nfe ) { break; }
                    icmd++;
                }while( icmd < args.length );
            }

            selectedMetrics = new HashSet<>();
            if( (icmd < args.length) && args[icmd].equalsIgnoreCase("-metrics") ) {
                icmd++;
                do{
                    try { selectedMetrics.add(Integer.valueOf(args[icmd])); }
                    catch( NumberFormatException nfe ) { break; }
                    icmd++;
                }while( icmd < args.length );
            }

            if( (icmd < args.length) && args[icmd].equalsIgnoreCase("-p") ) {
                icmd++;
                do{
                    packages.add(args[icmd]);
                    icmd++;
                }while( icmd < args.length );
            }
//        }

        benchmark = new Benchmark(defaultLogs, extLoc, packages);
        benchmark.performBenchmark(new ArrayList<Integer>(selectedMiners), new ArrayList<Integer>(selectedMetrics));

    }

    private static void showHelp() {
        System.out.println("");
        System.out.println("\tCOMMAND: java -jar BenchmarkCommandline");
        System.out.println("\tPARAMS: when using any of these parameters listed below their order has to be respected");
        System.out.println("");
        System.out.println("\t-algorithms\nthis shows the list of available mining and measurement algorithms along with their reference indexes");
        System.out.println("");
        System.out.println("\t-ext 'external log location path'\nthis allows to load external logs instead of using the internal ones, a folder containing the logs file has to be specified.");
        System.out.println("");
        System.out.println("\t-miners 'list of indexes of the mining algorithms to use for the benchmark'");
        System.out.println("this parameter allows to select the mining algorithms to use for the benchmark, the indexes are shown with the command:\t java -jar BenchmarkCommandline -algorithms");
        System.out.println("");
        System.out.println("\t-metrics 'list of indexes of the measurement algorithms to use for the benchmark'");
        System.out.println("this parameter allows to select the measurement algorithms to use for the benchmark, the indexes are shown with the command:\t java -jar BenchmarkCommandline -algorithms");
        System.out.println("");
        System.out.println("\t-p 'list of external packages'\nexternal mining or measurement algorithms not yet embedded in this benchmark can be loaded specifying their package as a string");
    }

    private static void showMiningAlgorithms(Set<String> packages) {
        List<MiningAlgorithm> miningAlgorithms = MiningAlgorithmDiscoverer.discoverAlgorithms(packages);
        List<MeasurementAlgorithm> measurementAlgorithms = MeasurementAlgorithmDiscoverer.discoverAlgorithms(packages);
        int index;

        Collections.sort(miningAlgorithms, new Comparator<MiningAlgorithm>() {
            @Override
            public int compare(MiningAlgorithm o1, MiningAlgorithm o2) {
                return o2.getAlgorithmName().compareTo(o1.getAlgorithmName());
            }
        });

        Collections.sort(measurementAlgorithms, new Comparator<MeasurementAlgorithm>() {
            @Override
            public int compare(MeasurementAlgorithm o1, MeasurementAlgorithm o2) {
                return o2.getMeasurementName().compareTo(o1.getMeasurementName());
            }
        });

        index = 0;
        System.out.println("Mining algorithms available: ");
        for(MiningAlgorithm ma : miningAlgorithms) System.out.println(index++ + " - " + ma.getAlgorithmName() + " (" + ma.getAcronym() + ")");
        System.out.println();

        index = 0;
        System.out.println("Measurement algorithms available: ");
        for(MeasurementAlgorithm ma : measurementAlgorithms) System.out.println(index++ + " - " + ma.getMeasurementName() + " : " + ma.getAcronym());

    }


}
