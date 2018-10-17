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
import org.deckfour.xes.model.XAttributeList;

import java.util.ArrayList;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/08/2016.
 */
public class XAttributeListImpl extends XAttributeCollectionImpl implements XAttributeList {

	/*
	 * For backwards compatibility, List extends from Literal. As a result, software
	 * that is unaware of the List may consider it to be a Literal.
	 */

    /**
     *
     */
    private static final long serialVersionUID = 5584421551344100844L;

    /**
     * @param key
     */
    public XAttributeListImpl(String key) {
        super(key, null);
    }

    /**
     * @param key
     * @param extension
     */
    public XAttributeListImpl(String key, XExtension extension) {
        super(key, extension);
        // Order attributes by appearance.
        collection = new ArrayList<XAttribute>();
    }
}
