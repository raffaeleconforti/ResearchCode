package com.raffaeleconforti.measurements.ui.allmeasurement;

/**
 * Created by Raffaele Conforti on 27/02/14.
 */
public class SelectMinerUIResultAM {

    private boolean[] selectedAlgorithm = new boolean[7];
    private int fold = 10;
    private boolean fitness = true;
    private boolean precision = true;
    private boolean generalization = true;
    private boolean semplicity = true;
    private boolean noise = true;

    public SelectMinerUIResultAM(boolean[] selectedAlgorithm) {
        this.selectedAlgorithm = selectedAlgorithm;
    }

    public boolean[] getSelectedAlgorithm() {
        return selectedAlgorithm;
    }

    public void setSelectedAlgorithm(boolean[] selectedAlgorithm) {
        this.selectedAlgorithm = selectedAlgorithm;
    }

    public int getFold() {
        return fold;
    }

    public void setFold(int fold) {
        this.fold = fold;
    }

    public boolean isSemplicity() {
        return semplicity;
    }

    public void setSemplicity(boolean semplicity) {
        this.semplicity = semplicity;
    }

    public boolean isGeneralization() {
        return generalization;
    }

    public void setGeneralization(boolean generalization) {
        this.generalization = generalization;
    }

    public boolean isPrecision() {
        return precision;
    }

    public void setPrecision(boolean precision) {
        this.precision = precision;
    }

    public boolean isFitness() {
        return fitness;
    }

    public void setFitness(boolean fitness) {
        this.fitness = fitness;
    }

    public boolean isNoise() {
        return noise;
    }

    public void setNoise(boolean noise) {
        this.noise = noise;
    }

}
