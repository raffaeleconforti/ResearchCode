package com.raffaeleconforti.measurements.impl;

import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.MeasurementAlgorithm;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.projectedrecallandprecision.framework.CompareParameters;
import org.processmining.projectedrecallandprecision.plugins.CompareLog2PetriNetPlugin;
import org.processmining.projectedrecallandprecision.result.ProjectedRecallPrecisionResult;

import java.io.*;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 2/12/16.
 */
public class ProjectedFMeasure implements MeasurementAlgorithm {

    @Override
    public boolean isMultimetrics() { return false; }

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, PetrinetWithMarking petrinetWithMarking, MiningAlgorithm miningAlgorithm, XLog log) {
        Measure measure = new Measure();

        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {}
        }));

        if(petrinetWithMarking == null) return measure;

        try {
            CompareParameters compareParameters = new CompareParameters(2);
            ProjectedRecallPrecisionResult projectedRecallPrecisionResult = CompareLog2PetriNetPlugin.measure(log, new AcceptingPetriNetImpl(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking()), xEventClassifier, compareParameters);
            double projectedRecall = projectedRecallPrecisionResult.getRecall();
            double projectedPrecision = projectedRecallPrecisionResult.getPrecision();
            double projected_fmeasure = 2*(projectedRecall*projectedPrecision)/(projectedRecall+projectedPrecision);

            measure.addMeasure(getMeasurementName(), projected_fmeasure);
            measure.addMeasure("Projected Recall", projectedRecall);
            measure.addMeasure("Projected Precision", projectedPrecision);

            measure.setValue(projected_fmeasure);
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
            return measure;
        } catch( Exception e ) {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
            return measure;
        }
    }

    @Override
    public String getMeasurementName() {
        return "Projected f-Measure";
    }

    @Override
    public String getAcronym() {return "f-measure(p)";}
}
