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
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.plugins.multietc.plugins.MultiETCPlugin;
import org.processmining.plugins.multietc.res.MultiETCResult;
import org.processmining.plugins.multietc.sett.MultiETCSettings;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import org.processmining.processtree.ProcessTree;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 23/11/16.
 */
public class AlignmentBasedFMeasure implements MeasurementAlgorithm {


    @Override
    public boolean isMultimetrics() { return true; }

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, ProcessTree processTree, MiningAlgorithm miningAlgorithm, XLog log) {
        return null;
    }

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, PetrinetWithMarking petrinetWithMarking, MiningAlgorithm miningAlgorithm, XLog log) {
        Measure measure = new Measure();

        if( !Soundness.isSound(petrinetWithMarking) ) return new Measure(getAcronym(), "-");

        AlignmentBasedFitness alignmentBasedFitness = new AlignmentBasedFitness();
        AlignmentBasedPrecision alignmentBasedPrecision = new AlignmentBasedPrecision();

        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {}
        }));

        MultiETCPlugin multiETCPlugin = new MultiETCPlugin();

        MultiETCSettings settings = new MultiETCSettings();
        settings.put(MultiETCSettings.ALGORITHM, MultiETCSettings.Algorithm.ALIGN_1);
        settings.put(MultiETCSettings.REPRESENTATION, MultiETCSettings.Representation.ORDERED);

        try {
            PNRepResult pnRepResult = alignmentBasedFitness.computeAlignment(pluginContext, xEventClassifier, petrinetWithMarking, log);
            Object[] res = multiETCPlugin.checkMultiETCAlign1(pluginContext, log, petrinetWithMarking.getPetrinet(), settings, pnRepResult);
            MultiETCResult multiETCResult = (MultiETCResult) res[0];

            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

            double fitness = getAlignmentValue(pnRepResult);
            double precision = (Double) (multiETCResult).getAttribute(MultiETCResult.PRECISION);
            double f_measure = 2*(fitness*precision)/(fitness+precision);

            measure.addMeasure(getAcronym(), f_measure);
            measure.addMeasure(alignmentBasedFitness.getAcronym(), fitness);
            measure.addMeasure(alignmentBasedPrecision.getAcronym(), precision);

            return measure;

        } catch (ConnectionCannotBeObtained connectionCannotBeObtained) {
            connectionCannotBeObtained.printStackTrace();
        }

        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        return measure;
    }

    private double getAlignmentValue(PNRepResult pnRepResult) {
        int unreliable = 0;
        if(pnRepResult == null) return Double.NaN;
        for(SyncReplayResult srp : pnRepResult) {
            if(!srp.isReliable()) {
                unreliable += srp.getTraceIndex().size();
            }
        }
        if(unreliable > pnRepResult.size() / 2) {
            return Double.NaN;
        }else {
            return (Double) pnRepResult.getInfo().get(PNRepResult.TRACEFITNESS);
        }
    }

    @Override
    public String getMeasurementName() {
        return "Alignment-Based f-Measure";
    }

    @Override
    public String getAcronym() {return "(a)f-measure";}
}
