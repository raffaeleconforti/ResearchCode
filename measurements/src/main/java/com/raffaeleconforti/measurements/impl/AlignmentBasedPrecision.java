package com.raffaeleconforti.measurements.impl;

import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.MeasurementAlgorithm;
import com.raffaeleconforti.wrappers.MiningAlgorithm;
import com.raffaeleconforti.wrappers.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.plugins.multietc.plugins.MultiETCPlugin;
import org.processmining.plugins.multietc.res.MultiETCResult;
import org.processmining.plugins.multietc.sett.MultiETCSettings;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.processtree.ProcessTree;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 18/10/2016.
 */
public class AlignmentBasedPrecision implements MeasurementAlgorithm {


    @Override
    public boolean isMultimetrics() { return false; }

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, ProcessTree processTree, MiningAlgorithm miningAlgorithm, XLog log) {
        return null;
    }

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, PetrinetWithMarking petrinetWithMarking, MiningAlgorithm miningAlgorithm, XLog log) {
        Measure measure = new Measure();

        if( !Soundness.isSound(petrinetWithMarking) ) return new Measure(getAcronym(), "-");

        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {}
        }));

        long start = System.currentTimeMillis();

        MultiETCPlugin multiETCPlugin = new MultiETCPlugin();

        MultiETCSettings settings = new MultiETCSettings();
        settings.put(MultiETCSettings.ALGORITHM, MultiETCSettings.Algorithm.ALIGN_1);
        settings.put(MultiETCSettings.REPRESENTATION, MultiETCSettings.Representation.ORDERED);

        try {
            AlignmentBasedFitness alignmentBasedFitness = new AlignmentBasedFitness();
            PNRepResult pnRepResult = alignmentBasedFitness.computeAlignment(pluginContext, xEventClassifier, petrinetWithMarking, log);
            Object[] res = multiETCPlugin.checkMultiETCAlign1(pluginContext, log, petrinetWithMarking.getPetrinet(), settings, pnRepResult);
            MultiETCResult multiETCResult = (MultiETCResult) res[0];

            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
            measure.setValue((Double) (multiETCResult).getAttribute(MultiETCResult.PRECISION));
            return measure;

        } catch (ConnectionCannotBeObtained connectionCannotBeObtained) {
            connectionCannotBeObtained.printStackTrace();
        }

        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        long time = System.currentTimeMillis() - start;
        measure.addMeasure("time", time);
        return measure;
    }

    @Override
    public String getMeasurementName() {
        return "Alignment-Based ETC Precision";
    }

    @Override
    public String getAcronym() {return "(a)precision";}
}
