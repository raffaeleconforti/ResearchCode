package com.raffaeleconforti.benchmark;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Set;

import com.raffaeleconforti.benchmark.logic.Benchmark;
import com.raffaeleconforti.noisefiltering.event.InfrequentBehaviourFilter;
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

    static {
        try {
            if(System.getProperty("os.name").startsWith("Windows")) {
                System.loadLibrary(LPSOLVE55);
                System.loadLibrary(LPSOLVE55J);
            }else {
                System.loadLibrary(LIBLPSOLVE55);
                System.loadLibrary(LIBLPSOLVE55J);
            }
        } catch (UnsatisfiedLinkError e) {
            loadFromJar();
        }
    }

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
        Benchmark benchmark;

        int icmd = 0;

        if( (args.length != 0) && (args[icmd].equalsIgnoreCase("-help"))) {
            showHelp();
            return;
        }

        if( (args.length != 0) && args[icmd].equalsIgnoreCase("-ext") ) {
            defaultLogs = false;
            icmd++;
        }

        if( (icmd < args.length) && !args[icmd].equalsIgnoreCase("-p") ) {
            extLoc = args[icmd];
            icmd++;
        }

        if( (icmd < args.length) && args[icmd].equalsIgnoreCase("-p") )
            do {
                icmd++;
                packages.add(args[icmd]);
            } while( icmd < args.length );

        benchmark = new Benchmark(defaultLogs, extLoc, packages);
        benchmark.performBenchmark();

    }

    private static void showHelp() {
        System.out.println("COMMAND: java -jar BenchmarkCommandline");
        System.out.println("PARAM1 (optional) - disabling default logs loading: -ext");
        System.out.println("\t- when this flag is present, only external logs are loaded for the benchmarking. Therefore it is mandatory declare param2");
        System.out.println("PARAM2 (optional or mandatory) - path to an external folder (containing additional logs to be used for the benchmark)");
        System.out.println("\t- when this param is present, a set of external logs (contained in the specified folder path) is loaded");
        System.out.println("PARAM3 (optional) - list of packages containing mining algorithms");
        System.out.println("\t- external mining algorithms not yet embedded in this benchmark can be loaded specifying their package as a string");
    }

}
