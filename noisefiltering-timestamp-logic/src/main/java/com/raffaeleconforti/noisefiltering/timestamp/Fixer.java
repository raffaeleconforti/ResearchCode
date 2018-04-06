package com.raffaeleconforti.noisefiltering.timestamp;

import com.raffaeleconforti.kernelestimation.distribution.impl.EventDistributionCalculatorNoiseImpl;
import com.raffaeleconforti.log.util.LogCloner;
import com.raffaeleconforti.log.util.NameExtractor;
import com.raffaeleconforti.log.util.TraceToString;
import com.raffaeleconforti.memorylog.XFactoryMemoryImpl;
import com.raffaeleconforti.noisefiltering.timestamp.check.TimeStampChecker;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeBooleanImpl;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by conforti on 7/02/15.
 */

public class Fixer {

    private final XEventClassifier xEventClassifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
    private final XConceptExtension xce = XConceptExtension.instance();
    private final XTimeExtension xte = XTimeExtension.instance();
    private final boolean self_cleaning = false;

    public XLog s1(XLog log) {

        LogCloner logCloner = new LogCloner();
        log = logCloner.cloneLog(log);

        TimeStampChecker timeStampChecker = new TimeStampChecker(xEventClassifier, new SimpleDateFormat("yyyy/MM/dd hh:mm:ss"));
        Map<String, List<XTrace>> uniqueTraces = new UnifiedMap<>();
        Map<String, List<XTrace>> excludedUniqueTraces = new UnifiedMap<>();
        Map<String, List<XTrace>> fixedExcludedUniqueTraces = new UnifiedMap<>();

        for(XTrace trace : log) {
            if (!timeStampChecker.containsSameTimestamps(trace)) {
                String s = TraceToString.convertXTraceToString(trace, new NameExtractor(xEventClassifier));
                List<XTrace> list;
                if ((list = uniqueTraces.get(s)) == null) {
                    list = new ArrayList<>();
                    uniqueTraces.put(s, list);
                }
                list.add(trace);
            }
        }

        for(XTrace trace : log) {
            if (timeStampChecker.containsSameTimestamps(trace)) {
                String s = TraceToString.convertXTraceToString(trace, new NameExtractor(xEventClassifier));
                List<XTrace> list;
                if ((list = excludedUniqueTraces.get(s)) == null) {
                    list = new ArrayList<>();
                    excludedUniqueTraces.put(s, list);
                }
                list.add(trace);
            }
        }

        Map<String, Set<String>> faultyEvents = new UnifiedMap<>();
        XFactory factory = new XFactoryMemoryImpl();
        XLog noiseFreeLog = factory.createLog(log.getAttributes());
        for(XTrace t : log) {
            if(!timeStampChecker.containsSameTimestamps(t)) {
                noiseFreeLog.add(logCloner.getXTrace(t));
            }else {
                Set<Set<XEvent>> setXEvents = timeStampChecker.findEventsSameTimeStamp(t);
                Set<String> stringSet = new UnifiedSet<>();
                for(Set<XEvent> set : setXEvents) {
                    for(XEvent e : set) {
                        stringSet.add(xce.extractName(e));
                    }
                    faultyEvents.put(xce.extractName(t), stringSet);
                }
            }
        }

        EventDistributionCalculatorNoiseImpl eventDistributionCalculator = new EventDistributionCalculatorNoiseImpl(log, xEventClassifier, faultyEvents, self_cleaning);
        eventDistributionCalculator.analyseLog();

//        EventPermutatorSmart eventPermutator = new EventPermutatorSmart(xEventClassifier, eventDistributionCalculator, timeStampChecker, TimeStampFixerSmart.discoverSequencesMarcello(log, timeStampChecker));

        int uniqueEvents = 0;
        int minUniqueEventsPerTrace = Integer.MAX_VALUE;
        int maxUniqueEventsPerTrace = 0;
        int uniqueEventsPerTrace = 0;

        int minEventsPerTrace = Integer.MAX_VALUE;
        int maxEventsPerTrace = 0;
        int eventsPerTrace = 0;

        int minGapsPerTrace = Integer.MAX_VALUE;
        int maxGapsPerTrace = 0;
        int tracesAffectedByGaps = 0;
        int numberOfGapsPerTrace = 0;

        int maxLengthGap = 0;
        int minLengthGap = Integer.MAX_VALUE;
        int lengthGap = 0;

        NameExtractor nameExtractor = new NameExtractor(new XEventNameClassifier());

        Set<String> events = new UnifiedSet<>();
        for(XTrace trace : log) {
            Set<String> traceEvents = new UnifiedSet<>();
            for(XEvent event : trace) {
                events.add(nameExtractor.getEventName(event));
                traceEvents.add(nameExtractor.getEventName(event));
            }
            minUniqueEventsPerTrace = Math.min(minUniqueEventsPerTrace, traceEvents.size());
            maxUniqueEventsPerTrace = Math.max(maxUniqueEventsPerTrace, traceEvents.size());
            uniqueEventsPerTrace += traceEvents.size();

            minEventsPerTrace = Math.min(minEventsPerTrace, trace.size());
            maxEventsPerTrace = Math.max(maxEventsPerTrace, trace.size());
            eventsPerTrace += trace.size();
            if (timeStampChecker.containsSameTimestamps(trace)) {
                Set<Set<XEvent>> sets = timeStampChecker.findEventsSameTimeStamp(trace);
                maxGapsPerTrace = Math.max(maxGapsPerTrace, sets.size());
                minGapsPerTrace = Math.min(minGapsPerTrace, sets.size());
                numberOfGapsPerTrace += sets.size();
                tracesAffectedByGaps++;
                for(Set<XEvent> set : sets) {
                    maxLengthGap = Math.max(maxLengthGap, set.size());
                    minLengthGap = Math.min(minLengthGap, set.size());
                    lengthGap += set.size();
                }
            }
        }
        uniqueEvents = events.size();

        double averageUniqueEventsPerTrace = (double) uniqueEventsPerTrace / (double) log.size();
        double averageEventsPerTrace = (double) eventsPerTrace / (double) log.size();
        double averageLengthGap = (double) lengthGap / (double) numberOfGapsPerTrace;
        double averageGapPerTrace = (double) numberOfGapsPerTrace / (double) tracesAffectedByGaps;
        double stdUniqueEventsPerTrace = 0;
        double stdEventsPerTrace = 0;
        double stdLengthGap = 0;
        double stdGapPerTrace = 0;

        for(XTrace trace : log) {
            Set<String> traceEvents = new UnifiedSet<>();
            for(XEvent event : trace) {
                traceEvents.add(nameExtractor.getEventName(event));
            }
            stdUniqueEventsPerTrace += Math.pow(averageUniqueEventsPerTrace - traceEvents.size(), 2);

            stdEventsPerTrace += Math.pow(averageEventsPerTrace - trace.size(), 2);
            if (timeStampChecker.containsSameTimestamps(trace)) {
                Set<Set<XEvent>> sets = timeStampChecker.findEventsSameTimeStamp(trace);
                stdGapPerTrace += Math.pow(averageGapPerTrace - sets.size(), 2);
                for(Set<XEvent> set : sets) {
                    stdLengthGap += Math.pow(averageLengthGap - set.size(), 2);
                }
            }
        }

        stdUniqueEventsPerTrace = Math.sqrt(stdUniqueEventsPerTrace / log.size());
        stdEventsPerTrace = Math.sqrt(stdEventsPerTrace / log.size());
        stdLengthGap = Math.sqrt(stdLengthGap / numberOfGapsPerTrace);
        stdGapPerTrace = Math.sqrt(stdGapPerTrace / tracesAffectedByGaps);

        String s = "";
        s += "NumberOfTraces: " + log.size() + "\n";
        s += "UniqueEvents: " + uniqueEvents + "\n";
        s += "minEventsPerTraces: " + minEventsPerTrace + "\n";
        s += "maxEventsPerTraces: " + maxEventsPerTrace + "\n";
        s += "averageEventsPerTraces: " + averageEventsPerTrace + "\n";
        s += "stdEventsPerTraces: " + stdEventsPerTrace + "\n";
        s += "minUniqueEventsPerTrace: " + minUniqueEventsPerTrace + "\n";
        s += "maxUniqueEventsPerTrace: " + maxUniqueEventsPerTrace + "\n";
        s += "averageUniqueEventsPerTrace: " + averageUniqueEventsPerTrace + "\n";
        s += "stdUniqueEventsPerTrace: " + stdUniqueEventsPerTrace + "\n";
        s += "NumberOfTracesAffectedByGaps: " + tracesAffectedByGaps + "\n";
        s += "minGapsPerTrace: " + minGapsPerTrace + "\n";
        s += "maxGapsPerTrace: " + maxGapsPerTrace + "\n";
        s += "averageGapPerTrace: " + averageGapPerTrace + "\n";
        s += "stdGapPerTrace: " + stdGapPerTrace + "\n";
        s += "minLengthGap: " + minLengthGap + "\n";
        s += "maxLengthGap: " + maxLengthGap + "\n";
        s += "averageLengthGap: " + averageLengthGap + "\n";
        s += "stdLengthGap: " + stdLengthGap + "\n";
        System.out.println(s);

        System.out.println("logSize; uniqueEvents; minEventsPerTrace; "
                + "maxEventsPerTrace; averageEventsPerTrace; "
                + "stdEventsPerTrace; minUniqueEventsPerTrace; "
                + "maxUniqueEventsPerTrace; averageUniqueEventsPerTrace; "
                + "stdUniqueEventsPerTrace; tracesAffectedByGaps; "
                + "minGapsPerTrace; maxGapsPerTrace; "
                + "averageGapPerTrace; stdGapPerTrace; "
                + "minLengthGap; maxLengthGap; "
                + "averageLengthGap; stdLengthGap");
        System.out.println(log.size() + "; " + uniqueEvents + "; " + minEventsPerTrace + "; "
                + maxEventsPerTrace + "; " + averageEventsPerTrace + "; "
                + stdEventsPerTrace + "; " + minUniqueEventsPerTrace + "; "
                + maxUniqueEventsPerTrace + "; " + averageUniqueEventsPerTrace + "; "
                + stdUniqueEventsPerTrace + "; " + tracesAffectedByGaps + "; "
                + minGapsPerTrace + "; " + maxGapsPerTrace + "; "
                + averageGapPerTrace + "; " + stdGapPerTrace + "; "
                + minLengthGap + "; " + maxLengthGap + "; "
                + averageLengthGap + "; " + stdLengthGap);

        if(true) return null;
        System.out.println("Insert Noise Level\n");
        double val = new Scanner(System.in).nextDouble();

        Random r = new Random(123456789);
        double changed = 0.0;
        Map.Entry<String, List<XTrace>>[] entries = uniqueTraces.entrySet().toArray(new Map.Entry[uniqueTraces.size()]);
        while(changed / log.size() < val) {
            int pos = r.nextInt(entries.length);
            List<XTrace> traces = entries[pos].getValue();
            for(XTrace trace : traces) {
                if (trace.getAttributes().get("change") == null) {
//                int number = r.nextInt(trace.size() - 1);
                    int number = 9 < trace.size() - 1 ? r.nextInt(9) : r.nextInt(trace.size() - 1);
                    int start = r.nextInt((trace.size() - 1) - number);

                    if (number > 0) {
                        trace.getAttributes().put("change", new XAttributeBooleanImpl("change", true));
                        Date date = null;

                        for (int i = 0; i < trace.size(); i++) {
                            XEvent event = trace.get(i);
                            if (i == start) {
                                date = xte.extractTimestamp(event);
                                event.getAttributes().put("change", new XAttributeBooleanImpl("change", true));
                            } else {
                                if (i > start && i <= start + number) {
                                    xte.assignTimestamp(event, date);
                                    event.getAttributes().put("change", new XAttributeBooleanImpl("change", true));
                                }
                            }
                        }

                        Collections.sort(trace, new Comparator<XEvent>() {
                            @Override
                            public int compare(XEvent o1, XEvent o2) {
                                Date date1 = xte.extractTimestamp(o1);
                                Date date2 = xte.extractTimestamp(o2);
                                if (!date1.equals(date2)) return date1.compareTo(date2);
                                else return xce.extractName(o1).compareTo(xce.extractName(o2));
                            }
                        });
                    }

                    changed++;
                }
            }
        }

        return log;
    }

