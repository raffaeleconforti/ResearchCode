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

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

/**
 * Created by conforti on 7/02/15.
 */

public class LogComparator {

    XConceptExtension xce = XConceptExtension.instance();

    public String check(XLog log1, XLog log2) {
        int notMaching = 0;
        for(XTrace trace1 : log1) {
            String traceID = xce.extractName(trace1);
            for (XTrace trace2 : log2) {
                if (xce.extractName(trace2).equals(traceID)) {
                    for (int i = 0; i < trace1.size(); i++) {
                        if (!xce.extractName(trace1.get(i)).equals(xce.extractName(trace2.get(i)))) {
                            notMaching++;
                            break;
                        }
                    }
                    break;
                }
            }
        }

        String s =  "<html><p>Number of Different Traces: " + notMaching + "</p></html>";

        return s;
    }

}
