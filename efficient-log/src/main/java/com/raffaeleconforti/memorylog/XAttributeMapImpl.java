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

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

import java.util.Map;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/08/2016.
 */
public class XAttributeMapImpl extends UnifiedMap<String, XAttribute> implements
        XAttributeMap {

    /**
     * serial version UID.
     */
    private static final long serialVersionUID = 2701256420845748051L;

    /**
     * Creates a new attribute map.
     */
    public XAttributeMapImpl() {
        this(0);
    }

    /**
     * Creates a new attribute map.
     *
     * @param size Initial size of the map.
     */
    public XAttributeMapImpl(int size) {
        super(size);
    }

    /**
     * Creates a new attribute map.
     *
     * @param template Copy the contents of this attribute
     * map to the new attrribute map.
     */
    public XAttributeMapImpl(Map<String,XAttribute> template) {
        super(template.size());
        for(String key : template.keySet()) {
            put(key, template.get(key));
        }
    }

    /**
     * Creates a clone, i.e. deep copy, of this attribute map.
     */
    public XAttributeMapImpl clone() {
        XAttributeMapImpl clone = new XAttributeMapImpl(this);
        clone.clear();
        for(String key : this.keySet()) {
            clone.put(key, (XAttribute) this.get(key).clone());
        }
        return clone;
    }

}
