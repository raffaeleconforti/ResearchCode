package com.raffaeleconforti.measurements.impl;

import au.edu.qut.petrinet.tools.PetrinetConverter;
import au.edu.qut.petrinet.tools.SoundnessChecker;
import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.MeasurementAlgorithm;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;


/**
 * Created by Adriano on 23/11/2016.
 */
public class Soundness implements MeasurementAlgorithm {
    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, PetrinetWithMarking petrinetWithMarking, MiningAlgorithm miningAlgorithm, XLog log) {
        Measure measure = new Measure();

        if(petrinetWithMarking == null) return measure;

        try {
            Petrinet petrinet = petrinetWithMarking.getPetrinet();
            SoundnessChecker checker = new SoundnessChecker(petrinet);

            if( checker.isSound() ) measure.addMeasure("Sound", "yes");
            else measure.addMeasure("Sound", "no");

            if( checker.isDead() ) measure.addMeasure("deadlock", "yes");
            if( !checker.isSafe() ) measure.addMeasure("safe", "no");

            return measure;
        } catch( Exception e ) { return measure; }
    }

    @Override
    public String getMeasurementName() {
        return "Soundness";
    }

    @Override
    public String getAcronym() {return "sound";}

    @Override
    public boolean isMultimetrics() {
        return true;
    }
}
