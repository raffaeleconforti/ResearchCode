package com.raffaeleconforti.foreignkeydiscovery.functionaldependencies;

/**
 * Objects of this class hold are functionale dependency. The class is used by
 * algorithms in TANEjava.java to calculate the cover and closure for a set F
 * of functional dependencies.
 *
 * @author Tobias
 */
public class FunctionalDependency implements Comparable<FunctionalDependency> {
    private ComparableSet<String> X = new ComparableSet<String>();
    private ComparableSet<String> Y = new ComparableSet<String>();

    /**
     * Returns the left-hand-side of a functional dependency
     *
     * @return ComparableSet<String> - the the left-hand-side attributes
     */
    public ComparableSet<String> getX() {
        return X;
    }

    /**
     * Returns the right-hand-side of a functional dependency
     *
     * @return ComparableSet<String> - the the right-hand-side attributes
     */
    public ComparableSet<String> getY() {
        return Y;
    }

    /**
     * Adds a new attribue to the left-hand-side
     *
     * @param attribute - the left-hand-side atttibute
     */
    public void addX(String attribute) {
        X.add(attribute);
    }

    /**
     * Adds an attribue set to the left-hand-side
     *
     * @param attribute - the left-hand-side atttibutes
     */
    public void addX(ComparableSet<String> attribute) {
        X.addAll(attribute);
    }

    /**
     * Adds an attribue to the right-hand-side
     *
     * @param attribute - the right-hand-side atttibute
     */
    public void addY(String attribute) {
        Y.add(attribute);
    }

    /**
     * Adds an attribue set to the right-hand-side
     *
     * @param attribute - the right-hand-side atttibutes
     */
    public void addY(ComparableSet<String> attribute) {
        Y.addAll(attribute);
    }

    public int compareTo(FunctionalDependency o) {
        int cmp = X.compareTo(o.X);

        //Wenn erster Paar gleich ist, dann entscheide anhand vom Zweiten
        if (cmp == 0) {
            cmp = Y.compareTo(o.Y);
        }
        return cmp;
    }

    /**
     * Prints a functional dependency.
     */
    public String toString() {
        return X + "->" + Y;
    }

    /**
     * Clears the attribues of the RHS and LHS candidates
     */
    public void clear() {
        X.clear();
        Y.clear();
    }
}
