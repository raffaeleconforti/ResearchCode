package com.raffaeleconforti.foreignkeydiscovery.conceptualmodels;

/**
 * Defines the possible cardinalities of relationships between entities
 *
 * @author Viara Popova
 */
public enum Cardinality {
    ZERO_OR_ONE("0..1"), ONE("1"), ZERO_OR_MANY("*"), ONE_OR_MANY("+");

    String label;

    Cardinality(String label) {
        this.label = label;
    }

    public String toString() {
        return label;
    }

}
