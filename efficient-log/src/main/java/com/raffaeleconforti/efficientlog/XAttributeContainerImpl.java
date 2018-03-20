package com.raffaeleconforti.efficientlog;

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
