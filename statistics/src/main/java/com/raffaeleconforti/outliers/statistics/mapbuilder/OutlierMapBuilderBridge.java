package com.raffaeleconforti.outliers.statistics.mapbuilder;

import com.raffaeleconforti.outliers.Outlier;
import com.raffaeleconforti.outliers.OutlierIdentifier;
import com.raffaeleconforti.outliers.statistics.OutlierMap;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

/**
 * Created by conforti on 12/02/15.
 */
public class OutlierMapBuilderBridge extends OutlierMapBuilderAbstract{

    protected final OutlierMap<String> map = new OutlierMap<String>();

    public OutlierMapBuilderBridge(XEventClassifier xEventClassifier) {
        super(xEventClassifier);
    }

    @Override
    public void clearMap() {
        map.clear();
    }

    @Override
    public OutlierMap<String> buildOutliers(XLog log, int lookAHead, boolean smart) {
        for (XTrace trace : log) {
            for (int i = 0; i < trace.size() - 2; i++) {
                XEvent event1 = trace.get(i);
                XEvent event2 = trace.get(i + 1);
                XEvent event3 = trace.get(i + 2);

                build(event1, event2, event3, smart);
            }
        }
        return map;
    }

    @Override
    public OutlierMap<String> buildOutliersReverse(XLog log, int lookAHead, boolean smart) {
        for (XTrace trace : log) {
            for (int i = trace.size() - 1; i >= 0 + 2; i--) {
                XEvent event1 = trace.get(i);
                XEvent event2 = trace.get(i - 1);
                XEvent event3 = trace.get(i - 2);

                build(event1, event2, event3, smart);
            }
        }
        return map;
    }

    private void build(XEvent event1, XEvent event2, XEvent event3, boolean smart) {
        OutlierIdentifier outlierIdentifier = outlierIdentifierGenerator.generate(getEventName(event1), getEventName(event3));
        Outlier<String> outlier = new Outlier<String>(getEventName(event2), outlierIdentifier, true);

        map.addOutlier(outlier);
        map.increaseFrequency(outlier);
        map.increaseIdentifierFrequency(outlierIdentifier);

        OutlierIdentifier outlierIdentifier1 = outlierIdentifierGenerator.generate(getEventName(event1), getEventName(event2));
        Outlier<String> outlier1 = new Outlier<String>(getEventName(event1)+getEventName(event2), outlierIdentifier, false);

        map.addOutlier(outlier1);
        map.increaseFrequency(outlier1);
        map.increaseIdentifierFrequency(outlierIdentifier1);
    }

}
