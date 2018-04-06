package com.raffaeleconforti.wrappers;

import au.edu.qut.processmining.miners.splitminer.SplitMiner;
import au.edu.qut.processmining.miners.splitminer.ui.dfgp.DFGPUIResult;
import au.edu.qut.processmining.miners.splitminer.ui.miner.SplitMinerUIResult;
import com.raffaeleconforti.conversion.bpmn.BPMNToPetriNetConverter;
import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.marking.MarkingDiscoverer;
import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.impl.*;
import com.raffaeleconforti.wrappers.impl.SplitMinerWrapper;
import com.raffaeleconforti.wrappers.settings.MiningSettings;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
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
        PetrinetWithMarking petrinet = null;
        Map<XLog, XLog> crossValidationLogs = XFoldAlignmentBasedFMeasure.getCrossValidationLogs(log, XFoldAlignmentBasedFMeasure.K);

        String lName = XConceptExtension.instance().extractName(log);
        String fName = ".\\splitminer_hyperparam_" + lName + "_" + System.currentTimeMillis() + ".csv";

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
        Double p_threshold;
        Double f_threshold;

        PrintWriter writer;
        try {
            writer = new PrintWriter(fName);
            writer.println("f_threshold,p_threshold,fitness,precision,fscore,gf1,gf2,gf3,gen,size,cfc,struct,soundness,mining-time");
        } catch(Exception e) {
            writer = new PrintWriter(System.out);
            System.out.println("ERROR - impossible to create the file for storing the results: printing only on terminal.");
        }

        f_threshold = f_MIN;
        do {
            p_threshold = p_MIN;
            do {
                try {

                    System.setOut(new PrintStream(new OutputStream() {
                        @Override
                        public void write(int b) throws IOException {}
                    }));

                    eTime = System.currentTimeMillis();
                    bpmn = yam.mineBPMNModel(log, xEventClassifier, f_threshold, p_threshold, DFGPUIResult.FilterType.WTH, true, true, false, SplitMinerUIResult.StructuringTime.NONE);
                    eTime = System.currentTimeMillis() - eTime;
                    petrinet = convertToPetrinet(context, bpmn);

                    if( sound = Soundness.isSound(petrinet) ) {
                        fit = fitnessCalculator.computeMeasurement(context, xEventClassifier, petrinet, this, log).getValue();
                        prec = precisionCalculator.computeMeasurement(context, xEventClassifier, petrinet, this, log).getValue();
                    } else {
                        fit = prec = -1.0;
                    }
                    gen = computeGeneralization(context, crossValidationLogs, xEventClassifier, f_threshold, p_threshold);
                    complexity = bpmnComplexity.computeMeasurementBPMN(bpmn);
                    size = Double.valueOf(complexity.getMetricValue("size"));
                    cfc = Double.valueOf(complexity.getMetricValue("cfc"));
                    struct = Double.valueOf(complexity.getMetricValue("struct."));

                    score = (fit * prec * 2) / (fit + prec);
                    if( score.isNaN() ) score = -1.0;

                    combination = f_threshold + "," + p_threshold + "," + fit + "," + prec + "," + score + "," + gen + "," + size + "," + cfc + "," + struct + "," + sound + "," + eTime;
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

        return petrinet;
    }

    private String computeGeneralization(UIPluginContext context, Map<XLog, XLog> crossValidationLogs, XEventClassifier xEventClassifier, double f_threshold, double p_threshold) {
        PetrinetWithMarking petrinetWithMarking;
        int k = crossValidationLogs.size();
        SplitMiner yam = new SplitMiner();
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

                BPMNDiagram bpmn = yam.mineBPMNModel(miningLog, xEventClassifier, f_threshold, p_threshold, DFGPUIResult.FilterType.WTH, true, true, false, SplitMinerUIResult.StructuringTime.NONE);
                petrinetWithMarking = convertToPetrinet(context, bpmn);

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
