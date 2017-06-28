package com.raffaeleconforti.wrapper.impl.heuristics;

import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.wrapper.LogPreprocessing;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.MiningSettings;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import com.raffaeleconforti.marking.MarkingDiscoverer;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.heuristics.HeuristicsNet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.heuristicsnet.miner.heuristics.converter.HeuristicsNetToPetriNetConverter;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.FlexibleHeuristicsMinerPlugin;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.gui.ParametersPanel;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.settings.HeuristicsMinerSettings;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by conforti on 20/02/15.
 */
@Plugin(name = "Heuristics Miner Wrapper", parameterLabels = {"Log"},
        returnLabels = {"PetrinetWithMarking"},
        returnTypes = {PetrinetWithMarking.class})
public class HeuristicsAlgorithmWrapper implements MiningAlgorithm {

    HeuristicsMinerSettings settings;

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@qut.edu.au",
            pack = "Noise Filtering")
    @PluginVariant(variantLabel = "Heuristics Miner Wrapper", requiredParameterLabels = {0})
    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log) {
        return minePetrinet(context, log, false, null);
    }

    @Override
    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log, boolean structure, MiningSettings params) {
        LogPreprocessing logPreprocessing = new LogPreprocessing();
        log = logPreprocessing.preprocessLog(context, log);

        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {}
        }));

        if(context instanceof FakePluginContext) {
            Collection<XEventClassifier> classifiers = new HashSet();
            classifiers.add(new XEventNameClassifier());
            ParametersPanel parameters = new ParametersPanel(classifiers);
            settings = parameters.getSettings();
        }else {
            Collection<XEventClassifier> classifiers = new HashSet();
            classifiers.add(new XEventNameClassifier());
            ParametersPanel parameters = new ParametersPanel(classifiers);
            parameters.removeAndThreshold();

            context.showConfiguration("Heuristics Miner Parameters", parameters);
            settings = parameters.getSettings();
        }
        HeuristicsNet heuristicsNet = FlexibleHeuristicsMinerPlugin.run(context, log, settings);
        Object[] result = HeuristicsNetToPetriNetConverter.converter(context, heuristicsNet);
        logPreprocessing.removedAddedElements((Petrinet) result[0]);

        if(result[1] == null) result[1] = MarkingDiscoverer.constructInitialMarking(context, (Petrinet) result[0]);
        else MarkingDiscoverer.createInitialMarkingConnection(context, (Petrinet) result[0], (Marking) result[1]);

        Marking finalMarking = MarkingDiscoverer.constructFinalMarking(context, (Petrinet) result[0]);

        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

        return new PetrinetWithMarking((Petrinet) result[0], (Marking) result[1], finalMarking);
    }

    @Override
    public BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, boolean structure, MiningSettings params) {
        PetrinetWithMarking petrinetWithMarking = minePetrinet(context, log, structure, params);
        return PetriNetToBPMNConverter.convert(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking(), true);
    }

    @Override
    public String getAlgorithmName() {
        return "Heuristics Miner ProM6";
    }

    @Override
    public String getAcronym() { return "HM6";}

}
