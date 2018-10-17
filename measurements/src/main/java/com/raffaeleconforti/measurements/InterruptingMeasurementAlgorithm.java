/*
 *  Copyright (C) 2018 Raffaele Conforti (www.raffaeleconforti.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.raffaeleconforti.measurements;

import com.raffaeleconforti.wrappers.MiningAlgorithm;
import com.raffaeleconforti.wrappers.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStream;
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
                System.setOut(new PrintStream(new OutputStream() {
                    @Override
                    public void write(int b) {}
                }));

                try {
                    result[0] = measurementAlgorithm.computeMeasurement(pluginContext, xEventClassifier, petrinetWithMarking, miningAlgorithm, log).getValue();
                } catch (Exception e) {

                }

                System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
            }
        };
        Thread t = new Thread(runnable);
        t.start();

        long time = 0;
        boolean reached = false;
        try {
            while(time < timeout && t.isAlive()) {
                Thread.currentThread().sleep(100);
                if(time % 300000 == 0) {
                    System.out.println("DEBUG - sleeping: " + getMeasurementName());
                }
                time += 100;
            }
            if (t.isAlive()) {
                t.interrupt();
                reached = true;
            }
            Thread.currentThread().sleep(1000);
            if (t.isAlive()) {
                t.stop();
            }
        } catch (Exception e) {

        }
        if(reached) {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
            System.out.println(getMeasurementName() + " - Timeout Reached!");
        }

        return result[0];
    }

    public String getMeasurementName() {
        return measurementAlgorithm.getMeasurementName();
    }
}
