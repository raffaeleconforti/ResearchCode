package com.raffaeleconforti.noisefiltering.timestamp;

import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.log.util.NameExtractor;
import com.raffaeleconforti.log.util.TraceToString;
import com.raffaeleconforti.noisefiltering.timestamp.check.TimeStampChecker;
import com.raffaeleconforti.outliers.statistics.StatisticsSelector;
import com.raffaeleconforti.outliers.statistics.mean.Mean;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by conforti on 7/02/15.
 */

public class TimeStampFilterChecker {

    XConceptExtension xce = XConceptExtension.instance();
    XTimeExtension xte = XTimeExtension.instance();

    public static void main(String[] args) throws Exception {
        String path = "/Volumes/Data/Dropbox/LaTex/2016/Timestamp Repair/Logs/Experiments/";
        String logExtension = ".xes.gz";

        String[] typeLogs = new String[] {"Event", "Trace", "UniqueTrace"};
        String[] typeFilters = new String[] {" ILP", "D", "R"};

        String[] typeExperiments = new String[] {"0.05", "0.10", "0.15", "0.20", "0.25", "0.30", "0.35", "0.40"};

        TimeStampFilterChecker timeStampFilterChecker = new TimeStampFilterChecker();

        XLog correctLog = LogImporter.importFromFile(new XFactoryNaiveImpl(), path + "TimeExperimentSimulation.xes.gz");
        for(String typeLog : typeLogs) {
            Set<String> done = new HashSet<>();
            for(String typeFilter : typeFilters) {
                for(String typeExperiment : typeExperiments) {
                    XLog filteredLog = LogImporter.importFromFile(new XFactoryNaiveImpl(), path + typeLog + "/" + typeLog + typeExperiment + typeFilter + logExtension);
                    XLog noisyLog = LogImporter.importFromFile(new XFactoryNaiveImpl(), path + typeLog + "/" + typeLog + typeExperiment + logExtension);

                    String base = typeLog + typeExperiment;
                    if(!done.contains(base)) {
                        done.add(base);
                        String res = timeStampFilterChecker.check(noisyLog, noisyLog, correctLog).replaceAll("<html><p>", "").replaceAll("<br>", "\n").replaceAll("</p></html>", "");

                        FileWriter fileWriter = new FileWriter(path + typeLog + "/" + typeLog + typeExperiment + ".txt");
                        fileWriter.write(res);
                        fileWriter.close();

                        res = res.substring(res.indexOf("Levenshtein Distance"), res.indexOf("Total time error"));
                        System.out.println(base);
                        System.out.println(res);
                        System.out.println();
                    }

                    String res = timeStampFilterChecker.check(filteredLog, noisyLog, correctLog).replaceAll("<html><p>", "").replaceAll("<br>", "\n").replaceAll("</p></html>", "");

                    FileWriter fileWriter = new FileWriter(path + typeLog + "/" + typeLog + typeExperiment + typeFilter + ".txt");
                    fileWriter.write(res);
                    fileWriter.close();

                    res = res.substring(res.indexOf("Levenshtein Distance"), res.indexOf("Total time error"));
                    System.out.println(typeLog + typeExperiment + typeFilter);
                    System.out.println(res);
                    System.out.println();
                }
            }
        }
    }

