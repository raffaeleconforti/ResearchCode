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
import org.deckfour.xes.model.impl.XAttributeBooleanImpl;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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
