package com.raffaeleconforti.bpmnminer.subprocessminer.selection;

import com.raffaeleconforti.wrappers.settings.MiningSettings;

/**
 * Created by Raffaele Conforti on 27/02/14.
 */
public class SelectMinerResult {

    public static final String SM = "Split Miner";
    public static final String IM = "Inductive Miner";
    public static final String HM6 = "Heuristics Miner ProM6";
    public static final String HM5 = "Heuristics Miner ProM5.2";
    public static final String ALPHA = "Alpha Algorithm";
    public static final String ILP = "ILP Miner";

    public static final int SMPOS = 0;
    public static final int IMPOS = 1;
    public static final int HMPOS6 = 2;
    public static final int HMPOS5 = 3;
    public static final int ALPHAPOS = 4;
    public static final int ILPPOS = 5;

    private int selectedAlgorithm;
    private MiningSettings params;
    private double timerEventPercentage;
    private double timerEventTolerance;
    private double interruptingEventTolerance;
    private double multiInstancePercentage;
    private double multiInstanceTolerance;
    private double noiseThreshold;

    public SelectMinerResult(int selectedAlgorithm,
                             MiningSettings params,
                             double interruptingEventTolerance,
                             double multiInstancePercentage,
                             double multiInstanceTolerance,
                             double timerEventPercentage,
                             double timerEventTolerance,
                             double noiseThreshold) {
        this.selectedAlgorithm = selectedAlgorithm;
        this.params = params;
        this.timerEventPercentage = timerEventPercentage;
        this.timerEventTolerance = timerEventTolerance;
        this.interruptingEventTolerance = interruptingEventTolerance;
        this.multiInstancePercentage = multiInstancePercentage;
        this.multiInstanceTolerance = multiInstanceTolerance;
        this.noiseThreshold = noiseThreshold;
    }

    public int getSelectedAlgorithm() {
        return selectedAlgorithm;
    }

    public void setSelectedAlgorithm(int selectedAlgorithm) {
        this.selectedAlgorithm = selectedAlgorithm;
    }

    public MiningSettings getMiningSettings() { return params; }
    public void setMiningSettings(MiningSettings params) { this.params = params; }

    public double getTimerEventPercentage() {
        return timerEventPercentage;
    }

    public void setTimerEventPercentage(double timerEventPercentage) {
        this.timerEventPercentage = timerEventPercentage;
    }

    public double getTimerEventTolerance() {
        return timerEventTolerance;
    }

    public void setTimerEventTolerance(double timerEventTolerance) {
        this.timerEventTolerance = timerEventTolerance;
    }

    public double getMultiInstancePercentage() {
        return multiInstancePercentage;
    }

    public void setMultiInstancePercentage(double multiInstancePercentage) {
        this.multiInstancePercentage = multiInstancePercentage;
    }

    public double getInterruptingEventTolerance() {
        return interruptingEventTolerance;
    }

    public void setInterruptingEventTolerance(double interruptingEventTolerance) {
        this.interruptingEventTolerance = interruptingEventTolerance;
    }

    public double getMultiInstanceTolerance() {
        return multiInstanceTolerance;
    }

    public void setMultiInstanceTolerance(double multiInstanceTolerance) {
        this.multiInstanceTolerance = multiInstanceTolerance;
    }

    public double getNoiseThreshold() {
        return noiseThreshold;
    }

    public void setNoiseThreshold(double noiseThreshold) {
        this.noiseThreshold = noiseThreshold;
    }
}
