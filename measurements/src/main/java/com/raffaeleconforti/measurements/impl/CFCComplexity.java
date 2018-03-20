package com.raffaeleconforti.measurements.impl;

import au.edu.qut.bpmn.metrics.ComplexityCalculator;
import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.MeasurementAlgorithm;
import com.raffaeleconforti.wrappers.MiningAlgorithm;
import com.raffaeleconforti.wrappers.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.processtree.ProcessTree;

/**
 * Created by Adriano on 19/10/2016.
 */
public class CFCComplexity  implements MeasurementAlgorithm {

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, ProcessTree processTree, MiningAlgorithm miningAlgorithm, XLog log) {
        return null;
    }

    @Override
    public boolean isMultimetrics() { return true; }

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, PetrinetWithMarking petrinetWithMarking, MiningAlgorithm miningAlgorithm, XLog log) {
        Measure measure = new Measure();

        if(petrinetWithMarking == null) return measure;

        try {
            BPMNDiagram bpmn = PetriNetToBPMNConverter.convert(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking(), false);
            ComplexityCalculator cc = new ComplexityCalculator(bpmn);
            measure.addMeasure(getAcronym(), cc.computeCFC());
            return measure;
        } catch( Exception e ) { return measure; }
    }

    @Override
    public String getMeasurementName() {
        return "Control Flow Complexity";
    }

    @Override
    public String getAcronym() {return "cfc";}
}
