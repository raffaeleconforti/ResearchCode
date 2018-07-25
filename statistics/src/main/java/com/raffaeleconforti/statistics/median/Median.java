package com.raffaeleconforti.statistics.median;

import com.raffaeleconforti.statistics.StatisticsMeasureAbstract;

import java.util.Arrays;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 14/11/16.
 */
public class Median extends StatisticsMeasureAbstract {

    @Override
    public double evaluate(Double val, double... values) {
        try {
            values = Arrays.copyOf(values, values.length);
            Arrays.sort(values);
            int pos = Math.round(values.length / 2) - 1;
            if(pos < 0) pos = 0;
            return values[pos];
        }catch (ArrayIndexOutOfBoundsException e) {

        }
        return 0;
    }

}
