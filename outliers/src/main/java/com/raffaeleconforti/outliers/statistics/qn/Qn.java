package com.raffaeleconforti.outliers.statistics.qn;

import com.raffaeleconforti.outliers.statistics.boxplot.Percentile;
import com.raffaeleconforti.outliers.statistics.median.Median;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.DoubleIntHashMap;

import java.util.Arrays;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 22/11/16.
 */
public class Qn {

    public static double evaluate(double... values) {
        DoubleIntHashMap map = new DoubleIntHashMap();
        double total = 0;
        try {
            for(int i = 0; i < values.length; i++) {
                int count = 1;
                double last = -1;
                for(int j = i + 1; j < values.length - 1; j++) {
                    if(last == -1) last = values[j];
                    else if(last == values[j]) count++;
                    else {
                        double key = Math.abs(values[i] - last);
                        int value = map.get(key);
                        map.put(key, value + count);
                        total += count;
                        last = values[j];
                        count = 1;
                    }
                }
                if(last == values[values.length - 1]) count++;
                else {
                    last = values[values.length - 1];
                    count = 1;
                }
                double key = Math.abs(values[i] - last);
                int value = map.get(key);
                map.put(key, value + count);
                total += count;
            }

            double[] keys = map.keySet().toArray();
            Arrays.sort(keys);

            int visited = 0;
            for(double key : keys) {
                visited += map.get(key);
                if(visited >= total * 0.25) {
                    return 2.219 * key;
                }
            }
        }catch (ArrayIndexOutOfBoundsException e) {

        }
        return 0;
    }

}
