package com.raffaeleconforti.wrappers;

import au.edu.qut.bpmn.structuring.StructuringService;
import com.raffaeleconforti.conversion.bpmn.BPMNToPetriNetConverter;
import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.marking.MarkingDiscoverer;
import com.raffaeleconforti.measurements.impl.AlignmentBasedFitness;
import com.raffaeleconforti.measurements.impl.AlignmentBasedPrecision;
import com.raffaeleconforti.wrapper.LogPreprocessing;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.MiningSettings;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
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

import java.io.*;
import java.util.*;

/**
 * Created by Adriano on 5/19/2017.
 */
public class HyperParamOptimizedStructuredHeuristicsMiner implements MiningAlgorithm {

    private static double STEP = 0.10D;
    private static double MIN = 0.00D;
    private static double MAX = 1.01D;

    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log) {
        return minePetrinet(context, log, false, null);
    }

    @Override
    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log, boolean structure, MiningSettings params) {
        return discoverBestOn(context, log, structure);
    }

    public PetrinetWithMarking discoverBestOn(UIPluginContext context, XLog log, boolean structure) {
        Map<String, PetrinetWithMarking> models = new HashMap<>();
        Map<Double, String> fitness = new HashMap<>();
        Map<Double, String> precision = new HashMap<>();
        Map<Double, String> fscore = new HashMap<>();


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
        String bestCombination;

        String combination = null;

        double d_threshold;
        double rtb_threshold;
        boolean longDistance = false;

        BPMNDiagram diagram, structuredDiagram;
        StructuringService ss = new StructuringService();

        Collection<XEventClassifier> classifiers = new HashSet();
        classifiers.add(new XEventNameClassifier());
        HeuristicsMinerSettings minerSettings;
        ParametersPanel parameters = new ParametersPanel(classifiers);
        minerSettings = parameters.getSettings();

        do {
            minerSettings.setUseLongDistanceDependency(longDistance);
            rtb_threshold = MIN;
            do {
                minerSettings.setRelativeToBestThreshold(rtb_threshold);
                d_threshold = MIN;
                do {
                    combination = ":p:" + rtb_threshold + ":d:" + d_threshold + ":l:" + longDistance;
                    try {
                        minerSettings.setDependencyThreshold(d_threshold);

                        System.setOut(new PrintStream(new OutputStream() {
                            @Override
                            public void write(int b) throws IOException {}
                        }));

                        HeuristicsNet heuristicsNet = FlexibleHeuristicsMinerPlugin.run(context, log, minerSettings);
                        Object[] result = HeuristicsNetToPetriNetConverter.converter(context, heuristicsNet);
//                        logPreprocessing.removedAddedElements((Petrinet) result[0]);

                        if(result[1] == null) result[1] = MarkingDiscoverer.constructInitialMarking(context, (Petrinet) result[0]);
                        else MarkingDiscoverer.createInitialMarkingConnection(context, (Petrinet) result[0], (Marking) result[1]);
                        Marking finalMarking = MarkingDiscoverer.constructFinalMarking(context, (Petrinet) result[0]);

                        diagram = PetriNetToBPMNConverter.convert((Petrinet) result[0], (Marking) result[1], finalMarking, false);
                        structuredDiagram = ss.structureDiagram(diagram, "ASTAR", 100, 500, 10, 100, 2, true, true, true);
                        result = BPMNToPetriNetConverter.convert(structuredDiagram);
                        petrinet = new PetrinetWithMarking((Petrinet) result[0], (Marking) result[1], (Marking) result[2]);

                        MarkingDiscoverer.createInitialMarkingConnection(context, (Petrinet) result[0], (Marking) result[1]);
                        MarkingDiscoverer.createFinalMarkingConnection(context, (Petrinet) result[0], (Marking) result[2]);

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
                        System.out.println("ERROR - S-Heuristics Miner output model broken @ " + combination);
                        fitness.put(0.0, combination);
                        precision.put(0.0, combination);
                        fscore.put(0.0, combination);
                    }

                    d_threshold += STEP;
                } while (d_threshold <= MAX);

                rtb_threshold += STEP;
            } while (rtb_threshold <= MAX);

//                if(longDistance) break;
//                else longDistance = true;
        } while (longDistance);

        bestValue = Collections.max(fscore.keySet());
        bestCombination = fscore.get(bestValue);

        System.out.println("BEST - @ " + bestCombination);
        return models.get(bestCombination);
    }

    public BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, boolean structure, MiningSettings params) {
        BPMNDiagram output = null;
        PetrinetWithMarking petrinet = minePetrinet(context, log);
        output = PetriNetToBPMNConverter.convert(petrinet.getPetrinet(), petrinet.getInitialMarking(), petrinet.getFinalMarking(), false);
        return output;
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
