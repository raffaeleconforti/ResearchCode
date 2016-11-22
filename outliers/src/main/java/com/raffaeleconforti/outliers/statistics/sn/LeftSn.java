package com.raffaeleconforti.outliers.statistics.sn;

import com.raffaeleconforti.outliers.statistics.median.Median;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 22/11/16.
 */
public class LeftSn {

    public static double evaluate(double... values) {
        try {
            double[] v = new double[values.length / 2];
            for(int i = 0; i < v.length; i++) {
                double[] v1 = new double[values.length / 2];
                for(int j = 0; j < v.length; j++) {
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
