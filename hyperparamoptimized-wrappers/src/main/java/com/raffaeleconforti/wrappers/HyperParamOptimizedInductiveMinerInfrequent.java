package com.raffaeleconforti.wrappers;


import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.measurements.impl.AlignmentBasedFitness;
import com.raffaeleconforti.measurements.impl.AlignmentBasedPrecision;
import com.raffaeleconforti.wrapper.LogPreprocessing;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.settings.MiningSettings;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class HyperParamOptimizedInductiveMinerInfrequent implements MiningAlgorithm {

    private static float STEP = 0.050F;
    private static float MIN = 0.00F;
    private static float MAX = 1.010F;

    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log) {
        return minePetrinet(context, log, false, null);
    }

    @Override
    public boolean canMineProcessTree() {
        return false;
    }

    @Override
    public ProcessTree mineProcessTree(UIPluginContext context, XLog log, boolean structure, MiningSettings params) {
        return null;
    }

    @Override
    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log, boolean structure, MiningSettings params) {
        return discoverBestOn(context, log, structure);
    }

    public PetrinetWithMarking discoverBestOn(UIPluginContext context, XLog log, boolean structure) {
        Map<Float, PetrinetWithMarking> models = new HashMap<>();
        Map<Double, Float> fitness = new HashMap<>();
        Map<Double, Float> precision = new HashMap<>();
        Map<Double, Float> fscore = new HashMap<>();

        IMPetriNet miner = new IMPetriNet();
        MiningParameters miningParameters = new MiningParametersIMf();
        PetrinetWithMarking petrinet;

        AlignmentBasedFitness fitnessCalculator = new AlignmentBasedFitness();
        AlignmentBasedPrecision precisionCalculator = new AlignmentBasedPrecision();
        XEventNameClassifier eventNameClassifier = new XEventNameClassifier();

        LogPreprocessing logPreprocessing = new LogPreprocessing();
        log = logPreprocessing.preprocessLog(context, log);

        Double fit;
        Double prec;
        Double score;

        Double bestValue;
        Float bestThreshold;

        Float threshold = MIN;

        do {
            try {

                miningParameters.setNoiseThreshold(threshold);
                Object[] result = miner.minePetriNetParameters(context, log, miningParameters);
//                logPreprocessing.removedAddedElements((Petrinet) result[0]);

                System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
                petrinet = new PetrinetWithMarking((Petrinet) result[0], (Marking) result[1], (Marking) result[2]);
                models.put(threshold, petrinet);

                fit = fitnessCalculator.computeMeasurement(context, eventNameClassifier, petrinet, this, log).getValue();
                prec = precisionCalculator.computeMeasurement(context, eventNameClassifier, petrinet, this, log).getValue();

                if (fit.isNaN()) fit = 0.0;
                if (prec.isNaN()) prec = 0.0;
                score = (fit * prec * 2) / (fit + prec);
                if(score.isNaN()) score = 0.0;

                fitness.put(fit, threshold);
                precision.put(prec, threshold);
                fscore.put(score, threshold);

                System.out.println("RESULT - @ " + threshold);
                System.out.println(fit);
                System.out.println(prec);
                System.out.println(score);
            } catch ( Exception e ) {
                System.out.println("ERROR - Inductive Miner output model broken @ " + threshold);
                fitness.put(0.0, threshold);
                precision.put(0.0, threshold);
                fscore.put(0.0, threshold);
            }

            threshold += STEP;
        } while ( threshold <= MAX);

        bestValue = Collections.max(fscore.keySet());
        bestThreshold = fscore.get(bestValue);

        System.out.println("BEST - @ " + bestThreshold);
        return models.get(bestThreshold);
    }

    public BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, boolean structure, MiningSettings params) {
        BPMNDiagram output = null;
        PetrinetWithMarking petrinet = minePetrinet(context, log);
        output = PetriNetToBPMNConverter.convert(petrinet.getPetrinet(), petrinet.getInitialMarking(), petrinet.getFinalMarking(), false);
        return output;
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