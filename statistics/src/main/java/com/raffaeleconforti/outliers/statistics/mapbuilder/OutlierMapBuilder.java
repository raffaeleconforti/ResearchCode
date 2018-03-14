package com.raffaeleconforti.outliers.statistics.mapbuilder;

import com.raffaeleconforti.outliers.statistics.OutlierMap;
import com.raffaeleconforti.outliers.statistics.outlieridentifiers.OutlierIdentifierGenerator;
import org.deckfour.xes.model.XLog;

/**
 * Created by conforti on 12/02/15.
 */
public interface OutlierMapBuilder {

    void clearMap();

    OutlierMap<String> buildOutliers(XLog log, int lookAHead, boolean smart);

    OutlierMap<String> buildOutliersReverse(XLog log, int lookAHead, boolean smart);

    void setOutlierIdentifierGenerator(OutlierIdentifierGenerator<String> outlierIdentifierGenerator);

}
