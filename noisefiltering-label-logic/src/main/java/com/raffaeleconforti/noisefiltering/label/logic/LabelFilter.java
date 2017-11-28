package com.raffaeleconforti.noisefiltering.label.logic;

import com.raffaeleconforti.automaton.Automaton;
import com.raffaeleconforti.automaton.AutomatonFactory;
import com.raffaeleconforti.log.util.*;
import com.raffaeleconforti.statistics.StatisticsSelector;
import com.raffaeleconforti.statistics.StatisticsSelector.StatisticsMeasures;
import org.apache.commons.lang3.ArrayUtils;
import org.deckfour.xes.classification.*;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.api.iterator.IntIterator;
import org.eclipse.collections.api.iterator.MutableIntIterator;
import org.eclipse.collections.api.tuple.primitive.IntDoublePair;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.*;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.json.JSONObject;
import org.python.util.PythonInterpreter;

import javax.script.*;
import java.io.*;
import java.util.*;

import static java.io.FileDescriptor.out;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 14/11/16.
 */
public class LabelFilter {
    private static final XConceptExtension xce = XConceptExtension.instance();
    private final XEventClassifier xEventClassifier;

    private int label_removed;

    private final StatisticsSelector statisticsSelector = new StatisticsSelector();

    private final ObjectIntHashMap<String> stringToIntMap = new ObjectIntHashMap<>();
    private final IntObjectHashMap<String> intToStringMap = new IntObjectHashMap<>();

    private int events = 1;
    public static String testName = "(Python)";

    private final int CoD = 0;
    private final int CoV = 1;
    private final int IoD = 2;
    private final int M = 3;
    private final int QCoD = 4;
    private final int QM = 5;
    private final int N = 6;

    private static String logName;

