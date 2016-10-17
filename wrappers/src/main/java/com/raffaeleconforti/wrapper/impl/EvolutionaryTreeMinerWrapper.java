package com.raffaeleconforti.wrapper.impl;

import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.wrapper.LogPreprocessing;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.plugins.etm.ui.plugins.ETMPlugin;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;

/**
 * Created by conforti on 9/02/2016.
 */
@Plugin(name = "Evolutionary Tree Miner Wrapper", parameterLabels = {"Log"},
        returnLabels = {"PetrinetWithMarking"},
        returnTypes = {PetrinetWithMarking.class})
public class EvolutionaryTreeMinerWrapper implements MiningAlgorithm {

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@qut.edu.au",
            pack = "Noise Filtering")
    @PluginVariant(variantLabel = "Evolutionary Tree Miner Wrapper", requiredParameterLabels = {0})
    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log) {
        return minePetrinet(context, log, false);
    }

    @Override
    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log, boolean structure) {
        try {
            LogPreprocessing logPreprocessing = new LogPreprocessing();
            log = logPreprocessing.preprocessLog(context, log);

            ETMPlugin etmPlugin = new ETMPlugin();
            ProcessTree processTree = etmPlugin.withoutSeed(context, log);
            ProcessTree2Petrinet.PetrinetWithMarkings petrinetWithMarkings = ProcessTree2Petrinet.convert(processTree);

            logPreprocessing.removedAddedElements(petrinetWithMarkings.petrinet);

            return new PetrinetWithMarking(petrinetWithMarkings.petrinet, petrinetWithMarkings.initialMarking, petrinetWithMarkings.finalMarking);
        } catch (ProcessTree2Petrinet.InvalidProcessTreeException e) {
            e.printStackTrace();
        } catch (ProcessTree2Petrinet.NotYetImplementedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, boolean structure) {
        PetrinetWithMarking petrinetWithMarking = minePetrinet(context, log, structure);
        return PetriNetToBPMNConverter.convert(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking(), true);
    }

    @Override
    public String getAlgorithmName() {
        return "Evolutionary Tree Miner";
    }
}
