package com.raffaeleconforti.statistics.mode;

import com.raffaeleconforti.statistics.StatisticsMeasure;

import java.util.Arrays;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 14/11/16.
 */
public class Mode implements StatisticsMeasure {

    @Override
    public double evaluate(Double val, double... values) {
        try {
            values = Arrays.copyOf(values, values.length);
            Arrays.sort(values);

            Double mode = null;
            int modeCount = 0;

            Double value = null;
            int count = 0;

            for(double v : values) {
                if(value == null || value != v) {
                    value = v;
                    count = 0;
                }
                count++;

                if(count > modeCount) {
                    modeCount = count;
                    mode = value;
                }
            }

            return mode;
        }catch (ArrayIndexOutOfBoundsException e) {

        }
        return 0;
    }

}
