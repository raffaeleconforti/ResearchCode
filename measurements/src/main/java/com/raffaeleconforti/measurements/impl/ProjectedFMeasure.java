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
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduceParametersDuplicates;
import org.processmining.processtree.ProcessTree;
import org.processmining.projectedrecallandprecision.framework.CompareParameters;
import org.processmining.projectedrecallandprecision.plugins.CompareLog2PetriNetPlugin;
import org.processmining.projectedrecallandprecision.plugins.CompareLog2ProcessTreePlugin;
import org.processmining.projectedrecallandprecision.result.ProjectedRecallPrecisionResult;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 2/12/16.
 */
public class ProjectedFMeasure implements MeasurementAlgorithm {

    @Override
    public boolean isMultimetrics() { return false; }

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, ProcessTree processTree, MiningAlgorithm miningAlgorithm, XLog log) {
        Measure measure = new Measure();

        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {}
        }));

        if(processTree == null) return measure;

        try {
            CompareParameters parameters = new CompareParameters(2);
            parameters.setClassifier(xEventClassifier);
            parameters.setTreeReduceParameters(new EfficientTreeReduceParametersDuplicates(false));
            parameters.setDebug(false);
            ProMCanceller canceller = new ProMCanceller() {
                @Override
                public boolean isCancelled() {
                    return false;
                }
            };

            ProjectedRecallPrecisionResult projectedRecallPrecisionResult = CompareLog2ProcessTreePlugin.measure(log, processTree, parameters, canceller);
            double projectedRecall = projectedRecallPrecisionResult.getRecall();
            double projectedPrecision = projectedRecallPrecisionResult.getPrecision();
            double projected_fmeasure = 2*(projectedRecall*projectedPrecision)/(projectedRecall+projectedPrecision);

            measure.addMeasure(getMeasurementName(), projected_fmeasure);
            measure.addMeasure("Projected Recall", projectedRecall);
            measure.addMeasure("Projected Precision", projectedPrecision);

            measure.setValue(projected_fmeasure);
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
            return measure;
        } catch( Exception e ) {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
            return measure;
        }
    }

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, PetrinetWithMarking petrinetWithMarking, MiningAlgorithm miningAlgorithm, XLog log) {
        Measure measure = new Measure();

        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {}
        }));

        if(petrinetWithMarking == null) return measure;

        try {
            CompareParameters parameters = new CompareParameters(2);
            parameters.setClassifier(xEventClassifier);
            parameters.setTreeReduceParameters(new EfficientTreeReduceParametersDuplicates(false));
            parameters.setDebug(false);
            ProMCanceller canceller = new ProMCanceller() {
                @Override
                public boolean isCancelled() {
                    return false;
                }
            };

            AcceptingPetriNet acceptingPetriNet;
            if(petrinetWithMarking.getFinalMarkings().size() > 1) {
                acceptingPetriNet = new AcceptingPetriNetImpl(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarkings());
            }else {
                acceptingPetriNet = new AcceptingPetriNetImpl(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking());
            }

            ProjectedRecallPrecisionResult projectedRecallPrecisionResult = CompareLog2PetriNetPlugin.measure(log, acceptingPetriNet, parameters, canceller);
            double projectedRecall = projectedRecallPrecisionResult.getRecall();
            double projectedPrecision = projectedRecallPrecisionResult.getPrecision();
            double projected_fmeasure = 2*(projectedRecall*projectedPrecision)/(projectedRecall+projectedPrecision);

            measure.addMeasure(getMeasurementName(), projected_fmeasure);
            measure.addMeasure("Projected Recall", projectedRecall);
            measure.addMeasure("Projected Precision", projectedPrecision);

            measure.setValue(projected_fmeasure);
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
            return measure;
        } catch( Exception e ) {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
            return measure;
        }
    }

    @Override
    public String getMeasurementName() {
        return "Projected f-Measure";
    }

    @Override
    public String getAcronym() {return "(p)f-measure";}
}
