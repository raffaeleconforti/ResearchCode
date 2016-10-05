package org.processmining.plugins.bpmnminer.converter;

/**
 * Created by Raffaele Conforti on 28/02/14.
 */

import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.finalmarkingprovider.FinalMarkingFactory;
import org.processmining.plugins.petrinet.initmarkingprovider.InitMarkingFactory;

@Plugin(name = "Convert Petrinet to BPMN", parameterLabels = {"PetriNet", "InitialMarking"},
        returnLabels = {"BPMNDiagram"},
        returnTypes = {BPMNDiagram.class})

public class BPMNConverterPlugin {

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@qut.edu.au",
            pack = "bpmnminer")
    @PluginVariant(variantLabel = "Convert Petrinet to BPMN", requiredParameterLabels = {0})
    public BPMNDiagram convert(final UIPluginContext context, Petrinet net) throws Exception {
        Marking initialMarking = (Marking) (new InitMarkingFactory()).constructInitMarking(context, net)[1];
        Marking finalMarking = (Marking) (new FinalMarkingFactory()).constructFinalMarking(context, net)[1];
        return PetriNetToBPMNConverter.convert(net, initialMarking, finalMarking, true);
    }

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@qut.edu.au",
            pack = "bpmnminer")
    @PluginVariant(variantLabel = "Convert Petrinet to BPMN", requiredParameterLabels = {0, 1})
    public BPMNDiagram convert(final UIPluginContext context, Petrinet net, Marking initialMarking, Marking finalMarking) throws Exception {
        return PetriNetToBPMNConverter.convert(net, initialMarking, finalMarking, true);
    }

}
