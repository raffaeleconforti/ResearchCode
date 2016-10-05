package com.raffaeleconforti.memorylog;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.*;
import org.deckfour.xes.util.XAttributeUtils;

import java.util.Collections;
import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/08/2016.
 */
public class XAttributeImpl implements XAttribute {

    /**
     *
     */
    private static final long serialVersionUID = 2570374546119649178L;

    /**
     * Key, i.e. unique name, of this attribute. If the attribute is defined in
     * an extension, its key will be prepended with the extension's defined
     * prefix string.
     */
    private final String key;
    /**
     * The extension defining this attribute. May be <code>null</code>, if this
     * attribute is not defined by an extension.
     */
    private final XExtension extension;
    /**
     * Map of meta-attributes, i.e. attributes of this attribute.
     */
    private XAttributeMap attributes;

    /**
     * Creates a new, empty attribute.
     *
     * @param key
     *            The key, i.e. unique name identifier, of this attribute.
     */
    protected XAttributeImpl(String key) {
        this(key, null);
    }

    /**
     * Creates a new attribute.
     *
     * @param key
     *            The key, i.e. unique name identifier, of this attribute.
     * @param extension
     *            The extension used for defining this attribute.
     */
    protected XAttributeImpl(String key, XExtension extension) {
        this.key = key;
        this.extension = extension;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deckfour.xes.model.impl.XAttribute#getKey()
     */
    public String getKey() {
        return key;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deckfour.xes.model.impl.XAttribute#getExtension()
     */
    public XExtension getExtension() {
        return extension;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deckfour.xes.model.impl.XAttribute#getAttributes()
     */
    public XAttributeMap getAttributes() {
        // This is not thread-safe, but we don't give any thread safety guarantee anyway
        if (attributes == null) {
            this.attributes = new XAttributeMapLazyImpl<XAttributeMapImpl>(
                    XAttributeMapImpl.class); // uses lazy implementation by default
        }
        return attributes;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.deckfour.xes.model.impl.XAttribute#setAttributes(org.deckfour.xes
     * .model.XAttributeMap)
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

    /*
     * (non-Javadoc)
     *
     * @see org.deckfour.xes.model.impl.XAttribute#getExtensions()
     */
    public Set<XExtension> getExtensions() {
        if (attributes != null) {
            return XAttributeUtils.extractExtensions(getAttributes());
        } else {
            return Collections.emptySet();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deckfour.xes.model.impl.XAttribute#clone()
     */
    public Object clone() {
        XAttributeImpl clone = null;
        try {
            clone = (XAttributeImpl) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
        if (attributes != null) {
            clone.attributes = (XAttributeMap) getAttributes().clone();
        }
        return clone;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof XAttribute) {
            XAttribute other = (XAttribute) obj;
            return other.getKey().equals(key);
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return key.hashCode();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(XAttribute o) {
        return key.compareTo(o.getKey());
    }

    /*
     * Runs the given visitor for the given parent on this attribute.
     *
     * (non-Javadoc)
     * @see org.deckfour.xes.model.XAttribute#accept(org.deckfour.xes.model.XVisitor, org.deckfour.xes.model.XAttributable)
     */
    public void accept(XVisitor visitor, XAttributable parent) {
		/*
		 * First call.
		 */
        visitor.visitAttributePre(this, parent);
        if (this instanceof XAttributeCollection) {
			/*
			 * Visit the (meta) attributes using the order a specified by the collection.
			 */
            for (XAttribute attribute: ((XAttributeCollection) this).getCollection()) {
                attribute.accept(visitor, this);
            }
        } else {
			/*
			 * Visit the (meta) attributes.
			 */
            if (attributes != null) {
                for (XAttribute attribute: getAttributes().values()) {
                    attribute.accept(visitor, this);
                }
            }
        }
		/*
		 * Last call.
		 */
        visitor.visitAttributePost(this, parent);
    }
}
