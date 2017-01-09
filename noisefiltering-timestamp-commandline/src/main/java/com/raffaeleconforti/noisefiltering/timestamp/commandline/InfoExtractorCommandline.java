package com.raffaeleconforti.noisefiltering.timestamp.commandline;

import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.noisefiltering.timestamp.LogInfoExtractor;
import org.deckfour.xes.factory.XFactory;
import com.raffaeleconforti.memorylog.XFactoryMemoryImpl;
import org.deckfour.xes.model.XLog;
import java.util.*;

/**
 * Created by conforti on 7/02/15.
 */

public class InfoExtractorCommandline {

    public static void main(String[] args) throws Exception {
        Scanner console = new Scanner(System.in);
        System.out.println("Input file:");
        String name = console.nextLine();
        XFactory factory = new XFactoryMemoryImpl();
        XLog log = LogImporter.importFromFile(factory, name);

        LogInfoExtractor infoExtractor = new LogInfoExtractor();
        String info = infoExtractor.extractInfo(log);

        System.out.println(info);
    }

}
