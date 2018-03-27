package com.raffaeleconforti.noisefiltering.timestamp.permutation;

import org.deckfour.xes.model.XTrace;

import java.util.Map;
import java.util.Set;

/**
 * Created by conforti on 5/02/2016.
 */
public interface EventPermutator {

    Set<XTrace> duplicatesTrace(XTrace trace);

    Set<String> getDuplicatedTraces();

    Map<String,Set<String>> getDuplicatedEvents();
}
