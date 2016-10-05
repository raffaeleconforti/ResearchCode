package com.raffaeleconforti.wrapper;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

/**
 * Created by conforti on 20/02/15.
 */
public class PetrinetWithMarking {

    private final Petrinet petrinet;
    private final Marking initialMarking;
    private Marking finalMarking;

    public PetrinetWithMarking(Petrinet petrinet, Marking initialMarking) {
        this.initialMarking = initialMarking;
        this.petrinet = petrinet;
    }

    public PetrinetWithMarking(Petrinet petrinet, Marking initialMarking, Marking finalMarking) {
        this.finalMarking = finalMarking;
        this.initialMarking = initialMarking;
        this.petrinet = petrinet;
    }

    public Petrinet getPetrinet() {
        return petrinet;
    }

    public Marking getInitialMarking() {
        return initialMarking;
    }

    public Marking getFinalMarking() {
        return finalMarking;
    }

    public void setFinalMarking(Marking finalMarking) {
        this.finalMarking = finalMarking;
    }
}
