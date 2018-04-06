package com.raffaeleconforti.foreignkeydiscovery.histogram;

import com.raffaeleconforti.foreignkeydiscovery.Cell;
import com.raffaeleconforti.foreignkeydiscovery.databasestructure.Tuple;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

import java.util.Set;

/**
 * Created by Raffaele Conforti on 17/10/14.
 */
public interface Histogram {

    UnifiedMap<Cell, Double> getHistogram();
    UnifiedMap<Cell, Set<Tuple<String>>> getQuantileHistogram();
    int getQuantiles();

}
