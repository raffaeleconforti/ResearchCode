package com.raffaeleconforti.statistics.min;

import com.raffaeleconforti.statistics.StatisticsMeasure;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 14/11/16.
 */
public class Min implements StatisticsMeasure {

    @Override
    public double evaluate(Double val, double... values) {
        try {
            double min = Double.MAX_VALUE;
            for(double v : values) {
                min = Math.min(min, v);
            }
            return min;
        }catch (ArrayIndexOutOfBoundsException e) {

        }
        return 0;
    }

}
