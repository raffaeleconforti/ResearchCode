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

package com.raffaeleconforti.noisefiltering.timestamp;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.XVisitor;
import org.deckfour.xes.model.impl.XAttributeBooleanImpl;

/**
 * Created by conforti on 7/02/15.
 */

public class TimeStampFilterMarker {

    XConceptExtension xce = XConceptExtension.instance();

    public XLog check(XLog noisyLog, XLog correctLog) {
        for(XTrace trace1 : noisyLog) {
            String traceID = xce.extractName(trace1);
            boolean matches = true;

            for (XTrace trace2 : correctLog) {
                if (xce.extractName(trace2).equals(traceID)) {

                    for (int i = 0; i < trace1.size(); i++) {
                        if (!xce.extractName(trace1.get(i)).equals(xce.extractName(trace2.get(i)))) {
                            matches = false;
                            trace1.get(i).getAttributes().put("change", new XAttributeBooleanImpl("change", true));
                        }
                    }

                    if (!matches) {
                        trace1.getAttributes().put("change", new XAttributeBooleanImpl("change", true));

                    }
                    break;
                }
            }
        }

        return noisyLog;
    }

}
