package com.raffaeleconforti.noisefiltering.timestamp.commandline;

import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.noisefiltering.timestamp.TimeStampFixerSmartExecutor;
import com.raffaeleconforti.noisefiltering.timestamp.permutation.PermutationTechnique;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;

import java.util.Scanner;

/**
 * Created by conforti on 7/02/15.
 */

public class TimeStampFixerCommandline {

    private final TimeStampFixerSmartExecutor timeStampFixerSmartExecutor;

    public static void main(String[] args) throws Exception {
        Scanner console = new Scanner(System.in);
        System.out.println("Solve using GUROBI? (commercial ILP Solver)");
        boolean useGurobi = console.nextLine().toLowerCase().contains("y");
        System.out.println("Solve using Arcs Frequency? ");
        boolean useArcsFrequency = console.nextLine().toLowerCase().contains("y");

        System.out.println("Input file:");
        String name = console.nextLine();
        XFactory factory = new XFactoryNaiveImpl();
        XLog log = LogImporter.importFromFile(factory, name);

        TimeStampFixerCommandline timeStampFixerCommandline = new TimeStampFixerCommandline(useGurobi, useArcsFrequency);
        XLog filteredlog = timeStampFixerCommandline.filterLog(log);

        System.out.println("Output file: ");
        String path = console.next();

        LogImporter.exportToFile(path.substring(0, path.lastIndexOf("/")) + 1, path.substring(path.lastIndexOf("/") + 1, path.length()), filteredlog);

    }

    public TimeStampFixerCommandline(boolean useGurobi, boolean useArcsFrequency) {
        timeStampFixerSmartExecutor = new TimeStampFixerSmartExecutor(useGurobi, useArcsFrequency, false);
    }

    public XLog filterLog(XLog rawlog) {
        return timeStampFixerSmartExecutor.filterLog(rawlog, 11, PermutationTechnique.ILP_LPSOLVE, false, false);
    }

}
