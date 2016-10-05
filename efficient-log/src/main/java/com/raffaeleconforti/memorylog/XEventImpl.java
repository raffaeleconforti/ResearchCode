package com.raffaeleconforti.memorylog;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.id.XID;
import org.deckfour.xes.id.XIDFactory;
import org.deckfour.xes.model.*;
import org.deckfour.xes.util.XAttributeUtils;

import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/08/2016.
 */
public class XEventImpl implements XEvent {

    /**
     * ID of this event.
     */
    private XID id;

    /**
     * Map of attributes for this event.
     */
    private XAttributeMap attributes;

    /**
     * Creates a new event.
     */
    public XEventImpl() {
        this(XIDFactory.instance().createId(), new XAttributeMapImpl());
    }

    /**
     * Creates a new event with a given ID.
     *
     * @param id
     *            the id for this event
     */
    public XEventImpl(XID id) {
        this(id, new XAttributeMapImpl());
    }

    /**
     * Creates a new event.
     *
     * @param attributes
     *            Map of attribute for the event.
     */
    public XEventImpl(XAttributeMap attributes) {
        this(XIDFactory.instance().createId(), attributes);
    }

    /**
     * Creates a new event with the given id and attributed
     *
     * @param id
     *            the id for this event
     * @param attributes
     *            Map of attribute for the event.
     */
    public XEventImpl(XID id, XAttributeMap attributes) {
        this.id = id;
        this.attributes = attributes;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deckfour.xes.model.XAttributable#getAttributes()
     */
    public XAttributeMap getAttributes() {
        return attributes;
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
        return !attributes.isEmpty();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deckfour.xes.model.XAttributable#getExtensions()
     */
    public Set<XExtension> getExtensions() {
        return XAttributeUtils.extractExtensions(attributes);
    }

    /**
     * Clones this event, i.e. creates a deep copy, but with a new ID, so equals
     * does not hold between this and the clone
     */
    public Object clone() {
        XEventImpl clone;
        try {
            clone = (XEventImpl) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
        clone.id = XIDFactory.instance().createId();
        clone.attributes = (XAttributeMap) attributes.clone();
        return clone;
    }

    /**
     * Tests for equality of IDs
     */
    public boolean equals(Object o) {
        if (o instanceof XEventImpl) {
            return ((XEventImpl) o).id.equals(id);
        } else {
            return false;
        }
    }

    /**
     * Returns the hashCode of the id
     */
    public int hashCode() {
        return id.hashCode();
    }

    /*
     * (non-Javadoc)
     * @see org.deckfour.xes.model.XEvent#getID()
     */
    public XID getID() {
        return id;
    }

    /**
     * Sets the ID. Should only be used for deserialization purposes
     *
     * @param id
     *            the new id.
     */
    public void setID(XID id) {
        this.id = id;
    }

    /*
     * Runs the given visitor for the given trace on this event.
     *
     * (non-Javadoc)
     * @see org.deckfour.xes.model.XEvent#accept(org.deckfour.xes.model.XVisitor, org.deckfour.xes.model.XTrace)
     */
    public void accept(XVisitor visitor, XTrace trace) {
		/*
		 * First call.
		 */
        visitor.visitEventPre(this, trace);
		/*
		 * Visit the attributes.
		 */
        for (XAttribute attribute: attributes.values()) {
            attribute.accept(visitor, this);
        }
		/*
		 * Last call.
		 */
        visitor.visitEventPost(this, trace);
    }

}
