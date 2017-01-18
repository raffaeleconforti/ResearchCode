package com.raffaeleconforti.statistics.medianabsolutedeviation;

import com.raffaeleconforti.statistics.StatisticsMeasure;
import com.raffaeleconforti.statistics.median.Median;

import java.util.Arrays;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 14/11/16.
 */
public class MedianAbsoluteDeviation implements StatisticsMeasure {

    private Median median = new Median();

    @Override
    public double evaluate(Double val, double... values) {
        try {
            values = Arrays.copyOf(values, values.length);
            Arrays.sort(values);
            double med = median.evaluate(null, values);
            double[] vals = new double[values.length];
            for(int i = 0; i < vals.length; i++) {
                vals[i] = Math.abs(values[i] - med);
            }
            return 1.4826 * median.evaluate(null, vals);
        }catch (ArrayIndexOutOfBoundsException e) {

        }
        return 0;
    }
}
