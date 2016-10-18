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
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMa;
import org.processmining.plugins.InductiveMiner.plugins.IMPetriNet;

/**
 * Created by conforti on 20/02/15.
 */
@Plugin(name = "Inductive Miner IMa Wrapper", parameterLabels = {"Log"},
        returnLabels = {"PetrinetWithMarking"},
        returnTypes = {PetrinetWithMarking.class})
public class InductiveMinerIMaWrapper implements MiningAlgorithm {

    MiningParameters miningParameters;

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@qut.edu.au",
            pack = "Noise Filtering")
    @PluginVariant(variantLabel = "Inductive Miner IMa Wrapper", requiredParameterLabels = {0})
    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log) {
        return minePetrinet(context, log, false);
    }

    @Override
    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log, boolean structure) {
        LogPreprocessing logPreprocessing = new LogPreprocessing();
        log = logPreprocessing.preprocessLog(context, log);

        IMPetriNet miner = new IMPetriNet();
        if(miningParameters == null) {
            miningParameters = new MiningParametersIMa();
        }
        Object[] result = miner.minePetriNetParameters(context, log, miningParameters);
        logPreprocessing.removedAddedElements((Petrinet) result[0]);

        return new PetrinetWithMarking((Petrinet) result[0], (Marking) result[1]);
    }

    @Override
    public BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, boolean structure) {
        PetrinetWithMarking petrinetWithMarking = minePetrinet(context, log, structure);
        return PetriNetToBPMNConverter.convert(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking(), true);
    }

    @Override
    public String getAlgorithmName() {
        return "Inductive Miner - all operators (IMa)";
    }

}
