package com.raffaeleconforti.statistics;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 23/11/16.
 */
public interface StatisticsMeasure {

    double evaluate(Double val, double... values);

}
