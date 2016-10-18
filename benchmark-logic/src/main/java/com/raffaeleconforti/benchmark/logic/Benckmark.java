package com.raffaeleconforti.benchmark.logic;

import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.measurements.MeasurementAlgorithm;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.List;
import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 18/10/2016.
 */
public class Benckmark {

    public void performBenchmark(List<XLog> logs) {
        Set<String> packages = new UnifiedSet<>();
        performBenchmark(packages, logs);
    }

    public void performBenchmark(Set<String> packages, List<XLog> logs) {
        XEventClassifier xEventClassifier = new XEventAndClassifier(new XEventNameClassifier());
        FakePluginContext fakePluginContext = new FakePluginContext();
        List<MiningAlgorithm> miningAlgorithms = MiningAlgorithmDiscoverer.discoverAlgorithms(packages);
        List<MeasurementAlgorithm> measurementAlgorithms = MeasurementAlgorithmDiscoverer.discoverAlgorithms(packages);
        for(XLog log : logs) {
            for(MiningAlgorithm miningAlgorithm : miningAlgorithms) {
                PetrinetWithMarking petrinetWithMarking = miningAlgorithm.minePetrinet(fakePluginContext, log, false);
                for(MeasurementAlgorithm measurementAlgorithm : measurementAlgorithms) {
                    double measurement = measurementAlgorithm.computeMeasurement(fakePluginContext, xEventClassifier, petrinetWithMarking, log);
                }
            }
        }
    }
}
