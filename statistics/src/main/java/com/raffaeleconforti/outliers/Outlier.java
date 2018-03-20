package com.raffaeleconforti.outliers;

/**
 * Created by conforti on 12/02/15.
 */
public class Outlier<T> {

    private final T elementToRemove;
    private final OutlierIdentifier identifier;
    private final boolean real;

    public Outlier(T elementToRemove, OutlierIdentifier identifier, boolean real) {
        this.elementToRemove = elementToRemove;
        this.identifier = identifier;
        this.real = real;
    }

    public boolean isReal() {
        return real;
    }

    public T getElementToRemove() {
        return elementToRemove;
    }

    public OutlierIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Outlier) {
            Outlier outlier = (Outlier) o;
            return outlier.getIdentifier().equals(identifier) && outlier.getElementToRemove().equals(elementToRemove);
        }
        return false;
    }

    @Override
    public int hashCode() {
        StringBuilder sb = new StringBuilder();
        sb.append(identifier.toString()).append("+").append(elementToRemove.toString());
        return sb.toString().hashCode();
    }
}
