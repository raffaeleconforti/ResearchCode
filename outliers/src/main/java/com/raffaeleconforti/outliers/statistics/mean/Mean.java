package com.raffaeleconforti.outliers.statistics.mean;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 14/11/16.
 */
public class Mean {

    public static double evaluate(double... values) {
        try {
            double mean = 0;
            for(double v : values) {
                mean += v;
            }
            return mean / values.length;
        }catch (ArrayIndexOutOfBoundsException e) {

        }
        return 0;
    }

}
