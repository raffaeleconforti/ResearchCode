package com.raffaeleconforti.outliers.statistics.median;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 14/11/16.
 */
public class MedianAbsoluteDeviantion {

    public static double evaluate(double... values) {
        try {
            double median = Median.evaluate(values);
            double[] vals = new double[values.length];
            for(int i = 0; i < vals.length; i++) {
                vals[i] = Math.abs(values[i] - median);
            }
            return Median.evaluate(vals);
        }catch (ArrayIndexOutOfBoundsException e) {

        }
        return 0;
    }
}
