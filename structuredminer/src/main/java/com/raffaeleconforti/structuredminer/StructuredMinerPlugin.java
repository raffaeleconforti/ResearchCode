package com.raffaeleconforti.structuredminer;

import com.raffaeleconforti.conversion.bpmn.BPMNToPetriNetConverter;
import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.structuredminer.miner.StructuredMiner;
import com.raffaeleconforti.structuredminer.ui.SettingsStructuredMiner;
import com.raffaeleconforti.structuredminer.ui.SettingsStructuredMinerUI;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

/**
 * Created by conforti on 19/12/2015.
 */
@Plugin(name = "Structured Miner", parameterLabels = {"Log"},
        returnLabels = {"BPMNModel"},
        returnTypes = {BPMNDiagram.class})

public class StructuredMinerPlugin {

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@unimelb.edu.au",
            pack = "StructuredMiner (raffaele.conforti@unimelb.edu.au)")
    @PluginVariant(variantLabel = "Structured Miner", requiredParameterLabels = {0})//, 1, 2, 3 })
    public BPMNDiagram generateDiagram(final UIPluginContext context, XLog log) {

        SettingsStructuredMinerUI settingsStructuredMinerUI = new SettingsStructuredMinerUI();
        SettingsStructuredMiner settings = settingsStructuredMinerUI.showGUI(context);

        StructuredMiner miner = new StructuredMiner(context, log, settings);
        BPMNDiagram diagram = miner.mine();
        Object[] petrinetWithMarking = BPMNToPetriNetConverter.convert(diagram);

        return PetriNetToBPMNConverter.convert((Petrinet) petrinetWithMarking[0], (Marking) petrinetWithMarking[1], (Marking) petrinetWithMarking[2], true);
    }

}
