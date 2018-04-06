package com.raffaeleconforti.wrappers;


import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.impl.*;
import com.raffaeleconforti.wrappers.impl.inductive.InductiveMinerIMfWrapper;
import com.raffaeleconforti.wrappers.settings.MiningSettings;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMf;
import org.processmining.plugins.InductiveMiner.plugins.IMPetriNet;
import org.processmining.processtree.ProcessTree;

import java.io.*;
import java.util.Map;


public class HyperParamOptimizedInductiveMinerInfrequent implements MiningAlgorithm {

    private static float STEP = 0.050F;
    private static float MIN = 0.00F;
    private static float MAX = 1.010F;

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
        IMPetriNet miner = new IMPetriNet();
        MiningParameters miningParameters = new MiningParametersIMf();
        PetrinetWithMarking petrinet = null;

        XEventClassifier eventNameClassifier = xEventClassifier;
        LogPreprocessing logPreprocessing = new LogPreprocessing();
        log = logPreprocessing.preprocessLog(context, log);

        Map<XLog, XLog> crossValidationLogs = XFoldAlignmentBasedFMeasure.getCrossValidationLogs(log, XFoldAlignmentBasedFMeasure.K);

        String lName = XConceptExtension.instance().extractName(log);
        String fName = ".\\inductiveminer_hyperparam_" + lName + "_" + System.currentTimeMillis() + ".csv";

        AlignmentBasedFitness fitnessCalculator = new AlignmentBasedFitness();
        AlignmentBasedPrecision precisionCalculator = new AlignmentBasedPrecision();
        BPMNComplexity bpmnComplexity = new BPMNComplexity();

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
        PrintWriter writer;

        try {
            writer = new PrintWriter(fName);
            writer.println("n_threshold,fitness,precision,fscore,gf1,gf2,gf3,gen,size,cfc,struct,soundness,mining-time");
        } catch(Exception e) {
            writer = new PrintWriter(System.out);
            System.out.println("ERROR - impossible to create the file for storing the results: printing only on terminal.");
        }

        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {}
        }));

        Float threshold = MIN;
        do {
            try {
                miningParameters.setNoiseThreshold(threshold);

                System.setOut(new PrintStream(new OutputStream() {
                    @Override
                    public void write(int b) {}
                }));

                eTime = System.currentTimeMillis();
                Object[] result = miner.minePetriNetParameters(context, log, miningParameters);
                eTime = System.currentTimeMillis() -eTime;

                petrinet = new PetrinetWithMarking((Petrinet) result[0], (Marking) result[1], (Marking) result[2]);

                if( sound = Soundness.isSound(petrinet) ) {
                    fit = fitnessCalculator.computeMeasurement(context, xEventClassifier, petrinet, this, log).getValue();
                    prec = precisionCalculator.computeMeasurement(context, xEventClassifier, petrinet, this, log).getValue();
                } else {
                    fit = prec = -1.0;
                }
                gen = computeGeneralization(context, crossValidationLogs, xEventClassifier, miningParameters);
                complexity = bpmnComplexity.computeMeasurement(context, eventNameClassifier, petrinet, this, log);
                size = Double.valueOf(complexity.getMetricValue("size"));
                cfc = Double.valueOf(complexity.getMetricValue("cfc"));
                struct = Double.valueOf(complexity.getMetricValue("struct."));

                score = (fit * prec * 2) / (fit + prec);
                if( score.isNaN() ) score = -1.0;

                combination = threshold + "," + fit + "," + prec + "," + score + "," + gen + "," + size + "," + cfc + "," + struct + "," + sound + "," + eTime;
                writer.println(combination);
                writer.flush();

            } catch ( Exception e ) {
                System.out.println("ERROR - Inductive Miner output model broken @ " + threshold);
            }

            threshold += STEP;
        } while ( threshold <= MAX);

        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        return petrinet;
    }

    private String computeGeneralization(UIPluginContext context, Map<XLog, XLog> crossValidationLogs, XEventClassifier xEventClassifier, MiningParameters miningParameters) {
        PetrinetWithMarking petrinetWithMarking;
        int k = crossValidationLogs.size();
        IMPetriNet miner = new IMPetriNet();
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

                Object[] result = miner.minePetriNetParameters(context, miningLog, miningParameters);
                petrinetWithMarking = new PetrinetWithMarking((Petrinet) result[0], (Marking) result[1], (Marking) result[2]);

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
        InductiveMinerIMfWrapper inductiveminer = new InductiveMinerIMfWrapper();
        return inductiveminer.mineBPMNDiagram(context, log, structure, params, xEventClassifier);
    }

    @Override
    public String getAlgorithmName() {
        return "Naive HyperParam-Optimized Inductive Miner Infrequent";
    }

    @Override
    public String getAcronym() {
        return "HPO-IMf";
    }

}