package com.raffaeleconforti.statistics;

public abstract class StatisticsMeasureAbstract implements StatisticsMeasure {

    public double evaluate(Float val, float... values) {
        Double val2 = (val != null) ? Double.valueOf(val) : null;
        double[] values2 = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            values2[i] = values[i];
        }
        return evaluate(val2, values2);
    }

    public double evaluate(Long val, long... values) {
        Double val2 = (val != null) ? Double.valueOf(val) : null;
        double[] values2 = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            values2[i] = values[i];
        }
        return evaluate(val2, values2);
    }

    public double evaluate(Integer val, int... values) {
        Double val2 = (val != null) ? Double.valueOf(val) : null;
        double[] values2 = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            values2[i] = values[i];
        }
        return evaluate(val2, values2);
    }


}
