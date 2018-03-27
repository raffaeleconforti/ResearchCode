package com.raffaeleconforti.kernelestimation.distribution.impl;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

import java.util.Map;
import java.util.Set;

/**
 * Created by conforti on 21/01/2016.
 */
public class EventDistributionCalculatorNoiseImpl extends EventDistributionCalculatorImpl {

    private final Map<String, Set<String>> duplicatedEvents;

//    public static void main(String[] args) throws Exception {
//        XLog log = LogImporter.importFromFile(new XFactoryMemoryImpl(), "/Volumes/Data/SharedFolder/Logs/TimeNoise/LoanApplication.xes.gz");
//        EventDistributionCalculatorNoiseImpl dc = new EventDistributionCalculatorNoiseImpl(log, new XEventNameClassifier(), null);
//        dc.analyseLog();
//        System.out.println(dc.computeLikelihood(log.get(100)));
//    }

    public EventDistributionCalculatorNoiseImpl(XLog log, XEventClassifier xEventClassifier, Map<String, Set<String>> duplicatedEvents, boolean self_cleaning) {
        super(log, xEventClassifier, self_cleaning);
        this.duplicatedEvents = duplicatedEvents;
    }

    @Override
    public void analyseLog() {
        for(XTrace trace : log) {
            XEvent last = null;
            String lastName;
            for(XEvent event : trace) {
                if(last != null) {
                    lastName = getEventName(last);
                    String eventName = getEventName(event);

                    if((duplicatedEvents.get(getTraceName(trace)) == null || !duplicatedEvents.get(getTraceName(trace)).contains(lastName)) &&
                            (duplicatedEvents.get(getTraceName(trace)) == null || !duplicatedEvents.get(getTraceName(trace)).contains(eventName))) {
                        Map<String, Integer> map;
                        if ((map = distribution.get(lastName)) == null) {
                            map = new UnifiedMap<>();
                        }
                        Integer i;
                        if ((i = map.get(eventName)) == null) {
                            i = 0;
                        }
                        i++;
                        map.put(eventName, i);
                        distribution.put(lastName, map);

                        if ((map = distributionReverse.get(eventName)) == null) {
                            map = new UnifiedMap<>();
                        }
                        if ((i = map.get(lastName)) == null) {
                            i = 0;
                        }
                        i++;
                        map.put(lastName, i);
                        distributionReverse.put(eventName, map);
                    }
                }
                last = event;
            }

            Integer likelihood;
            if((duplicatedEvents.get(getTraceName(trace)) == null || !duplicatedEvents.get(getTraceName(trace)).contains(trace.get(0)))) {
                if ((likelihood = startEvent.get(getEventName(trace.get(0)))) == null) {
                    likelihood = 0;
                }
                likelihood++;
                startEvent.put(getEventName(trace.get(0)), likelihood);
            }

            if((duplicatedEvents.get(getTraceName(trace)) == null || !duplicatedEvents.get(getTraceName(trace)).contains(getEventName(trace.get(trace.size() - 1))))) {
                if ((likelihood = endEvent.get(getEventName(trace.get(trace.size() - 1)))) == null) {
                    likelihood = 0;
                }
                likelihood++;
                endEvent.put(getEventName(trace.get(trace.size() - 1)), likelihood);
            }
        }
    }

}
