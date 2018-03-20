package com.raffaeleconforti.bpmnminer.converter;

/**
 * Created by Raffaele Conforti on 28/02/14.
 */

import com.raffaeleconforti.conversion.bpmn.BPMNToPetriNetConverter;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

@Plugin(name = "Convert BPMN to Petrinet", parameterLabels = {"BPMNDiagram"},
        returnLabels = {"Petrinet"},
        returnTypes = {Petrinet.class})
public class BPMNToPetriNetConverterPlugin {

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@unimelb.edu.au",
            pack = "bpmnminer")
    @PluginVariant(variantLabel = "Convert BPMN to Petrinet", requiredParameterLabels = {0})
    public Petrinet convert(final UIPluginContext context, BPMNDiagram diagram) {
        Object[] result = BPMNToPetriNetConverter.convert(diagram);
        context.addConnection(new InitialMarkingConnection((Petrinet) result[0], (Marking) result[1]));
        context.addConnection(new FinalMarkingConnection((Petrinet) result[0], (Marking) result[2]));
        return (Petrinet) result[0];
    }

	public Petrinet convert( BPMNDiagram diagram) {
        return (Petrinet) BPMNToPetriNetConverter.convert(diagram)[0];
    }
}
