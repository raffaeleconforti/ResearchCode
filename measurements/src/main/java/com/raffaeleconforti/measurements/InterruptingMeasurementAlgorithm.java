package com.raffaeleconforti.measurements;

import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 9/11/16.
 */
public class InterruptingMeasurementAlgorithm {

    private MeasurementAlgorithm measurementAlgorithm;
    private long timeout;

    public InterruptingMeasurementAlgorithm(MeasurementAlgorithm measurementAlgorithm, long timeout) {
        this.measurementAlgorithm = measurementAlgorithm;
        this.timeout = timeout;
    }

    public double computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, PetrinetWithMarking petrinetWithMarking, MiningAlgorithm miningAlgorithm, XLog log) {
        double[] result = new double[1];

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    result[0] = measurementAlgorithm.computeMeasurement(pluginContext, xEventClassifier, petrinetWithMarking, miningAlgorithm, log);
                } catch (Exception e) {

                }
            }
        };
        Thread t = new Thread(runnable);
        t.start();

        long time = 0;
        try {
            while(time < timeout && t.isAlive()) {
                Thread.currentThread().sleep(100);
                time += 100;
            }
            if (t.isAlive()) {
                t.interrupt();
                System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
                System.out.println(getMeasurementName() + " - Timeout Reached!");
            }
        } catch (InterruptedException e) {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
            System.out.println(getMeasurementName() + " - Timeout Reached!");
        }

        return result[0];
    }

    public String getMeasurementName() {
        return measurementAlgorithm.getMeasurementName();
    }
}
