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

/**
 * Created by Adriano on 13/10/2017.
 */
public class TimePerformance implements MeasurementAlgorithm {

    static int REP = 5;

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, ProcessTree processTree, MiningAlgorithm miningAlgorithm, XLog log) {
        return null;
    }

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier,
                                      PetrinetWithMarking petrinetWithMarking, MiningAlgorithm miningAlgorithm, XLog log) {
        Measure measure = new Measure();
        double otime;
        double etime;

        try {
            etime = 0;
            for(int i = 0; i<REP; i++) {
                otime = System.currentTimeMillis();
                miningAlgorithm.mineBPMNDiagram(pluginContext, log, false, null, xEventClassifier);
                etime += (System.currentTimeMillis() - otime);
            }
            etime = etime/REP;
            measure.addMeasure(getAcronym(), etime);
        } catch( Exception e ) { return measure; }

        return  measure;
    }

    @Override
    public String getMeasurementName() {
        return "Time Performance";
    }

    @Override
    public String getAcronym() {return "avg-time";}

    @Override
    public boolean isMultimetrics() {
        return true;
    }
}
