package com.raffaeleconforti.log.util;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.*;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;

import java.util.Date;
import java.util.Map;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 16/02/15.
 */
public class LogOptimizer {

    private final ConcurrentHashMap<Object, Object> map = new ConcurrentHashMap<>();
    private final XFactory factory;

    public LogOptimizer() {
//        factory = new XFactoryMemoryImpl();
        factory = new XFactoryNaiveImpl();
    }

    public LogOptimizer(XFactory factory) {
        this.factory = factory;
    }

    public XLog optimizeLog(XLog log) {

        XLog result = factory.createLog();

        for(Map.Entry<String, XAttribute> entry : log.getAttributes().entrySet()) {
            String key = (String) getObject(entry.getKey());
            Object value = getObject(getAttributeValue(entry.getValue()));
            XAttribute attribute = createXAttribute(key, value, entry.getValue());

            result.getAttributes().put(key, attribute);
        }

        for(XTrace trace : log) {
            XTrace newTrace = factory.createTrace();

            for(Map.Entry<String, XAttribute> entry : trace.getAttributes().entrySet()) {
                String key = (String) getObject(entry.getKey());
                Object value = getObject(getAttributeValue(entry.getValue()));
                XAttribute attribute = createXAttribute(key, value, entry.getValue());

                newTrace.getAttributes().put(key, attribute);
            }

            for(XEvent event : trace) {
                XEvent newEvent = factory.createEvent();

                for(Map.Entry<String, XAttribute> entry : event.getAttributes().entrySet()) {
                    String key = (String) getObject(entry.getKey());
                    Object value = getObject(getAttributeValue(entry.getValue()));
                    XAttribute attribute = createXAttribute(key, value, entry.getValue());

                    newEvent.getAttributes().put(key, attribute);
                }

                newTrace.add(newEvent);
            }

            result.add(newTrace);
        }

        return result;
    }

    public Map<Object, Object> getReductionMap() {
        return map;
    }

    private Object getObject(Object o) {
        Object result;
        if(o instanceof Date) return o;
        if((result = map.get(o)) == null) {
            map.put(o, o);
            result = o;
        }
        return result;
    }

    private Object getAttributeValue(XAttribute attribute) {
        if(attribute instanceof XAttributeLiteral) return ((XAttributeLiteral) attribute).getValue();
        else if(attribute instanceof XAttributeBoolean) return ((XAttributeBoolean) attribute).getValue();
        else if(attribute instanceof XAttributeDiscrete) return ((XAttributeDiscrete) attribute).getValue();
        else if(attribute instanceof XAttributeContinuous) return ((XAttributeContinuous) attribute).getValue();
        else if(attribute instanceof XAttributeTimestamp) return ((XAttributeTimestamp) attribute).getValue();
        else {
            System.out.println(attribute);
            System.out.println("Log Attribute Error getAttributeValue");
            return null;
        }
    }

    private XAttribute createXAttribute(String key, Object value, XAttribute originalAttribute) {
        XAttribute result;
        if(originalAttribute instanceof XAttributeLiteral) result = factory.createAttributeLiteral(key, (String) value, null);
        else if(originalAttribute instanceof XAttributeBoolean) result = factory.createAttributeBoolean(key, (Boolean) value, null);
        else if(originalAttribute instanceof XAttributeDiscrete) result = factory.createAttributeDiscrete(key, (Long) value, null);
        else if(originalAttribute instanceof XAttributeContinuous) result = factory.createAttributeContinuous(key, (Double) value, null);
        else if(originalAttribute instanceof XAttributeTimestamp) result = factory.createAttributeTimestamp(key, (Date) value, null);
        else {
            System.out.println("Log Attribute Error createXAttribute");
            return null;
        }
        return result;
    }
}
