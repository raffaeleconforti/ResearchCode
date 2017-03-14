package com.raffaeleconforti.noisefiltering.label.logic;

import com.raffaeleconforti.automaton.Automaton;
import com.raffaeleconforti.automaton.AutomatonFactory;
import com.raffaeleconforti.automaton.Edge;
import com.raffaeleconforti.automaton.Node;
import com.raffaeleconforti.log.util.*;
import com.raffaeleconforti.statistics.StatisticsSelector;
import org.deckfour.xes.classification.*;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.api.iterator.IntIterator;
import org.eclipse.collections.api.iterator.MutableIntIterator;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectDoubleHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.processmining.framework.util.Pair;
import org.processmining.plugins.log.logabstraction.LogRelations;
import org.processmining.plugins.log.logabstraction.factories.LogRelationsFactory;

import java.util.*;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 14/11/16.
 */
public class LabelFilter {
    private static final XConceptExtension xce = XConceptExtension.instance();
    private final AutomatonFactory automatonFactory;
    private Automaton<Integer> automaton;
    private final XEventClassifier xEventClassifier;

    private double event_distance = 0.674490;
    private double trace_distance = 0.674490;
    private int label_removed;

    private final StatisticsSelector statisticsSelector = new StatisticsSelector();

    private final ObjectDoubleHashMap traceFrequencyMap = new ObjectDoubleHashMap();
    private final ObjectDoubleHashMap uniqueTraceFrequencyMap = new ObjectDoubleHashMap();
//    private final ObjectDoubleHashMap minimizedTraceFrequencyMap = new ObjectDoubleHashMap();
//    private final ObjectDoubleHashMap minimizedUniqueTraceFrequencyMap = new ObjectDoubleHashMap();

    private final Set<String> unique_traces = new UnifiedSet<>();
//    private final Set<String> minimized_unique_traces = new UnifiedSet<>();

    private final ObjectIntHashMap<String> stringToIntMap = new ObjectIntHashMap<>();
    private final IntObjectHashMap<String> intToStringMap = new IntObjectHashMap<>();

//    private final UnifiedMap<IntArrayList, IntArrayList> minimized_traces = new UnifiedMap<>();

    private int events = 1;

    private StatisticsMeasures center = StatisticsMeasures.MEAN;
    private StatisticsMeasures left_distance_from_center = StatisticsMeasures.LEFT_SD;
    private StatisticsMeasures right_distance_from_center = StatisticsMeasures.RIGHT_SD;

    private double upperlimit_events_frequency_in_log = 0.66;
    private double lowerlimit_events_frequency_in_log = 0.33;

    private double upperlimit_events_frequency_in_trace = 0.66;
    private double lowerlimit_events_frequency_in_trace = 0.33;

    private double upperlimit_events_frequency_in_unique_trace = 0.66;
    private double lowerlimit_events_frequency_in_unique_trace = 0.33;

    private double upperlimit_product = 0.66;
    private double lowerlimit_product = 0.33;

//    private double upperlimit_events_frequency_in_minimized_trace = 0.66;
//    private double lowerlimit_events_frequency_in_minimized_trace = 0.33;
//
//    private double upperlimit_events_frequency_in_minimized_unique_trace = 0.66;
//    private double lowerlimit_events_frequency_in_minimized_unique_trace = 0.33;

    private double mean_events_frequency_in_log = 0;
    private double left_sd_events_frequency_in_log = 0;
    private double right_sd_events_frequency_in_log = 0;

    private double mean_events_frequency_in_traces = 0;
    private double left_sd_events_frequency_in_traces = 0;
    private double right_sd_events_frequency_in_traces = 0;

    private double mean_events_frequency_in_unique_trace = 0;
    private double left_sd_events_frequency_in_unique_trace = 0;
    private double right_sd_events_frequency_in_unique_trace = 0;

    private double mean_product = 0;
    private double left_sd_product = 0;
    private double right_sd_product = 0;

//    private double mean_events_frequency_in_minimized_traces = 0;
//    private double left_sd_events_frequency_in_minimized_traces = 0;
//    private double right_sd_events_frequency_in_minimized_traces = 0;
//
//    private double mean_events_frequency_in_minimized_unique_trace = 0;
//    private double left_sd_events_frequency_in_minimized_unique_trace = 0;
//    private double right_sd_events_frequency_in_minimized_unique_trace = 0;


