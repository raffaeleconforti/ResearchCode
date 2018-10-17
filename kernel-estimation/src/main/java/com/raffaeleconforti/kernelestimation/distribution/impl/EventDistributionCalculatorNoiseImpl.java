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
//        XLog log = LogImporter.importFromFile(new XFactoryMemoryImpl(), "/Volumes/Data/SharedFolder/Logs/TimeNoise/LoanApplication" + file_ext);
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
