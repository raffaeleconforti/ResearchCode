package com.raffaeleconforti.log.util;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.Map;
import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 20/03/2016.
 */
public class LogAnalyser {

    private static final XConceptExtension xce = XConceptExtension.instance();
    private static final XLifecycleExtension xle = XLifecycleExtension.instance();
    private static final XOrganizationalExtension xoe = XOrganizationalExtension.instance();
    private static final XTimeExtension xte = XTimeExtension.instance();

    public static int countEvents(XLog log) {
        int events = 0;
        for(XTrace trace : log) {
            events += countEvents(trace);
        }
        return events;
    }

    public static int countEvents(XTrace trace) {
        return trace.size();
    }

    public static int countTraces(XLog log) {
       return log.size();
    }

    public static int countUniqueActivities(XLog log, XEventClassifier eventClassifier) {
        return getUniqueActivities(log, eventClassifier).size();
    }

    public static Map<String, Integer> getFinalActivityFriquencies(XLog log) {
        Map<String, Integer> finalActivityFrequencies = new UnifiedMap<>();
        for(XTrace trace : log) {
            String activity = getFinalActivity(trace);
            Integer frequency;
            if((frequency = finalActivityFrequencies.get(activity)) == null) {
                frequency = 0;
            }
            frequency++;
            finalActivityFrequencies.put(activity, frequency);
        }
        return finalActivityFrequencies;
    }

    public static Set<String> getFinalActivities(XLog log) {
        return getFinalActivityFriquencies(log).keySet();
    }

    public static String getFinalActivity(XTrace trace) {
        return xce.extractName(trace.get(trace.size() - 1));
    }

    public static Map<String, Integer> getInitialActivityFriquencies(XLog log) {
        Map<String, Integer> initialActivityFrequencies = new UnifiedMap<>();
        for(XTrace trace : log) {
            String activity = getInitialActivity(trace);
            Integer frequency;
            if((frequency = initialActivityFrequencies.get(activity)) == null) {
                frequency = 0;
            }
            frequency++;
            initialActivityFrequencies.put(activity, frequency);
        }
        return initialActivityFrequencies;
    }

    public static Set<String> getInitialActivities(XLog log) {
        return getInitialActivityFriquencies(log).keySet();
    }

    public static String getInitialActivity(XTrace trace) {
        return xce.extractName(trace.get(0));
    }

    public static Set<String> getUniqueActivities(XLog log, XEventClassifier eventClassifier) {
        Set<String> uniqueActivities = new UnifiedSet<>();
        for(XTrace trace : log) {
            uniqueActivities.addAll(getUniqueActivities(trace, eventClassifier));
        }
        return uniqueActivities;
    }

    public static Set<String> getUniqueActivities(XTrace trace, XEventClassifier eventClassifier) {
        Set<String> uniqueActivities = new UnifiedSet<>();
        for(XEvent event : trace) {
            uniqueActivities.add(eventClassifier.getClassIdentity(event));
        }
        return uniqueActivities;
    }

}
