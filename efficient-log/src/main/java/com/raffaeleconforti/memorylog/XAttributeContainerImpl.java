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
import org.deckfour.xes.model.XAttributeContainer;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/08/2016.
 */
public class XAttributeContainerImpl extends XAttributeCollectionImpl implements XAttributeContainer {

	/*
	 * For backwards compatibility, Container extends from Literal. As a result, software
	 * that is unaware of the Container may consider it to be a Literal.
	 */

    /**
     *
     */
    private static final long serialVersionUID = -2171609637065248221L;

    /**
     * @param key
     */
    public XAttributeContainerImpl(String key) {
        super(key, null);
    }

    public XAttributeContainerImpl(String key, XExtension extension) {
        // Dummy (but unique) key, dummy value, no extension.
        super(key, extension);
        // No separate collection required, existing map will do fine.
        collection = null;
    }
}
