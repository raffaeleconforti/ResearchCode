package com.raffaeleconforti.measurements.impl;

import au.edu.unimelb.processmining.accuracy.MarkovianAccuracyCalculator.Abs;
import au.edu.unimelb.processmining.accuracy.MarkovianAccuracyCalculator.Opd;
import au.edu.unimelb.processmining.accuracy.MarkovianAccuracyCalculator;
import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.MeasurementAlgorithm;
import com.raffaeleconforti.wrappers.MiningAlgorithm;
import com.raffaeleconforti.wrappers.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.processtree.ProcessTree;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by Adriano on 06/07/18.
 */
public class MarkovianPrecision implements MeasurementAlgorithm {


    @Override
    public boolean isMultimetrics() {
        return false;
    }

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, ProcessTree processTree, MiningAlgorithm miningAlgorithm, XLog log) {
        return null;
    }

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, PetrinetWithMarking petrinetWithMarking, MiningAlgorithm miningAlgorithm, XLog log) {
        Measure measure = new Measure();
        double m3prec;

        if (!Soundness.isSound(petrinetWithMarking)) return new Measure(getAcronym(), "-");

        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
            }
        }));
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

        long start = System.currentTimeMillis();
        MarkovianAccuracyCalculator mac = new MarkovianAccuracyCalculator();
        m3prec = mac.accuracy(Abs.MARK, Opd.GRD, log, petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), 3)[1];
        long time = System.currentTimeMillis() - start;

        measure.addMeasure(this.getAcronym(), m3prec);
        return measure;
    }

    @Override
    public String getMeasurementName() {
        return "3rd-order Markovian-Based Precision";
    }

    @Override
    public String getAcronym() {
        return "(m3)precision";
    }
}
