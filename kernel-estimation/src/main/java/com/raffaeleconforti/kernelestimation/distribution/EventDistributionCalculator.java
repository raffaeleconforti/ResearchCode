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

package com.raffaeleconforti.kernelestimation.distribution;

import com.raffaeleconforti.automaton.Automaton;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

import java.util.List;
import java.util.Map;

/**
 * Created by conforti on 27/01/2016.
 */
public interface EventDistributionCalculator {

    void analyseLog();

    Map<String, Double> computeLikelihoodPreviousEvent(String follower);

    double computeLikelihood(XTrace trace);
    double computeLikelihoodWithoutZero(XTrace trace);

    double computeLikelihood(List<XEvent> trace);

    double computeEnrichedLikelihood(List<XEvent> trace);
    void updateEnrichedLikelihood(XEvent event1, XEvent event2);

    double computeLikelihood(List<XEvent> list, double best);

    Map<String, Double> computeLikelihoodNextEvent(String activity);
    int getInitiator();
    int getTerminator();


    void filter(Automaton<String> automatonClean);
}

