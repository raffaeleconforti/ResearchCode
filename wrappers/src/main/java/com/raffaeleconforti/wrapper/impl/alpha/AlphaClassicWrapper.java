package com.raffaeleconforti.wrapper.impl.alpha;

import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.wrapper.LogPreprocessing;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import com.raffaeleconforti.wrapper.marking.MarkingDiscoverer;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.alphaminer.abstractions.AlphaAbstractionFactory;
import org.processmining.alphaminer.abstractions.AlphaClassicAbstraction;
import org.processmining.alphaminer.abstractions.impl.AlphaClassicAbstractionImpl;
import org.processmining.alphaminer.algorithms.AlphaMinerFactory;
import org.processmining.alphaminer.algorithms.AlphaPlusMinerImpl;
import org.processmining.alphaminer.parameters.AlphaMinerParameters;
import org.processmining.alphaminer.parameters.AlphaPlusMinerParameters;
import org.processmining.alphaminer.parameters.AlphaVersion;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.log.logabstraction.BasicLogRelations;

import java.util.concurrent.ExecutionException;

/**
 * Created by conforti on 20/02/15.
 */
@Plugin(name = "Alpha Plus Wrapper", parameterLabels = {"Log"},
        returnLabels = {"PetrinetWithMarking"},
        returnTypes = {PetrinetWithMarking.class})
public class AlphaClassicWrapper implements MiningAlgorithm {

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@qut.edu.au",
            pack = "Noise Filtering")
    @PluginVariant(requiredParameterLabels = {0})
    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log) {
        return minePetrinet(context, log, false);
    }

    @Override
    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log, boolean structure) {
        LogPreprocessing logPreprocessing = new LogPreprocessing();
        log = logPreprocessing.preprocessLog(context, log);

        // Call the miner
        AlphaMinerParameters parameters = new AlphaMinerParameters(AlphaVersion.CLASSIC);
        AlphaMinerFactory factory = new AlphaMinerFactory();
        Pair<Petrinet, Marking> pair = factory.createAlphaMiner(log, new XEventNameClassifier(), parameters).run();
        logPreprocessing.removedAddedElements(pair.getFirst());

        MarkingDiscoverer.createInitialMarkingConnection(context, pair.getFirst(), pair.getSecond());
        return new PetrinetWithMarking(pair.getFirst(), pair.getSecond(), MarkingDiscoverer.constructFinalMarking(context, pair.getFirst()));
    }

    @Override
    public BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, boolean structure) {
        PetrinetWithMarking petrinetWithMarking = minePetrinet(context, log, structure);
        return PetriNetToBPMNConverter.convert(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking(), true);
    }

    @Override
    public String getAlgorithmName() {
        return "Alpha Miner Classic";
    }

    @Override
    public String getAcronym() { return "AM";}
}
