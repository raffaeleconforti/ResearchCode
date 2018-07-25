package com.raffaeleconforti.statistics.percentile;

import com.raffaeleconforti.statistics.StatisticsMeasureAbstract;

import java.util.Arrays;

/**
 * Created by conforti on 11/02/15.
 */
public class Percentile extends StatisticsMeasureAbstract {

    @Override
    public double evaluate(Double percentile, double... values) {
        try {
            values = Arrays.copyOf(values, values.length);
            Arrays.sort(values);
            int pos = (int) Math.round(values.length * percentile) - 1;
            if(pos < 0) pos = 0;
            return values[pos];
        }catch (ArrayIndexOutOfBoundsException e) {

        }
        return 0;
    }
}
