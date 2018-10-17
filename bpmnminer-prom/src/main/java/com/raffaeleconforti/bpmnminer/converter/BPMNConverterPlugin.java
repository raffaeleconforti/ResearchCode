/*
 *  Copyright (C) 2018 Raffaele Conforti (www.raffaeleconforti.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.raffaeleconforti.bpmnminer.converter;

/**
 * Created by Raffaele Conforti on 28/02/14.
 */

import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.marking.MarkingDiscoverer;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

@Plugin(name = "Convert Petrinet to BPMN", parameterLabels = {"PetriNet", "InitialMarking", "FinalMarking"},
        returnLabels = {"BPMNDiagram"},
        returnTypes = {BPMNDiagram.class})

public class BPMNConverterPlugin {

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@unimelb.edu.au",
            pack = "bpmnminer")
    @PluginVariant(variantLabel = "Convert Petrinet to BPMN", requiredParameterLabels = {0})
    public BPMNDiagram convert(final UIPluginContext context, Petrinet net) {
        Marking initialMarking = MarkingDiscoverer.constructInitialMarking(context, net);
        Marking finalMarking = MarkingDiscoverer.constructFinalMarking(context, net);
        return PetriNetToBPMNConverter.convert(net, initialMarking, finalMarking, true);
    }

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@unimelb.edu.au",
            pack = "bpmnminer")
    @PluginVariant(variantLabel = "Convert Petrinet to BPMN", requiredParameterLabels = {0, 1, 2})
    public BPMNDiagram convert(final UIPluginContext context, Petrinet net, Marking initialMarking, Marking finalMarking) {
        return PetriNetToBPMNConverter.convert(net, initialMarking, finalMarking, true);
    }

}
