package com.raffaeleconforti.outliers.statistics.median;

import com.raffaeleconforti.outliers.statistics.boxplot.Percentile;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 14/11/16.
 */
public class Median {

    public static double evaluate(double... values) {
        try {
            int pos = Math.round(values.length / 2) - 1;
            if(pos < 0) pos = 0;
            return values[pos];
        }catch (ArrayIndexOutOfBoundsException e) {

        }
        return 0;
    }

}
