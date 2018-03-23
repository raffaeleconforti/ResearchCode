package com.raffaeleconforti.wrappers;

import au.edu.qut.bpmn.structuring.StructuringService;
import com.raffaeleconforti.conversion.bpmn.BPMNToPetriNetConverter;
import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.marking.MarkingDiscoverer;
import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.impl.AlignmentBasedFitness;
import com.raffaeleconforti.measurements.impl.AlignmentBasedPrecision;
import com.raffaeleconforti.measurements.impl.BPMNComplexity;
import com.raffaeleconforti.wrappers.impl.heuristics.HeuristicsAlgorithmWrapper;
import com.raffaeleconforti.wrappers.settings.MiningSettings;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.heuristics.HeuristicsNet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.heuristicsnet.miner.heuristics.converter.HeuristicsNetToPetriNetConverter;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.FlexibleHeuristicsMinerPlugin;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.gui.ParametersPanel;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.settings.HeuristicsMinerSettings;
import org.processmining.processtree.ProcessTree;

import java.io.*;
import java.util.*;

/**
 * Created by Adriano on 5/19/2017.
 */
public class HyperParamOptimizedStructuredHeuristicsMiner implements MiningAlgorithm {

    private static double STEP = 0.20D;
    private static double MIN = 0.00D;
    private static double MAX = 1.01D;

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
        return hyperparamEvaluation(context, log, true, xEventClassifier);
    }

    public PetrinetWithMarking hyperparamEvaluation(UIPluginContext context, XLog log, boolean structure, XEventClassifier xEventClassifier) {
        PetrinetWithMarking petrinet;

        LogPreprocessing logPreprocessing = new LogPreprocessing();
        log = logPreprocessing.preprocessLog(context, log);

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

        double d_threshold;
        double rtb_threshold;

        BPMNDiagram diagram, structuredDiagram;
        StructuringService ss = new StructuringService();

        Collection<XEventClassifier> classifiers = new HashSet();
        classifiers.add(new XEventNameClassifier());
        HeuristicsMinerSettings minerSettings;
        ParametersPanel parameters = new ParametersPanel(classifiers);
        minerSettings = parameters.getSettings();
        minerSettings.setUseLongDistanceDependency(false);

        PrintWriter writer;
        try {
            writer = new PrintWriter(".\\structheuristicsminer_hyperparam_" + System.currentTimeMillis() + ".txt");
            writer.println("rtb_threshold,d_threshold,fitness,precision,fscore,generalization,size,cfc,struct");
        } catch(Exception e) {
            writer = new PrintWriter(System.out);
            System.out.println("ERROR - impossible to create the file for storing the results: printing only on terminal.");
        }

        rtb_threshold = MIN;
        do {
            minerSettings.setRelativeToBestThreshold(rtb_threshold);
            d_threshold = MIN;
            do {

                try {
                    minerSettings.setDependencyThreshold(d_threshold);

                    System.setOut(new PrintStream(new OutputStream() {
                        @Override
                        public void write(int b) throws IOException {}
                    }));

                    HeuristicsNet heuristicsNet = FlexibleHeuristicsMinerPlugin.run(context, log, minerSettings);
                    Object[] result = HeuristicsNetToPetriNetConverter.converter(context, heuristicsNet);

                    if(result[1] == null) result[1] = MarkingDiscoverer.constructInitialMarking(context, (Petrinet) result[0]);
                    else MarkingDiscoverer.createInitialMarkingConnection(context, (Petrinet) result[0], (Marking) result[1]);
                    Marking finalMarking = MarkingDiscoverer.constructFinalMarking(context, (Petrinet) result[0]);

                    diagram = PetriNetToBPMNConverter.convert((Petrinet) result[0], (Marking) result[1], finalMarking, false);
                    structuredDiagram = ss.structureDiagram(diagram, "ASTAR", 100, 500, 10, 100, 2, true, true, true);
                    result = BPMNToPetriNetConverter.convert(structuredDiagram);
                    petrinet = new PetrinetWithMarking((Petrinet) result[0], (Marking) result[1], (Marking) result[2]);

                    MarkingDiscoverer.createInitialMarkingConnection(context, (Petrinet) result[0], (Marking) result[1]);
                    MarkingDiscoverer.createFinalMarkingConnection(context, (Petrinet) result[0], (Marking) result[2]);

                    fit = fitnessCalculator.computeMeasurement(context, xEventClassifier, petrinet, this, log).getValue();
                    prec = precisionCalculator.computeMeasurement(context, xEventClassifier, petrinet, this, log).getValue();
                    gen = computeGeneralization();
                    complexity = bpmnComplexity.computeMeasurement(context, xEventClassifier, petrinet, this, log);
                    size = Double.valueOf(complexity.getMetricValue("size"));
                    cfc = Double.valueOf(complexity.getMetricValue("cfc"));
                    struct = Double.valueOf(complexity.getMetricValue("struct."));

                    if( fit.isNaN() || prec.isNaN() ) fit = prec = score = 0.0;
                    else score = (fit * prec * 2) / (fit + prec);
                    if( score.isNaN() ) score = 0.0;

                    combination = rtb_threshold + "," + d_threshold + "," + fit + "," + prec + "," + score + "," + gen + "," + size + "," + cfc + "," + struct;
                    writer.println(combination);
                    writer.flush();

                } catch (Exception e) {
                    System.out.println("ERROR - S-Heuristics Miner output model broken @ " + rtb_threshold + " : " + d_threshold);
                }

                d_threshold += STEP;
            } while (d_threshold <= MAX);

            rtb_threshold += STEP;
        } while (rtb_threshold <= MAX);

        return null;
    }

    private Double computeGeneralization() {
        return 0.0;
    }

    public BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, boolean structure, MiningSettings params, XEventClassifier xEventClassifier) {
        HeuristicsAlgorithmWrapper heuristicsminer = new HeuristicsAlgorithmWrapper();
        return heuristicsminer.mineBPMNDiagram(context, log, true, params, xEventClassifier);
    }

    @Override
    public String getAlgorithmName() {
        return "Naive HyperParam-Optimized Structured Heuristics Miner";
    }

    @Override
    public String getAcronym() {
        return "HPO-SHM";
    }

}
