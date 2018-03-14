package com.raffaeleconforti.marking;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 28/10/2016.
 */
public class MarkingDiscoverer {

    public static Marking constructInitialMarking(PluginContext context, Petrinet petrinet) {
        Marking initialMarking = new Marking();
        for(Place p : petrinet.getPlaces()) {
            int in = 0;
            for(PetrinetEdge edge : petrinet.getEdges()) {
                if(edge.getTarget().equals(p)) {
                    in++;
                }
            }
            if(in == 0) {
                initialMarking.add(p);
            }
        }

        if(initialMarking.size() == 0) {
            for(Place p : petrinet.getPlaces()) {
                if(p.getLabel().equalsIgnoreCase("source") ||
                        p.getLabel().equalsIgnoreCase("start")) {
                    initialMarking.add(p);
                }
            }
        }

        if(initialMarking.size() == 0) {
            for(Transition t : petrinet.getTransitions()) {
                if(t.getLabel().toLowerCase().contains("start")) {
                    for(PetrinetEdge e : petrinet.getEdges()) {
                        if(e.getTarget().equals(t)) {
                            initialMarking.add((Place) e.getSource());
                        }
                    }
                }
            }
        }

        createInitialMarkingConnection(context, petrinet, initialMarking);
        return initialMarking;
    }

    public static Marking constructFinalMarking(PluginContext context, Petrinet petrinet) {
        Marking finalMarking = new Marking();
        for(Place p : petrinet.getPlaces()) {
            int out = 0;
            for(PetrinetEdge edge : petrinet.getEdges()) {
                if(edge.getSource().equals(p)) {
                    out++;
                }
            }
            if(out == 0) {
                finalMarking.add(p);
            }
        }

        if(finalMarking.size() == 0) {
            for(Place p : petrinet.getPlaces()) {
                if(p.getLabel().equalsIgnoreCase("sink") ||
                        p.getLabel().equalsIgnoreCase("end") ||
                        p.getLabel().equalsIgnoreCase("final")) {
                    finalMarking.add(p);
                }
            }
        }

        if(finalMarking.size() == 0) {
            for(Transition t : petrinet.getTransitions()) {
                if(t.getLabel().toLowerCase().contains("end")) {
                    for(PetrinetEdge e : petrinet.getEdges()) {
                        if(e.getSource().equals(t)) {
                            finalMarking.add((Place) e.getTarget());
                        }
                    }
                }
            }
        }

        createFinalMarkingConnection(context, petrinet, finalMarking);
        return finalMarking;
    }

    public static Set<Marking> constructFinalMarkings(PluginContext context, Petrinet petrinet) {
        Set<Marking> finalMarking = new HashSet<>();
        for(Place p : petrinet.getPlaces()) {
            int out = 0;
            for(PetrinetEdge edge : petrinet.getEdges()) {
                if(edge.getSource().equals(p)) {
                    out++;
                }
            }
            if(out == 0) {
                Marking m = new Marking();
                m.add(p);
                finalMarking.add(m);
            }
        }

        if(finalMarking.size() == 0) {
            for(Place p : petrinet.getPlaces()) {
                if(p.getLabel().equalsIgnoreCase("sink") ||
                        p.getLabel().equalsIgnoreCase("end") ||
                        p.getLabel().equalsIgnoreCase("final")) {
                    Marking m = new Marking();
                    m.add(p);
                    finalMarking.add(m);
                }
            }
        }

        if(finalMarking.size() == 0) {
            for(Transition t : petrinet.getTransitions()) {
                if(t.getLabel().toLowerCase().contains("end")) {
                    for(PetrinetEdge e : petrinet.getEdges()) {
                        if(e.getSource().equals(t)) {
                            Marking m = new Marking();
                            m.add((Place) e.getTarget());
                            finalMarking.add(m);
                        }
                    }
                }
            }
        }

        for (Marking m: finalMarking) {
            createFinalMarkingConnection(context, petrinet, m);
        }
        return finalMarking;
    }

    public static void createInitialMarkingConnection(PluginContext context, Petrinet petrinet, Marking initialMarking) {
        context.addConnection(new InitialMarkingConnection(petrinet, initialMarking));
    }

    public static void createFinalMarkingConnection(PluginContext context, Petrinet petrinet, Marking finalMarking) {
        context.addConnection(new FinalMarkingConnection(petrinet, finalMarking));
    }
}
