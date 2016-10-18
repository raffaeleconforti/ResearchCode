package com.raffaeleconforti.measurements;

import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 18/10/2016.
 */
public interface MeasurementAlgorithm {

    double computeMeasurement(PluginContext pluginContext, XEventClassifier xEventClassifier, PetrinetWithMarking petrinetWithMarking, XLog log);
    String getMeasurementName();

}