    public String check(XLog filteredLog, XLog noisyLog, XLog correctLog) {
        int numberOfTracesAffected = 0;
        int numberOfEventsAffected = 0;

        int eventErrorIntroduced = 0;
        int traceErrorIntroduced = 0;

        int numberOfTracesAttempted = 0;

        int numberOfTracesCorrectlyChanged = 0;
        int numberOfEventsCorrectlyChanged = 0;

        int numberOfTracesWronglyChanged = 0;
        int numberOfEventsWronglyChanged = 0;

        Map<Integer, Integer> mapGapsPerTrace = new UnifiedMap<>();
        XEventClassifier xEventClassifier = new XEventAndClassifier(new XEventNameClassifier());
        TimeStampChecker timeStampChecker = new TimeStampChecker(xEventClassifier, new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"));

        List<Long> errors = new ArrayList<>();

        for(XTrace trace : noisyLog) {
            if (trace.getAttributes().get("change") != null) {
                numberOfTracesAffected++;
                for(XEvent event : trace) {
                    if (event.getAttributes().get("change") != null) {
                        numberOfEventsAffected++;
                    }
                }
            }
        }

        for(XTrace trace1 : filteredLog) {
            String traceID = xce.extractName(trace1);
            boolean matches = true;
            if (trace1.getAttributes().get("change") != null || numberOfTracesAffected == 0) {
                if (trace1.getAttributes().get("fixed") != null) {
                    numberOfTracesAttempted++;
                }

                for (XTrace trace2 : correctLog) {
                    if (xce.extractName(trace2).equals(traceID)) {

                        for (int i = 0; i < trace1.size(); i++) {
                            if (trace1.get(i).getAttributes().get("change") != null || numberOfTracesAffected == 0) {

                                if (!xce.extractName(trace1.get(i)).equals(xce.extractName(trace2.get(i)))) {
                                    matches = false;
                                    numberOfEventsWronglyChanged++;
                                }else {
                                    numberOfEventsCorrectlyChanged++;
                                }

                                if(trace1.get(i).getAttributes().get("originalTimeStamp") != null) {
                                    Date originalTimeStamp = ((XAttributeTimestamp) trace1.get(i).getAttributes().get("originalTimeStamp")).getValue();
                                    Date timestamp = xte.extractTimestamp(trace1.get(i));
                                    errors.add(timestamp.getTime() - originalTimeStamp.getTime());
                                }
                            }
                        }

                        if (matches) {
                            numberOfTracesCorrectlyChanged++;
                        } else {
                            numberOfTracesWronglyChanged++;

                            for (XTrace trace : noisyLog) {
                                if (xce.extractName(trace).equals(traceID)) {
                                    if (timeStampChecker.containsSameTimestamps(trace)) {
                                        Set<Set<XEvent>> sets = timeStampChecker.findEventsSameTimeStamp(trace);

                                        Integer count;
                                        for (Set<XEvent> set : sets) {
                                            if ((count = mapGapsPerTrace.get(set.size())) == null) {
                                                count = 0;
                                            }
                                            count++;
                                            mapGapsPerTrace.put(set.size(), count);
                                        }
                                    }
                                    break;
                                }
                            }
                        }

                        break;
                    }
                }
            }else if (trace1.getAttributes().get("fixed") != null) {
                for (XTrace trace2 : correctLog) {
                    if (xce.extractName(trace2).equals(traceID)) {
                        for (int i = 0; i < trace1.size(); i++) {
                            if (!xce.extractName(trace1.get(i)).equals(xce.extractName(trace2.get(i))) && !xte.extractTimestamp(trace1.get(i)).equals(xte.extractTimestamp(trace2.get(i)))) {
                                matches = false;
                                eventErrorIntroduced++;
                            }
                        }
                        if (!matches) {
                            traceErrorIntroduced++;
                        }
                        break;
                    }
                }
            }
        }

        NameExtractor nameExtractor = new NameExtractor(new XEventNameClassifier());
        double[] partialDistances = new double[filteredLog.size()];
        int totalDistance = 0;
        int counter = 0;
        int pos = 0;
        Map<String, String> nameMapper = new UnifiedMap<>();
        for(XTrace trace1 : filteredLog) {
            String traceID = xce.extractName(trace1);
            for (XTrace trace2 : correctLog) {
                if (xce.extractName(trace2).equals(traceID)) {
                    for(XEvent event : trace1) {
                        if(!nameMapper.containsKey(event)) {
                            nameMapper.put(nameExtractor.getEventName(event), "" + counter);
                            counter++;
                        }
                    }
                    String[] string1 = TraceToString.convertXTraceToListOfString(trace1, nameMapper, nameExtractor);
                    String[] string2 = TraceToString.convertXTraceToListOfString(trace2, nameMapper, nameExtractor);
                    partialDistances[pos] = getLevenshteinDistanceLinearSpace(string1, string2);
                    totalDistance += partialDistances[pos];
                    pos++;
                }
            }
        }

        double percentageOfTracesAttempted = (double) numberOfTracesAttempted/ (double) numberOfTracesAffected;

        double percentageOfTracesCorrectlyChanged = (double) numberOfTracesCorrectlyChanged/ (double) numberOfTracesAffected;
        double percentageOfTracesWronglyChanged = (double) numberOfTracesWronglyChanged/ (double) numberOfTracesAffected;

        double percentageOfEventsCorrectlyChanged = (double) numberOfEventsCorrectlyChanged/ (double) numberOfEventsAffected;
        double percentageOfEventsWronglyChanged = (double) numberOfEventsWronglyChanged/ (double) numberOfEventsAffected;


        double totalError = 0;
        for(Long error : errors) {
            totalError += Math.abs(error);
        }
        double averageError = totalError / errors.size();

        double totalSquareDifferenced = 0;
        for(Long error : errors) {
            totalSquareDifferenced += Math.pow(averageError - error, 2);
        }
        double stdDeviation = Math.sqrt(totalSquareDifferenced/ errors.size());

        StatisticsSelector statisticsSelector = new StatisticsSelector();

        String s =  "<html><p>Number of Noisy Traces: " + numberOfTracesAffected +
                    "<br>Number of Attempted Noisy Traces: " + numberOfTracesAttempted + //" (" + format(percentageOfTracesAttempted) + "%) " +
                    "<br>Number of Fixed Noisy Traces: " + numberOfTracesCorrectlyChanged + //" (" + format(percentageOfTracesCorrectlyChanged) + "%) " +
                    "<br>Number of Not Fixed Noisy Traces: " + numberOfTracesWronglyChanged + //" (" + format(percentageOfTracesWronglyChanged) + "%) " +
                    "<br>Number of Wrongly Fixed Traces: " + traceErrorIntroduced+ //" (" + format(percentageOfTracesWronglyChanged) + "%) " +

                    "<br>Number of Noisy Events: " + numberOfEventsAffected +
                    "<br>Number of Fixed Noisy Events: " + numberOfEventsCorrectlyChanged + //" (" + format(percentageOfEventsCorrectlyChanged) + "%) " +
                    "<br>Number of Not Fixed Noisy Events: " + numberOfEventsWronglyChanged + //" (" + format(percentageOfEventsWronglyChanged) + "%) " +
                    "<br>Number of Wrongly Fixed Events: " + eventErrorIntroduced +
                    "<br>Levenshtein Distance : " + totalDistance +
                    "<br>Min Levenshtein Distance : " + statisticsSelector.evaluate(StatisticsSelector.StatisticsMeasures.MIN, null, partialDistances) +
                    "<br>Max Levenshtein Distance : " + statisticsSelector.evaluate(StatisticsSelector.StatisticsMeasures.MAX, null, partialDistances) +
                    "<br>Average Levenshtein Distance : " + truncate(statisticsSelector.evaluate(StatisticsSelector.StatisticsMeasures.MEAN, null, partialDistances)) +
                    "<br>Sd Levenshtein Distance : " + truncate(statisticsSelector.evaluate(StatisticsSelector.StatisticsMeasures.SD, null, partialDistances)) +

                    "<br>Total time error: " + format(totalError) +
                    "<br>Average time error: " + format(averageError) +
                    "<br>Std Deviation time error: " + format(stdDeviation);

        s += "<br>GapSize and Frequencies:";
        for(Map.Entry<Integer, Integer> entry : mapGapsPerTrace.entrySet()) {
            s += "<br>" + entry.getKey() + " : " + entry.getValue();
        }

        s += "</p></html>";

        return s;
    }

    private String truncate(double value) {
        DecimalFormat df = new DecimalFormat("#.###");
        return df.format(value);
    }

    private String format(double value) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(value * 100);
    }

    public int getLevenshteinDistanceLinearSpace(String[] seq1, String[] seq2) {
        int lengthSeq1 = seq1.length;
        int lengthSeq2 = seq2.length;
        int[] S = new int[lengthSeq2 + 1];
        S[0] = 0;
        S[1] = S[0] + 1;

        int s;
        for(s = 2; s <= lengthSeq2; ++s) {
            S[s] = S[s - 1] + 1;
        }

        for(int i = 1; i <= lengthSeq1; ++i) {
            s = S[0];
            int c;
            S[0] = c = S[0] + 1;
            String[] sI = Arrays.copyOfRange(seq1, (i - 1), i);

            for(int j = 1; j <= lengthSeq2; ++j) {
                String[] tJ = Arrays.copyOfRange(seq2, (j - 1), j);
                byte cost = 0;
                if(!Arrays.equals(sI, tJ)) {
                    cost = 1;
                }

                c = this.Minimum(S[j] + 1, s + cost, c + 1);
                s = S[j];
                S[j] = c;
            }
        }

        return S[lengthSeq2];
    }

    private int Minimum(int a, int b, int c) {
        int mi = a;
        if(b < a) {
            mi = b;
        }

        if(c < mi) {
            mi = c;
        }

        return mi;
    }


}
