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