    public static void main(String[] args) throws Exception {
        XFactory factory = new XFactoryNaiveImpl();
//        String[] logNames = new String[] {"BPI2012"};
        String[] logNames = new String[] {"BPI2011", "BPI2012", "BPI2014", "BPI2015-1", "BPI2015-2", "BPI2015-3", "BPI2015-4", "BPI2015-5", "Road"};
        for(String logName : logNames) {
            XLog log = LogImporter.importFromFile(factory, "/Volumes/Data/SharedFolder/Logs/Label/" + logName + ".xes.gz");
            LogModifier logModifier = new LogModifier(factory, XConceptExtension.instance(), XTimeExtension.instance(), new LogOptimizer());
            logModifier.insertArtificialStartAndEndEvent(log);

            LabelFilter labelFilter = new LabelFilter(
                    new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier()));

            log = labelFilter.filterLog(log);

            xce.assignName(log, xce.extractName(log) + " (Label)");

            if (labelFilter.label_removed > 0) {
                log = logModifier.removeArtificialStartAndEndEvent(log);
                LogImporter.exportToFile("/Volumes/Data/SharedFolder/Logs/Label/" + logName + " (Label).xes.gz", log);
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

    private XLog decodeLog(List<IntArrayList> log) {
        XFactory xFactory = new XFactoryNaiveImpl();
        XLog decodedLog = xFactory.createLog();
        for(IntArrayList trace : log) {
            XTrace decodedTrace = xFactory.createTrace();
            for(int event : trace.toArray()) {
                XEvent decodedEvent = xFactory.createEvent();
                xEventClassifier.setName(intToStringMap.get(event));
                decodedTrace.add(decodedEvent);
            }
            decodedLog.add(decodedTrace);
        }
        return decodedLog;
    }

    public LabelFilter(XEventClassifier xEventClassifier) {
        this.xEventClassifier = xEventClassifier;
        automatonFactory = new AutomatonFactory(xEventClassifier);
    }

//    public XLog filterLog(XLog log) {
//        List<IntArrayList> convertedLog = codeLog(log);
//        IntHashSet removed = new IntHashSet();
//        IntHashSet removedTotal = new IntHashSet();
//
//        IntHashSet infrequentLabels = identifySuperInfrequentLabels(convertedLog);
//        IntIntHashMap map = removeSuperInfrequentLabels(convertedLog, infrequentLabels);
//        removed.addAll(infrequentLabels);
//        removedTotal.addAll(removed);
//
//        IntIterator intIterator = map.keySet().intIterator();
//        while(intIterator.hasNext()) {
//            int label = intIterator.next();
//            String labelName = intToStringMap.get(label);
//            System.out.println("Infrequent: " + labelName + " removed " + map.get(label) + " times");
//        }
//
//        do {
//            removed.clear();
//            Integer label = identifyLabel(convertedLog);
//            while (label != null) {
//                removed.add(label);
//                int rem = 0;
//                for (IntArrayList trace : convertedLog) {
//                    MutableIntIterator iterator = trace.intIterator();
//                    while (iterator.hasNext()) {
//                        if (label == iterator.next()) {
//                            iterator.remove();
//                            rem++;
//                        }
//                    }
//                }
//                System.out.println(intToStringMap.get(label) + " removed " + rem + " times");
//                label = identifyLabel(convertedLog);
//            }
//
//            removedTotal.addAll(removed);
//            label_removed = removedTotal.size();
//            System.out.println("Removed " + label_removed);// + " out of " + candidates.size());
//        }while (removed.size() > 0);
//
//        log = matchLogs(log, removedTotal);
//        return log;
//    }

    public XLog filterLog(XLog log) {
        List<IntArrayList> convertedLog = codeLog(log);
        IntHashSet removed = new IntHashSet();
        IntHashSet removedTotal = new IntHashSet();

        IntHashSet infrequentLabels = identifySuperInfrequentLabels(convertedLog);
        IntIntHashMap map = removeSuperInfrequentLabels(convertedLog, infrequentLabels);
        removed.addAll(infrequentLabels);
        removedTotal.addAll(removed);

        IntIterator intIterator = map.keySet().intIterator();
        while(intIterator.hasNext()) {
            int label = intIterator.next();
            String labelName = intToStringMap.get(label);
            System.out.println("Infrequent: " + labelName + " removed " + map.get(label) + " times");
        }

        IntHashSet labels = identifyLabels(convertedLog);
        for (IntArrayList trace : convertedLog) {
            MutableIntIterator iterator = trace.intIterator();
            while (iterator.hasNext()) {
                if (labels.contains(iterator.next())) {
                    iterator.remove();
                }
            }
        }

        removedTotal.addAll(labels);
        label_removed = removedTotal.size();
        System.out.println("Removed " + label_removed);

        log = matchLogs(log, removedTotal);
        return log;
    }

    private XLog matchLogs(XLog log, IntHashSet removed) {
        for (XTrace trace : log) {
            trace.removeIf(xEvent -> removed.contains(stringToIntMap.get(xEventClassifier.getClassIdentity(xEvent))));
        }
        return log;
    }

    private IntHashSet identifySuperInfrequentLabels(List<IntArrayList> log) {
        automaton = automatonFactory.generate(log);
        Set<Node<Integer>> nodes = automaton.getNodes();
        IntHashSet labels = new IntHashSet();

        Comparator<Integer> comparator = createComparator(log);
        Set<String> traces = new UnifiedSet<>();
        for(IntArrayList list : log) {
            traces.add(TraceToString.listToString(list, comparator));
        }

        discoverLogStatistics(log);
        discoverAverages();

        double min_events_in_log = lowerlimit_events_frequency_in_log * mean_events_frequency_in_log;
        double min_events_in_trace = lowerlimit_events_frequency_in_trace * mean_events_frequency_in_traces;
        double min_events_in_unique_trace = lowerlimit_events_frequency_in_unique_trace * mean_events_frequency_in_unique_trace;
//        double min_events_in_minimized_trace = lowerlimit_events_frequency_in_minimized_trace * mean_events_frequency_in_minimized_traces;
//        double min_events_in_minimized_unique_trace = lowerlimit_events_frequency_in_minimized_unique_trace * mean_events_frequency_in_minimized_unique_trace;

//        double node_threshold = Math.min(Math.min(min_events_in_log, min_events_in_trace), min_events_in_unique_trace);
        double node_threshold = lowerlimit_events_frequency_in_trace * lowerlimit_events_frequency_in_unique_trace * Math.min(mean_events_frequency_in_traces, mean_events_frequency_in_unique_trace);

        for (Node<Integer> node : nodes) {
            if(automaton.getNodeFrequency(node) < node_threshold) {
//                labels.add(node.getData());
            }
        }

        return labels;
    }

    private IntIntHashMap removeSuperInfrequentLabels(List<IntArrayList> log, IntHashSet infrequentLabels) {
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

    private void discoverLogStatistics(List<IntArrayList> log) {
        traceFrequencyMap.clear();
        uniqueTraceFrequencyMap.clear();
        unique_traces.clear();
//        minimizedTraceFrequencyMap.clear();
//        minimizedUniqueTraceFrequencyMap.clear();
//        minimized_unique_traces.clear();

        Comparator<Integer> comparator = createComparator(log);

        IntHashSet visited = new IntHashSet();
        IntHashSet uniqueVisited = new IntHashSet();
        IntHashSet minimized_visited = new IntHashSet();
        IntHashSet minimized_uniqueVisited = new IntHashSet();
        String t, minimized_t;
        for (IntArrayList trace : log) {
            visited.clear();
            uniqueVisited.clear();
            minimized_visited.clear();
            minimized_uniqueVisited.clear();

            t = TraceToString.listToString(trace, comparator);
//            IntArrayList minimized_trace = minimizeTrace(trace);
//            minimized_t = TraceToString.listToString(minimized_trace, comparator);

            boolean justAdded = false;
//            boolean justAddedMinimized = false;
            if(unique_traces.add(t)) {
                justAdded = true;
            }
//            if(minimized_unique_traces.add(minimized_t)) {
//                justAddedMinimized = true;
//            }

            Integer nameCurrent = null;
            for (int i = 1; i < trace.size() - 1; i++) {
                Integer tmpCurrent = trace.get(i);
                if(nameCurrent == null || !nameCurrent.equals(tmpCurrent)) {
                    nameCurrent = tmpCurrent;

                    if (visited.add(nameCurrent)) {
                        traceFrequencyMap.put(nameCurrent, traceFrequencyMap.get(nameCurrent) + 1);
                    }
                    if (justAdded) {
                        if (uniqueVisited.add(nameCurrent)) {
                            uniqueTraceFrequencyMap.put(nameCurrent, uniqueTraceFrequencyMap.get(nameCurrent) + 1);
                        }
                    }
                }
            }

//            nameCurrent = null;
//            for (int i = 1; i < minimized_trace.size() - 1; i++) {
//                Integer tmpCurrent = minimized_trace.get(i);
//                if(nameCurrent == null || !nameCurrent.equals(tmpCurrent)) {
//                    nameCurrent = tmpCurrent;
//
//                    if (minimized_visited.add(nameCurrent)) {
//                        minimizedTraceFrequencyMap.put(nameCurrent, minimizedTraceFrequencyMap.get(nameCurrent) + 1);
//                    }
//                    if (justAddedMinimized) {
//                        if (minimized_uniqueVisited.add(nameCurrent)) {
//                            minimizedUniqueTraceFrequencyMap.put(nameCurrent, minimizedUniqueTraceFrequencyMap.get(nameCurrent) + 1);
//                        }
//                    }
//                }
//            }
        }
    }

    private Comparator<Integer> createComparator(List<IntArrayList> log) {
        IntObjectHashMap<IntHashSet> parallel = discoverParallelRelations(log);

        return new Comparator<Integer>() {
            final IntObjectHashMap cache = new IntObjectHashMap();
            @Override
            public int compare(Integer o1, Integer o2) {
                int val = codeTwoEvents(o1, o2);
                Integer res;
                if((res = (Integer) cache.get(val)) != null) {
                    return res;
                }else {
                    IntHashSet concurrent;
                    if ((concurrent = parallel.get(o1)) != null) {
                        if (concurrent.contains(o2)) {
                            res = o1.compareTo(o2);
                            cache.put(val, res);
                            return res;
                        }
                    }
                    cache.put(val, 0);
                    return 0;
                }
            }
        };
    }

    private IntObjectHashMap<IntHashSet> discoverParallelRelations(List<IntArrayList> log) {
        XLog decodedLog = decodeLog(log);
        XLogInfo logInfo = XLogInfoFactory.createLogInfo(decodedLog, new XEventNameClassifier());
        LogRelations logRelations = LogRelationsFactory.constructAlphaLogRelations(decodedLog, logInfo);
        Map<String, Set<String>> parallel = new UnifiedMap<>();
        for(Map.Entry<Pair<XEventClass, XEventClass>, Double> entry : logRelations.getParallelRelations().entrySet()) {
            Set<String> concurrent;
            String key = entry.getKey().getFirst().toString();
            String value = entry.getKey().getSecond().toString();
            if((concurrent = parallel.get(key)) == null) {
                concurrent = new UnifiedSet<>();
                parallel.put(key, concurrent);
            }
            concurrent.add(value);
        }

        for(String key : parallel.keySet()) {
            Set<String> concurrent = parallel.get(key);
            Set<String> update = new UnifiedSet<>(concurrent);
            for(String key2 : concurrent) {
                if(!key.equals(key2)) {
                    Set<String> concurrent2 = parallel.get(key2);
                    update.addAll(concurrent2);
                    concurrent2.addAll(update);
                }
            }
        }

        IntObjectHashMap<IntHashSet> par = new IntObjectHashMap<>();
        for(String key : parallel.keySet()) {
            int k = stringToIntMap.get(key);
            IntHashSet set = new IntHashSet();
            Set<String> concurrent = parallel.get(key);
            for(String key2 : concurrent) {
                int v = stringToIntMap.get(key2);
                set.add(v);
            }
            par.put(k, set);
        }
        return par;
    }

    private void discoverAverages() {
        Set<Node<Integer>> nodes = automaton.getNodes();
        DoubleArrayList event_frequency = new DoubleArrayList(nodes.size());
        DoubleArrayList trace_frequency = new DoubleArrayList(nodes.size());
        DoubleArrayList unique_trace_frequency = new DoubleArrayList(nodes.size());
        DoubleArrayList product_frequency = new DoubleArrayList(nodes.size());
//        DoubleArrayList minimized_trace_frequency = new DoubleArrayList(nodes.size());
//        DoubleArrayList minimized_unique_trace_frequency = new DoubleArrayList(nodes.size());

        for (Node<Integer> node : nodes) {
            event_frequency.add(automaton.getNodeFrequency(node));
            trace_frequency.add(traceFrequencyMap.get(node.getData()));
            unique_trace_frequency.add(uniqueTraceFrequencyMap.get(node.getData()));

//            product_frequency.add(automaton.getNodeFrequency(node) * traceFrequencyMap.get(node.getData()) * uniqueTraceFrequencyMap.get(node.getData()));
//            product_frequency.add(traceFrequencyMap.get(node.getData()) * uniqueTraceFrequencyMap.get(node.getData()));
//            product_frequency.add(automaton.getNodeFrequency(node) * traceFrequencyMap.get(node.getData()));
//            product_frequency.add(automaton.getNodeFrequency(node) * uniqueTraceFrequencyMap.get(node.getData()));
//            product_frequency.add(automaton.getNodeFrequency(node));
//            product_frequency.add(traceFrequencyMap.get(node.getData()));
            product_frequency.add(uniqueTraceFrequencyMap.get(node.getData()));

//            minimized_trace_frequency.add(minimizedTraceFrequencyMap.get(node.getData()));
//            minimized_unique_trace_frequency.add(minimizedUniqueTraceFrequencyMap.get(node.getData()));
        }

        mean_events_frequency_in_log = statisticsSelector.evaluate(center, null, event_frequency.toArray());
        left_sd_events_frequency_in_log = statisticsSelector.evaluate(left_distance_from_center, mean_events_frequency_in_log, event_frequency.toArray());
        right_sd_events_frequency_in_log = statisticsSelector.evaluate(right_distance_from_center, mean_events_frequency_in_log, event_frequency.toArray());

        mean_events_frequency_in_traces = statisticsSelector.evaluate(center, null, trace_frequency.toArray());
        left_sd_events_frequency_in_traces = statisticsSelector.evaluate(left_distance_from_center, mean_events_frequency_in_traces, trace_frequency.toArray());
        right_sd_events_frequency_in_traces = statisticsSelector.evaluate(right_distance_from_center, mean_events_frequency_in_traces, trace_frequency.toArray());

        mean_events_frequency_in_unique_trace = statisticsSelector.evaluate(center, null, unique_trace_frequency.toArray());
        left_sd_events_frequency_in_unique_trace = statisticsSelector.evaluate(left_distance_from_center, mean_events_frequency_in_unique_trace, unique_trace_frequency.toArray());
        right_sd_events_frequency_in_unique_trace = statisticsSelector.evaluate(right_distance_from_center, mean_events_frequency_in_unique_trace, unique_trace_frequency.toArray());

        mean_product = statisticsSelector.evaluate(center, null, product_frequency.toArray());
        left_sd_product = statisticsSelector.evaluate(left_distance_from_center, mean_product, product_frequency.toArray());
        right_sd_product = statisticsSelector.evaluate(right_distance_from_center, mean_product, product_frequency.toArray());

//        mean_events_frequency_in_minimized_traces = statisticsSelector.evaluate(center, null, minimized_trace_frequency.toArray());
//        left_sd_events_frequency_in_minimized_traces = statisticsSelector.evaluate(left_distance_from_center, mean_events_frequency_in_minimized_traces, minimized_trace_frequency.toArray());
//        right_sd_events_frequency_in_minimized_traces = statisticsSelector.evaluate(right_distance_from_center, mean_events_frequency_in_minimized_traces, minimized_trace_frequency.toArray());
//
//        mean_events_frequency_in_minimized_unique_trace = statisticsSelector.evaluate(center, null, minimized_unique_trace_frequency.toArray());
//        left_sd_events_frequency_in_minimized_unique_trace = statisticsSelector.evaluate(left_distance_from_center, mean_events_frequency_in_minimized_unique_trace, minimized_unique_trace_frequency.toArray());
//        right_sd_events_frequency_in_minimized_unique_trace = statisticsSelector.evaluate(right_distance_from_center, mean_events_frequency_in_minimized_unique_trace, minimized_unique_trace_frequency.toArray());

        lowerlimit_events_frequency_in_log = (mean_events_frequency_in_log - event_distance * left_sd_events_frequency_in_log) / mean_events_frequency_in_log;
        upperlimit_events_frequency_in_log = (mean_events_frequency_in_log + event_distance * right_sd_events_frequency_in_log) / mean_events_frequency_in_log;

        lowerlimit_events_frequency_in_trace = (mean_events_frequency_in_traces - trace_distance * left_sd_events_frequency_in_traces) / mean_events_frequency_in_traces;
        upperlimit_events_frequency_in_trace = (mean_events_frequency_in_traces + trace_distance * right_sd_events_frequency_in_traces) / mean_events_frequency_in_traces;

        lowerlimit_events_frequency_in_unique_trace = (mean_events_frequency_in_unique_trace - trace_distance * left_sd_events_frequency_in_unique_trace) / mean_events_frequency_in_unique_trace;
        upperlimit_events_frequency_in_unique_trace = (mean_events_frequency_in_unique_trace + trace_distance * right_sd_events_frequency_in_unique_trace) / mean_events_frequency_in_unique_trace;

        lowerlimit_product = (mean_product - trace_distance * left_sd_product) / mean_product;
        upperlimit_product = (mean_product + trace_distance * right_sd_product) / mean_product;

//        lowerlimit_events_frequency_in_minimized_trace = (mean_events_frequency_in_minimized_traces - left_sd_events_frequency_in_minimized_traces) / mean_events_frequency_in_minimized_traces;
//        upperlimit_events_frequency_in_minimized_trace = (mean_events_frequency_in_minimized_traces + right_sd_events_frequency_in_minimized_traces) / mean_events_frequency_in_minimized_traces;
//
//        lowerlimit_events_frequency_in_minimized_unique_trace = (mean_events_frequency_in_minimized_unique_trace - left_sd_events_frequency_in_minimized_unique_trace) / mean_events_frequency_in_minimized_unique_trace;
//        upperlimit_events_frequency_in_minimized_unique_trace = (mean_events_frequency_in_minimized_unique_trace + right_sd_events_frequency_in_minimized_unique_trace) / mean_events_frequency_in_minimized_unique_trace;
    }

    private Integer identifyLabel(List<IntArrayList> log) {
        automaton = automatonFactory.generate(log);
        discoverLogStatistics(log);
        discoverAverages();

        Integer bestLabel = null;
        double best = 1;

        Set<Node<Integer>> nodes = automaton.getNodes();
        for (Node<Integer> node : nodes) {
            if(node.getData().equals(stringToIntMap.get("ArtificialStartEvent+"))) continue;
            if(node.getData().equals(stringToIntMap.get("ArtificialEndEvent+"))) continue;

            double events_frequency_in_log = automaton.getNodeFrequency(node);
            double events_frequency_in_traces = traceFrequencyMap.get(node.getData());
            double events_frequency_in_unique_traces = uniqueTraceFrequencyMap.get(node.getData());
//            double events_frequency_in_minimized_traces = minimizedTraceFrequencyMap.get(node.getData());
//            double events_frequency_in_minimized_unique_traces = minimizedUniqueTraceFrequencyMap.get(node.getData());

            int incoming_edges = get_incoming_edges(node).size();
            int outgoing_edges = get_outgoing_edges(node).size();

//            double removability = removability(events_frequency_in_log, events_frequency_in_unique_traces, events_frequency_in_traces, events_frequency_in_minimized_traces, events_frequency_in_minimized_unique_traces, incoming_edges, outgoing_edges);
            double removability = removability(events_frequency_in_log, events_frequency_in_unique_traces, events_frequency_in_traces, incoming_edges, outgoing_edges);

            if (best > removability) {
                best = removability;
                bestLabel = node.getData();
            }
        }
        System.out.println(best);
        return bestLabel;
    }

    private IntHashSet identifyLabels(List<IntArrayList> log) {
        automaton = automatonFactory.generate(log);
        IntHashSet labels = new IntHashSet();
        discoverLogStatistics(log);
        discoverAverages();

        Integer bestLabel = null;
        double best = 1;

        Set<Node<Integer>> nodes = automaton.getNodes();
        for (Node<Integer> node : nodes) {
            if(node.getData().equals(stringToIntMap.get("ArtificialStartEvent+"))) continue;
            if(node.getData().equals(stringToIntMap.get("ArtificialEndEvent+"))) continue;

            double events_frequency_in_log = automaton.getNodeFrequency(node);
            double events_frequency_in_traces = traceFrequencyMap.get(node.getData());
            double events_frequency_in_unique_traces = uniqueTraceFrequencyMap.get(node.getData());
//            double events_frequency_in_minimized_traces = minimizedTraceFrequencyMap.get(node.getData());
//            double events_frequency_in_minimized_unique_traces = minimizedUniqueTraceFrequencyMap.get(node.getData());

            int incoming_edges = get_incoming_edges(node).size();
            int outgoing_edges = get_outgoing_edges(node).size();

//            double removability = removability(events_frequency_in_log, events_frequency_in_unique_traces, events_frequency_in_traces, events_frequency_in_minimized_traces, events_frequency_in_minimized_unique_traces, incoming_edges, outgoing_edges);
            double removability = removability(events_frequency_in_log, events_frequency_in_unique_traces, events_frequency_in_traces, incoming_edges, outgoing_edges);

            if (best > removability) {
                labels.add(node.getData());
            }
        }
        return labels;
    }

    private Set<Edge<Integer>> get_incoming_edges(Node<Integer> node) {
        Set<Edge<Integer>> incoming_edges = new UnifiedSet<>();
        for(Edge<Integer> edge : automaton.getEdges()) {
            if(edge.getTarget().equals(node)) {
                incoming_edges.add(edge);
            }
        }
        return incoming_edges;
    }

    private Set<Edge<Integer>> get_outgoing_edges(Node<Integer> node) {
        Set<Edge<Integer>> outgoing_edges = new UnifiedSet<>();
        for(Edge<Integer> edge : automaton.getEdges()) {
            if(edge.getSource().equals(node)) {
                outgoing_edges.add(edge);
            }
        }
        return outgoing_edges;
    }

//    private double removability(double events_frequency_in_log, double events_frequency_in_traces, double events_frequency_in_unique_traces, double events_frequency_in_minimized_traces, double events_frequency_in_minimized_unique_traces, int incoming_edges, int outgoing_edges) {
    private double removability(double events_frequency_in_log, double events_frequency_in_traces, double events_frequency_in_unique_traces, int incoming_edges, int outgoing_edges) {
        double res = 0;
        double value = 1;

//        double product = events_frequency_in_log * events_frequency_in_traces * events_frequency_in_unique_traces;
//        double product = events_frequency_in_traces * events_frequency_in_unique_traces;
//        double product = events_frequency_in_log * events_frequency_in_traces;
//        double product = events_frequency_in_log * events_frequency_in_unique_traces;
//        double product = events_frequency_in_log;
//        double product = events_frequency_in_traces;
        double product = events_frequency_in_unique_traces;

        double ratio_product = scale(product / mean_product, 0, 2);

        double ratio_events_frequency_in_log = scale(events_frequency_in_log / mean_events_frequency_in_log, 0, 2);
        double ratio_events_frequency_in_traces = scale(events_frequency_in_traces / mean_events_frequency_in_traces, 0, 2);
        double ratio_events_frequency_in_unique_traces = scale(events_frequency_in_unique_traces / mean_events_frequency_in_unique_trace, 0, 2);

        if(ratio_product < lowerlimit_product) {
            res++;
            value *= ratio_product;
        }

//        if(incoming_edges == 1 || outgoing_edges == 1) res = -1;
//
//        if(ratio_events_frequency_in_log > upperlimit_events_frequency_in_log) res--;
//        else if(ratio_events_frequency_in_log < lowerlimit_events_frequency_in_log) {
//            res++;
//            value *= ratio_events_frequency_in_log;
//        }
//
//        if(ratio_events_frequency_in_traces > upperlimit_events_frequency_in_trace) res--;
//        else if(ratio_events_frequency_in_traces < lowerlimit_events_frequency_in_trace) {
//            res++;
//            value *= ratio_events_frequency_in_traces;
//        }
//
//        if(ratio_events_frequency_in_unique_traces > upperlimit_events_frequency_in_unique_trace) res--;
//        else if(ratio_events_frequency_in_unique_traces < lowerlimit_events_frequency_in_unique_trace) {
//            res++;
//            value *= ratio_events_frequency_in_unique_traces;
//        }


//        if(ratio_events_frequency_in_minimized_traces > upperlimit_events_frequency_in_minimized_trace) res--;
//        else if(ratio_events_frequency_in_minimized_traces < lowerlimit_events_frequency_in_minimized_trace) {
//            res++;
//        }
//        value *= ratio_events_frequency_in_minimized_traces;
//
//        if(ratio_events_frequency_in_minimized_unique_traces > upperlimit_events_frequency_in_minimized_unique_trace) res--;
//        else if(ratio_events_frequency_in_minimized_unique_traces < lowerlimit_events_frequency_in_minimized_unique_trace) {
//            res++;
//        }
//        value *= ratio_events_frequency_in_minimized_unique_traces;

        if(res > 0) return value;
        return 2;
    }

    private double scale(double value, double min, double max) {
        return Math.min(max, Math.max(min, value));
    }

    private int codeTwoEvents(int event1, int event2) {
        return (int) (event1 * Math.pow(10, Integer.toString(events).length()) + event2);
    }

//    private IntArrayList minimizeTrace(IntArrayList trace) {
//        IntArrayList minimized_trace;
//        if((minimized_trace = minimized_traces.get(trace)) == null) {
//            minimized_trace = new IntArrayList();
//            IntHashSet direct_follow_relations = getDirectFollowRelations(trace);
//
//            IntHashSet existing_direct_follow_relations = new IntHashSet();
//            IntHashSet existing_long_distance_relations = new IntHashSet();
//
//            minimized_trace.add(trace.get(0));
//            for (int i = 1; i < trace.size(); i++) {
//                int current = trace.get(i);
//                int newDFR = getNewDirectFollowRelations(existing_direct_follow_relations, minimized_trace, current);
//                IntHashSet newLDR = getNewLongDistanceRelations(existing_long_distance_relations, minimized_trace, current);
//                if (newDFR > -1 || newLDR.size() > 0) {
//                    minimized_trace.add(current);
//                    if (newDFR > -1) existing_direct_follow_relations.add(newDFR);
//                    existing_long_distance_relations.addAll(newLDR);
//                } else if (i < trace.size() - 1) {
//                    if (!direct_follow_relations.contains(codeTwoEvents(minimized_trace.getLast(), trace.get(i + 1)))) {
//                        minimized_trace.add(current);
//                    } else if (!existing_direct_follow_relations.contains(codeTwoEvents(current, trace.get(i + 1)))) {
//                        minimized_trace.add(current);
//                    }
//                }
//            }
//
//            minimized_traces.put(trace, minimized_trace);
//        }
//        return minimized_trace;
//    }

    private IntHashSet getDirectFollowRelations(IntArrayList trace) {
        IntHashSet direct_follow_relations = new IntHashSet();
        for(int i = 0; i < trace.size() - 1; i++) {
            int current = trace.get(i);
            int next = trace.get(i + 1);
            int code = codeTwoEvents(current, next);
            direct_follow_relations.add(code);
        }

        return  direct_follow_relations;
    }

    private IntHashSet getDirectFollowRelations(IntHashSet existing_direct_follow_relations, IntArrayList trace) {
        for(int i = trace.size() - 2; i >= 0 && i < trace.size() - 1; i++) {
            int current = trace.get(i);
            int next = trace.get(i + 1);
            int code = codeTwoEvents(current, next);
            existing_direct_follow_relations.add(code);
        }

        return  existing_direct_follow_relations;
    }

    private int getNewDirectFollowRelations(IntHashSet existing_direct_follow_relations, IntArrayList trace, int event_to_insert) {
        int direct_follow_relation = codeTwoEvents(trace.getLast(), event_to_insert);
        if(getDirectFollowRelations(existing_direct_follow_relations, trace).contains(direct_follow_relation)) return -1;
        else return direct_follow_relation;
    }

    private IntHashSet getLongDistanceRelations(IntArrayList trace) {
        IntHashSet long_distance_relations = new IntHashSet();

        for(int i = 0; i < trace.size(); i++) {
            int current = trace.get(i);
            for(int j = i + 2; j < trace.size(); j++) {
                int next = trace.get(j);
                int code = codeTwoEvents(current, next);
                long_distance_relations.add(code);
            }
        }

        return long_distance_relations;
    }

    private IntHashSet getNewLongDistanceRelations(IntHashSet existing_long_distance_relations, IntArrayList trace, int event_to_insert) {
        IntHashSet new_long_distance_relations = new IntHashSet();
        for(int i = 0; i < trace.size() - 1; i++) {
            int current = trace.get(i);
            int code = codeTwoEvents(current, event_to_insert);
            if(!existing_long_distance_relations.contains(code)) new_long_distance_relations.add(code);
        }

        return new_long_distance_relations;
    }

}
