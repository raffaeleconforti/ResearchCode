package com.raffaeleconforti.noisefiltering.label.logic.clustering;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 29/12/17.
 */
public class ClusterElement<T> {

    private double cost;
    private Map<T, double[]> elements;

    public ClusterElement(double cost) {
        this.cost = cost;
        elements = new HashMap<>();
    }

    public void addElement(T element, double[] cost) {
        elements.put(element, cost);
    }

    public double getCost() {
        return cost;
    }

    public Map<T, double[]> getElements() {
        return elements;
    }

    @Override
    public String toString() {
        return "Cost: " + cost + "\n" + elements.toString();
    }
}
