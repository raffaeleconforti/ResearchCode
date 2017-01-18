package com.raffaeleconforti.outliers.statistics.outlieridentifiers;

import com.raffaeleconforti.outliers.OutlierIdentifier;

/**
 * Created by conforti on 12/02/15.
 */
public class DoubleOutlierIdentifier<T> implements OutlierIdentifier {

    private T identifier1;
    private T identifier2;

    public DoubleOutlierIdentifier(T identifier1, T identifier2) {
        this.identifier1 = identifier1;
        this.identifier2 = identifier2;
    }

    public T getIdentifier1() {
        return identifier1;
    }
    public T getIdentifier2() {
        return identifier2;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof DoubleOutlierIdentifier) {
            DoubleOutlierIdentifier outlier = (DoubleOutlierIdentifier) o;
            return (outlier.getIdentifier1().equals(identifier1) && outlier.getIdentifier2().equals(identifier2));
        }
        return false;
    }

    @Override
    public int hashCode() {
        StringBuilder sb = new StringBuilder();
        sb.append(identifier1.toString()).append("+").append(identifier2.toString());
        return sb.toString().hashCode();
    }

    @Override
    public String toString() {
        return identifier1.toString() + " " + identifier2.toString();
    }

}
