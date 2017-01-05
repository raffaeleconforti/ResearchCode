package com.raffaeleconforti.measurements.impl;

import com.raffaeleconforti.measurements.Measure;
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
    public boolean isMultimetrics() { return false; }

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, PetrinetWithMarking petrinetWithMarking, MiningAlgorithm miningAlgorithm, XLog log) {
        Measure measure = new Measure();
        double precision = 0.0;
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

            try {
                petrinetWithMarking = miningAlgorithm.minePetrinet(pluginContext, log1, false);
                Double p = alignmentBasedPrecision.computeMeasurement(pluginContext, xEventClassifier, petrinetWithMarking, miningAlgorithm, log).getValue();
                precision += (p != null)?p:0.0;
                System.out.println("DEBUG - " + (i+1) + "/" + fold + " -fold precision: " + p);
            } catch( Exception e ) { return measure; }
        }

        measure.setValue(precision / (double) fold);
        return measure;
    }

    @Override
    public String getMeasurementName() {
        return fold+"-Fold Alignment-Based ETC Precision";
    }

    @Override
    public String getAcronym() { return "(a)("+fold+"-f)prec."; }

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
