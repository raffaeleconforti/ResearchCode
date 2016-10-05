package com.raffaeleconforti.outliers.statistics.outlieridentifiers;

import com.raffaeleconforti.outliers.OutlierIdentifier;

/**
 * Created by conforti on 12/02/15.
 */
public class OutlierIdentifierGenerator<T> {

    public OutlierIdentifier generate(T identifier1) {
        return new SingleOutlierIdentifier<T>(identifier1);
    }

    public OutlierIdentifier generate(T identifier1, T identifier2) {
        return new DoubleOutlierIdentifier<T>(identifier1, identifier2);
    }

}
