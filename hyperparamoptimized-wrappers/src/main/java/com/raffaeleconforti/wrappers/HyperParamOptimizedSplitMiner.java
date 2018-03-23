package com.raffaeleconforti.wrappers;

import au.edu.qut.processmining.miners.splitminer.SplitMiner;
import au.edu.qut.processmining.miners.splitminer.ui.dfgp.DFGPUIResult;
import au.edu.qut.processmining.miners.splitminer.ui.miner.SplitMinerUIResult;
import com.raffaeleconforti.conversion.bpmn.BPMNToPetriNetConverter;
import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.marking.MarkingDiscoverer;
import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.impl.AlignmentBasedFitness;
import com.raffaeleconforti.measurements.impl.AlignmentBasedPrecision;
import com.raffaeleconforti.measurements.impl.BPMNComplexity;
import com.raffaeleconforti.wrappers.impl.SplitMinerWrapper;
import com.raffaeleconforti.wrappers.settings.MiningSettings;
import org.deckfour.xes.classification.XEventClassifier;
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
import org.processmining.processtree.ProcessTree;

import java.io.*;
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

    private static double p_STEP = 0.10D;
    private static double p_MIN = 0.00D;
    private static double p_MAX = 1.05D;

    private static double f_STEP = 0.10D;
    private static double f_MIN = 0.10D;
    private static double f_MAX = 1.05D;

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Adriano Augusto",
            email = "adriano.august@ut.ee",
            pack = "bpmntk-osgi")
    @PluginVariant(variantLabel = "Naive HyperParam-Optimized Split Miner Wrapper", requiredParameterLabels = {0})
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
        return hyperparamEvaluation(context, log, structure, xEventClassifier);
    }

    public PetrinetWithMarking hyperparamEvaluation(UIPluginContext context, XLog log, boolean structure, XEventClassifier xEventClassifier) {
        SplitMiner yam = new SplitMiner();
        BPMNDiagram bpmn;
        PetrinetWithMarking petrinet;

        AlignmentBasedFitness fitnessCalculator = new AlignmentBasedFitness();
        AlignmentBasedPrecision precisionCalculator = new AlignmentBasedPrecision();
        BPMNComplexity bpmnComplexity = new BPMNComplexity();

        Double fit;
        Double prec;
        Double score;
        Double gen;
        Measure complexity;
        Double size;
        Double cfc;
        Double struct;

        String combination;
        Double p_threshold;
        Double f_threshold;

        PrintWriter writer;
        try {
            writer = new PrintWriter(".\\splitminer_hyperparam_" + System.currentTimeMillis() + ".txt");
            writer.println("f_threshold,p_threshold,fitness,precision,fscore,generalization,size,cfc,struct");
        } catch(Exception e) {
            writer = new PrintWriter(System.out);
            System.out.println("ERROR - impossible to create the file for storing the results: printing only on terminal.");
        }

        f_threshold = f_MIN;
        do {
            p_threshold = p_MIN;
            do {
                try {
                    bpmn = yam.mineBPMNModel(log, xEventClassifier, f_threshold, p_threshold, DFGPUIResult.FilterType.WTH, true, true, false, SplitMinerUIResult.StructuringTime.NONE);
                    petrinet = convertToPetrinet(context, bpmn);

                    fit = fitnessCalculator.computeMeasurement(context, xEventClassifier, petrinet, this, log).getValue();
                    prec = precisionCalculator.computeMeasurement(context, xEventClassifier, petrinet, this, log).getValue();
                    gen = computeGeneralization();
                    complexity = bpmnComplexity.computeMeasurementBPMN(bpmn);
                    size = Double.valueOf(complexity.getMetricValue("size"));
                    cfc = Double.valueOf(complexity.getMetricValue("cfc"));
                    struct = Double.valueOf(complexity.getMetricValue("struct."));

                    if( fit.isNaN() || prec.isNaN() ) fit = prec = score = 0.0;
                    else score = (fit * prec * 2) / (fit + prec);
                    if( score.isNaN() ) score = 0.0;

                    combination = f_threshold + "," + p_threshold + "," + fit + "," + prec + "," + score + "," + gen + "," + size + "," + cfc + "," + struct;
                    writer.println(combination);
                    writer.flush();

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("ERROR - splitminer output model broken @ " + f_threshold + " : " + p_threshold);
                }
                p_threshold += p_STEP;
            } while ( p_threshold <= p_MAX );
            f_threshold += f_STEP;
        } while( f_threshold <= f_MAX );

        return null;
    }

    private Double computeGeneralization() {
        return 0.0;
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

    public BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, boolean structure, MiningSettings params, XEventClassifier xEventClassifier) {
        SplitMinerWrapper splitminer = new SplitMinerWrapper();
        return splitminer.mineBPMNDiagram(context, log, structure, params, xEventClassifier);
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
