package com.raffaeleconforti.statistics.outlierremover;

import com.raffaeleconforti.statistics.OutlierMap;
import com.raffaeleconforti.statistics.outlieridentifiers.OutlierIdentifierGenerator;
import org.deckfour.xes.model.XLog;

/**
 * Created by conforti on 12/02/15.
 */
public interface OutlierRemover {

    void setMapOutliers(OutlierMap<String> mapOutliers);

    void selectOulierToRemove(XLog log, int lookAHead, boolean selectOnlyOneOutlier, boolean smallestOrLargest);

    XLog generateNewLog(XLog log, OutlierIdentifierGenerator<String> outlierIdentifierGenerator, int lookAHead, boolean selectOnlyOneOutlier);

    void selectOulierToRemoveReverse(XLog log, int lookAHead, boolean selectOnlyOneOutlier, boolean smallestOrLargest);

    XLog generateNewLogReverse(XLog log, OutlierIdentifierGenerator<String> outlierIdentifierGenerator, int lookAHead, boolean selectOnlyOneOutlier);

}
