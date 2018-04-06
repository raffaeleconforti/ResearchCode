package com.raffaeleconforti.wrappers;

import com.raffaeleconforti.marking.MarkingDiscoverer;
import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.impl.*;
import com.raffaeleconforti.wrappers.impl.heuristics.HeuristicsAlgorithmWrapper;
import com.raffaeleconforti.wrappers.settings.MiningSettings;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Adriano on 5/19/2017.
 */
public class HyperParamOptimizedHeuristicsMiner implements MiningAlgorithm {

    private static double STEP = 0.10D;
    private static double MIN = 0.00D;
    private static double MAX = 1.00D;

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
        PetrinetWithMarking petrinet = null;
        Map<XLog, XLog> crossValidationLogs;

        XEventClassifier eventNameClassifier = xEventClassifier;
        LogPreprocessing logPreprocessing = new LogPreprocessing();
        log = logPreprocessing.preprocessLog(context, log);

        AlignmentBasedFitness fitnessCalculator = new AlignmentBasedFitness();
        AlignmentBasedPrecision precisionCalculator = new AlignmentBasedPrecision();
        BPMNComplexity bpmnComplexity = new BPMNComplexity();

        String lName = XConceptExtension.instance().extractName(log);
        String fName = ".\\heuristicsminer_hyperparam_" + lName + "_" + System.currentTimeMillis() + ".csv";

        Double fit;
        Double prec;
        Double score;
        String gen;
        Measure complexity;
        Double size;
        Double cfc;
        Double struct;
        long eTime;
        boolean sound;

        String combination;

        double d_threshold;
        double rtb_threshold;


        Collection<XEventClassifier> classifiers = new HashSet();
        classifiers.add(new XEventNameClassifier());
        HeuristicsMinerSettings minerSettings;
        ParametersPanel parameters = new ParametersPanel(classifiers);
        minerSettings = parameters.getSettings();
        minerSettings.setUseLongDistanceDependency(false);

        PrintWriter writer;
        try {
            writer = new PrintWriter(fName);
            writer.println("rtb_threshold,d_threshold,fitness,precision,fscore,gf1,gf2,gf3,gen,size,cfc,struct,soundness,mining-time");
        } catch(Exception e) {
            writer = new PrintWriter(System.out);
            System.out.println("ERROR - impossible to create the file for storing the results: printing only on terminal.");
        }


        crossValidationLogs = XFoldAlignmentBasedFMeasure.getCrossValidationLogs(log, XFoldAlignmentBasedFMeasure.K);

