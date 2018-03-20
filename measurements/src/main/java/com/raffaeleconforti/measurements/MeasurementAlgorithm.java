package com.raffaeleconforti.measurements;

import com.raffaeleconforti.wrappers.MiningAlgorithm;
import com.raffaeleconforti.wrappers.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.processtree.ProcessTree;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 18/10/2016.
 */
public interface MeasurementAlgorithm {

    Measure computeMeasurement(UIPluginContext pluginContext,
                               XEventClassifier xEventClassifier,
                               ProcessTree processTree,
                               MiningAlgorithm miningAlgorithm, XLog log);

    Measure computeMeasurement(UIPluginContext pluginContext,
                              XEventClassifier xEventClassifier,
                              PetrinetWithMarking petrinetWithMarking,
                              MiningAlgorithm miningAlgorithm, XLog log);

    String getMeasurementName();

    String getAcronym();

    boolean isMultimetrics();

}