    public static void main(String[] args) throws Exception {
        XFactory factory = new XFactoryNaiveImpl();
//        String[] logNames = new String[] {"Sepsis"};
        String[] logNames = new String[] {"BPI2011", "BPI2012", "BPI2013_cp", "BPI2013_i", "BPI2014", "BPI2015-1", "BPI2015-2", "BPI2015-3", "BPI2015-4", "BPI2015-5", "BPI2017", "Road", "Sepsis"};

        for(int i = logNames.length - 1; i >= 0; i--) {
            logName = logNames[i];
            XLog log = LogImporter.importFromFile(factory, "/Volumes/Data/SharedFolder/Logs/Label/" + logName + ".xes.gz");
//            LogModifier logModifier = new LogModifier(factory, XConceptExtension.instance(), XTimeExtension.instance(), new LogOptimizer());
//            logModifier.insertArtificialStartAndEndEvent(log);

            LabelFilter labelFilter = new LabelFilter(
//                    new XEventAndClassifier(new XEventNameClassifier()));
                    new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier()));

            log = labelFilter.filterLog(log);

            xce.assignName(log, xce.extractName(log) + " (Label)");

            if (labelFilter.label_removed > 0) {
//                log = logModifier.removeArtificialStartAndEndEvent(log);
                LogImporter.exportToFile("/Volumes/Data/SharedFolder/Logs/Label/" + logName + " " + testName + ".xes.gz", log);
            }
        }
    }

    private List<IntArrayList> codeLog(XLog log) {
        List<IntArrayList> codedLog = new ArrayList<>(log.size());
        for(XTrace trace : log) {
            IntArrayList list = new IntArrayList(trace.size());
            for(XEvent event : trace) {
                String name = xEventClassifier.getClassIdentity(event);
                Integer value;
                if((value = stringToIntMap.get(name)) > 0) {
                    list.add(value);
                }else {
                    stringToIntMap.put(name, events);
                    intToStringMap.put(events, name);
                    list.add(events);
                    events++;
                }
            }
            codedLog.add(list);
        }
        return codedLog;
    }

    public LabelFilter(XEventClassifier xEventClassifier) {
        this.xEventClassifier = xEventClassifier;
    }

    public XLog filterLog(XLog log) {
        Map<String, Integer> map1 = new HashMap<>();
        Map<String, Integer> map2 = new HashMap<>();
        for(XTrace trace : log) {
            Set<String> set = new HashSet<>();
            for(XEvent event : trace) {
                String name = xEventClassifier.getClassIdentity(event);
                set.add(name);
                Integer count;
                if((count = map1.get(name)) == null) {
                    count = 0;
                }
                count++;
                map1.put(name, count);
            }
            for(String name : set) {
                Integer count;
                if((count = map2.get(name)) == null) {
                    count = 0;
                }
                count++;
                map2.put(name, count);
            }
        }

        List<String> arguments = new ArrayList<>();
        arguments.add("python3.6");
        arguments.add("/Volumes/Data/IdeaProjects/ResearchCodePublic/noisefiltering-label-logic/src/main/java/com/raffaeleconforti/noisefiltering/label/logic/label_filter.py");
        for(String name : map1.keySet()) {
            arguments.add(name);
            arguments.add("" + map1.get(name));
        }
        List<String> labels1 = callPythonScript(arguments);

        arguments = new ArrayList<>();
        arguments.add("python3.6");
        arguments.add("/Volumes/Data/IdeaProjects/ResearchCodePublic/noisefiltering-label-logic/src/main/java/com/raffaeleconforti/noisefiltering/label/logic/label_filter.py");
        for(String name : map2.keySet()) {
            arguments.add(name);
            arguments.add("" + map2.get(name));
        }
        List<String> labels2 = callPythonScript(arguments);

        Set<String> toremove = new HashSet<>();
        for(String s : labels1) {
            if(labels2.contains(s)) {
                toremove.add(s);
            }
        }
//        toremove.addAll(labels1);
//        toremove.addAll(labels2);

        this.label_removed = toremove.size();
        XFactory factory = new XFactoryNaiveImpl();
        XLog filteredLog = factory.createLog(log.getAttributes());
        for(XTrace trace : log) {
            XTrace filteredTrace = factory.createTrace(trace.getAttributes());
            for(XEvent event : trace) {
                if (!toremove.contains(xEventClassifier.getClassIdentity(event))) {
                    filteredTrace.add(event);
                }
            }
            filteredLog.add(filteredTrace);
        }
        return filteredLog;
    }

    private List<String> callPythonScript(List<String> arguments) {
        List<String> labels = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder().command(arguments);
            pb.redirectErrorStream(true);
            pb.redirectInput();

            Process p = pb.start();

            InputStream stdout = new BufferedInputStream(p.getInputStream());

            Scanner scanner = new Scanner(stdout);
            boolean record = false;
            while (scanner.hasNextLine()) {
                System.out.println();
                String result = scanner.nextLine();
                StringTokenizer st = new StringTokenizer(result, "[',]");
                while(st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if(!token.equals(" "))
                        if(token.equals(" ok ")) record = true;
                        else if(record) {
                            labels.add(token);
                            System.out.println(token);
                        }else {
                            System.out.println(token);
                        }
                }
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return labels;
    }

    public XLog filterLog2(XLog log) {
        List<IntArrayList> convertedLog = codeLog(log);
        IntHashSet removed = new IntHashSet();
        IntHashSet save = new IntHashSet();

        IntDoubleHashMap activityCountSingle1 = new IntDoubleHashMap();
        IntDoubleHashMap activityCountSingle2 = new IntDoubleHashMap();
        IntDoubleHashMap activityCountSingle3 = new IntDoubleHashMap();
        IntDoubleHashMap activityCount1 = new IntDoubleHashMap();
        IntDoubleHashMap activityCount2 = new IntDoubleHashMap();
        IntDoubleHashMap activityCount3 = new IntDoubleHashMap();
        IntDoubleHashMap activityProd = new IntDoubleHashMap();

        for(IntArrayList trace : convertedLog) {
            IntHashSet visited = new IntHashSet();
            IntDoubleHashMap activityRepetitions = new IntDoubleHashMap();

            for(int i : trace.toArray()) {
                if(!visited.contains(i)) {
                    visited.add(i);
                    activityCountSingle1.addToValue(i, 1);
                    activityCountSingle2.addToValue(i, 1);
                    activityCountSingle3.addToValue(i, 1);
                }
                activityCount1.addToValue(i, 1);
                activityCount2.addToValue(i, 1);
                activityCount3.addToValue(i, 1);
                activityRepetitions.addToValue(i, 1);
            }
        }
        for(int activity : activityCountSingle1.keySet().toArray()) {
            activityProd.put(activity, (activityCount1.get(activity) / activityCountSingle1.get(activity)));
        }

        double cut_A = activityCount1.average();
        double cut_single_A = activityCountSingle1.average();
        double cut_M = findCut(activityCount1.keyValuesView().toList(), M);
        double cut_single_M = findCut(activityCountSingle1.keyValuesView().toList(), M);

        for(int activity : activityCountSingle1.keySet().toArray()) {
            if (activityCountSingle1.get(activity) == convertedLog.size()) {
                activityCountSingle1.remove(activity);
                activityCount1.remove(activity);
                activityCountSingle2.remove(activity);
                activityCount2.remove(activity);
                save.add(activity);
                System.out.println("Saved because in every trace: " + intToStringMap.get(activity));
            }
        }

        cut_A = activityCount1.average();
        cut_single_A = activityCountSingle1.average();
        cut_M = findCut(activityCount1.keyValuesView().toList(), M);
        cut_single_M = findCut(activityCountSingle1.keyValuesView().toList(), M);

        for(int activity : activityCountSingle2.keySet().toArray()) {
            if(activityCountSingle2.get(activity) <= cut_single_M && activityCount2.get(activity) > cut_A){
                System.out.println("Saved because Exceptional A: " + intToStringMap.get(activity) + " - in " + activityCount2.get(activity) + " traces, executed " + activityCountSingle2.get(activity) + " times");
                activityCount2.remove(activity);
                activityCountSingle2.remove(activity);
                save.add(activity);
            }
            else
            if(activityCountSingle2.get(activity) > cut_single_A && activityCount2.get(activity) <= cut_M) {
                System.out.println("Saved because Exceptional B: " + intToStringMap.get(activity) + " - in " + activityCount2.get(activity) + " traces, executed " + activityCountSingle2.get(activity) + " times");
                activityCount2.remove(activity);
                activityCountSingle2.remove(activity);
                save.add(activity);
            }
        }

//        cut_M = activityCount2.average();
//        cut_single_M = activityCountSingle2.average();
//        cut_M = findCut(activityCount2.keyValuesView().toList(), M);
//        cut_single_M = findCut(activityCountSingle2.keyValuesView().toList(), M);

        for(int activity : activityCountSingle2.keySet().toArray()) {
            int count = 0;

            if(activityCount3.get(activity) <= cut_M) {
                count++;
            }

            if(activityCountSingle3.get(activity) <= cut_single_M) {
                count++;
            }

            if(count > 0) {
//                if (save.contains(activity)) {
//                    count--;
//                    System.out.println("Removable but saved: " + intToStringMap.get(activity) + " - in " + activityCount3.get(activity) + " traces, executed " + activityCountSingle3.get(activity) + " times");
//                }else {
                    removed.add(activity);
                    System.out.println("Removed: " + intToStringMap.get(activity) + " - in " + activityCount3.get(activity) + " traces, executed " + activityCountSingle3.get(activity) + " times");
//                }
            }
        }

        IntIntHashMap map = removeInfrequentLabels(convertedLog, removed);

        IntIterator intIterator = map.keySet().intIterator();
        int highest = 0;
        while(intIterator.hasNext()) {
            int label = intIterator.next();
            String labelName = intToStringMap.get(label);
            highest = Math.max(highest, map.get(label));
//            System.out.println("Infrequent: " + labelName + " removed " + map.get(label) + " times");
        }

        label_removed = removed.size();

        System.out.println("Log " + logName);
        System.out.println("Highest " + highest);
        System.out.println("Removed " + label_removed);

        log = matchLogs(log, removed);
        return log;
    }

    private double findCut(List<IntDoublePair> list, int measure) {
        double[] full_list = new double[list.size()];
        for(int i = 0; i < list.size(); i ++) {
            full_list[i] = list.get(i).getTwo();
        }
        Arrays.sort(full_list);
        ArrayUtils.reverse(full_list);

        int top = findBestTop(full_list, measure);
        return full_list[top];
    }

    private int findBestTop(double[] full_list, int measure) {
        double best_qcd = Double.MAX_VALUE;
        int best_top = 0;

        double[] log_normal_full_list = new double[full_list.length];
        for(int i = 0; i < full_list.length; i++) {
            log_normal_full_list[i] = full_list[i];//Math.log10(full_list[i]);
        }

        for(int j = 0; j < log_normal_full_list.length; j++) {
            while(j < log_normal_full_list.length - 1 && log_normal_full_list[j] == log_normal_full_list[j + 1]) {
                j++;
            }

            double[] current_top_list = Arrays.copyOfRange(log_normal_full_list, 0, j + 1);
            double[] current_bottom_list = Arrays.copyOfRange(log_normal_full_list, j + 1, log_normal_full_list.length);

            double qcd = computeDistance(current_top_list, current_bottom_list, measure);
            if (qcd <= best_qcd) {
                best_qcd = qcd;
                best_top = j;
            }
        }

        return (best_top < log_normal_full_list.length - 1) ? best_top + 1 : best_top;
    }

    private double computeDistance(double[] list1, double[] list2, int measure) {
        double mean_1 = statisticsSelector.evaluate(StatisticsMeasures.MEAN, null, list1);
//        double standard_deviation_1 = statisticsSelector.evaluate(StatisticsMeasures.SD, mean_1, list1);
//        double coefficient_of_variation_1 = standard_deviation_1 / mean_1;
//
        double mean_2 = statisticsSelector.evaluate(StatisticsMeasures.MEAN, null, list2);
//        double standard_deviation_2 = statisticsSelector.evaluate(StatisticsMeasures.SD, mean_2, list2);
//        double coefficient_of_variation_2 = standard_deviation_2 / mean_2;
//
//
//        double index_of_dispersion_1 = Math.pow(standard_deviation_1, 2) / mean_1;
//        double index_of_dispersion_2 = Math.pow(standard_deviation_2, 2) / mean_2;
//
//
        double median_1 = statisticsSelector.evaluate(StatisticsMeasures.MEDIAN, null, list1);
        double median_absolute_deviation_1 = statisticsSelector.evaluate(StatisticsMeasures.MAD, median_1, list1);
        double coefficient_of_dispersion_1 = median_absolute_deviation_1 / median_1;

        double median_2 = statisticsSelector.evaluate(StatisticsMeasures.MEDIAN, null, list2);
        double median_absolute_deviation_2 = statisticsSelector.evaluate(StatisticsMeasures.MAD, median_2, list2);
        double coefficient_of_dispersion_2 = median_absolute_deviation_2 / median_2;
//
//
//        double quartile3_1 = statisticsSelector.evaluate(StatisticsMeasures.PERCENTILE, 0.75, list1);
//        double quartile1_1 = statisticsSelector.evaluate(StatisticsMeasures.PERCENTILE, 0.25, list1);
//        double quartile_coefficient_of_dispersion_1 = (quartile3_1 - quartile1_1) / (quartile3_1 + quartile1_1);
//
//        double quartile3_2 = statisticsSelector.evaluate(StatisticsMeasures.PERCENTILE, 0.75, list2);
//        double quartile1_2 = statisticsSelector.evaluate(StatisticsMeasures.PERCENTILE, 0.25, list2);
//        double quartile_coefficient_of_dispersion_2 = (quartile3_2 - quartile1_2) / (quartile3_2 + quartile1_2);


        double max_1 = statisticsSelector.evaluate(StatisticsMeasures.MAX, null, list1);
        double min_1 = statisticsSelector.evaluate(StatisticsMeasures.MIN, null, list1);
        double max_2 = statisticsSelector.evaluate(StatisticsMeasures.MAX, null, list2);
        double min_2 = statisticsSelector.evaluate(StatisticsMeasures.MIN, null, list2);


//        double[] list = new double[list1.length + list2.length];
//        for(int i = 0; i < list1.length; i++) list[i] = list1[i];
//        for(int i = 0; i < list2.length; i++) list[i + list1.length] = list2[i];
//        double mean = statisticsSelector.evaluate(StatisticsMeasures.MEAN, null, list);
//        double standard_deviation = statisticsSelector.evaluate(StatisticsMeasures.SD, mean, list);
//        double snr = mean / standard_deviation;
//        double snr_1 = mean_1 / standard_deviation_1;
//        double snr_2 = mean_2 / standard_deviation_2;
//        double noise = Double.MAX_VALUE;
//        double avg_noise = (snr_1 + snr_2) / 2;//((snr_1 * list1.length / list.length) + (snr_2 * list2.length / list.length)) / 2;
//        if(standard_deviation_1 > 0 && standard_deviation_2 > 0 && snr_1 > 1 && snr_2 > 1 && snr < snr_1) noise = -avg_noise;



        double coefficient_of_dispersion = (coefficient_of_dispersion_1 + coefficient_of_dispersion_2) / 2;
//        double coefficient_of_variation = (coefficient_of_variation_1 + coefficient_of_variation_2) / 2;
//        double index_of_dispersion = (index_of_dispersion_1 + index_of_dispersion_2) / 2;
        double m = - ((min_1 - max_2) / (mean_1 - mean_2));
//        double quartile_coefficient_of_dispersion = (quartile_coefficient_of_dispersion_1 + quartile_coefficient_of_dispersion_2) / 2;
//        double qm = 1 - (Math.abs((Math.abs(quartile3_1 + quartile1_1) / 2) - (Math.abs(quartile3_2 + quartile1_2) / 2)) / (max_1 - min_2));

        if(measure == CoD) return coefficient_of_dispersion;
//        else if(measure == CoV) return coefficient_of_variation;
//        else if(measure == IoD) return index_of_dispersion;
        else if(measure == M) return m;
//        else if(measure == N) return noise;
//        else if(measure == QCoD) return quartile_coefficient_of_dispersion;
//        else if(measure == QM) return qm;
        else return 0;
    }

    private XLog matchLogs(XLog log, IntHashSet removed) {
        for (XTrace trace : log) {
            trace.removeIf(xEvent -> removed.contains(stringToIntMap.get(xEventClassifier.getClassIdentity(xEvent))));
        }
        return log;
    }

    private IntIntHashMap removeInfrequentLabels(List<IntArrayList> log, IntHashSet infrequentLabels) {
        IntIntHashMap map = new IntIntHashMap();
        for (IntArrayList trace : log) {
            MutableIntIterator iterator = trace.intIterator();
            while (iterator.hasNext()) {
                int label = iterator.next();
                if (infrequentLabels.contains(label)) {
                    iterator.remove();
                    int val = map.get(label);
                    val++;
                    map.put(label, val);
                }
            }
        }
        return map;
    }

}
