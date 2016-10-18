package com.raffaeleconforti.benchmark;

import java.util.Set;

import com.raffaeleconforti.benchmark.logic.Benchmark;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;


/**
 * Created by Adriano on 12/10/2016.
 */
public class BenchmarkCommandline {

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
