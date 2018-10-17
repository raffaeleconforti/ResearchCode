/*
 *  Copyright (C) 2018 Raffaele Conforti (www.raffaeleconforti.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.raffaeleconforti.memorylog;

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

