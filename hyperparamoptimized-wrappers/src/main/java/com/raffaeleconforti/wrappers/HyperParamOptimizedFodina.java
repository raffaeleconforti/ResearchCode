package com.raffaeleconforti.wrappers;

import com.raffaeleconforti.marking.MarkingDiscoverer;
import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.impl.AlignmentBasedFitness;
import com.raffaeleconforti.measurements.impl.AlignmentBasedPrecision;
import com.raffaeleconforti.measurements.impl.BPMNComplexity;
import com.raffaeleconforti.wrappers.impl.FodinaAlgorithmWrapper;
import com.raffaeleconforti.wrappers.settings.MiningSettings;
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

/**
 * Created by Adriano on 5/18/2017.
 */
public class HyperParamOptimizedFodina implements MiningAlgorithm {

    private static double d_STEP = 0.10D;
    private static double d_MIN = 0.00D;
    private static double d_MAX = 1.01D;

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
        PetrinetWithMarking petrinet;

        AlignmentBasedFitness fitnessCalculator = new AlignmentBasedFitness();
        AlignmentBasedPrecision precisionCalculator = new AlignmentBasedPrecision();
        BPMNComplexity bpmnComplexity = new BPMNComplexity();

        XEventClassifier eventNameClassifier = xEventClassifier;

        Double fit;
        Double prec;
        Double score;
        Double gen;
        Measure complexity;
        Double size;
        Double cfc;
        Double struct;

        String combination;

        LogPreprocessing logPreprocessing = new LogPreprocessing();
        XLog plog = logPreprocessing.preprocessLog(context, log);

        double d_threshold;
        boolean longDistance = false;

        PrintWriter writer;

        try {
            writer = new PrintWriter(".\\fodina_hyperparam_" + System.currentTimeMillis() + ".txt");
            writer.println("longdistance,d_threshold,fitness,precision,fscore,generalization,size,cfc,struct");
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

        do {
//            first parameter to optimize: long distance dependency > "longDistance"
            minerSettings.useLongDistanceDependency = longDistance;
//            second parameter to optimize: dependency threshold > "d_threshold"
            d_threshold = d_MIN;
            do {

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

                    fit = fitnessCalculator.computeMeasurement(context, eventNameClassifier, petrinet, this, log).getValue();
                    prec = precisionCalculator.computeMeasurement(context, eventNameClassifier, petrinet, this, log).getValue();
                    gen = computeGeneralization();
                    complexity = bpmnComplexity.computeMeasurement(context, eventNameClassifier, petrinet, this, log);
                    size = Double.valueOf(complexity.getMetricValue("size"));
                    cfc = Double.valueOf(complexity.getMetricValue("cfc"));
                    struct = Double.valueOf(complexity.getMetricValue("struct."));

                    System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

                    if( fit.isNaN() || prec.isNaN() ) fit = prec = score = 0.0;
                    else score = (fit * prec * 2) / (fit + prec);
                    if( score.isNaN() ) score = 0.0;

                    combination = longDistance + "," + d_threshold + "," + fit + "," + prec + "," + score + "," + gen + "," + size + "," + cfc + "," + struct;
                    writer.println(combination);
                    writer.flush();

                } catch (Exception e) {
                    System.out.println("ERROR - Fodina output model broken @ " + longDistance + " : " + d_threshold);
                }

                d_threshold += d_STEP;
            } while (d_threshold <= d_MAX);

            if(longDistance) break;
            else longDistance = true;
        } while (longDistance);

        return null;
    }

    private Double computeGeneralization() {
        return 0.0;
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
