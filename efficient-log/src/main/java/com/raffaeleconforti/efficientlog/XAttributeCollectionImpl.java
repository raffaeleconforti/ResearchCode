package com.raffaeleconforti.efficientlog;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeCollection;

import java.util.Collection;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/08/2016.
 */
public class XAttributeCollectionImpl extends XAttributeLiteralImpl implements XAttributeCollection {

    /**
     *
     */
    private static final long serialVersionUID = 4322597532345796274L;

    protected Collection<XAttribute> collection;

    /**
     * @param key
     */
    public XAttributeCollectionImpl(String key) {
        super(key, "", null);
    }

    /**
     * @param key
     * @param extension
     */
    public XAttributeCollectionImpl(String key, XExtension extension) {
        super(key, "", extension);
    }

    public void addToCollection(XAttribute attribute) {
        if (collection != null) {
            collection.add(attribute);
        }
    }

    public Collection<XAttribute> getCollection() {
        return collection != null ? collection : getAttributes().values();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        String sep = "[";
        for (XAttribute attribute: getCollection()) {
            buf.append(sep);
            sep = ",";
            buf.append(attribute.getKey());
            buf.append(":");
            buf.append(attribute.toString());
        }
        if (buf.length() == 0) {
            buf.append("[");
        }
        buf.append("]");
        return buf.toString();
    }
}

