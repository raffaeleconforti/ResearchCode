package com.raffaeleconforti.foreignkeydiscovery.histogram.impl;

import com.raffaeleconforti.foreignkeydiscovery.Cell;
import com.raffaeleconforti.foreignkeydiscovery.databasestructure.Column;
import com.raffaeleconforti.foreignkeydiscovery.databasestructure.Tuple;
import com.raffaeleconforti.foreignkeydiscovery.histogram.Histogram;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Raffaele Conforti on 17/10/14.
 */
public class DistributionHistogram implements Histogram {

    private UnifiedMap<Cell, Set<Tuple<String>>> quantileDistributionHistogram = new UnifiedMap<Cell, Set<Tuple<String>>>();
    private UnifiedMap<Cell, Double> histogram = null;
    private int numberTuples = 0;
    private int numbersOfQuantiles;
    private Integer hashCode;

    public DistributionHistogram(Histogram histogram, TreeSet<Column> columns) {

        this.numbersOfQuantiles = histogram.getQuantiles();
//        Column column = columns.iterator().next();

//        UnifiedMap<String, Integer> columnNames = new UnifiedMap<String, Integer>();
        Integer pos = 0;
        for(Column c : columns) {
//            columnNames.put(c.getColumnName(), pos);
            pos++;
        }

        Tuple<String>[] setTuples = createTuples(columns);
        this.numberTuples = setTuples.length;
        Arrays.sort(setTuples);

        for (Map.Entry<Cell, Set<Tuple<String>>> entry : histogram.getQuantileHistogram().entrySet()) {

            Cell cell = entry.getKey();
            Set<Tuple<String>> value = entry.getValue();
            Set<Tuple<String>> set = new UnifiedSet<Tuple<String>>();

            for (Tuple<String> tuple : value) {
                if (Arrays.binarySearch(setTuples, tuple) > -1) {
                    set.add(tuple);
                }
            }

            quantileDistributionHistogram.put(cell, set);

        }

    }

    private Tuple<String>[] createTuples(Set<Column> columns) {
        Column[] arrayColumns = columns.toArray(new Column[columns.size()]);

        Tuple<String>[] tuples = new Tuple[arrayColumns[0].getColumnValues().getValues().length];

        for(int i = 0; i < tuples.length; i++) {
            String[] elements = new String[arrayColumns.length];
            for (int j = 0; j < arrayColumns.length; j++) {
                elements[j] = arrayColumns[j].getColumnValues().getValues()[i];
            }
            tuples[i] = new Tuple<String>(elements);
        }

        return  tuples;

    }

    @Override
    public UnifiedMap<Cell, Double> getHistogram() {
        if(histogram == null) {
            histogram = new UnifiedMap<Cell, Double>();
            for(Map.Entry<Cell, Set<Tuple<String>>> entry : quantileDistributionHistogram.entrySet()) {
                histogram.put(entry.getKey(), ((double) entry.getValue().size() / (double) numberTuples));
            }
        }
        return histogram;
    }

    @Override
    public UnifiedMap<Cell, Set<Tuple<String>>> getQuantileHistogram() {
        return quantileDistributionHistogram ;
    }

    @Override
    public int getQuantiles() {
        return numbersOfQuantiles;
    }

    @Override
    public int hashCode() {
        if(hashCode == null) {
            hashCode = quantileDistributionHistogram.hashCode();
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof DistributionHistogram) {
            DistributionHistogram dh = (DistributionHistogram) o;
            return dh.quantileDistributionHistogram.equals(quantileDistributionHistogram);
        }
        return false;
    }

    @Override
    public String toString() {
        return quantileDistributionHistogram.toString();
    }
}
