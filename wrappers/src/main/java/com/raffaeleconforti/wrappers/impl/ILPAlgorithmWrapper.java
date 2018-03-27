package com.raffaeleconforti.wrappers.impl;

import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.ilpminer.ILPMiner;
import com.raffaeleconforti.marking.MarkingDiscoverer;
import com.raffaeleconforti.wrappers.LogPreprocessing;
import com.raffaeleconforti.wrappers.MiningAlgorithm;
import com.raffaeleconforti.wrappers.PetrinetWithMarking;
import com.raffaeleconforti.wrappers.settings.MiningSettings;
import org.deckfour.uitopia.api.event.TaskListener;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.ilpminer.ILPMinerSettings;
import org.processmining.plugins.ilpminer.ILPMinerUI;
import org.processmining.plugins.log.logabstraction.LogRelations;
import org.processmining.plugins.log.logabstraction.implementations.AlphaLogRelationsImpl;
import org.processmining.processtree.ProcessTree;

/**
 * Created by conforti on 20/02/15.
 */
@Plugin(name = "ILP Miner Wrapper", parameterLabels = {"Log"},
        returnLabels = {"PetrinetWithMarking"},
        returnTypes = {PetrinetWithMarking.class})
public class ILPAlgorithmWrapper implements MiningAlgorithm {

    ILPMinerSettings settings;

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@unimelb.edu.au",
            pack = "Noise Filtering")
    @PluginVariant(variantLabel = "ILP Miner Wrapper", requiredParameterLabels = {0})
    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log) {
        return minePetrinet(context, log, false, null, new XEventNameClassifier());
    }

    @Override
    public boolean canMineProcessTree() {
        return false;
    }

    @Override
    public ProcessTree mineProcessTree(UIPluginContext context, XLog log, boolean structure, MiningSettings params, XEventClassifier xEventClassifier) {
        return null;
    }

    @Override
    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log, boolean structure, MiningSettings params, XEventClassifier xEventClassifier) {
        LogPreprocessing logPreprocessing = new LogPreprocessing();
        log = logPreprocessing.preprocessLog(context, log);

        try {
            ILPMiner miner = new ILPMiner();
            Object[] result = null;
            if(settings == null) {
                if (context instanceof FakePluginContext) {
                    settings = new ILPMinerSettings();
                    LogRelations relations = new AlphaLogRelationsImpl(log);
                    result = miner.doILPMiningPrivateWithRelations(context, relations.getSummary(), relations, settings);
                } else {
                    ILPMinerUI ui = new ILPMinerUI();
                    TaskListener.InteractionResult r = context.showWizard("Configure the ILP Mining Algorithm", true, true, ui.initComponents());
                    settings = ui.getSettings();
                    result = miner.doILPMiningWithSettings(context, log, XLogInfoFactory.createLogInfo(log), settings);
                }
            }else {
                if (context instanceof FakePluginContext) {
                    LogRelations relations = new AlphaLogRelationsImpl(log);
                    result = miner.doILPMiningPrivateWithRelations(context, relations.getSummary(), relations, settings);
                } else {
                    result = miner.doILPMiningWithSettings(context, log, XLogInfoFactory.createLogInfo(log), settings);
                }
            }

            logPreprocessing.removedAddedElements((Petrinet) result[0]);

            MarkingDiscoverer.createInitialMarkingConnection(context, (Petrinet) result[0], (Marking) result[1]);

            return new PetrinetWithMarking((Petrinet) result[0], (Marking) result[1], MarkingDiscoverer.constructFinalMarking(context, (Petrinet) result[0]));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, boolean structure, MiningSettings params, XEventClassifier xEventClassifier) {
        PetrinetWithMarking petrinetWithMarking = minePetrinet(context, log, structure, params, xEventClassifier);
        return PetriNetToBPMNConverter.convert(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking(), true);
    }

    @Override
    public String getAlgorithmName() {
        return "ILP Miner";
    }

    @Override
    public String getAcronym() { return "ILP";}

}
