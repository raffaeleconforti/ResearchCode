package com.raffaeleconforti.hyperparamoptimized;

import au.edu.qut.processmining.miners.splitminer.SplitMiner;
import au.edu.qut.processmining.miners.splitminer.ui.miner.SplitMinerUIResult;
import au.edu.qut.promplugins.SplitMinerPlugin;
import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.conversion.bpmn.BPMNToPetriNetConverter;
import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.marking.MarkingDiscoverer;
import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.impl.AlignmentBasedFitness;
import com.raffaeleconforti.measurements.impl.AlignmentBasedPrecision;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.DoNotCreateNewInstance;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.bpmn.plugins.BpmnExportPlugin;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Adriano on 5/5/2017.
 */
@Plugin(name = "Naive HyperParam-Optimized Split Miner Wrapper",
        parameterLabels = {"Event Log"},
        returnLabels = {"PetrinetWithMarking"},
        returnTypes = {PetrinetWithMarking.class})
public class HyperParamOptimizedSplitMiner implements MiningAlgorithm {

    static double[] pt_values={0.05, 0.10, 0.25, 0.50, 0.75, 1.00};

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Adriano Augusto",
            email = "adriano.august@ut.ee",
            pack = "bpmntk-osgi")
    @PluginVariant(variantLabel = "Naive HyperParam-Optimized Split Miner Wrapper", requiredParameterLabels = {0})
    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log) {
        return minePetrinet(context, log, false);
    }

    @Override
    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log, boolean structure) {
        Map<Double, PetrinetWithMarking> models = new HashMap<>();
        Map<Double, Double> thresholds = new HashMap<>();
        SplitMiner yam = new SplitMiner();
        BPMNDiagram bpmn;
        PetrinetWithMarking petrinet;

        AlignmentBasedFitness fitnessCalculator = new AlignmentBasedFitness();
        AlignmentBasedPrecision precisionCalculator = new AlignmentBasedPrecision();
        XEventNameClassifier eventNameClassifier = new XEventNameClassifier();
        Double fitness;
        Double precision;
        Double fscore;
        Double maxScore;

        for (Double p_threshold : pt_values) {
            bpmn = yam.mineBPMNModel(log, 1.0, p_threshold, true, SplitMinerUIResult.StructuringTime.NONE);
            petrinet = convertToPetrinet(context, bpmn);
            fitness = fitnessCalculator.computeMeasurement(context, eventNameClassifier, petrinet, this, log).getValue();
            precision = precisionCalculator.computeMeasurement(context, eventNameClassifier, petrinet, this, log).getValue();

            fscore = (fitness * precision * 2) / (fitness + precision);
            if( fscore.isNaN() ) fscore = 0.0;

            models.put(fscore, petrinet);
            thresholds.put(fscore, p_threshold);
            System.out.println("DEBUG - result for " + p_threshold + " is: " + fscore);
        }

        maxScore = Collections.max(models.keySet());
        System.out.println("DEBUG - best results got for: " + thresholds.get(maxScore));
        return models.get(maxScore);
    }

    private PetrinetWithMarking convertToPetrinet(UIPluginContext context, BPMNDiagram diagram) {

        Object[] result = BPMNToPetriNetConverter.convert(diagram);

        if(result[1] == null) result[1] = PetriNetToBPMNConverter.guessInitialMarking((Petrinet) result[0]);
        if(result[2] == null) result[2] = PetriNetToBPMNConverter.guessFinalMarking((Petrinet) result[0]);

        if(result[1] == null) result[1] = MarkingDiscoverer.constructInitialMarking(context, (Petrinet) result[0]);
        else MarkingDiscoverer.createInitialMarkingConnection(context, (Petrinet) result[0], (Marking) result[1]);

        if(result[2] == null) result[2] = MarkingDiscoverer.constructFinalMarking(context, (Petrinet) result[0]);
        else MarkingDiscoverer.createFinalMarkingConnection(context, (Petrinet) result[0], (Marking) result[1]);
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

        return new PetrinetWithMarking((Petrinet) result[0], (Marking) result[1], (Marking) result[2]);
    }

    public BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, boolean structure) {
        BPMNDiagram output = null;
        PetrinetWithMarking petrinet = minePetrinet(context, log);
        output = PetriNetToBPMNConverter.convert(petrinet.getPetrinet(), petrinet.getInitialMarking(), petrinet.getFinalMarking(), false);
        return output;
    }

    @Override
    public String getAlgorithmName() {
        return "Naive HyperParam-Optimized Split Miner";
    }

    @Override
    public String getAcronym() {
        return "HPO-SM";
    }

    private void export(BPMNDiagram diagram, String name) {
        BpmnExportPlugin bpmnExportPlugin = new BpmnExportPlugin();
        UIContext context = new UIContext();
        UIPluginContext uiPluginContext = context.getMainPluginContext();
        try {
            bpmnExportPlugin.export(uiPluginContext, diagram, new File(name + ".bpmn"));
        } catch (Exception e) { System.out.println("ERROR - impossible to export .bpmn result of split-miner"); }
    }

}
