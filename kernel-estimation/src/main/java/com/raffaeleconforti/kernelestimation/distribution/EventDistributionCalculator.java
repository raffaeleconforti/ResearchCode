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

