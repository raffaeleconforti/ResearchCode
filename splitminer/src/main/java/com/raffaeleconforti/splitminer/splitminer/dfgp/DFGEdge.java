package com.raffaeleconforti.splitminer.splitminer.dfgp;

import com.raffaeleconforti.splitminer.log.graph.LogEdge;

/**
 * Created by Adriano on 24/10/2016.
 */
public class DFGEdge extends LogEdge {

    private int frequency;

    public DFGEdge(DFGNode source, DFGNode target) {
        super(source, target);
        frequency = 0;
    }

    public DFGEdge(DFGNode source, DFGNode target, String label) {
        super(source, target, label);
        frequency = 0;
    }

    public DFGEdge(DFGNode source, DFGNode target, int frequency) {
        super(source, target);
        this.frequency = frequency;
    }

    public DFGEdge(DFGNode source, DFGNode target, String label, int frequency) {
        super(source, target, label);
        this.frequency = frequency;
    }

    public void increaseFrequency() {
        frequency++;
    }

    public void increaseFrequency(int amount) {
        frequency += amount;
    }

    public int getFrequency() {
        return frequency;
    }

    @Override
    public String toString() {
        return Integer.toString(frequency);
    }

    public String print() {
        return getSourceCode() + " > " + getTargetCode() + " [" + getFrequency() + "]";
    }

    @Override
    public int compareTo(Object o) {
        if ((o instanceof DFGEdge)) {
            if (frequency == (((DFGEdge) o).frequency)) {
                if (getSourceCode() == ((DFGEdge) o).getSourceCode())
                    return getTargetCode() - ((DFGEdge) o).getTargetCode();
                else return getSourceCode() - ((DFGEdge) o).getSourceCode();
            } else return frequency - ((DFGEdge) o).frequency;
        } else return -1;
    }
}
