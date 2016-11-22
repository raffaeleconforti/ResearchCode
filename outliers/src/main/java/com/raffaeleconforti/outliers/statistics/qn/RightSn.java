package com.raffaeleconforti.outliers.statistics.qn;

import com.raffaeleconforti.outliers.statistics.median.Median;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 22/11/16.
 */
public class RightSn {

    public static double evaluate(double... values) {
        try {
            double[] v = new double[values.length / 2];
            int pos1 = 0;
            for(int i = values.length / 2; i < values.length; i++) {
                double[] v1 = new double[values.length / 2];
                int pos2 = 0;
                for(int j = values.length / 2; j < values.length; j++) {
                    v1[pos2] = Math.abs(values[i] - values[j]);
                    pos2++;
                }
                v[pos1] = Median.evaluate(v1);
                pos1++;
            }
            return 1.1925 * Median.evaluate(v);
        }catch (ArrayIndexOutOfBoundsException e) {

        }
        return 0;
    }

}
