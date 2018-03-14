package com.raffaeleconforti.statistics.medianabsolutedeviation;

import com.raffaeleconforti.statistics.StatisticsMeasure;
import com.raffaeleconforti.statistics.median.Median;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;

import java.util.Arrays;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 14/11/16.
 */
public class LeftMedianAbsoluteDeviation implements StatisticsMeasure {

    private Median median = new Median();

    @Override
    public double evaluate(Double val, double... values) {
        try {
            values = Arrays.copyOf(values, values.length);
            Arrays.sort(values);
            double med = median.evaluate(null, values);
            DoubleArrayList vals = new DoubleArrayList();
            for(int i = 0; i < values.length && values[i] <= val; i++) {
                vals.add(Math.abs(values[i] - med));
            }
            return 1.4826 * median.evaluate(null, vals.toArray());
        }catch (ArrayIndexOutOfBoundsException e) {

        }
        return 0;
    }

}
