package com.raffaeleconforti.noisefiltering.timestamp.commandline;

import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.noisefiltering.timestamp.TimeStampFixerSmartExecutor;
import com.raffaeleconforti.noisefiltering.timestamp.permutation.PermutationTechnique;
import org.deckfour.xes.factory.XFactory;
import com.raffaeleconforti.memorylog.XFactoryMemoryImpl;
import org.deckfour.xes.model.XLog;

import java.util.Scanner;

/**
 * Created by conforti on 7/02/15.
 */

public class TimeStampFixerCommandline {

    private final TimeStampFixerSmartExecutor timeStampFixerSmartExecutor = new TimeStampFixerSmartExecutor();

    public static void main(String[] args) throws Exception {
        Scanner console = new Scanner(System.in);
        System.out.println("Input file:");
        String name = console.nextLine();
        XFactory factory = new XFactoryMemoryImpl();
        XLog log = LogImporter.importFromFile(factory, name);

        TimeStampFixerCommandline timeStampFixerCommandline = new TimeStampFixerCommandline();
        XLog filteredlog = timeStampFixerCommandline.filterLog(log);

        System.out.println("Output file: ");
        String path = console.next();

        LogImporter.exportToFile(path.substring(0, path.lastIndexOf("/")) + 1, path.substring(path.lastIndexOf("/") + 1, path.length()), filteredlog);

    }

    public XLog filterLog(XLog rawlog) {
        return timeStampFixerSmartExecutor.filterLog(rawlog, 11, PermutationTechnique.ILP_LPSOLVE);
    }

}
