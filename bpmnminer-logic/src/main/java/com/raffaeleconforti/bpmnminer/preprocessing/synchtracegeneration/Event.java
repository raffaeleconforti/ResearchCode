package com.raffaeleconforti.bpmnminer.preprocessing.synchtracegeneration;

import org.deckfour.xes.model.XAttribute;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

import java.util.Iterator;

public class Event {
    private String name;
    private UnifiedMap<String, XAttribute> data;

    public Event(String n, UnifiedMap<String, XAttribute> d) {
        this.name = n;
        this.data = d;
    }

    public String getName() {
        return name;
    }

    public UnifiedMap<String, XAttribute> getData() {
        return data;
    }

    public String getConcept() {
        return name;
    }

    public XAttribute getTimestamp() {
        Iterator<String> dataItr = data.keySet().iterator();
        XAttribute timestamp = null;
        while (dataItr.hasNext()) {
            String dataAttr = dataItr.next();
            if (name.equals(dataAttr)) {
                timestamp = data.get(dataAttr);
            }
        }
        return timestamp;
    }
}
