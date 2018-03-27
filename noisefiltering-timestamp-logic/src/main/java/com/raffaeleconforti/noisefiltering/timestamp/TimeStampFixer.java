package com.raffaeleconforti.noisefiltering.timestamp;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import java.util.Map;
import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 16/09/2016.
 */
public interface TimeStampFixer {

    XLog obtainPermutedLog();
    Set<String> getDuplicatedTraces();
    Map<String, XTrace> getOriginalTraces();
    Map<String, Set<String>> getDuplicatedEvents();
    XLog getNoiseFreeLog();
    Map<String,Set<XTrace>> getPossibleTraces();
    Map<String, Set<String>> getFaultyEvents();

}
