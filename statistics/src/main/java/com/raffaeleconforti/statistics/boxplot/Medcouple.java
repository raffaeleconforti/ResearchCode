package com.raffaeleconforti.statistics.boxplot;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by conforti on 11/02/15.
 */
public class Medcouple {

    public static double evaluate(double... values) {
        double[] x = Arrays.copyOf(values, values.length);
        Arrays.sort(x);

        double median = median(x);
        ArrayList<Double> res = new ArrayList<Double>();

        for(int i = 0; i < x.length && x[i] <= median; i++) {
            for(int j = 0; j < x.length; j++) {
                if(x[j] >= median) {
                    res.add(kernel(x[i], x[j], median));
                }
            }
        }

        Double[] result = res.toArray(new Double[res.size()]);
        Arrays.sort(result);
        return median(result);
    }

    private static double median(Double... values) {
        if(values.length % 2 == 0) {
            int half = values.length / 2;
            return (values[half - 1] + values[half]) / 2.0;
        }else {
            int half = (int) Math.floor(values.length / 2);
            return values[half];
        }
    }

    private static double median(double... values) {
        if(values.length % 2 == 0) {
            int half = values.length / 2;
            return (values[half - 1] + values[half]) / 2.0;
        }else {
            int half = (int) Math.floor(values.length / 2);
            return values[half];
        }
    }

    private static double kernel(double x_i, double x_j, double median) {
        return ((x_j - median) - (median - x_i)) / (x_j - x_i);
    }

}
