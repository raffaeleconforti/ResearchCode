package com.raffaeleconforti.measurements.impl;

import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.MeasurementAlgorithm;
import com.raffaeleconforti.wrappers.MiningAlgorithm;
import com.raffaeleconforti.wrappers.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.processtree.ProcessTree;


/**
 * Created by Adriano on 23/11/2016.
 */
public class XFoldAlignmentBasedFMeasure implements MeasurementAlgorithm {

    private int fold = 3;

    @Override
    public boolean isMultimetrics() { return true; }

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, ProcessTree processTree, MiningAlgorithm miningAlgorithm, XLog log) {
        return null;
    }

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, PetrinetWithMarking petrinetWithMarking, MiningAlgorithm miningAlgorithm, XLog log) {
        Measure measure = new Measure();
        double precision;
        double fitness;
        double f_measure;

        XFoldAlignmentBasedFitness xFoldAlignmentBasedFitness = new XFoldAlignmentBasedFitness();
        XFoldAlignmentBasedPrecision xFoldAlignmentBasedPrecision = new XFoldAlignmentBasedPrecision();

        fitness = xFoldAlignmentBasedFitness.computeMeasurement(pluginContext, xEventClassifier, petrinetWithMarking, miningAlgorithm, log).getValue();
        precision = xFoldAlignmentBasedPrecision.computeMeasurement(pluginContext, xEventClassifier, petrinetWithMarking, miningAlgorithm, log).getValue();
        f_measure = 2*(fitness*precision)/(fitness+precision);

        measure.addMeasure(getAcronym(), String.format("%.2f", f_measure));
        measure.addMeasure(xFoldAlignmentBasedFitness.getAcronym(), String.format("%.2f", fitness));
        measure.addMeasure(xFoldAlignmentBasedPrecision.getAcronym(), String.format("%.2f", precision));

        return measure;
    }

    @Override
    public String getMeasurementName() {
        return fold+"-Fold Alignment-Based f-Measure";
    }

    @Override
    public String getAcronym() { return "(a)("+fold+"-f)f-meas."; }

}