        rtb_threshold = MIN;
        do {
            minerSettings.setRelativeToBestThreshold(rtb_threshold);
            d_threshold = MIN;
            do {
                try {
                    minerSettings.setDependencyThreshold(d_threshold);

                    System.setOut(new PrintStream(new OutputStream() {
                        @Override
                        public void write(int b) {}
                    }));

                    eTime = System.currentTimeMillis();
                    HeuristicsNet heuristicsNet = FlexibleHeuristicsMinerPlugin.run(context, log, minerSettings);
                    eTime = System.currentTimeMillis() - eTime;

                    Object[] result = HeuristicsNetToPetriNetConverter.converter(context, heuristicsNet);
//                            logPreprocessing.removedAddedElements((Petrinet) result[0]);

                    if(result[1] == null) result[1] = MarkingDiscoverer.constructInitialMarking(context, (Petrinet) result[0]);
                    else MarkingDiscoverer.createInitialMarkingConnection(context, (Petrinet) result[0], (Marking) result[1]);
                    Marking finalMarking = MarkingDiscoverer.constructFinalMarking(context, (Petrinet) result[0]);
                    petrinet = new PetrinetWithMarking((Petrinet) result[0], (Marking) result[1], finalMarking);

                    if( sound = Soundness.isSound(petrinet) ) {
                        fit = fitnessCalculator.computeMeasurement(context, xEventClassifier, petrinet, this, log).getValue();
                        prec = precisionCalculator.computeMeasurement(context, xEventClassifier, petrinet, this, log).getValue();
                    } else {
                        fit = prec = -1.0;
                    }
                    gen = computeGeneralization(context, crossValidationLogs, xEventClassifier, minerSettings);
                    complexity = bpmnComplexity.computeMeasurement(context, xEventClassifier, petrinet, this, log);
                    size = Double.valueOf(complexity.getMetricValue("size"));
                    cfc = Double.valueOf(complexity.getMetricValue("cfc"));
                    struct = Double.valueOf(complexity.getMetricValue("struct."));

                    score = (fit * prec * 2) / (fit + prec);
                    if( score.isNaN() ) score = -1.0;

                    combination = rtb_threshold + "," + d_threshold + "," + fit + "," + prec + "," + score + "," + gen + "," + size + "," + cfc + "," + struct + "," + sound + "," + eTime;
                    writer.println(combination);
                    writer.flush();

                } catch (Exception e) {
                    System.out.println("ERROR - Heuristics Miner output model broken @ " + rtb_threshold + " : " + d_threshold);
                }

                d_threshold += STEP;
            } while (d_threshold <= MAX);

            rtb_threshold += STEP;
        } while (rtb_threshold <= MAX);

        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        return petrinet;
    }

    private String computeGeneralization(UIPluginContext context, Map<XLog, XLog> crossValidationLogs, XEventClassifier xEventClassifier, HeuristicsMinerSettings minerSettings) {
        PetrinetWithMarking petrinetWithMarking;
        int k = crossValidationLogs.size();
        String comb = "";

        XLog evalLog;
        AlignmentBasedFitness alignmentBasedFitness = new AlignmentBasedFitness();
        AlignmentBasedPrecision alignmentBasedPrecision = new AlignmentBasedPrecision();

        double fitness = 0.0;
        double precision = 0.0;
        Double fscore = 0.0;
        Double f;
        Double p;
        Double fs;

        for (XLog miningLog : crossValidationLogs.keySet()) {
            evalLog = crossValidationLogs.get(miningLog);
            f = 0.0;
            p = 0.0;
            fs = 0.0;

            try {

                System.setOut(new PrintStream(new OutputStream() {
                    @Override
                    public void write(int b) {}
                }));

                HeuristicsNet heuristicsNet = FlexibleHeuristicsMinerPlugin.run(context, miningLog, minerSettings);
                Object[] result = HeuristicsNetToPetriNetConverter.converter(context, heuristicsNet);
//                            logPreprocessing.removedAddedElements((Petrinet) result[0]);

                if(result[1] == null) result[1] = MarkingDiscoverer.constructInitialMarking(context, (Petrinet) result[0]);
                else MarkingDiscoverer.createInitialMarkingConnection(context, (Petrinet) result[0], (Marking) result[1]);
                Marking finalMarking = MarkingDiscoverer.constructFinalMarking(context, (Petrinet) result[0]);
                petrinetWithMarking = new PetrinetWithMarking((Petrinet) result[0], (Marking) result[1], finalMarking);

                if (Soundness.isSound(petrinetWithMarking)) {
                    f = alignmentBasedFitness.computeMeasurement(context, xEventClassifier, petrinetWithMarking, this, evalLog).getValue();
//                    p = alignmentBasedPrecision.computeMeasurement(context, xEventClassifier, petrinetWithMarking, this, evalLog).getValue();
//                    fs = (2.0*f*p)/(f+p);
                }

                fitness += f;
//                precision += p;
//                fscore += fs;
            } catch (Exception e) { }

            comb += Double.toString(f) + ",";
        }

        comb += Double.toString(fitness / (double) k);

//        precision = precision/(double)k;
//        fscore = fscore/(double)k;

        return comb;
    }


    public BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, boolean structure, MiningSettings params, XEventClassifier xEventClassifier) {
    HeuristicsAlgorithmWrapper heuristicsminer = new HeuristicsAlgorithmWrapper();
    return heuristicsminer.mineBPMNDiagram(context, log, structure, params, xEventClassifier);
    }

    @Override
    public String getAlgorithmName() {
        return "Naive HyperParam-Optimized Heuristics Miner 6.0";
    }

    @Override
    public String getAcronym() {
        return "HPO-HM6";
    }
}
