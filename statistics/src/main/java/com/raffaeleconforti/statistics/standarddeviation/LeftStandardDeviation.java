package com.raffaeleconforti.statistics.standarddeviation;

import com.raffaeleconforti.statistics.StatisticsMeasure;
import com.raffaeleconforti.statistics.mean.Mean;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 14/11/16.
 */
public class LeftStandardDeviation implements StatisticsMeasure {

    private Mean mean = new Mean();

    @Override
    public double evaluate(Double val, double... values) {
        try {
            double avg = mean.evaluate(null, values);
            double sd = 0;
            int count = 0;
            for(int i = 0; i < values.length; i++) {
                if(values[i] <= val) {
                    sd += Math.pow((values[i] - avg), 2);
                    count++;
                }
            }
            return Math.sqrt(sd / count);
        }catch (ArrayIndexOutOfBoundsException e) {

        }
        return 0;
    }

}
