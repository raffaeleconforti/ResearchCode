package com.raffaeleconforti.wrappers;

import com.raffaeleconforti.conversion.bpmn.BPMNToPetriNetConverter;
import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.marking.MarkingDiscoverer;
import com.raffaeleconforti.measurements.impl.AlignmentBasedFitness;
import com.raffaeleconforti.measurements.impl.AlignmentBasedPrecision;
import com.raffaeleconforti.wrapper.LogPreprocessing;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.settings.MiningSettings;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.bpmnminer.causalnet.CausalNet;
import org.processmining.plugins.bpmnminer.converter.CausalNetToPetrinet;
import org.processmining.plugins.bpmnminer.plugins.FodinaMinerPlugin;
import org.processmining.plugins.bpmnminer.types.MinerSettings;
import org.processmining.plugins.bpmnminer.ui.FullParameterPanel;
import org.processmining.processtree.ProcessTree;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Adriano on 5/18/2017.
 */
public class HyperParamOptimizedFodina implements MiningAlgorithm {

    private static double d_STEP = 0.10D;
    private static double d_MIN = 0.00D;
    private static double d_MAX = 1.01D;

    private static double p_STEP = 10.0D;
    private static double p_MIN = -50.0D;
    private static double p_MAX = 50.01D;

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
        return discoverBestOn(context, log, structure, xEventClassifier);
    }

    public PetrinetWithMarking discoverBestOn(UIPluginContext context, XLog log, boolean structure, XEventClassifier xEventClassifier) {
        Map<String, PetrinetWithMarking> models = new HashMap<>();
        Map<Double, String> fitness = new HashMap<>();
        Map<Double, String> precision = new HashMap<>();
        Map<Double, String> fscore = new HashMap<>();


        MinerSettings minerSettings = new MinerSettings();
        PetrinetWithMarking petrinet;

        AlignmentBasedFitness fitnessCalculator = new AlignmentBasedFitness();
        AlignmentBasedPrecision precisionCalculator = new AlignmentBasedPrecision();
        XEventClassifier eventNameClassifier = xEventClassifier;

        Double fit;
        Double prec;
        Double score;

        Double bestValue;
        String bestCombination;

        String combination = null;

        LogPreprocessing logPreprocessing = new LogPreprocessing();
        XLog plog = logPreprocessing.preprocessLog(context, log);

        double d_threshold;
        double p_threshold;
        boolean longDistance = false;

        minerSettings = new MinerSettings();
        minerSettings.classifier = xEventClassifier;
        double nom = (double) log.size() / ((double) log.size() + (double) minerSettings.dependencyDivisor);
        if (nom <= 0.0D) {
            nom = 0.0D;
        }

        if (nom >= 0.9D) {
            nom = 0.9D;
        }

        minerSettings.dependencyThreshold = nom;
        minerSettings.l1lThreshold = nom;
        minerSettings.l2lThreshold = nom;
        FullParameterPanel parameters = new FullParameterPanel(minerSettings);
        context.showConfiguration("Miner Parameters", parameters);
        minerSettings = parameters.getSettings();

        do {
            minerSettings.useLongDistanceDependency = longDistance;
//            p_threshold = p_MIN;
//            do {
//                minerSettings.patternThreshold = p_threshold;
                d_threshold = d_MIN;
                do {
                    combination = ":d:" + d_threshold + ":l:" + longDistance;
                    try {
                        minerSettings.dependencyThreshold = d_threshold;

                        System.setOut(new PrintStream(new OutputStream() {
                            @Override
                            public void write(int b) throws IOException {}
                        }));

                        Object[] bpmnResults = FodinaMinerPlugin.runMiner(context, plog, minerSettings);
                        CausalNet net = (CausalNet) bpmnResults[0];

                        Object[] result = CausalNetToPetrinet.convert(context, net);
                        logPreprocessing.removedAddedElements((Petrinet) result[0]);


                        boolean includeLifeCycle = true;
                        if(xEventClassifier instanceof XEventNameClassifier) includeLifeCycle = false;
                        if(!includeLifeCycle) logPreprocessing.removedLifecycleFromName((Petrinet) result[0]);

                        MarkingDiscoverer.createInitialMarkingConnection(context, (Petrinet) result[0], (Marking) result[1]);

                        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

                        petrinet = new PetrinetWithMarking((Petrinet) result[0], (Marking) result[1], MarkingDiscoverer.constructFinalMarking(context, (Petrinet) result[0]));

                        models.put(combination, petrinet);

                        fit = fitnessCalculator.computeMeasurement(context, eventNameClassifier, petrinet, this, log).getValue();
                        prec = precisionCalculator.computeMeasurement(context, eventNameClassifier, petrinet, this, log).getValue();

                        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

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
                        System.out.println("ERROR - Fodina output model broken @ " + combination);
                        fitness.put(0.0, combination);
                        precision.put(0.0, combination);
                        fscore.put(0.0, combination);
                    }

                    d_threshold += d_STEP;
                } while (d_threshold <= d_MAX);

//                p_threshold += p_STEP;
//            } while (p_threshold <= p_MAX);

            if(longDistance) break;
            else longDistance = true;
        } while (longDistance);

        bestValue = Collections.max(fscore.keySet());
        bestCombination = fscore.get(bestValue);

        System.out.println("BEST - @ " + bestCombination);
        return models.get(bestCombination);
    }

    public BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, boolean structure, MiningSettings params, XEventClassifier xEventClassifier) {
        BPMNDiagram output = null;
        PetrinetWithMarking petrinet = minePetrinet(context, log, structure, params, xEventClassifier);
        output = PetriNetToBPMNConverter.convert(petrinet.getPetrinet(), petrinet.getInitialMarking(), petrinet.getFinalMarking(), false);
        return output;
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

    @Override
    public String getAlgorithmName() {
        return "Naive HyperParam-Optimized Fodina";
    }

    @Override
    public String getAcronym() {
        return "HPO-FO";
    }

}
