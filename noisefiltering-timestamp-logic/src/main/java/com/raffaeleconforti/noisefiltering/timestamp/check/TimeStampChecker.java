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

package com.raffaeleconforti.noisefiltering.timestamp.check;

import com.raffaeleconforti.log.util.NameExtractor;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Set;

/**
 * Created by conforti on 5/02/2016.
 */
public class TimeStampChecker {

    private final XTimeExtension xte = XTimeExtension.instance();
    private final SimpleDateFormat dateFormatSeconds;
    private final NameExtractor nameExtractor;

    public TimeStampChecker(XEventClassifier xEventClassifier, SimpleDateFormat dateFormatSeconds) {
        this.dateFormatSeconds = dateFormatSeconds;
        nameExtractor = new NameExtractor(xEventClassifier);
    }

    public boolean containsSameTimestamps(XTrace trace) {
        Set<String> times = new UnifiedSet<>();

        for(XEvent event : trace) {
            String time = dateFormatSeconds.format(xte.extractTimestamp(event));

            if(times.contains(time)) {
                return true;
            }else times.add(time);
        }

        return false;
    }

    public Set<Set<XEvent>> findEventsSameTimeStamp(XTrace trace) {
        Set<Set<XEvent>> setXEvents = new UnifiedSet<>();
        Map<String, Set<XEvent>> times = new UnifiedMap<>();
        Map<String, Set<String>> labels = new UnifiedMap<>();

        for(XEvent event : trace) {
            String time = dateFormatSeconds.format(xte.extractTimestamp(event));

            Set<XEvent> set;
            Set<String> setLabels;
            if((set = times.get(time)) == null) {
                set = new UnifiedSet<>();
                setLabels = new UnifiedSet<>();
                labels.put(time, setLabels);
                times.put(time, set);
            }else {
                setLabels = labels.get(time);
            }
            setLabels.add(nameExtractor.getEventName(event));
            set.add(event);
        }

        for(Map.Entry<String, Set<XEvent>> entry : times.entrySet()) {
            if(entry.getValue().size() > 1) setXEvents.add(entry.getValue());
        }

        return setXEvents;
    }

}
