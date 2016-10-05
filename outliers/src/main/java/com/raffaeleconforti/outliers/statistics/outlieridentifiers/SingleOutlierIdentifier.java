package com.raffaeleconforti.outliers.statistics.outlieridentifiers;

import com.raffaeleconforti.outliers.OutlierIdentifier;

/**
 * Created by conforti on 12/02/15.
 */
public class SingleOutlierIdentifier<T> implements OutlierIdentifier {

    private final T identifier1;

    public SingleOutlierIdentifier(T identifier) {
        this.identifier1 = identifier;
    }

    public T getIdentifier() {
        return identifier1;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof SingleOutlierIdentifier) {
            SingleOutlierIdentifier outlier = (SingleOutlierIdentifier) o;
            return outlier.getIdentifier().equals(identifier1);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return identifier1.hashCode();
    }

    @Override
    public String toString() {
        return identifier1.toString();
    }

}
