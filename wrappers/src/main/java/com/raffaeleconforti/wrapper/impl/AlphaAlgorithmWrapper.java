package com.raffaeleconforti.wrapper.impl;

import com.raffaeleconforti.alphaminer.AlphaMiner;
import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.wrapper.LogPreprocessing;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.log.logabstraction.BasicLogRelations;

import java.util.concurrent.ExecutionException;

/**
 * Created by conforti on 20/02/15.
 */
@Plugin(name = "Alpha Algorithm Wrapper", parameterLabels = {"Log"},
        returnLabels = {"PetrinetWithMarking"},
        returnTypes = {PetrinetWithMarking.class})
public class AlphaAlgorithmWrapper implements MiningAlgorithm {

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@qut.edu.au",
            pack = "Noise Filtering")
    @PluginVariant(variantLabel = "Alpha Algorithm Wrapper", requiredParameterLabels = {0})
    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log) {
        return minePetrinet(context, log, false);
    }

    @Override
    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log, boolean structure) {
        try {
            LogPreprocessing logPreprocessing = new LogPreprocessing();
            log = logPreprocessing.preprocessLog(context, log);

            // The following gathers information required by the Alpha miner
            XLogInfo logInfo = XLogInfoFactory.createLogInfo(log);
            BasicLogRelations basicLogRelations = new BasicLogRelations(log);

            // Call the miner
            Object[] result = new AlphaMiner().doAlphaMiningPrivateWithRelations(context, logInfo, basicLogRelations);
            logPreprocessing.removedAddedElements((Petrinet) result[0]);

            return new PetrinetWithMarking((Petrinet) result[0], (Marking) result[1]);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, boolean structure) {
        PetrinetWithMarking petrinetWithMarking = minePetrinet(context, log, structure);
        return PetriNetToBPMNConverter.convert(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking(), true);
    }

}
