package com.raffaeleconforti.outliers.statistics.boxplot;

import java.util.ArrayList;

/**
 * Created by conforti on 11/02/15.
 */
public class BoxPlot {

    public int[] discoverOutlier(double[] values, double significance) {
        ArrayList<Integer> result = new ArrayList<Integer>();

        double IQR = computeIQR(values);
        double lower = computeLowerBound(values, IQR);
        double upper = computeUpperBound(values, IQR);

        for(int j = 0; j < values.length; j++) {
            if (values[j] < lower || values[j] > upper) {
                result.add(j);
            }
        }

        int[] pos = new int[result.size()];
        int i = 0;
        for(Integer r : result) {
            pos[i] = r;
            i++;
        }

        return pos;
    }

    private double computeIQR(double[] values) {
        return (Percentile.evaluate(0.75, values) - Percentile.evaluate(0.25, values));
    }

    public double computeLowerBound(double[] values, double IQR) {
        return (Percentile.evaluate(0.25, values) - (1.5 * IQR));
    }

    public double computeUpperBound(double[] values, double IQR) {
        return (Percentile.evaluate(0.75, values) + (1.5 * IQR));
    }
}
