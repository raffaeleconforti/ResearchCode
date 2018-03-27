package com.raffaeleconforti.noisefiltering.timestamp;

import com.raffaeleconforti.log.util.LogCloner;
import com.raffaeleconforti.log.util.NameExtractor;
import com.raffaeleconforti.log.util.TraceToString;
import com.raffaeleconforti.noisefiltering.timestamp.check.TimeStampChecker;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by conforti on 7/02/15.
 */

public class LogInfoExtractor {

    final XEventClassifier xEventClassifier = new XEventAndClassifier(new XEventNameClassifier());

    public String extractInfo(XLog log) {

        LogCloner logCloner = new LogCloner();
        log = logCloner.cloneLog(log);

        TimeStampChecker timeStampChecker = new TimeStampChecker(xEventClassifier, new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"));
        Map<String, List<XTrace>> uniqueTraces = new UnifiedMap<>();
        Map<String, List<XTrace>> uniqueTracesAffectedByGaps = new UnifiedMap<>();

        for(XTrace trace : log) {
            String s = TraceToString.convertXTraceToString(trace, new NameExtractor(xEventClassifier));
            List<XTrace> list;
            if ((list = uniqueTraces.get(s)) == null) {
                list = new ArrayList<>();
                uniqueTraces.put(s, list);
            }
            list.add(trace);
        }

        int uniqueEvents = 0;
        int minUniqueEventsPerTrace = Integer.MAX_VALUE;
        int maxUniqueEventsPerTrace = 0;
        int uniqueEventsPerTrace = 0;

        int minEventsPerTrace = Integer.MAX_VALUE;
        int maxEventsPerTrace = 0;
        int totalNumberOfEvents = 0;

        int minGapsPerTrace = Integer.MAX_VALUE;
        int maxGapsPerTrace = 0;

        Map<Integer, Integer> mapGapsPerTrace = new UnifiedMap<>();

        int tracesAffectedByGaps = 0;
        int numberOfGapsPerTrace = 0;

        int maxLengthGap = 0;
        int minLengthGap = Integer.MAX_VALUE;
        int eventsAffectedByGap = 0;

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
            totalNumberOfEvents += trace.size();
            if (timeStampChecker.containsSameTimestamps(trace)) {
                Set<Set<XEvent>> sets = timeStampChecker.findEventsSameTimeStamp(trace);

                Integer count;
                for(Set<XEvent> set : sets) {
                    if ((count = mapGapsPerTrace.get(set.size())) == null) {
                        count = 0;
                    }
                    count++;
                    mapGapsPerTrace.put(set.size(), count);
                }

                maxGapsPerTrace = Math.max(maxGapsPerTrace, sets.size());
                minGapsPerTrace = Math.min(minGapsPerTrace, sets.size());

                numberOfGapsPerTrace += sets.size();
                tracesAffectedByGaps++;
                String s = TraceToString.convertXTraceToString(trace, new NameExtractor(xEventClassifier));
                List<XTrace> list;
                if ((list = uniqueTracesAffectedByGaps.get(s)) == null) {
                    list = new ArrayList<>();
                    uniqueTracesAffectedByGaps.put(s, list);
                }
                list.add(trace);
                for(Set<XEvent> set : sets) {
                    maxLengthGap = Math.max(maxLengthGap, set.size());
                    minLengthGap = Math.min(minLengthGap, set.size());
                    eventsAffectedByGap += set.size();
                }
            }
        }
        uniqueEvents = events.size();

        double averageUniqueEventsPerTrace = (double) uniqueEventsPerTrace / (double) log.size();
        double averageEventsPerTrace = (double) totalNumberOfEvents / (double) log.size();
        double averageLengthGap = (double) eventsAffectedByGap / (double) numberOfGapsPerTrace;
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

        String s = "<html><table width=\"400\"><tr><td width=\"33%\"></td><td width=\"33%\"><table>";
        s += "<tr><td>NumberOfTraces</td><td>:</td><td>" + log.size() + "</td></tr>";
        s += "<tr><td>NumberOfUniqueTraces</td><td>:</td><td>" + uniqueTraces.size() + "</td></tr>";

        s += "<tr><td>NumberOfTracesAffectedByGaps</td><td>:</td><td>" + tracesAffectedByGaps + "</td></tr>";
        s += "<tr><td>PercentageOfTracesAffectedByGaps</td><td>:</td><td>" + ((double) tracesAffectedByGaps / (double) log.size()) + "</td></tr>";
        s += "<tr><td>NumberOfUniqueTracesAffectedByGaps</td><td>:</td><td>" + uniqueTracesAffectedByGaps.size() + "</td></tr>";
        s += "<tr><td>PercentageOfUniqueTracesAffectedByGaps</td><td>:</td><td>" + ((double) uniqueTracesAffectedByGaps.size() / (double) uniqueTraces.size()) + "</td></tr>";

        s += "<tr><td>NumberEvents</td><td>:</td><td>" + totalNumberOfEvents + "</td></tr>";
        s += "<tr><td>UniqueEvents</td><td>:</td><td>" + uniqueEvents + "</td></tr>";

        s += "<tr><td>totalEventsWithGap</td><td>:</td><td>" + eventsAffectedByGap + "</td></tr>";
        s += "<tr><td>percentageTotalEventsWithGap</td><td>:</td><td>" + ((double) eventsAffectedByGap / (double) totalNumberOfEvents) + "</td></tr>";

        s += "<tr><td>minEventsPerTraces</td><td>:</td><td>" + minEventsPerTrace + "</td></tr>";
        s += "<tr><td>maxEventsPerTraces</td><td>:</td><td>" + maxEventsPerTrace + "</td></tr>";
        s += "<tr><td>averageEventsPerTraces</td><td>:</td><td>" + averageEventsPerTrace + "</td></tr>";
        s += "<tr><td>stdEventsPerTraces</td><td>:</td><td>" + stdEventsPerTrace + "</td></tr>";
        s += "<tr><td>minUniqueEventsPerTrace</td><td>:</td><td>" + minUniqueEventsPerTrace + "</td></tr>";
        s += "<tr><td>maxUniqueEventsPerTrace</td><td>:</td><td>" + maxUniqueEventsPerTrace + "</td></tr>";
        s += "<tr><td>averageUniqueEventsPerTrace</td><td>:</td><td>" + averageUniqueEventsPerTrace + "</td></tr>";
        s += "<tr><td>stdUniqueEventsPerTrace</td><td>:</td><td>" + stdUniqueEventsPerTrace + "</td></tr>";
        s += "<tr><td>minTimeGapsPerTrace</td><td>:</td><td>" + minGapsPerTrace + "</td></tr>";
        s += "<tr><td>maxTimeGapsPerTrace</td><td>:</td><td>" + maxGapsPerTrace + "</td></tr>";
        s += "<tr><td>averageTimeGapPerTrace</td><td>:</td><td>" + averageGapPerTrace + "</td></tr>";
        s += "<tr><td>stdTimeGapPerTrace</td><td>:</td><td>" + stdGapPerTrace + "</td></tr>";
        s += "<tr><td>minLengthTimeGap</td><td>:</td><td>" + minLengthGap + "</td></tr>";
        s += "<tr><td>maxLengthTimeGap</td><td>:</td><td>" + maxLengthGap + "</td></tr>";
        s += "<tr><td>averageLengthTimeGap</td><td>:</td><td>" + averageLengthGap + "</td></tr>";
        s += "<tr><td>stdLengthTimeGap</td><td>:</td><td>" + stdLengthGap + "</td></tr>";

        s += "<tr><td>GapSize and Frequencies</td><td>:</td><td></td></tr>";
        for(Map.Entry<Integer, Integer> entry : mapGapsPerTrace.entrySet()) {
            s += "<tr><td>" + entry.getKey() + "</td><td>:</td><td>" + entry.getValue() + "</td></tr>";
        }

        s += "</table></td><td width=\"33%\"></td></tr></table></html>";

        System.out.println("logSize; uniqueTraces; uniqueEvents; minEventsPerTrace; totalNumberEvents; totalEventsWithGap; "
                + "maxEventsPerTrace; averageEventsPerTrace; "
                + "stdEventsPerTrace; minUniqueEventsPerTrace; "
                + "maxUniqueEventsPerTrace; averageUniqueEventsPerTrace; "
                + "stdUniqueEventsPerTrace; tracesAffectedByGaps; "
                + "minGapsPerTrace; maxGapsPerTrace; "
                + "averageGapPerTrace; stdGapPerTrace; "
                + "minLengthGap; maxLengthGap; "
                + "averageLengthGap; stdLengthGap");
        System.out.println(log.size() + "; " + uniqueTraces.size() + "; " + uniqueEvents + "; " + minEventsPerTrace + "; " + totalNumberOfEvents + "; " + eventsAffectedByGap + "; "
                + maxEventsPerTrace + "; " + averageEventsPerTrace + "; "
                + stdEventsPerTrace + "; " + minUniqueEventsPerTrace + "; "
                + maxUniqueEventsPerTrace + "; " + averageUniqueEventsPerTrace + "; "
                + stdUniqueEventsPerTrace + "; " + tracesAffectedByGaps + "; "
                + minGapsPerTrace + "; " + maxGapsPerTrace + "; "
                + averageGapPerTrace + "; " + stdGapPerTrace + "; "
                + minLengthGap + "; " + maxLengthGap + "; "
                + averageLengthGap + "; " + stdLengthGap);

        return s;
    }

}
