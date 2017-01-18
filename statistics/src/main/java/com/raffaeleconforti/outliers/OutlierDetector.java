package com.raffaeleconforti.outliers;

import com.raffaeleconforti.outliers.statistics.OutlierMap;
import com.raffaeleconforti.outliers.statistics.outlieridentifiers.OutlierIdentifierGenerator;
import org.deckfour.xes.model.XLog;

/**
 * Created by conforti on 10/02/15.
 */
public interface OutlierDetector {

    int[] discoverOutlier(double[] values, double significance);

    void setOutlierIdentifierGenerator(OutlierIdentifierGenerator<String> outlierIdentifierGenerator);

    void cleanMap();

    void countEvents(XLog log);

    OutlierMap<String> detectOutliers(XLog log, Object... parameters);

    OutlierMap<String> detectOutliersReverse(XLog log, Object... parameters);

}
