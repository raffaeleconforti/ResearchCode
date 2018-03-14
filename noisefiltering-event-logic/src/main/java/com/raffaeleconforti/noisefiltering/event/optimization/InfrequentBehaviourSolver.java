package com.raffaeleconforti.noisefiltering.event.optimization;

import com.raffaeleconforti.automaton.Edge;

import java.util.Set;

/**
 * Created by conforti on 16/10/15.
 */
public interface InfrequentBehaviourSolver<T> {

    Set<Edge<T>> identifyRemovableEdges();

}
