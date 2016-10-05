package com.raffaeleconforti.outliers.statistics.boxplot;

/**
 * Created by conforti on 11/02/15.
 */
public class Percentile {

    public static double evaluate(double percentile, double... values) {
        try {
            int pos = (int) Math.round(values.length * percentile) - 1;
            if(pos < 0) pos = 0;
            return values[pos];
        }catch (ArrayIndexOutOfBoundsException e) {
//            if(values.length == 0) {
//                System.out.println("CIAO");
//            }
//            e.printStackTrace();
        }
        return 0;
    }

}
