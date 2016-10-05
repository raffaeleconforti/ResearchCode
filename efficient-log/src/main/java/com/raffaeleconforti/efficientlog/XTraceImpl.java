package com.raffaeleconforti.efficientlog;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.*;
import org.deckfour.xes.util.XAttributeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/08/2016.
 */
public class XTraceImpl extends ArrayList<XEvent> implements XTrace {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 843122019760036963L;

    /**
     * Map of attributes for this trace.
     */
    private XAttributeMap attributes;

    /**
     * Creates a new trace.
     *
     * @param attributeMap
     *            Attribute map used to store this trace's attributes.
     */
    public XTraceImpl(XAttributeMap attributeMap) {
        this.attributes = attributeMap;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deckfour.xes.model.XAttributable#getAttributes()
     */
    public XAttributeMap getAttributes() {
        if(attributes == null) attributes = new XAttributeMapLazyImpl<XAttributeMapImpl>(XAttributeMapImpl.class);
        return attributes;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deckfour.xes.model.XAttributable#getExtensions()
     */
    public Set<XExtension> getExtensions() {
        return XAttributeUtils.extractExtensions(attributes);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deckfour.xes.model.XAttributable#setAttributes(java.util.Map)
     */
    public void setAttributes(XAttributeMap attributes) {
        this.attributes = attributes;
    }

    /* (non-Javadoc)
     * @see org.deckfour.xes.model.XAttributable#hasAttributes()
     */
    @Override
    public boolean hasAttributes() {
        return attributes != null && !attributes.isEmpty();
    }

    /**
     * Creates a clone, i.e. deep copy, of this trace.
     */
    public Object clone() {
        XTraceImpl clone = (XTraceImpl) super.clone();
        clone.attributes = (XAttributeMap) attributes.clone();
        clone.clear();
        for (XEvent event : this) {
            clone.add((XEvent) event.clone());
        }
        return clone;
    }

    public synchronized int insertOrdered(XEvent event) {
        if (this.size() == 0) {
            // append if list is empty
            add(event);
            return 0;
        }
        XAttribute insTsAttr = event.getAttributes().get(
                XTimeExtension.KEY_TIMESTAMP);
        if (insTsAttr == null) {
            // append if event has no timestamp
            add(event);
            return (size() - 1);
        }
        Date insTs = ((XAttributeTimestamp) insTsAttr).getValue();
        for (int i = (size() - 1); i >= 0; i--) {
            XAttribute refTsAttr = get(i).getAttributes().get(
                    XTimeExtension.KEY_TIMESTAMP);
            if (refTsAttr == null) {
                // trace contains events w/o timestamps, append.
                add(event);
                return (size() - 1);
            }
            Date refTs = ((XAttributeTimestamp) refTsAttr).getValue();
            if (insTs.before(refTs) == false) {
                // insert position reached
                add(i + 1, event);
                return (i + 1);
            }
        }
        // beginning reached, insert at head
        add(0, event);
        return 0;
    }

    /*
     * Runs the given visitor for the given log on this trace.
     *
     * (non-Javadoc)
     * @see org.deckfour.xes.model.XTrace#accept(org.deckfour.xes.model.XVisitor, org.deckfour.xes.model.XLog)
     */
    public void accept(XVisitor visitor, XLog log) {
		/*
		 * First call.
		 */
        visitor.visitTracePre(this, log);
		/*
		 * Visit the attributes.
		 */
        for (XAttribute attribute: attributes.values()) {
            attribute.accept(visitor, this);
        }
		/*
		 * Visit the events.
		 */
        for (XEvent event: this) {
            event.accept(visitor, this);
        }
		/*
		 * Last call.
		 */
        visitor.visitTracePost(this, log);
    }
}
