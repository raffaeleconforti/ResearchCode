package com.raffaeleconforti.wrappers;

import au.edu.qut.processmining.miners.splitminer.SplitMiner;
import au.edu.qut.processmining.miners.splitminer.ui.dfgp.DFGPUIResult;
import au.edu.qut.processmining.miners.splitminer.ui.miner.SplitMinerUIResult;
import com.raffaeleconforti.conversion.bpmn.BPMNToPetriNetConverter;
import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.marking.MarkingDiscoverer;
import com.raffaeleconforti.measurements.impl.AlignmentBasedFitness;
import com.raffaeleconforti.measurements.impl.AlignmentBasedPrecision;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.settings.MiningSettings;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
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

    private static double p_STEP = 0.10D;
    private static double p_MIN = 0.00D;
    private static double p_MAX = 1.05D;

    private static double f_STEP = 0.10D;
    private static double f_MIN = 0.10D;
    private static double f_MAX = 1.05D;

//    static double[] pt_values={0.05, 0.10, 0.25, 0.50, 0.75, 1.00};
//    static double[] pt_values={0.10, 0.40, 0.70, 1.00};

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Adriano Augusto",
            email = "adriano.august@ut.ee",
            pack = "bpmntk-osgi")
    @PluginVariant(variantLabel = "Naive HyperParam-Optimized Split Miner Wrapper", requiredParameterLabels = {0})
    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log) {
        return minePetrinet(context, log, false, null);
    }

    @Override
    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log, boolean structure, MiningSettings params) {
        return discoverBestOn(context, log, structure, BestOn.FSCORE);
    }

    public PetrinetWithMarking discoverBestOn(UIPluginContext context, XLog log, boolean structure, BestOn metric) {
        Map<String, PetrinetWithMarking> models = new HashMap<>();
        Map<Double, String> fitness = new HashMap<>();
        Map<Double, String> precision = new HashMap<>();
        Map<Double, String> fscore = new HashMap<>();
        String combination;

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
        String bestCombination = null;

        Double p_threshold;
        Double f_threshold = f_MIN;
        do {
            p_threshold = p_MIN;
            do {
                combination = ":p:" + p_threshold + ":f:" + f_threshold;
                try {
                    bpmn = yam.mineBPMNModel(log, f_threshold, p_threshold, DFGPUIResult.FilterType.FWG, true, true, SplitMinerUIResult.StructuringTime.NONE);
                    petrinet = convertToPetrinet(context, bpmn);
                    models.put(combination, petrinet);

                    fit = fitnessCalculator.computeMeasurement(context, eventNameClassifier, petrinet, this, log).getValue();
                    prec = precisionCalculator.computeMeasurement(context, eventNameClassifier, petrinet, this, log).getValue();

                    if (fit.isNaN()) fit = 0.0;
                    if (prec.isNaN()) prec = 0.0;
                    score = (fit * prec * 2) / (fit + prec);
                    if(score.isNaN()) score = 0.0;

                    fitness.put(fit, combination);
                    precision.put(prec, combination);
                    fscore.put(score, combination);

                    System.out.println("RESULT - @ " + combination);
                    System.out.println(fit);
                    System.out.println(prec);
                    System.out.println(score);
                } catch (Exception e) {
                    System.out.println("ERROR - splitminer output model broken @ " + combination);
                    fitness.put(0.0D, combination);
                    precision.put(0.0D, combination);
                    fscore.put(0.0D, combination);
                }

                p_threshold += p_STEP;
            } while ( p_threshold <= p_MAX );
            f_threshold += f_STEP;
        } while( f_threshold <= f_MAX );

        switch ( metric ) {
            case FIT:
                bestValue = Collections.max(fitness.keySet());
                bestCombination = fitness.get(bestValue);
                break;
            case PREC: bestValue = Collections.max(precision.keySet());
                bestCombination = precision.get(bestValue);
                break;
            case FSCORE: bestValue = Collections.max(fscore.keySet());
                bestCombination = fscore.get(bestValue);
                break;
        }

        System.out.println("DEBUG - best result @ " + bestCombination);
        return models.get(bestCombination);
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

    public BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, boolean structure, MiningSettings params) {
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
