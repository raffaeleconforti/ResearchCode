package com.raffaeleconforti.noisefiltering.timestamp.permutation;

import com.raffaeleconforti.datastructures.cache.Cache;
import com.raffaeleconforti.datastructures.cache.impl.SelfCleaningCache;
import com.raffaeleconforti.kernelestimation.distribution.EventDistributionCalculator;
import com.raffaeleconforti.log.util.LogCloner;
import com.raffaeleconforti.log.util.NameExtractor;
import com.raffaeleconforti.log.util.TraceToString;
import com.raffaeleconforti.noisefiltering.timestamp.check.TimeStampChecker;
import com.raffaeleconforti.noisefiltering.timestamp.collections.Collections2;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeBooleanImpl;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.*;

/**
 * Created by conforti on 5/02/2016.
 */
public interface EventPermutator {

    Set<XTrace> duplicatesTrace(XTrace trace);

    Set<String> getDuplicatedTraces();

    Map<String,Set<String>> getDuplicatedEvents();
}
