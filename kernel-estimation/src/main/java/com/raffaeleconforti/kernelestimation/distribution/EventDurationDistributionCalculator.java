package com.raffaeleconforti.kernelestimation.distribution;

import com.raffaeleconforti.automaton.Automaton;
import com.raffaeleconforti.kernelestimation.distribution.impl.NoDataAvailableException;
import org.deckfour.xes.model.XEvent;

/**
 * Created by conforti on 28/01/2016.
 */
public interface EventDurationDistributionCalculator {

    long estimateDuration(XEvent event) throws NoDataAvailableException;
    long estimateDuration(XEvent event, XEvent preceedingEvent) throws NoDataAvailableException;

    void filter(Automaton<String> automatonClean);

}
