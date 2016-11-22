package com.raffaeleconforti.outliers.statistics.mad;

import com.raffaeleconforti.outliers.statistics.median.Median;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 14/11/16.
 */
public class RightMedianAbsoluteDeviation {

    public static double evaluate(double... values) {
        try {
            double median = Median.evaluate(values);
            double[] vals = new double[values.length / 2];
            int pos = 0;
            for(int i = values.length / 2; i < values.length; i++) {
                vals[pos] = Math.abs(values[i] - median);
                pos++;
            }
            return 1.4826 * Median.evaluate(vals);
        }catch (ArrayIndexOutOfBoundsException e) {

        }
        return 0;
    }

}
