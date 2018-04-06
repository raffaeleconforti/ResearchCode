package com.raffaeleconforti.foreignkeydiscovery.functionaldependencies;


import org.deckfour.xes.model.XAttribute;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.ArrayList;


/**
 * Data structure for moving data from parser to tane algorithm
 * Contains the data for one event type
 *
 * @author Tanel Teinemaa, Viara Popova
 */
public class Data {

    public String title;

    public String dataType;

    public String[] columnTitles;

    public ArrayList<XAttribute> timestamps;

    public ArrayList<UnifiedMap<String, XAttribute>> table;

    public ArrayList<UnifiedSet<String>> keys;

    public UnifiedSet<String> primaryKey;

    public Data() {
        table = new ArrayList<UnifiedMap<String, XAttribute>>();
        timestamps = new ArrayList<XAttribute>();
    }

    public String getDataType() {
        return dataType;
    }

}
