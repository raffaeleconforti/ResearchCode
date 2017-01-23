package com.raffaeleconforti.wrapper.impl;

import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.wrapper.LogPreprocessing;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import com.raffaeleconforti.marking.MarkingDiscoverer;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.etm.ETM;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.TreeUtils;
import org.processmining.plugins.etm.model.narytree.conversion.NAryTreeToProcessTree;
import org.processmining.plugins.etm.parameters.ETMParam;
import org.processmining.plugins.etm.parameters.ETMParamFactory;
import org.processmining.plugins.etm.ui.plugins.ETMPlugin;
import org.processmining.plugins.etm.ui.plugins.ETMwithoutGUI;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.TerminationCondition;

import java.io.*;
import java.util.Iterator;
import java.util.List;

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

            System.setOut(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {}
            }));

            ProcessTree processTree;
            if(context instanceof FakePluginContext) {
                ETMParam params = ETMParamFactory.buildStandardParam(log, context);
                params.addTerminationConditionMaxDuration(3600000); //1 hour
                processTree = ETMwithoutGUI.minePTWithParameters(context, log, new XEventNameClassifier(), params);
            }else {
                ETMPlugin etmPlugin = new ETMPlugin();
                processTree = etmPlugin.withoutSeed(context, log);
            }
            ProcessTree2Petrinet.PetrinetWithMarkings petrinetWithMarkings = ProcessTree2Petrinet.convert(processTree);

            logPreprocessing.removedAddedElements(petrinetWithMarkings.petrinet);

            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

            MarkingDiscoverer.createInitialMarkingConnection(context, petrinetWithMarkings.petrinet, petrinetWithMarkings.initialMarking);
            MarkingDiscoverer.createFinalMarkingConnection(context, petrinetWithMarkings.petrinet, petrinetWithMarkings.finalMarking);
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

    @Override
    public String getAcronym() { return "ETM";}
}
