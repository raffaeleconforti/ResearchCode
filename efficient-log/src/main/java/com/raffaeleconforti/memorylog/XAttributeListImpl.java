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
