package com.raffaeleconforti.measurements;

import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 18/10/2016.
 */
public interface MeasurementAlgorithm {

    double computeMeasurement(UIPluginContext pluginContext,
                              XEventClassifier xEventClassifier,
                              PetrinetWithMarking petrinetWithMarking,
                              MiningAlgorithm miningAlgorithm, XLog log);

    String getMeasurementName();

}
