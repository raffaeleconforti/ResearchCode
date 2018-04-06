package com.raffaeleconforti.noisefiltering.timestamp.noise;

import com.raffaeleconforti.log.util.LogCloner;
import com.raffaeleconforti.log.util.NameExtractor;
import com.raffaeleconforti.log.util.TraceToString;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeBooleanImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

import java.util.*;

/**
 * Created by conforti on 8/02/15.
 */

public class TimeStampNoiseGenerator {

    final NameExtractor nameExtractor = new NameExtractor(new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier()));
    final XTimeExtension xte = XTimeExtension.instance();

    public XLog insertNoiseTotalTracesEvents(XLog rawlog, double percentageTraces, double percentageEvents) {
        LogCloner logCloner = new LogCloner();
        XLog log = logCloner.cloneLog(rawlog);

        Random r = new Random(123456789);
        double[] changedTraces = new double[] {0.0};
        double[] changedEvents = new double[] {0.0};

        int events = 0;
        for(XTrace t : log) {
            events += t.size();
        }

        while(changedTraces[0] / log.size() < percentageTraces || changedEvents[0] / events < percentageEvents) {
            System.out.println("%Traces " + changedTraces[0] / log.size());
            System.out.println("%Events " + changedEvents[0] / events);

            int pos = r.nextInt(log.size());
            XTrace trace = log.get(pos);
            if(changedTraces[0] / log.size() < percentageTraces) {
                if (trace.getAttributes().get("change") == null) {
                    insertNoise(trace, r, changedEvents, changedTraces);
                }
            }else {
                if(percentageTraces > 0) {
                    while (trace.getAttributes().get("change") == null) {
                        pos = r.nextInt(log.size());
                        trace = log.get(pos);
                    }
                }else {
                    while (trace.getAttributes().get("change") != null) {
                        pos = r.nextInt(log.size());
                        trace = log.get(pos);
                    }
                }
                insertNoise(trace, r, changedEvents, changedTraces);
            }
        }

        return log;
    }

    public XLog insertNoiseUniqueTracesEvents(XLog rawlog, double percentageUniqueTraces, double percentageEvents) {
        LogCloner logCloner = new LogCloner();
        XLog log = logCloner.cloneLog(rawlog);

        Map<String, List<XTrace>> uniqueTraces = detectUniqueTraces(log);

        Random r = new Random(123456789);
        double[] changedTraces = new double[] {0.0};
        double[] changedUniqueTraces = new double[] {0.0};
        double[] changedEvents = new double[] {0.0};

        int events = 0;
        for(XTrace t : log) {
            events += t.size();
        }

        Map.Entry<String, List<XTrace>>[] entries = uniqueTraces.entrySet().toArray(new Map.Entry[uniqueTraces.size()]);

        while(changedUniqueTraces[0] / uniqueTraces.size() < percentageUniqueTraces || changedEvents[0] / events < percentageEvents) {
            int pos = r.nextInt(entries.length);
            List<XTrace> traces = entries[pos].getValue();

            if(changedUniqueTraces[0] / uniqueTraces.size() < percentageUniqueTraces) {
                boolean done = false;
                for (XTrace trace : traces) {
                    if (trace.getAttributes().get("change") == null) {
                        boolean res = false;
                        while (!res) {
                            res = insertNoise(trace, r, changedEvents, changedTraces);
                        }
                        done = true;
                    }
                }
                if(done) changedUniqueTraces[0]++;
            }else {
                if(percentageUniqueTraces > 0) {
                    while (traces.get(0).getAttributes().get("change") == null) {
                        pos = r.nextInt(entries.length);
                        traces = entries[pos].getValue();
                    }
                }else {
                    while (traces.get(0).getAttributes().get("change") != null) {
                        pos = r.nextInt(entries.length);
                        traces = entries[pos].getValue();
                    }
                }

                for(XTrace trace : traces) {
                    insertNoise(trace, r, changedEvents, changedTraces);
                }
            }
        }

        return log;
    }

    private boolean insertNoise(XTrace trace, final Random r, double[] changedEvents, double[] changedTraces) {
        int number = r.nextInt(trace.size() - 1);
        int start = r.nextInt((trace.size() - 1) - number);

        if (number > 0) {
            trace.getAttributes().put("change", new XAttributeBooleanImpl("change", true));
            Date date = null;

            if(number == 1) {
                if(nameExtractor.getEventName(trace.get(start)).equals(nameExtractor.getEventName(trace.get(start + 1)))) {
                    return false;
                }
            }

            for (int i = 0; i < trace.size(); i++) {
                XEvent event = trace.get(i);
                if (i == start) {
                    if(event.getAttributes().get("originalTimeStamp") == null) {
                        event.getAttributes().put("originalTimeStamp", new XAttributeTimestampImpl("originalTimeStamp", xte.extractTimestamp(event)));
                    }

                    date = xte.extractTimestamp(event);
                    event.getAttributes().put("change", new XAttributeBooleanImpl("change", true));
                    changedEvents[0]++;
                } else if (i > start && i <= start + number) {
                    if(event.getAttributes().get("originalTimeStamp") == null) {
                        event.getAttributes().put("originalTimeStamp", new XAttributeTimestampImpl("originalTimeStamp", xte.extractTimestamp(event)));
                    }

                    xte.assignTimestamp(event, date);
                    event.getAttributes().put("change", new XAttributeBooleanImpl("change", true));
                    changedEvents[0]++;
                }
            }

            String oldTrace = TraceToString.convertXTraceToString(trace, nameExtractor);

            while(oldTrace.equals(TraceToString.convertXTraceToString(trace, nameExtractor))) {
                Collections.sort(trace, new Comparator<XEvent>() {
                    Random random = r;

                    @Override
                    public int compare(XEvent o1, XEvent o2) {
                        Date date1 = xte.extractTimestamp(o1);
                        Date date2 = xte.extractTimestamp(o2);
                        if (!date1.equals(date2)) return date1.compareTo(date2);
                        else return random.nextInt(2) > 0 ? 1 : -1;
                    }
                });
            }

            changedTraces[0]++;
            return true;
        }

        return false;
    }



    public XLog insertNoiseGapNumberAndLenght(XLog log, double totalGaps, double minGapLength, double maxGapLength, double averageGapLength, double minGapNumber, double maxGapNumber, double averageGapNumber) {
        return null;
    }

    private Map<String, List<XTrace>> detectUniqueTraces(XLog log) {
        Map<String, List<XTrace>> uniqueTraces = new UnifiedMap<>();
        for(XTrace trace : log) {
            String s = TraceToString.convertXTraceToString(trace, nameExtractor);
            List<XTrace> list;
            if ((list = uniqueTraces.get(s)) == null) {
                list = new ArrayList<>();
                uniqueTraces.put(s, list);
            }
            list.add(trace);
        }
        return uniqueTraces;
    }
}
