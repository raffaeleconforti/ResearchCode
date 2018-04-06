package com.raffaeleconforti.noisefiltering.event.commandline;

import com.raffaeleconforti.automaton.Automaton;
import com.raffaeleconforti.automaton.AutomatonFactory;
import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.log.util.LogModifier;
import com.raffaeleconforti.log.util.LogOptimizer;
import com.raffaeleconforti.noisefiltering.event.InfrequentBehaviourFilter;
import com.raffaeleconforti.noisefiltering.event.commandline.ui.NoiseFilterUI;
import com.raffaeleconforti.noisefiltering.event.selection.NoiseFilterResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XLog;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Scanner;


/**
 * Created by conforti on 7/02/15.
 */

public class InfrequentBehaviourFilterCommandLine {

    private final XEventClassifier xEventClassifier = new XEventAndClassifier(new XEventNameClassifier());
    private final AutomatonFactory automatonFactory = new AutomatonFactory(xEventClassifier);
    private final InfrequentBehaviourFilter infrequentBehaviourFilter;

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

    public static void main(String[] args) throws Exception {

        System.out.println("This is the stand alone version of the log filtering algorithm proposed in:");
        System.out.println();
        System.out.println("R. Conforti, M. La Rosa, and A.H.M. ter Hofstede.");
        System.out.println("Filtering out Infrequent Behavior from Business Process Event Logs.");
        System.out.println("IEEE Transactions on Knowledge and Data Engineering, 2016.");
        System.out.println();
        System.out.println("For more info contact me at raffaele.conforti@unimelb.edu.au");
        System.out.println("or visit my website www.raffaeleconforti.com");

        Scanner console = new Scanner(System.in);

        System.out.println("Solve using GUROBI? (commercial ILP Solver)");
        boolean useGurobi = console.nextLine().toLowerCase().contains("y");
        System.out.println("Solve using Arcs Frequency?");
        boolean useArcsFrequency = console.nextLine().toLowerCase().contains("y");

        System.out.println("Input file:");
        String name = console.nextLine();

        while(name.endsWith(" ")) {
            name = name.substring(0, name.length() - 1);
        }

        XFactory factory = new XFactoryNaiveImpl();
        XLog log = LogImporter.importFromFile(factory, name);

        InfrequentBehaviourFilterCommandLine ibfcl = new InfrequentBehaviourFilterCommandLine(useGurobi, useArcsFrequency, false);
        XLog filteredlog = ibfcl.filterLog(log);

        System.out.println("Output file: ");
        String path = console.next();

        if(path.contains("/")) {
            LogImporter.exportToFile(path.substring(0, path.lastIndexOf("/") + 1), path.substring(path.lastIndexOf("/") + 1, path.length()), filteredlog);
        }else if(path.contains("\\")) {
            LogImporter.exportToFile(path.substring(0, path.lastIndexOf("\\") + 1), path.substring(path.lastIndexOf("\\") + 1, path.length()), filteredlog);
        }else {
            LogImporter.exportToFile("", path, filteredlog);
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

    public InfrequentBehaviourFilterCommandLine(boolean useGurobi, boolean useArcsFrequency, boolean debug_mode) {
         infrequentBehaviourFilter = new InfrequentBehaviourFilter(xEventClassifier, useGurobi, useArcsFrequency, debug_mode);
    }

    public XLog filterLog(XLog rawlog) {
        XLog log = rawlog;
        XFactory factory = new XFactoryNaiveImpl();
        LogOptimizer logOptimizer = new LogOptimizer(factory);
        log = logOptimizer.optimizeLog(log);

        XFactoryRegistry.instance().setCurrentDefault(factory);
        LogModifier logModifier = new LogModifier(factory, XConceptExtension.instance(), XTimeExtension.instance(), logOptimizer);
        logModifier.insertArtificialStartAndEndEvent(log);

        Automaton<String> automatonOriginal = automatonFactory.generate(log);

        double[] arcs = infrequentBehaviourFilter.discoverArcs(automatonOriginal, 1.0);

        NoiseFilterUI noiseUI = new NoiseFilterUI();
        NoiseFilterResult result = noiseUI.showGUI(this, arcs, automatonOriginal.getNodes());

        return infrequentBehaviourFilter.filterLog(new FakePluginContext(), rawlog, result);
    }

    public double discoverThreshold(double[] arcs, double initialPercentile) {
        return infrequentBehaviourFilter.discoverThreshold(arcs, initialPercentile);
    }
}
