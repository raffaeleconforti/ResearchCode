package com.raffaeleconforti.noisefiltering.timestamp.permutation;

import org.deckfour.xes.model.XEvent;

import java.util.List;
import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 14/09/2016.
 */
public interface PermutationTechnique {

    int ILP_GUROBI = 0;
    int ILP_GUROBI_ARCS = 1;
    int ILP_LPSOLVE = 2;
    int ILP_LPSOLVE_ARCS = 3;
    int HEURISTICS_BEST = 4;
    int HEURISTICS_SET = 5;

    Set<List<XEvent>> findBestStartEnd();

}
