package com.raffaeleconforti.wrappers;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by conforti on 20/02/15.
 */
public class PetrinetWithMarking {

    private final Petrinet petrinet;
    private final Marking initialMarking;
    private Marking finalMarking;
    private Set<Marking> finalMarkings;

    public PetrinetWithMarking(Petrinet petrinet, Marking initialMarking) {
        this.initialMarking = initialMarking;
        this.petrinet = petrinet;
    }

    public PetrinetWithMarking(Petrinet petrinet, Marking initialMarking, Marking finalMarking) {
        this.finalMarking = finalMarking;
        this.finalMarkings = new HashSet<>();
        this.finalMarkings.add(finalMarking);
        this.initialMarking = initialMarking;
        this.petrinet = petrinet;
    }

    public PetrinetWithMarking(Petrinet petrinet, Marking initialMarking, Set<Marking> finalMarkings) {
        if(finalMarkings.size() == 1) {
            this.finalMarking = finalMarkings.iterator().next();
        }else {
            this.finalMarking = null;
        }
        this.finalMarkings = finalMarkings;
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

    public Set<Marking> getFinalMarkings() {
        return finalMarkings;
    }

    public void setFinalMarking(Marking finalMarking) {
        this.finalMarking = finalMarking;
        this.finalMarkings = new HashSet<>();
        this.finalMarkings.add(finalMarking);
    }

    public void setFinalMarkings(Set<Marking> finalMarkings) {
        if(finalMarkings.size() == 1) {
            this.finalMarking = finalMarkings.iterator().next();
        }else {
            this.finalMarking = null;
        }
        this.finalMarkings = finalMarkings;
    }
}
