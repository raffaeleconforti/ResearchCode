package com.raffaeleconforti.measurements.impl;

import com.raffaeleconforti.measurements.MeasurementAlgorithm;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.plugins.multietc.plugins.MultiETCPlugin;
import org.processmining.plugins.multietc.res.MultiETCResult;
import org.processmining.plugins.multietc.sett.MultiETCSettings;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 18/10/2016.
 */
public class AlignmentBasedPrecision implements MeasurementAlgorithm {


    @Override
    public double computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, PetrinetWithMarking petrinetWithMarking, MiningAlgorithm miningAlgorithm, XLog log) {
        if(petrinetWithMarking == null) return Double.NaN;

        MultiETCPlugin multiETCPlugin = new MultiETCPlugin();

        MultiETCSettings settings = new MultiETCSettings();
        settings.put(MultiETCSettings.ALGORITHM, MultiETCSettings.Algorithm.ALIGN_1);
        settings.put(MultiETCSettings.REPRESENTATION, MultiETCSettings.Representation.ORDERED);

        try {
            AlignmentBasedFitness alignmentBasedFitness = new AlignmentBasedFitness();
            PNRepResult pnRepResult = alignmentBasedFitness.computeAlignment(pluginContext, xEventClassifier, petrinetWithMarking, log);
            Object[] res = multiETCPlugin.checkMultiETCAlign1(pluginContext, log, petrinetWithMarking.getPetrinet(), settings, pnRepResult);
            MultiETCResult multiETCResult = (MultiETCResult) res[0];
            return (Double) (multiETCResult).getAttribute(MultiETCResult.PRECISION);

        } catch (ConnectionCannotBeObtained connectionCannotBeObtained) {
            connectionCannotBeObtained.printStackTrace();
        }
        return Double.NaN;
    }

    @Override
    public String getMeasurementName() {
        return "Alignment-Based ETC Precision";
    }
}
