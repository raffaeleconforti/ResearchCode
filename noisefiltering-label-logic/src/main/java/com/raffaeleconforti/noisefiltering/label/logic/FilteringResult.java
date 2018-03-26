package com.raffaeleconforti.noisefiltering.label.logic;

import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 26/12/17.
 */
public class FilteringResult<T> {

    private String technique;
    private Set<T> outliers;

    public FilteringResult(String technique, Set<T> outliers) {
        this.technique = technique;
        this.outliers = outliers;
    }

    public String getTechnique() {
        return technique;
    }

    public Set<T> getOutliers() {
        return outliers;
    }
}
