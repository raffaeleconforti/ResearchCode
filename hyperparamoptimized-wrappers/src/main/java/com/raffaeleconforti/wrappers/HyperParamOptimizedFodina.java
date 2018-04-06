package com.raffaeleconforti.wrappers;

import com.raffaeleconforti.marking.MarkingDiscoverer;
import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.impl.*;
import com.raffaeleconforti.wrappers.impl.FodinaAlgorithmWrapper;
import com.raffaeleconforti.wrappers.settings.MiningSettings;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
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
import java.util.Map;

/**
 * Created by Adriano on 5/18/2017.
 */
public class HyperParamOptimizedFodina implements MiningAlgorithm {

    private static double d_STEP = 0.100D;
    private static double d_MIN = 0.000D;
    private static double d_MAX = 1.000D;

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
        MinerSettings minerSettings;
        PetrinetWithMarking petrinet = null;
        Map<XLog, XLog> crossValidationLogs;

        AlignmentBasedFitness fitnessCalculator = new AlignmentBasedFitness();
        AlignmentBasedPrecision precisionCalculator = new AlignmentBasedPrecision();
        BPMNComplexity bpmnComplexity = new BPMNComplexity();

        boolean includeLifeCycle = true;
        if(xEventClassifier instanceof XEventNameClassifier) includeLifeCycle = false;

        String lName = XConceptExtension.instance().extractName(log);
        String fName = ".\\fodina_hyperparam_" + lName + "_" + System.currentTimeMillis() + ".csv";

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

        LogPreprocessing logPreprocessing = new LogPreprocessing();
        XLog plog = logPreprocessing.preprocessLog(context, log);

        double d_threshold;
        boolean longDistance = false;

        PrintWriter writer;

        try {
            writer = new PrintWriter(fName);
            writer.println("long_distance,d_threshold,fitness,precision,fscore,gf1,gf2,gf3,gen,size,cfc,struct,soundness,mining-time");
        } catch(Exception e) {
            writer = new PrintWriter(System.out);
            System.out.println("ERROR - impossible to create the file for storing the results: printing only on terminal.");
        }

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
        crossValidationLogs = XFoldAlignmentBasedFMeasure.getCrossValidationLogs(log, XFoldAlignmentBasedFMeasure.K);

        do {
//            first parameter to optimize: long distance dependency > "longDistance"
            minerSettings.useLongDistanceDependency = longDistance;
//            second parameter to optimize: dependency threshold > "d_threshold"
            d_threshold = d_MAX;
            do {

                try {
                    minerSettings.dependencyThreshold = d_threshold;

                    System.setOut(new PrintStream(new OutputStream() {
                        @Override
                        public void write(int b) {}
                    }));

                    eTime = System.currentTimeMillis();
                    Object[] bpmnResults = FodinaMinerPlugin.runMiner(context, plog, minerSettings);
                    eTime = System.currentTimeMillis() - eTime;
                    CausalNet net = (CausalNet) bpmnResults[0];

                    Object[] result = CausalNetToPetrinet.convert(context, net);
                    logPreprocessing.removedAddedElements((Petrinet) result[0]);

                    if(!includeLifeCycle) logPreprocessing.removedLifecycleFromName((Petrinet) result[0]);
                    MarkingDiscoverer.createInitialMarkingConnection(context, (Petrinet) result[0], (Marking) result[1]);

                    petrinet = new PetrinetWithMarking((Petrinet) result[0], (Marking) result[1], MarkingDiscoverer.constructFinalMarking(context, (Petrinet) result[0]));

                    if( sound = Soundness.isSound(petrinet) ) {
                        fit = fitnessCalculator.computeMeasurement(context, xEventClassifier, petrinet, this, log).getValue();
                        prec = precisionCalculator.computeMeasurement(context, xEventClassifier, petrinet, this, log).getValue();
                    } else {
                        fit = prec = -1.0;
                    }
                    gen = computeGeneralization(context, crossValidationLogs, logPreprocessing, xEventClassifier, minerSettings, includeLifeCycle);
                    complexity = bpmnComplexity.computeMeasurement(context, xEventClassifier, petrinet, this, log);
                    size = Double.valueOf(complexity.getMetricValue("size"));
                    cfc = Double.valueOf(complexity.getMetricValue("cfc"));
                    struct = Double.valueOf(complexity.getMetricValue("struct."));

                    score = (fit * prec * 2) / (fit + prec);
                    if( score.isNaN() ) score = -1.0;

                    combination = longDistance + "," + d_threshold + "," + fit + "," + prec + "," + score + "," + gen + "," + size + "," + cfc + "," + struct + "," + sound + "," + eTime;
                    writer.println(combination);
                    writer.flush();

                } catch (Exception e) {
                    System.out.println("ERROR - Fodina output model broken @ " + longDistance + " : " + d_threshold);
                }

                d_threshold -= d_STEP;
            } while (d_threshold >= d_MIN);

            if(longDistance) break;
            else longDistance = true;
        } while (longDistance);

        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

        return petrinet;
    }

    private String computeGeneralization(UIPluginContext context, Map<XLog, XLog> crossValidationLogs, LogPreprocessing logPreprocessing, XEventClassifier xEventClassifier, MinerSettings minerSettings, boolean includeLifeCycle) {
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

        for( XLog miningLog : crossValidationLogs.keySet() ) {
            evalLog = crossValidationLogs.get(miningLog);
            f = 0.0;
            p = 0.0;
            fs = 0.0;

            try {

                System.setOut(new PrintStream(new OutputStream() {
                    @Override
                    public void write(int b) {}
                }));

                Object[] bpmnResults = FodinaMinerPlugin.runMiner(context, miningLog, minerSettings);
                CausalNet net = (CausalNet) bpmnResults[0];

                Object[] result = CausalNetToPetrinet.convert(context, net);
                logPreprocessing.removedAddedElements((Petrinet) result[0]);

                if(!includeLifeCycle) logPreprocessing.removedLifecycleFromName((Petrinet) result[0]);
                MarkingDiscoverer.createInitialMarkingConnection(context, (Petrinet) result[0], (Marking) result[1]);

                petrinetWithMarking = new PetrinetWithMarking((Petrinet) result[0], (Marking) result[1], MarkingDiscoverer.constructFinalMarking(context, (Petrinet) result[0]));
                if( Soundness.isSound(petrinetWithMarking) ) {
                    f = alignmentBasedFitness.computeMeasurement(context, xEventClassifier, petrinetWithMarking, this, evalLog).getValue();
//                    p = alignmentBasedPrecision.computeMeasurement(context, xEventClassifier, petrinetWithMarking, this, evalLog).getValue();
//                    fs = (2.0*f*p)/(f+p);
                }

                fitness += f;
//                precision += p;
//                fscore += fs;
            } catch( Exception e ) { }

            comb += Double.toString(f) + ",";
        }

        comb += Double.toString(fitness / (double) k);

//        precision = precision/(double)k;
//        fscore = fscore/(double)k;

        return comb;
    }

    @Override
    public BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, boolean structure, MiningSettings params, XEventClassifier xEventClassifier) {
        FodinaAlgorithmWrapper fodina = new FodinaAlgorithmWrapper();
        return fodina.mineBPMNDiagram(context, log, structure, params, xEventClassifier);
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
