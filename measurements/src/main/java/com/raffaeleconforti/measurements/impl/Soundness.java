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

import au.edu.qut.petrinet.tools.SoundnessChecker;
import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.MeasurementAlgorithm;
import com.raffaeleconforti.wrappers.MiningAlgorithm;
import com.raffaeleconforti.wrappers.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.processtree.ProcessTree;


/**
 * Created by Adriano on 23/11/2016.
 */
public class Soundness implements MeasurementAlgorithm {

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, ProcessTree processTree, MiningAlgorithm miningAlgorithm, XLog log) {
        return null;
    }

    @Override
    public Measure computeMeasurement(UIPluginContext pluginContext, XEventClassifier xEventClassifier, PetrinetWithMarking petrinetWithMarking, MiningAlgorithm miningAlgorithm, XLog log) {
        Measure measure = new Measure();
        String soundness;

        if(isSound(petrinetWithMarking)) soundness = "sound";
        else soundness = "unsound";

        measure.addMeasure(getAcronym(), soundness);
        return measure;
    }

    @Override
    public String getMeasurementName() {
        return "Soundness";
    }

    @Override
    public String getAcronym() {return "soundness";}

    @Override
    public boolean isMultimetrics() {
        return true;
    }

    static public boolean isSound(PetrinetWithMarking petrinetWithMarking) {
        if(petrinetWithMarking == null) return false;
        AcceptingPetriNet acceptingPetriNet = getAcceptingPetriNet(petrinetWithMarking);
        try {
//            System.out.print("DEBUG - checking soundness...");
            SoundnessChecker checker = new SoundnessChecker(acceptingPetriNet.getNet());
            //                System.out.println("sound");
//                System.out.println("unsound");
            return checker.isSound();
        } catch( Exception e ) { return false; }
    }

    static private AcceptingPetriNet getAcceptingPetriNet(PetrinetWithMarking petrinetWithMarking) {
        if(petrinetWithMarking.getFinalMarkings().size() > 1) return new AcceptingPetriNetImpl(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarkings());
        else return new AcceptingPetriNetImpl(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking());
    }
}
