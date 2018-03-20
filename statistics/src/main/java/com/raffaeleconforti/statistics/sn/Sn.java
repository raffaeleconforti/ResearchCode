package com.raffaeleconforti.statistics.sn;

import com.raffaeleconforti.statistics.StatisticsMeasure;
import com.raffaeleconforti.statistics.median.Median;

import java.util.Arrays;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 22/11/16.
 */
public class Sn implements StatisticsMeasure {

    private Median median = new Median();

    @Override
    public double evaluate(Double val, double... values) {
        try {
            values = Arrays.copyOf(values, values.length);
            Arrays.sort(values);

            double[] v = new double[values.length];
            for(int i = 0; i < values.length; i++) {
                double[] v1 = new double[values.length];
                for(int j = 0; j < values.length; j++) {
                    v1[i] = Math.abs(values[i] - values[j]);
                }
                v[i] = median.evaluate(null, v1);
            }
            return 1.1925 * median.evaluate(null, v);
        }catch (ArrayIndexOutOfBoundsException e) {

        }
        return 0;
    }

}
