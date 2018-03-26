package com.raffaeleconforti.measurements.impl;

import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.MeasurementAlgorithm;
import com.raffaeleconforti.wrappers.MiningAlgorithm;
import com.raffaeleconforti.wrappers.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.processtree.ProcessTree;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * Created by Adriano on 23/11/2016.
 */
public class XFoldAlignmentBasedFMeasure implements MeasurementAlgorithm {

    public static int K = 3;
    public static Random R = new Random(123456789);

    @Override
    public boolean isMultimetrics() { return true; }

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, ProcessTree processTree, MiningAlgorithm miningAlgorithm, XLog log) {
        return null;
    }

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, PetrinetWithMarking petrinetWithMarking, MiningAlgorithm miningAlgorithm, XLog log) {
        Measure measure = new Measure();
        double precision = 0.0;
        double fitness = 0.0;
        Double fscore = 0.0;
        Double p;
        Double f;
        Double fs;
        XLog evalLog;
        Map<XLog, XLog> crossValidationLogs = XFoldAlignmentBasedFMeasure.getCrossValidationLogs(log, K);
        int i = 0;

        AlignmentBasedFitness alignmentBasedFitness = new AlignmentBasedFitness();
        AlignmentBasedPrecision alignmentBasedPrecision = new AlignmentBasedPrecision();

        for( XLog miningLog : crossValidationLogs.keySet() ) {
            evalLog = crossValidationLogs.get(miningLog);
            i++;
            f = 0.0;
            p = 0.0;
            fs = 0.0;

            try {
                petrinetWithMarking = miningAlgorithm.minePetrinet(pluginContext, miningLog, false, null, xEventClassifier);
                if( Soundness.isSound(petrinetWithMarking) ) {
                    f = alignmentBasedFitness.computeMeasurement(pluginContext, xEventClassifier, petrinetWithMarking, miningAlgorithm, evalLog).getValue();
                    p = alignmentBasedPrecision.computeMeasurement(pluginContext, xEventClassifier, petrinetWithMarking, miningAlgorithm, evalLog).getValue();
                    fs = (2.0*f*p)/(f+p);
                }

                fitness += f;
                precision += p;
                fscore += fs;
            } catch( Exception e ) { System.out.println("ERROR - impossible to assess f-score for fold: " + i);  }

            System.out.println("DEBUG - " + i + "/" + K + " -fold f-score: " + fs);
        }

        fscore = fscore/(double)K;
        fitness = fitness/(double)K;
        precision = precision/(double)K;

        measure.addMeasure(getAcronym(), String.format("%.2f", fscore));
        measure.addMeasure((new XFoldAlignmentBasedFitness()).getAcronym(), String.format("%.2f", fitness));
        measure.addMeasure((new XFoldAlignmentBasedPrecision()).getAcronym(), String.format("%.2f", precision));

        return measure;
    }

/*
 *  This method return a map of logs to compute cross-validation for fitness, precision or f-score.
 *  The key is a log that can be used to discover a process model
 *  The value of the key is the log that should be used to assess the accuracy of the process model discovered.
 */
    public static Map<XLog, XLog> getCrossValidationLogs(XLog log, int k) {
        XFactory factory = new XFactoryNaiveImpl();
        XLog miningLog;
        Map<XLog, XLog> genLogs = new HashMap<>();

        if(log.size() < k) k = log.size();
        XLog[] logFolds = new XLog[k];

        for(int i = 0; i < k; i++)
            logFolds[i] = factory.createLog(log.getAttributes());

        if(log.size() == k) {
            int pos = 0;
            for (XTrace t : log) {
                logFolds[pos].add(t);
                pos++;
            }
        } else {
            boolean finish = false;
            while(!finish) {
                finish = true;

                for( XTrace t : log ) {
                    int pos = R.nextInt(k);
                    logFolds[pos].add(t);
                }

                for(int i = 0; i < logFolds.length; i++)
                    if(logFolds[i].size() == 0) finish = false;

                if(!finish)
                    for(int i = 0; i < k; i++) logFolds[i].clear();
            }
        }

        for(int i = 0; i < k; i++) {
            miningLog = factory.createLog(log.getAttributes());

            for(int j = 0; j < k; j++)
                if(j != i) miningLog.addAll(logFolds[j]);

            genLogs.put(miningLog, logFolds[i]);
        }

        return genLogs;
    }

    @Override
    public String getMeasurementName() {
        return K +"-Fold Alignment-Based f-Measure";
    }

    @Override
    public String getAcronym() { return "(a)("+ K +"-f)f-meas."; }

}
