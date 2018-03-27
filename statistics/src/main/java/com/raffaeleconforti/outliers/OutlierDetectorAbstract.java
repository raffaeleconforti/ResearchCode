package com.raffaeleconforti.outliers;

import com.raffaeleconforti.log.util.NameExtractor;
import com.raffaeleconforti.outliers.statistics.OutlierMap;
import com.raffaeleconforti.outliers.statistics.mapbuilder.OutlierMapBuilder;
import com.raffaeleconforti.outliers.statistics.outlieridentifiers.OutlierIdentifierGenerator;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

import java.util.Map;

/**
 * Created by conforti on 10/02/15.
 */
public abstract class OutlierDetectorAbstract implements OutlierDetector {

    private final OutlierMapBuilder outlierMapBuilder = null;
    private OutlierIdentifierGenerator<String> outlierIdentifierGenerator = null;
    private final OutlierMap<String> map = new OutlierMap<String>();
    private final Map<OutlierIdentifier, Double> mapAverage = new UnifiedMap<OutlierIdentifier, Double>();
    private final OutlierMap<String> mapOutliers = new OutlierMap<String>();
    private final Map<String, Double> mapNumberOfEvents = new UnifiedMap<String, Double>();
    private final NameExtractor nameExtractor;

    public static final int STANDARDDEV= 0;
    public static final int MAD = 1;

    public OutlierDetectorAbstract(XEventClassifier xEventClassifier) {
        this.nameExtractor = new NameExtractor(xEventClassifier);
    }

    @Override
    public void setOutlierIdentifierGenerator(OutlierIdentifierGenerator<String> outlierIdentifierGenerator) {
        this.outlierIdentifierGenerator = outlierIdentifierGenerator;
    }

    @Override
    public void cleanMap() {
        map.clear();
        mapAverage.clear();
        mapOutliers.clear();
        mapNumberOfEvents.clear();
        outlierMapBuilder.clearMap();
    }

    @Override
    public void countEvents(XLog log) {
        for(XTrace trace : log) {
            for(XEvent event : trace) {
                String name = nameExtractor.getEventName(event);
                Double count;
                if((count = mapNumberOfEvents.get(name)) == null) {
                    count = 0.0;
                }
                count++;
                mapNumberOfEvents.put(name, count);
            }
        }
    }
}
