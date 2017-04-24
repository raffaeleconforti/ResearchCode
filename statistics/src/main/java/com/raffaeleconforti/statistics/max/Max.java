package com.raffaeleconforti.statistics.max;

import com.raffaeleconforti.statistics.StatisticsMeasure;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 14/11/16.
 */
public class Max implements StatisticsMeasure {

    @Override
    public double evaluate(Double val, double... values) {
        try {
            double max = -Double.MAX_VALUE;
            for(double v : values) {
                max = Math.max(max, v);
            }
            return max;
        }catch (ArrayIndexOutOfBoundsException e) {

        }
        return 0;
    }

}
