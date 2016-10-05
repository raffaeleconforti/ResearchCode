package com.raffaeleconforti.log.util;

import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.*;

import java.util.*;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 16/02/15.
 */
public class LogModifier {

    private XFactory factory = null;
    private XConceptExtension xce = null;
    private XTimeExtension xte = null;
    private Map<Object, Object> map = null;

    private final Comparator<XEvent> comparatorXEvent = new Comparator<XEvent>() {
        @Override
        public int compare(XEvent o1, XEvent o2) {
            Date d1 = xte.extractTimestamp(o1);
            Date d2 = xte.extractTimestamp(o2);
            return d1.compareTo(d2);
        }

        @Override
        public boolean equals(Object obj) {
            return false;
        }
    };

    private Comparator<XTrace> comparatorXTrace = new Comparator<XTrace>() {
        @Override
        public int compare(XTrace o1, XTrace o2) {
            String s1 = xce.extractName(o1);
            String s2 = xce.extractName(o2);
            return s1.compareTo(s2);
        }

        @Override
        public boolean equals(Object obj) {
            return false;
        }
    };

    public LogModifier(XFactory factory, XConceptExtension xce, XTimeExtension xte, LogOptimizer logOptimizer) {
        this.factory = factory;
        this.xce = xce;
        this.xte = xte;
        map = logOptimizer.getReductionMap();
    }

    public XLog insertArtificialStartAndEndEvent(XLog log) {

        for (XTrace trace : log) {
            XEvent start = factory.createEvent();
            xce.assignName(start, "ArtificialStartEvent");
            xte.assignTimestamp(start, new Date(Long.MIN_VALUE));

            XEvent end = factory.createEvent();
            xce.assignName(end, "ArtificialEndEvent");
            xte.assignTimestamp(end, new Date(Long.MAX_VALUE));

            trace.add(0, start);
            trace.add(trace.size(), end);
        }
        return log;

    }

    public XLog removeArtificialStartAndEndEvent(XLog log) {

        for (XTrace trace : log) {
            Set<XEvent> remove = new UnifiedSet<>();
            for (XEvent event : trace) {
                if (xce.extractName(event).contains("ArtificialStartEvent")) {
                    remove.add(event);
                }

                if (xce.extractName(event).contains("ArtificialEndEvent")) {
                    remove.add(event);
                }
            }

            for (XEvent event : remove) {
                trace.remove(event);
            }
        }
        return log;

    }

    public XLog sortLog(XLog log) {
        XLog newLog = factory.createLog(log.getAttributes());
        List<XEvent> events;
        for (XTrace trace : log) {
            events = new ArrayList<>();
            for (XEvent event : trace) {
                events.add(event);
            }
            Collections.sort(events, comparatorXEvent);
            XTrace newTrace = factory.createTrace(trace.getAttributes());

            for (XEvent event : events) {
                newTrace.add(event);
            }
            newLog.add(newTrace);
        }
        return newLog;
    }

    public boolean sameEvent(XEvent event, XEvent e, boolean logOptimizerUsed) {
        for (Map.Entry<String, XAttribute> entry : event.getAttributes().entrySet()) {
            if(logOptimizerUsed) {
                if (entry.getValue() != e.getAttributes().get(entry.getKey())) {
                    return false;
                }
            }else {
                if (!getAttributeValue(entry.getValue()).equals(getAttributeValue(e.getAttributes().get(entry.getKey())))) {
                    return false;
                }
            }
        }
        return true;
    }

    public String getAttributeValue(XAttribute attribute) {
        String value = null;
        if (attribute instanceof XAttributeBoolean) {
            value = Boolean.toString(((XAttributeBoolean) attribute).getValue());
        } else if (attribute instanceof XAttributeContinuous) {
            value = Double.toString(((XAttributeContinuous) attribute).getValue());
        } else if (attribute instanceof XAttributeDiscrete) {
            value = Long.toString(((XAttributeDiscrete) attribute).getValue());
        } else if (attribute instanceof XAttributeLiteral) {
            value = ((XAttributeLiteral) attribute).getValue();
        } else if (attribute instanceof XAttributeTimestamp) {
            value = ((XAttributeTimestamp) attribute).getValue().toString();
        }
        return getObject(value);
    }


    private String getObject(String o) {
        String result = null;
        if(o != null && (result = (String) map.get(o)) == null) {
            map.put(o, o);
            result = o;
        }
        return result;
    }

}
