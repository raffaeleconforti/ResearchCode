package com.raffaeleconforti.measurements.impl;

import au.edu.qut.metrics.ComplexityCalculator;
import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.measurements.MeasurementAlgorithm;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

/**
 * Created by Adriano on 19/10/2016.
 */
public class StructurednessComplexity implements MeasurementAlgorithm {

    @Override
    public double computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, PetrinetWithMarking petrinetWithMarking, MiningAlgorithm miningAlgorithm, XLog log) {
        if(petrinetWithMarking == null) return Double.NaN;

        try {
            BPMNDiagram bpmn = PetriNetToBPMNConverter.convert(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking(), false);
            ComplexityCalculator cc = new ComplexityCalculator(bpmn);
            String structuredness = cc.computeStructuredness();
            return Double.valueOf(structuredness);
        } catch (Exception e) { return Double.NaN; }
    }

    @Override
    public String getMeasurementName() {
        return "Structuredness";
    }
}
