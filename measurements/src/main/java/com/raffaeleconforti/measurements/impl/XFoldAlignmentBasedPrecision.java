package com.raffaeleconforti.measurements.impl;

import com.raffaeleconforti.measurements.MeasurementAlgorithm;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;

import java.util.Random;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 18/10/2016.
 */
public class XFoldAlignmentBasedPrecision implements MeasurementAlgorithm {

    private int fold = 3;
    private XFactory factory = new XFactoryNaiveImpl();
    private XLog log;
    private Random r = new Random(123456789);

    @Override
    public double computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, PetrinetWithMarking petrinetWithMarking, MiningAlgorithm miningAlgorithm, XLog log) {
        double fitness = 0.0;
        this.log = log;
        XLog[] logs = createdXFolds();

        AlignmentBasedPrecision alignmentBasedPrecision = new AlignmentBasedPrecision();

        for(int i = 0; i < fold; i++) {
            XLog log1 = factory.createLog(log.getAttributes());
            for (int j = 0; j < fold; j++) {
                if (j != i) {
                    log1.addAll(logs[j]);
                }
            }

            petrinetWithMarking = miningAlgorithm.minePetrinet(pluginContext, logs[i], false);

            Double f = alignmentBasedPrecision.computeMeasurement(pluginContext, xEventClassifier, petrinetWithMarking, miningAlgorithm, log1);
            fitness += (f != null)?f:0.0;
        }
        return fitness / (double) fold;
    }

    @Override
    public String getMeasurementName() {
        return null;
    }

    private XLog[] createdXFolds() {

        if(log.size() < fold) fold = log.size();
        XLog[] logs = new XLog[fold];

        for(int i = 0; i < fold; i++) {
            logs[i] = factory.createLog(log.getAttributes());
        }

        if(log.size() == fold) {
            int pos = 0;
            for (XTrace t : log) {
                logs[pos].add(t);
                pos++;
            }
        }else {
            boolean finish = false;
            while (!finish) {
                finish = true;
                for (XTrace t : log) {
                    int pos = r.nextInt(fold);
                    logs[pos].add(t);
                }
                for (int i = 0; i < logs.length; i++) {
                    if (logs[i].size() == 0) {
                        finish = false;
                    }
                }
                if(!finish) {
                    for(int i = 0; i < fold; i++) {
                        logs[i].clear();
                    }
                }
            }
        }

        return logs;
    }
}
