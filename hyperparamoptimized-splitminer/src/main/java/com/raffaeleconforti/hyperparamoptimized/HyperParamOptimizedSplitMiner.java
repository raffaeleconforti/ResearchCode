package com.raffaeleconforti.hyperparamoptimized;

import au.edu.qut.processmining.miners.splitminer.SplitMiner;
import au.edu.qut.processmining.miners.splitminer.ui.dfgp.DFGPUIResult;
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

    public enum BestOn {FIT, PREC, FSCORE}

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
        return discoverBestOn(context, log, structure, HyperParamOptimizedSplitMiner.BestOn.PREC);
    }

    public PetrinetWithMarking discoverBestOn(UIPluginContext context, XLog log, boolean structure, BestOn metric) {
        Map<Double, PetrinetWithMarking> models = new HashMap<>();
        Map<Double, Double> fitness = new HashMap<>();
        Map<Double, Double> precision = new HashMap<>();
        Map<Double, Double> fscore = new HashMap<>();

        SplitMiner yam = new SplitMiner();
        BPMNDiagram bpmn;
        PetrinetWithMarking petrinet;

        AlignmentBasedFitness fitnessCalculator = new AlignmentBasedFitness();
        AlignmentBasedPrecision precisionCalculator = new AlignmentBasedPrecision();
        XEventNameClassifier eventNameClassifier = new XEventNameClassifier();

        Double fit;
        Double prec;
        Double score;

        Double bestValue;
        Double bestThreshold;

        for (Double p_threshold : pt_values) {
            try {
                bpmn = yam.mineBPMNModel(log, 1.0, p_threshold, DFGPUIResult.FilterType.WTH, true, SplitMinerUIResult.StructuringTime.NONE);
                petrinet = convertToPetrinet(context, bpmn);
                models.put(p_threshold, petrinet);

                fit = fitnessCalculator.computeMeasurement(context, eventNameClassifier, petrinet, this, log).getValue();
                prec = precisionCalculator.computeMeasurement(context, eventNameClassifier, petrinet, this, log).getValue();

                if( fit.isNaN() ) fit = 0.0;
                if( prec.isNaN() ) prec = 0.0;
                score = (fit*prec*2)/(fit+prec);

                fitness.put(fit, p_threshold);
                precision.put(prec, p_threshold);
                fscore.put(score, p_threshold);

                System.out.println("DEBUG - fitness @ " + p_threshold + " : " + fit);
                System.out.println("DEBUG - precision @ " + p_threshold + " : " + prec);
                System.out.println("DEBUG - f-score @ " + p_threshold + " : " + score);
            } catch (Exception e) {
                System.out.println("ERROR - splitminer output model broken @ " + p_threshold);
                fitness.put(0.0, p_threshold);
                precision.put(0.0, p_threshold);
                fscore.put(0.0, p_threshold);
            }
        }

        switch ( metric ) {
            case FIT:
                bestValue = Collections.max(fitness.keySet());
                bestThreshold = fitness.get(bestValue);
                break;
            case PREC: bestValue = Collections.max(precision.keySet());
                bestThreshold = precision.get(bestValue);
                break;
            case FSCORE: bestValue = Collections.max(fscore.keySet());
                bestThreshold = fscore.get(bestValue);
                break;
            default: bestThreshold = 0.05;
        }

        System.out.println("DEBUG - best result @ " + bestThreshold);
        return models.get(bestThreshold);
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
