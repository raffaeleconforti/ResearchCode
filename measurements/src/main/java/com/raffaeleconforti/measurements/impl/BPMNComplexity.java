package com.raffaeleconforti.measurements.impl;

import au.edu.qut.bpmn.metrics.ComplexityCalculator;
import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.MeasurementAlgorithm;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

/**
 * Created by Adriano on 13/12/2016.
 */
public class BPMNComplexity implements MeasurementAlgorithm {

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, PetrinetWithMarking petrinetWithMarking, MiningAlgorithm miningAlgorithm, XLog log) {
        Measure measure = new Measure();

        if(petrinetWithMarking == null) return measure;

        try {
            BPMNDiagram bpmn = PetriNetToBPMNConverter.convert(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking(), false);
            ComplexityCalculator cc = new ComplexityCalculator(bpmn);
            measure.addMeasure("size", cc.computeSize());
            measure.addMeasure("cfc", cc.computeCFC());
            measure.addMeasure("struct.", cc.computeStructuredness());
            return measure;
        } catch( Exception e ) { return measure; }

    }

    @Override
    public String getMeasurementName() {
        return "Complexity on BPMN Model";
    }

    @Override
    public String getAcronym() {
        return "size, cfc, struct.";
    }

    @Override
    public boolean isMultimetrics() {
        return true;
    }
}