    public XLog s3(XLog log) {

        Iterator<XTrace> iterator = log.iterator();
        while(iterator.hasNext()) {
            XTrace trace = iterator.next();
            for(int i = 0; i < trace.size(); i++) {
                XEvent event = trace.get(i);
                String name = xce.extractName(event);
                if(name.equals("Assess loan risk")) {
                    for(int j = i + 1; j < trace.size(); j++) {
                        XEvent event1 = trace.get(j);
                        String name1 = xce.extractName(event1);
                        if(name1.equals("Check credit history")) {
                            swap2(event, event1);

                            Collections.sort(trace, new Comparator<XEvent>() {
                                @Override
                                public int compare(XEvent o1, XEvent o2) {
                                    Date date1 = xte.extractTimestamp(o1);
                                    Date date2 = xte.extractTimestamp(o2);
                                    if (!date1.equals(date2)) return date1.compareTo(date2);
                                    else return xce.extractName(o1).compareTo(xce.extractName(o2));
                                }
                            });
                            break;
                        }
                    }
                    break;
                }
            }
        }

        return log;
    }

    public XLog s2(XLog log) {

        LogCloner logCloner = new LogCloner();
        log = logCloner.cloneLog(log);

        System.out.println("Insert Noise Level\n");
        double val = new Scanner(System.in).nextDouble();

        Random r = new Random(123456789);
        double changed = 0.0;

        double total = 0.0;
        for(XTrace trace : log) {
            total += trace.size();
        }

        while(changed / total < val) {
            int pos = r.nextInt(log.size());
            XTrace trace = log.get(pos);

            if(trace.getAttributes().get("change") == null) {
//            int number = r.nextInt(trace.size() - 1);
                int number = 9 < trace.size() - 1 ? r.nextInt(9) : r.nextInt(trace.size() - 1);
                int start = r.nextInt((trace.size() - 1) - number);

                if (number > 0) {
                    trace.getAttributes().put("change", new XAttributeBooleanImpl("change", true));
                    Date date = null;

                    for (int i = 0; i < trace.size(); i++) {
                        XEvent event = trace.get(i);
                        if (i == start) {
                            date = xte.extractTimestamp(event);

                            if (event.getAttributes().get("change") == null) {
                                event.getAttributes().put("change", new XAttributeBooleanImpl("change", true));
                                changed++;
                            }
                        } else {
                            if (i > start && i <= start + number) {
                                xte.assignTimestamp(event, date);

                                if (event.getAttributes().get("change") == null) {
                                    event.getAttributes().put("change", new XAttributeBooleanImpl("change", true));
                                    changed++;
                                }
                            }
                        }
                    }

                    Collections.sort(trace, new Comparator<XEvent>() {
                        @Override
                        public int compare(XEvent o1, XEvent o2) {
                            Date date1 = xte.extractTimestamp(o1);
                            Date date2 = xte.extractTimestamp(o2);
                            if (!date1.equals(date2)) return date1.compareTo(date2);
                            else return xce.extractName(o1).compareTo(xce.extractName(o2));
                        }
                    });
                }
            }

        }

        return log;
    }

