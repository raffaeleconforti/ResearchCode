package com.raffaeleconforti.statistics.mean;

import com.raffaeleconforti.statistics.StatisticsMeasureAbstract;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 14/11/16.
 */
public class Mean extends StatisticsMeasureAbstract {

    @Override
    public double evaluate(Double val, double... values) {
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
