package com.raffaeleconforti.measurements.impl;

import au.edu.qut.metrics.ComplexityCalculator;
import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.MeasurementAlgorithm;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

/**
 * Created by Adriano on 23/11/2016.
 */
public class Soundness implements MeasurementAlgorithm {
    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, PetrinetWithMarking petrinetWithMarking, MiningAlgorithm miningAlgorithm, XLog log) {
        Measure measure = new Measure();

        if(petrinetWithMarking == null) return measure;

        try {
            return measure;
        } catch( Exception e ) { return measure; }
    }

    @Override
    public String getMeasurementName() {
        return "Soudness";
    }

    @Override
    public boolean isMultimetrics() {
        return true;
    }
}
