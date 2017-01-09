package com.raffaeleconforti.noisefiltering.timestamp.permutation;

import org.deckfour.xes.model.XEvent;

import java.util.List;
import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 14/09/2016.
 */
public interface PermutationTechnique {

    int ILP_GUROBI = 0;
    int ILP_LPSOLVE = 1;
    int HEURISTICS_BEST = 2;
    int HEURISTICS_SET = 3;

    Set<List<XEvent>> findBestStartEnd();

}
