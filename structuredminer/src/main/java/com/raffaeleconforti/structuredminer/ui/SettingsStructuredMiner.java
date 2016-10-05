package com.raffaeleconforti.structuredminer.ui;

/**
 * Created by Raffaele Conforti on 27/02/14.
 */
public class SettingsStructuredMiner {

    private int selectedAlgorithm = 0;

    public static final int HMPOS = 0;
    public static final int HMPOS52 = 1;
    public static final int HDMPOS = 2;
    public static final int FODINAPOS = 3;

    private boolean uma = false;
    private boolean simplify = false;
    private boolean structured = true;

    public SettingsStructuredMiner(int selectedAlgorithm) {
        this.selectedAlgorithm = selectedAlgorithm;
    }

    public int getSelectedAlgorithm() {
        return selectedAlgorithm;
    }

    public void setSelectedAlgorithm(int selectedAlgorithm) {
        this.selectedAlgorithm = selectedAlgorithm;
    }

    public boolean isSimplify() {
        return simplify;
    }

    public void setSimplify(boolean simplify) {
        this.simplify = simplify;
    }

    public boolean isUma() {
        return uma;
    }

    public void setUma(boolean uma) {
        this.uma = uma;
    }

    public boolean isStructured() {
        return structured;
    }

    public void setStructured(boolean structured) {
        this.structured = structured;
    }

}
