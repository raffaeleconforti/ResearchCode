package com.raffaeleconforti.noisefiltering.timestamp.commandline;

import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.noisefiltering.timestamp.noise.TimeStampNoiseGenerator;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;

import java.util.Scanner;

/**
 * Created by conforti on 7/02/15.
 */

public class TimeStampNoiseGeneratorCommandline {

    private final TimeStampNoiseGenerator timeStampNoiseGenerator = new TimeStampNoiseGenerator();

    public static void main(String[] args) throws Exception {
        Scanner console = new Scanner(System.in);
        System.out.println("Input file:");
        String name = console.nextLine();
        XFactory factory = new XFactoryNaiveImpl();
        XLog log = LogImporter.importFromFile(factory, name);

        TimeStampNoiseGeneratorCommandline timeStampNoiseGeneratorCommandline = new TimeStampNoiseGeneratorCommandline();
        XLog filteredlog = timeStampNoiseGeneratorCommandline.insertNoise(log);

        System.out.println("Output file: ");
        String path = console.next();

        LogImporter.exportToFile(path.substring(0, path.lastIndexOf("/")) + 1, path.substring(path.lastIndexOf("/") + 1, path.length()), filteredlog);

    }

    public XLog insertNoise(XLog log) {
        int traceEvents = -1;
        System.out.println("Insert noise using traces and event percentage (0), or using number of Gaps and gap length (1)");
        while (traceEvents < 0 || traceEvents > 1){
            traceEvents = new Scanner(System.in).nextInt();
            System.out.println("Insert noise using traces and event percentage (0), or using number of Gaps and gap length (1)");
        }

        if(traceEvents == 0) {
            System.out.println("Insert percentage of events affected by noise");
            double percentageEvents = new Scanner(System.in).nextDouble();

            int totalVSunique = -1;
            while (totalVSunique < 0 || traceEvents > 1) {
                System.out.println("Insert noise using total number of traces (0) or unique number of traces (1)");
                totalVSunique = new Scanner(System.in).nextInt();
            }

            if(totalVSunique == 0) {
                System.out.println("Insert percentage of traces affected by noise");
                double percentageTraces = new Scanner(System.in).nextDouble();

                return timeStampNoiseGenerator.insertNoiseTotalTracesEvents(log, percentageTraces, percentageEvents);
            }else {
                System.out.println("Insert percentage of unique traces affected by noise");
                double percentageUniqueTraces = new Scanner(System.in).nextDouble();

                return timeStampNoiseGenerator.insertNoiseUniqueTracesEvents(log, percentageUniqueTraces, percentageEvents);
            }
        }else {
            System.out.println("Insert total gaps");
            double totalGaps = new Scanner(System.in).nextInt();
            System.out.println("Insert minimum gap length");
            double minGapLength = new Scanner(System.in).nextInt();
            System.out.println("Insert maximum gap length");
            double maxGapLength = new Scanner(System.in).nextInt();
            System.out.println("Insert average gap length");
            double averageGapLength = new Scanner(System.in).nextDouble();

            System.out.println("Insert minimum number gaps per trace");
            double minGapNumber = new Scanner(System.in).nextInt();
            System.out.println("Insert maximum number gaps per trace");
            double maxGapNumber = new Scanner(System.in).nextInt();
            System.out.println("Insert average number gaps per trace");
            double averageGapNumber = new Scanner(System.in).nextDouble();

            return timeStampNoiseGenerator.insertNoiseGapNumberAndLenght(log, totalGaps, minGapLength, maxGapLength, averageGapLength, minGapNumber, maxGapNumber, averageGapNumber);
        }
    }

}
