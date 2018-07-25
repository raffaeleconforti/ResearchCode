package com.raffaeleconforti.statistics;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 23/11/16.
 */
public interface StatisticsMeasure {

    double evaluate(Double val, double... values);

    double evaluate(Float val, float... values);

    double evaluate(Long val, long... values);

    double evaluate(Integer val, int... values);

}
