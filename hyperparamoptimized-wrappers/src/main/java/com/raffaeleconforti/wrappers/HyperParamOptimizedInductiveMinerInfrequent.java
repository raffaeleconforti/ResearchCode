package com.raffaeleconforti.wrappers;


import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.impl.AlignmentBasedFitness;
import com.raffaeleconforti.measurements.impl.AlignmentBasedPrecision;
import com.raffaeleconforti.measurements.impl.BPMNComplexity;
import com.raffaeleconforti.wrappers.impl.inductive.InductiveMinerIMfWrapper;
import com.raffaeleconforti.wrappers.settings.MiningSettings;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMf;
import org.processmining.plugins.InductiveMiner.plugins.IMPetriNet;
import org.processmining.processtree.ProcessTree;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;


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
        PetrinetWithMarking petrinet;

        XEventClassifier eventNameClassifier = xEventClassifier;
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
        PrintWriter writer;

        try {
            writer = new PrintWriter(".\\inductiveminer_hyperparam_" + System.currentTimeMillis() + ".txt");
            writer.println("noise_threshold,fitness,precision,fscore,generalization,size,cfc,struct");
        } catch(Exception e) {
            writer = new PrintWriter(System.out);
            System.out.println("ERROR - impossible to create the file for storing the results: printing only on terminal.");
        }

        Float threshold = MIN;
        do {
            try {
                miningParameters.setNoiseThreshold(threshold);
                Object[] result = miner.minePetriNetParameters(context, log, miningParameters);

                System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
                petrinet = new PetrinetWithMarking((Petrinet) result[0], (Marking) result[1], (Marking) result[2]);

                fit = fitnessCalculator.computeMeasurement(context, eventNameClassifier, petrinet, this, log).getValue();
                prec = precisionCalculator.computeMeasurement(context, eventNameClassifier, petrinet, this, log).getValue();
                gen = computeGeneralization();
                complexity = bpmnComplexity.computeMeasurement(context, eventNameClassifier, petrinet, this, log);
                size = Double.valueOf(complexity.getMetricValue("size"));
                cfc = Double.valueOf(complexity.getMetricValue("cfc"));
                struct = Double.valueOf(complexity.getMetricValue("struct."));

                if( fit.isNaN() || prec.isNaN() ) fit = prec = score = 0.0;
                else score = (fit * prec * 2) / (fit + prec);
                if( score.isNaN() ) score = 0.0;

                combination = threshold + "," + fit + "," + prec + "," + score + "," + gen + "," + size + "," + cfc + "," + struct;
                writer.println(combination);
                writer.flush();

            } catch ( Exception e ) {
                System.out.println("ERROR - Inductive Miner output model broken @ " + threshold);
            }

            threshold += STEP;
        } while ( threshold <= MAX);

        return null;
    }

    private Double computeGeneralization() {
        return 0.0;
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