    public XLog s(XLog log) {


        Random r = new Random(123456789);
        int count = 0;
        Iterator<XTrace> iterator = log.iterator();
        while(iterator.hasNext()) {
            XTrace trace = iterator.next();
            if(count < 3000) {
                for(int i = 0; i < trace.size(); i++) {
                    XEvent event = trace.get(i);
                    String name = xce.extractName(event);

                    if(name.equals("Check credit history")) {
                        int option = r.nextInt(4);
                        switch (option) {
                            case 1: swap1(trace.get(i), trace.get(i+1), trace.get(i+2), trace.get(i), trace.get(i+2), trace.get(i+1));
                            case 2: swap1(trace.get(i), trace.get(i+1), trace.get(i+2), trace.get(i+1), trace.get(i), trace.get(i+2));
                            case 3: swap1(trace.get(i), trace.get(i+1), trace.get(i+2), trace.get(i+1), trace.get(i+2), trace.get(i));
                            case 0:
                        }
                    }

                    if(name.equals("Send home insurance quote")) {
                        int option = r.nextInt(2);
                        switch (option) {
                            case 1: swap2(trace.get(i-1), trace.get(i));
                            case 0:
                        }
                        if(option == 1) {
                            System.out.println("k");
                        }
                    }
                }
            }else {
                iterator.remove();
            }
            count++;
        }


        return log;
    }

    private void swap1(XEvent xEvent1, XEvent xEvent2, XEvent xEvent3, XEvent newxEvent1, XEvent newxEvent2, XEvent newxEvent3) {
        Date date1 = xte.extractTimestamp(xEvent1);
        Date date2 = xte.extractTimestamp(xEvent2);
        Date date3 = xte.extractTimestamp(xEvent3);

        xte.assignTimestamp(newxEvent1, date1);
        xte.assignTimestamp(newxEvent2, date2);
        xte.assignTimestamp(newxEvent3, date3);
    }

    private void swap2(XEvent xEvent1, XEvent xEvent2) {
        Date date1 = xte.extractTimestamp(xEvent1);
        Date date2 = xte.extractTimestamp(xEvent2);

        xte.assignTimestamp(xEvent2, date1);
        xte.assignTimestamp(xEvent1, date2);
    }

}
