package com.raffaeleconforti.foreignkeydiscovery;

import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.Set;

/**
 * Created by Raffaele Conforti on 15/10/14.
 */
public class JaccardCoefficientEstimator {

    public static int computeEstimator(BottomKSketch bottomKSketch1, BottomKSketch bottomKSketch2, int k) {
        int rk_1 = Math.min(bottomKSketch1.getRankOfPlus1Sketch(k), bottomKSketch2.getRankOfPlus1Sketch(k));
        Set<Couple<String, Integer>> SCS = new UnifiedSet<Couple<String, Integer>>();

        Set<Couple<String, Integer>> S = new UnifiedSet<Couple<String, Integer>>(bottomKSketch1.getRankSketches(k));
        S.addAll(bottomKSketch2.getRankSketches(k));

        for(Couple<String, Integer> c : S) {
            if(c.getSecondElement() < rk_1) {
                SCS.add(c);
            }
        }

        return SCS.size();
    }

}
