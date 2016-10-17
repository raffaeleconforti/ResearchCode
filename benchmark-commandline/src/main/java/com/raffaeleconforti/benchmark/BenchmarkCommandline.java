package com.raffaeleconforti.benchmark;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.raffaeleconforti.memorylog.XFactoryMemoryImpl;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;

import static com.raffaeleconforti.log.util.LogImporter.importFromFile;
import static com.raffaeleconforti.log.util.LogImporter.importFromInputStream;

/**
 * Created by Adriano on 12/10/2016.
 */
public class BenchmarkCommandline {

    public static void main(String[] args) throws Exception {
        int icmd = 0;
        List<XLog> logs = new ArrayList<>();
        boolean internal = true;
        BenchmarkCommandline bcl = new BenchmarkCommandline();

        try {
            if( (args.length != 0) && (args[icmd].equalsIgnoreCase("-help")) ) {
                showHelp();
                return;
            }

            if( args.length == 0 ) logs = bcl.getAllLogs(internal, null);
            else {
                if( args[icmd].equalsIgnoreCase("-ext") ) {
                    internal = false;
                    icmd++;
                }
                logs = bcl.getAllLogs(internal, args[icmd]);
            }

            System.out.println("DEBUG - number of logs to analyze: " + logs.size());

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            System.out.println("RUN: java -jar BenchmarkCommandline [-ext] [ext_logs_folder_path]");
            System.out.println("HELP: java -jar BenchmarkCommandline -help");
        }

    }

    private static void showHelp(){
        System.out.println("COMMAND: java -jar BenchmarkCommandline");
        System.out.println("PARAM1 (optional) - disabling default logs loading: -ext");
        System.out.println("\t- when this flag is present, only external logs are loaded for the benchmarking. Therefore it is mandatory declare param2");
        System.out.println("PARAM2 (optional or mandatory) - path to an external folder (containing additional logs to be used for the benchmark)");
        System.out.println("\t- when this param is present, a set of external logs (contained in the specified folder path) is loaded");
    }

    private List<XLog> getAllLogs(boolean default_logs, String static_location) {
        List<XLog> logs = new ArrayList<>();
        String logName;
        InputStream in;
        XLog log;

        try {
            /* Loading first the logs inside the resources folder (default logs) */
            if( default_logs ) {
                System.out.println("DEBUG - importing internal logs.");
                ClassLoader classLoader = getClass().getClassLoader();
                String path = "logs/";
                File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

                if( jarFile.isFile() ) {
                    JarFile jar = new JarFile(jarFile);
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        logName = entries.nextElement().getName();
                        if( logName.startsWith(path) && !logName.equalsIgnoreCase(path) ) {
                            System.out.println("DEBUG - name: " + logName);
                            in = classLoader.getResourceAsStream(logName);
                            System.out.println("DEBUG - stream size: " + in.available());
                            log = importFromInputStream(in, new XesXmlParser(new XFactoryMemoryImpl()));
                            System.out.println("DEBUG - log size: " + log.size());
                            logs.add(log);
                        }
                    }
                    jar.close();
                }
            }

            /* checking if the user wants to upload also external logs */
            if( static_location != null ) {
                System.out.println("DEBUG - importing external logs.");
                File folder = new File(static_location);
                File[] listOfFiles = folder.listFiles();
                if( folder.isDirectory() ) {
                    for( File file : listOfFiles )
                        if( file.isFile() ) {
                            logName = file.getPath();
                            System.out.println("DEBUG - name: " + logName);
                            log = importFromFile(new XFactoryMemoryImpl(), logName);
                            System.out.println("DEBUG - log size: " + log.size());
                            logs.add(log);
                        }
                } else {
                    System.out.println("ERROR - external logs loading failed, input path is not a folder.");
                }
            }
        } catch( Exception e ) {
            System.out.println("ERROR - something went wrong reading the resource folder: " + e.getMessage());
            e.printStackTrace();
        }

        return logs;
    }
}
