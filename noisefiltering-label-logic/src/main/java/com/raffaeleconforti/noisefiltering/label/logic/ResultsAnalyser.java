package com.raffaeleconforti.noisefiltering.label.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 19/10/17.
 */
public class ResultsAnalyser {

    private static File file;
    private static BufferedReader br;
    private static boolean nextLine = false;
    private static String line;

    public static void main(String[] args) {
//        file = new File("/Volumes/Data/Dropbox/LaTex/2017/LabelFiltering/IMFAlignment.txt");
//        file = new File("/Volumes/Data/Dropbox/LaTex/2017/LabelFiltering/IMFProjected.txt");
//        file = new File("/Volumes/Data/Dropbox/LaTex/2017/LabelFiltering/IMAlignment.txt");
//        file = new File("/Volumes/Data/Dropbox/LaTex/2017/LabelFiltering/IMProjected.txt");
        file = new File("/Volumes/Data/Dropbox/LaTex/2017/LabelFiltering/IMAlignment.txt");
//        file = new File("/Volumes/Data/Dropbox/LaTex/2017/LabelFiltering/HMProjected.txt");
//        file = new File("/Volumes/Data/Dropbox/LaTex/2017/LabelFiltering/SMAlignment.txt");
//        file = new File("/Volumes/Data/Dropbox/LaTex/2017/LabelFiltering/SMProjected.txt");

        List<String> infoFScores = new ArrayList<>();
        List<String> infoFitnesses = new ArrayList<>();
        List<String> infoPrecisions = new ArrayList<>();
        String[] infoFScore = new String[] {"Log", "Original", "95", "90", "85", "80", "75", "70", "Our", "Python"};
        String[] infoFitness = new String[] {"Log", "Original", "95", "90", "85", "80", "75", "70", "Our", "Python"};
        String[] infoPrecision = new String[] {"Log", "Original", "95", "90", "85", "80", "75", "70", "Our", "Python"};
        String oldLogName = "";
        int pos = 0;

        while(hasNextLine()) {
            String entry = nextLine();

            if(entry.startsWith("DEBUG") && entry.contains("log:")) {
                String relevant = entry.substring(entry.indexOf("log:") + 5);
                StringTokenizer stringTokenizer = new StringTokenizer(relevant, " ");
                String logName = stringTokenizer.nextToken();
                String algo = "original";
                if(entry.contains("(")) {
                    algo = stringTokenizer.nextToken();
                    if(!algo.contains(")")) algo = stringTokenizer.nextToken();
                    algo = algo.substring(0, algo.indexOf(")"));
                    if(algo.contains("Python")) algo = "Python";
                }else {
                    logName = logName.substring(0, logName.indexOf("."));
                }

                if(!logName.equals(oldLogName)) {
                    infoFitnesses.add(Arrays.toString(infoFitness).replace("[", "").replace("]", ""));
                    infoPrecisions.add(Arrays.toString(infoPrecision).replace("[", "").replace("]", ""));
                    infoFScores.add(Arrays.toString(infoFScore).replace("[", "").replace("]", ""));

                    infoFitness = new String[10];
                    infoFitness[0] = logName;
                    infoPrecision = new String[10];
                    infoPrecision[0] = logName;
                    infoFScore = new String[10];
                    infoFScore[0] = logName;
                    oldLogName = logName;
                }

                pos = pos(algo);
                String size = stringTokenizer.nextToken();
            }else if(entry.startsWith("Projected Recall : ") || entry.startsWith("(a)fitness : ")) {
                String relevant = entry.substring(entry.indexOf(" : ") + 3);
                infoFitness[pos] = relevant;
            }else if(entry.contains("Projected Precision : ") || entry.startsWith("(a)precision : ")) {
                String relevant = entry.substring(entry.indexOf(" : ") + 3);
                infoPrecision[pos] = relevant;
            }else if(entry.contains("Projected f-Measure : ") || entry.startsWith("DEBUG - Alignment-Based f-Measure(a)f-measure : ")) {
                String relevant = entry.substring(entry.indexOf(" : ") + 3);
                infoFScore[pos] = relevant;
            }
        }
        infoFitnesses.add(Arrays.toString(infoFitness).replace("[", "").replace("]", ""));
        infoPrecisions.add(Arrays.toString(infoPrecision).replace("[", "").replace("]", ""));
        infoFScores.add(Arrays.toString(infoFScore).replace("[", "").replace("]", ""));

        System.out.println("Fitness");
        for(String s : infoFitnesses) {
            System.out.println(s);
        }

        System.out.println();
        System.out.println("Precision");
        for(String s : infoPrecisions) {
            System.out.println(s);
        }

        System.out.println();
        System.out.println("F-Score");
        for(String s : infoFScores) {
            System.out.println(s.replaceAll(", ", "\t"));
        }
    }

    private static int pos(String s) {
        if(s.equals("original")) return 1;
        else if(s.equals("95")) return 2;
        else if(s.equals("90")) return 3;
        else if(s.equals("85")) return 4;
        else if(s.equals("80")) return 5;
        else if(s.equals("75")) return 6;
        else if(s.equals("70")) return 7;
        else if(s.equals("Bagging")) return 8;
        else if(s.equals("Python")) return 9;
        else return 0;
    }

    private static void init() {
        try {
            if(!file.exists()) {
                file.createNewFile();
            }
            br = new BufferedReader(new FileReader(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean hasNextLine() {
        if(br == null) init();
        try {
            line = br.readLine();
            nextLine = (line != null);
            return nextLine;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String nextLine() {
        if(nextLine) {
            nextLine = false;
            return line;
        }
        return null;
    }

}
