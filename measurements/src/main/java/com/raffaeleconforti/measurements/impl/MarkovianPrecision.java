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

package com.raffaeleconforti.measurements.impl;

import au.edu.unimelb.processmining.accuracy.MarkovianAccuracyCalculator.Abs;
import au.edu.unimelb.processmining.accuracy.MarkovianAccuracyCalculator.Opd;
import au.edu.unimelb.processmining.accuracy.MarkovianAccuracyCalculator;
import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.MeasurementAlgorithm;
import com.raffaeleconforti.wrappers.MiningAlgorithm;
import com.raffaeleconforti.wrappers.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.processtree.ProcessTree;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by Adriano on 06/07/18.
 */
public class MarkovianPrecision implements MeasurementAlgorithm {


    @Override
    public boolean isMultimetrics() {
        return false;
    }

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, ProcessTree processTree, MiningAlgorithm miningAlgorithm, XLog log) {
        return null;
    }

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, PetrinetWithMarking petrinetWithMarking, MiningAlgorithm miningAlgorithm, XLog log) {
        Measure measure = new Measure();
        double m3prec;

        if (!Soundness.isSound(petrinetWithMarking)) return new Measure(getAcronym(), "-");

        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
            }
        }));
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

        long start = System.currentTimeMillis();
        MarkovianAccuracyCalculator mac = new MarkovianAccuracyCalculator();
        m3prec = mac.precision(Abs.STA, Opd.HUN, "", "", 3);
        long time = System.currentTimeMillis() - start;

        measure.addMeasure(this.getAcronym(), m3prec);
        return measure;
    }

    @Override
    public String getMeasurementName() {
        return "3rd-order Markovian-Based Precision";
    }

    @Override
    public String getAcronym() {
        return "(m3)precision";
    }
}
