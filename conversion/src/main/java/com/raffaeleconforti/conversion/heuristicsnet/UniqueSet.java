package com.raffaeleconforti.conversion.heuristicsnet;

import org.processmining.framework.models.heuristics.HNSubSet;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 9/03/2016.
 */
public class UniqueSet {
    private HNSubSet set;
    private int id;
    private boolean in;

    public UniqueSet(HNSubSet set, int id, boolean in) {
        this.set = set;
        this.id = id;
        this.in = in;
    }

    public boolean equals(Object o) {
        if (!(o instanceof UniqueSet)) {
            return false;
        }

        UniqueSet s = (UniqueSet) o;
        return (s.set.equals(set)) && (s.id == id) && (s.in == in);

    }

    public String toString() {
        return set.toString();
    }

}