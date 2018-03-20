package com.raffaeleconforti.wrappers.extractors;

import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.wrappers.PetrinetWithMarking;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

/**
 * Created by conforti on 28/01/2016.
 */
@Plugin(name = "Extract BPMN Diagram", parameterLabels = {"PetrinetWithMarking"},
        returnLabels = {"BPMN Diagram"},
        returnTypes = {BPMNDiagram.class})
public class BPMNFromPetrinetWithMarkingExtractor {

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
        author = "Raffaele Conforti",
        email = "raffaele.conforti@unimelb.edu.au",
        pack = "Noise Filtering")
    @PluginVariant(variantLabel = "Extract BPMN Diagram", requiredParameterLabels = {0})
    public BPMNDiagram extractPetrinet(UIPluginContext context, PetrinetWithMarking petrinetWithMarking) {
        return PetriNetToBPMNConverter.convert(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking(), true);
    }
}
