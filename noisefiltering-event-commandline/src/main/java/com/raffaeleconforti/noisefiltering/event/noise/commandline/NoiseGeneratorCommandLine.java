package com.raffaeleconforti.noisefiltering.event.noise.commandline;

import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.noisefiltering.event.noise.NoiseGenerator;
import com.raffaeleconforti.noisefiltering.event.noise.commandline.ui.NoiseUI;
import com.raffaeleconforti.noisefiltering.event.noise.selection.NoiseResult;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;

import java.util.Scanner;

/**
 * Created by conforti on 26/02/15.
 */
public class NoiseGeneratorCommandLine {

    public static void main(String[] args) throws Exception {
        Scanner console = new Scanner(System.in);
        System.out.println("Input file:");
        String name = console.nextLine();
        XFactory factory = new XFactoryNaiveImpl();
        XLog log = LogImporter.importFromFile(factory, name);

        NoiseGeneratorCommandLine ngcl = new NoiseGeneratorCommandLine();
        XLog filteredlog = ngcl.generateNoise(log);

        System.out.println("Output file: ");
        String path = console.next();

        LogImporter.exportToFile(path.substring(0, path.lastIndexOf("/")) + 1, path.substring(path.lastIndexOf("/") + 1, path.length()), filteredlog);

    }

    public XLog generateNoise(XLog log) {
        NoiseGenerator noiseGenerator = new NoiseGenerator(log);
        NoiseUI noiseUI = new NoiseUI();
        NoiseResult result = noiseUI.showGUI();
        return noiseGenerator.insertNoise(result.getNoiseLevel());
    }
}
