package com.raffaeleconforti.measurements.impl;

import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.MeasurementAlgorithm;
import com.raffaeleconforti.wrappers.MiningAlgorithm;
import com.raffaeleconforti.wrappers.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.processtree.ProcessTree;

import java.util.Map;
import java.util.Random;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 18/10/2016.
 */
public class XFoldAlignmentBasedPrecision implements MeasurementAlgorithm {

    private int k;
    private Random r;

    public XFoldAlignmentBasedPrecision() {
        k = XFoldAlignmentBasedFMeasure.K;
        r = XFoldAlignmentBasedFMeasure.R;
    }

    @Override
    public boolean isMultimetrics() { return false; }

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, ProcessTree processTree, MiningAlgorithm miningAlgorithm, XLog log) {
        return null;
    }

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, PetrinetWithMarking petrinetWithMarking, MiningAlgorithm miningAlgorithm, XLog log) {
        Measure measure = new Measure();
        double precision = 0.0;
        Double p;
        XLog evalLog;
        Map<XLog, XLog> crossValidationLogs = XFoldAlignmentBasedFMeasure.getCrossValidationLogs(log, k);
        int i = 0;

        AlignmentBasedPrecision alignmentBasedPrecision = new AlignmentBasedPrecision();

        for( XLog miningLog : crossValidationLogs.keySet() ) {
            evalLog = crossValidationLogs.get(miningLog);
            i++;
            p = 0.0;

            try {
                petrinetWithMarking = miningAlgorithm.minePetrinet(pluginContext, miningLog, false, null, xEventClassifier);
                if( Soundness.isSound(petrinetWithMarking) )
                    p = alignmentBasedPrecision.computeMeasurement(pluginContext, xEventClassifier, petrinetWithMarking, miningAlgorithm, evalLog).getValue();
                precision += p;
            } catch( Exception e ) { System.out.println("ERROR - impossible to assess precision for fold: " + i);  }

            System.out.println("DEBUG - " + i + "/" + k + " -fold precision: " + p);
        }

        measure.setValue(precision / (double) k);
        return measure;
    }

    @Override
    public String getMeasurementName() {
        return k+"-Fold Alignment-Based ETC Precision";
    }

    @Override
    public String getAcronym() { return "(a)("+k+"-f)prec."; }
}
