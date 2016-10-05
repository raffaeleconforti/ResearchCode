package com.raffaeleconforti.wrapper.extractors;

import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

/**
 * Created by conforti on 28/01/2016.
 */
@Plugin(name = "Extract Petrinet", parameterLabels = {"PetrinetWithMarking"},
        returnLabels = {"Petrinet"},
        returnTypes = {Petrinet.class})
public class PetrinetFromPetrinetWithMarkingExtractor {

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
        author = "Raffaele Conforti",
        email = "raffaele.conforti@qut.edu.au",
        pack = "Noise Filtering")
    @PluginVariant(variantLabel = "Extract Petrinet", requiredParameterLabels = {0})
    public Petrinet extractPetrinet(UIPluginContext context, PetrinetWithMarking petrinetWithMarking) {
        return petrinetWithMarking.getPetrinet();
    }
}
