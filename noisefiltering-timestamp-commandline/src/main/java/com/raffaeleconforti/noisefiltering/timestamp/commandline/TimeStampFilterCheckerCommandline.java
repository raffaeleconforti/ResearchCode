package com.raffaeleconforti.noisefiltering.timestamp.commandline;

import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.noisefiltering.timestamp.TimeStampFilterChecker;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;

import java.util.Scanner;

/**
 * Created by conforti on 7/02/15.
 */

public class TimeStampFilterCheckerCommandline {

    public static void main(String[] args) throws Exception {
        Scanner console = new Scanner(System.in);
        XFactory factory = new XFactoryNaiveImpl();

        System.out.println("Input filtered log:");
        String name = console.nextLine();
        XLog filteredLog = LogImporter.importFromFile(factory, name);

        System.out.println("Input noisy log:");
        name = console.nextLine();
        XLog noisyLog = LogImporter.importFromFile(factory, name);

        System.out.println("Input correct log:");
        name = console.nextLine();
        XLog correctLog = LogImporter.importFromFile(factory, name);

        TimeStampFilterChecker timeStampFilterChecker = new TimeStampFilterChecker();
        String info = timeStampFilterChecker.check(filteredLog, noisyLog, correctLog);

        System.out.println(info);
    }

}
