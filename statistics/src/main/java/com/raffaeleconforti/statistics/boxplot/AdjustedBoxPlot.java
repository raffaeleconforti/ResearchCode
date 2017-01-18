package com.raffaeleconforti.statistics.boxplot;

import com.raffaeleconforti.statistics.percentile.Percentile;

/**
 * Created by conforti on 11/02/15.
 */
public class AdjustedBoxPlot extends BoxPlot {

    private Percentile percentile = new Percentile();

    @Override
    public double computeLowerBound(double[] values, double IQR) {
        double MC = Medcouple.evaluate(values);
        if(MC >= 0) {
            return (percentile.evaluate(0.25, values) - (1.5 * Math.exp(-4.0 * MC) * IQR));
        }else {
            return (percentile.evaluate(0.25, values) - (1.5 * Math.exp(-3.0 * MC) * IQR));
        }
//        return (Percentile.evaluate(0.25, values) - (1.5 * Math.exp(-3.5 * MC) * IQR));
    }

    @Override
    public double computeUpperBound(double[] values, double IQR) {
        double MC = Medcouple.evaluate(values);
        if(MC >= 0) {
            return (percentile.evaluate(0.75, values) + (1.5 * Math.exp(3.0 * MC) * IQR));
        }else {
            return (percentile.evaluate(0.75, values) + (1.5 * Math.exp(4.0 * MC) * IQR));
        }
//        return (Percentile.evaluate(0.75, values) + (1.5 * Math.exp(3.5 * MC) * IQR));
    }

}
