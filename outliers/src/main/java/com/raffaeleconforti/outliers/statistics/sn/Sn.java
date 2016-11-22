package com.raffaeleconforti.outliers.statistics.sn;

import com.raffaeleconforti.outliers.statistics.median.Median;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 22/11/16.
 */
public class Sn {

    public static double evaluate(double... values) {
        try {
            double[] v = new double[values.length];
            for(int i = 0; i < values.length; i++) {
                double[] v1 = new double[values.length];
                for(int j = 0; j < values.length; j++) {
                    v1[i] = Math.abs(values[i] - values[j]);
                }
                v[i] = Median.evaluate(v1);
            }
            return 1.1925 * Median.evaluate(v);
        }catch (ArrayIndexOutOfBoundsException e) {

        }
        return 0;
    }

}
