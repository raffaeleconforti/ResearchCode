package com.raffaeleconforti.noisefiltering.event.selection;

import com.raffaeleconforti.automaton.Node;

import java.util.Set;

/**
 * Created by conforti on 26/02/15.
 */
public class NoiseFilterResult {

    private int approach;
    private boolean fixLevel;
    private double noiseLevel;
    private double percentile;
    private boolean repeated;
    private Set<Node<String>> requiredStates;

    public int getApproach() {
        return approach;
    }

    public void setApproach(int approach) {
        this.approach = approach;
    }

    public boolean isFixLevel() {
        return fixLevel;
    }

    public void setFixLevel(boolean fixLevel) {
        this.fixLevel = fixLevel;
    }

    public double getNoiseLevel() {
        return noiseLevel;
    }

    public void setNoiseLevel(double noiseLevel) {
        this.noiseLevel = noiseLevel;
    }

    public double getPercentile() {
        return percentile;
    }

    public void setPercentile(double percentile) {
        this.percentile = percentile;
    }

    public boolean isRepeated() {
        return repeated;
    }

    public void setRepeated(boolean repeated) {
        this.repeated = repeated;
    }

    public Set<Node<String>> getRequiredStates() {
        return requiredStates;
    }

    public void setRequiredStates(Set<Node<String>> requiredStates) {
        this.requiredStates = requiredStates;
    }

}
