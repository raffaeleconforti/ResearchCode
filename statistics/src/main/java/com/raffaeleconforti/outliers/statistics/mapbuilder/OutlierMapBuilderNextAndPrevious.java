package com.raffaeleconforti.outliers.statistics.mapbuilder;

import com.raffaeleconforti.outliers.Outlier;
import com.raffaeleconforti.outliers.OutlierIdentifier;
import com.raffaeleconforti.outliers.statistics.OutlierMap;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

import java.util.Map;
import java.util.Set;

/**
 * Created by conforti on 12/02/15.
 */
public class OutlierMapBuilderNextAndPrevious extends OutlierMapBuilderAbstract{

    protected final OutlierMap<String> map = new OutlierMap<String>();
    protected final Map<String, Set<String>> existingDependencies = new UnifiedMap<String, Set<String>>();

    public OutlierMapBuilderNextAndPrevious(XEventClassifier xEventClassifier) {
        super(xEventClassifier);
    }

    @Override
    public void clearMap() {
        map.clear();
    }

    @Override
    public OutlierMap<String> buildOutliers(XLog log, int lookAHead, boolean smart) {
        for (XTrace trace : log) {
            for (int i = 0; i < trace.size() - lookAHead; i++) {
                XEvent event1 = trace.get(i);
                XEvent event2 = trace.get(i + lookAHead);

                build(event1, event2, smart);
            }
        }
        return map;
    }

    @Override
    public OutlierMap<String> buildOutliersReverse(XLog log, int lookAHead, boolean smart) {
        for (XTrace trace : log) {
            for (int i = trace.size() - 1; i >= 0 + lookAHead; i--) {
                XEvent event1 = trace.get(i);
                XEvent event2 = trace.get(i - lookAHead);

                build(event1, event2, smart);
            }
        }
        return map;
    }

    private void build(XEvent event1, XEvent event2, boolean smart) {
        OutlierIdentifier outlierIdentifier = outlierIdentifierGenerator.generate(getEventName(event1));
        Outlier<String> outlier = new Outlier<String>(getEventName(event2), outlierIdentifier, true);
        map.addOutlier(outlier);

        if(smart) {
            if(existingDependencies.get(getEventName(event1)) != null) {
                if(existingDependencies.get(getEventName(event1)).contains(getEventName(event2))) {
                    map.increaseFrequency(outlier);
                    map.increaseIdentifierFrequency(outlierIdentifier);
                }else {
                    map.setFrequency(outlier, 0.0);
                }
            }else {
                System.out.println("ERROR");
            }
        }else {
            map.increaseFrequency(outlier);
            map.increaseIdentifierFrequency(outlierIdentifier);
        }
    }

}
