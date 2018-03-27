package com.raffaeleconforti.soundnesschecker;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 5/7/17.
 */

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import org.apache.commons.lang3.StringUtils;
import org.processmining.models.graphbased.NodeID;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

public class PetriNet2Lola {

    public static String convert(final Petrinet petriNet, final Marking initialMarking) {
        StringBuilder result = new StringBuilder();

        //places
        result.append("PLACE ");
        result.append(StringUtils.join(
                FluentIterable.from(petriNet.getPlaces()).transform(new Function<Place, String>() {
                    public String apply(Place place) {
                        return removeNode(place.getId());
                    }
                }), ", "));
        result.append(";\n");

        //marking
        result.append("MARKING ");
        result.append(StringUtils.join(FluentIterable.from(initialMarking).transform(new Function<Place, String>() {
            public String apply(Place place) {
                return removeNode(place.getId()) + ":" + initialMarking.occurrences(place);
            }
        }), ", "));
        result.append(";\n");

        //transitions
        for (Transition transition : petriNet.getTransitions()) {

            result.append("TRANSITION ");
            result.append(removeNode(transition.getId()));

            result.append(" CONSUME ");
            result.append(StringUtils.join(
                    FluentIterable.from(petriNet.getInEdges(transition)).transform(
                            new Function<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>, String>() {
                                public String apply(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arg0) {
                                    return removeNode(arg0.getSource().getId());
                                }
                            }), ", "));

            result.append("; PRODUCE ");
            result.append(StringUtils.join(
                    FluentIterable.from(petriNet.getOutEdges(transition)).transform(
                            new Function<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>, String>() {
                                public String apply(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arg0) {
                                    return removeNode(arg0.getTarget().getId());
                                }
                            }), ", "));
            result.append(";\n");
        }

        return result.toString();
    }

    public static String removeNode(NodeID id) {
        return "n" + id.toString().substring(6).replace('-', '_');
    }
}
