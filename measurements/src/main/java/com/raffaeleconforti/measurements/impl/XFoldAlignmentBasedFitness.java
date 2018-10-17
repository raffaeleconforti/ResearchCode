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

import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.MeasurementAlgorithm;
import com.raffaeleconforti.wrappers.MiningAlgorithm;
import com.raffaeleconforti.wrappers.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.processtree.ProcessTree;

import java.util.Map;
import java.util.Random;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 18/10/2016.
 */
public class XFoldAlignmentBasedFitness implements MeasurementAlgorithm {

    private int k;
    private Random r;

    public XFoldAlignmentBasedFitness() {
        k = XFoldAlignmentBasedFMeasure.K;
        r = XFoldAlignmentBasedFMeasure.R;
    }

    @Override
    public boolean isMultimetrics() { return false; }

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, ProcessTree processTree, MiningAlgorithm miningAlgorithm, XLog log) {
        return null;
    }

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, PetrinetWithMarking petrinetWithMarking, MiningAlgorithm miningAlgorithm, XLog log) {
        Measure measure = new Measure();
        double fitness = 0.0;
        Double f;
        XLog evalLog;
        Map<XLog, XLog> crossValidationLogs = XFoldAlignmentBasedFMeasure.getCrossValidationLogs(log, k);
        int i = 0;

        AlignmentBasedFitness alignmentBasedFitness = new AlignmentBasedFitness();

        for( XLog miningLog : crossValidationLogs.keySet() ) {
            evalLog = crossValidationLogs.get(miningLog);
            i++;
            f = 0.0;

            try {
                petrinetWithMarking = miningAlgorithm.minePetrinet(pluginContext, miningLog, false, null, xEventClassifier);
                if( Soundness.isSound(petrinetWithMarking) )
                    f = alignmentBasedFitness.computeMeasurement(pluginContext, xEventClassifier, petrinetWithMarking, miningAlgorithm, evalLog).getValue();
                fitness += f;
            } catch( Exception e ) { System.out.println("ERROR - impossible to assess fitness for fold: " + i); }

            System.out.println("DEBUG - " + (i) + "/" + k + " -fold fitness: " + f);
        }

        measure.setValue(fitness / (double) k);
        return measure;
    }

    @Override
    public String getMeasurementName() {
        return k+"-Fold Alignment-Based Fitness";
    }

    @Override
    public String getAcronym() { return "(a)("+k+"-f)fit."; }
}
