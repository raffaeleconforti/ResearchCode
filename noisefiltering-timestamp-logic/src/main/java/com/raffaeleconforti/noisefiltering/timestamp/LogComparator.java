package com.raffaeleconforti.noisefiltering.timestamp;

import com.raffaeleconforti.noisefiltering.timestamp.check.TimeStampChecker;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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
