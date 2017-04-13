package com.raffaeleconforti.noisefiltering.timestamp.permutation;

import com.raffaeleconforti.ilpsolverwrapper.impl.gurobi.Gurobi_Solver;
import com.raffaeleconforti.ilpsolverwrapper.impl.lpsolve.LPSolve_Solver;
import com.raffaeleconforti.kernelestimation.distribution.EventDistributionCalculator;
import com.raffaeleconforti.noisefiltering.timestamp.permutation.permutators.HeuristicBestSolution;
import com.raffaeleconforti.noisefiltering.timestamp.permutation.permutators.HeuristicSetSolutions;
import com.raffaeleconforti.noisefiltering.timestamp.permutation.permutators.ILPApproach;
import org.deckfour.xes.model.XEvent;

import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 14/09/2016.
 */
public class PermutationTechniqueFactory {

    public static PermutationTechnique getPermutationTechnique(int approach, Set<XEvent> events, EventDistributionCalculator eventDistributionCalculator, XEvent start, XEvent end) {
        if(approach == PermutationTechnique.HEURISTICS_SET) {
            return new HeuristicSetSolutions(events, eventDistributionCalculator, start, end);
        }else if(approach == PermutationTechnique.HEURISTICS_BEST) {
            return new HeuristicBestSolution(events, eventDistributionCalculator, start, end);
        }else if(approach == PermutationTechnique.ILP_GUROBI) {
            return new ILPApproach(events, eventDistributionCalculator, start, end, new Gurobi_Solver());
        }else if(approach == PermutationTechnique.ILP_LPSOLVE) {
            return new ILPApproach(events, eventDistributionCalculator, start, end, new LPSolve_Solver());
        }
        return null;
    }

    public static String getPermutationTechniqueName(int approach) {
        if(approach == PermutationTechnique.HEURISTICS_SET) {
            return "HEURISTICS_SET";
        }else if(approach == PermutationTechnique.HEURISTICS_BEST) {
            return "HEURISTICS_BEST";
        }else if(approach == PermutationTechnique.ILP_GUROBI) {
            return "ILP_GUROBI";
        }else if(approach == PermutationTechnique.ILP_LPSOLVE) {
            return "ILP_LPSOLVE";
        }
        return null;
    }
}
