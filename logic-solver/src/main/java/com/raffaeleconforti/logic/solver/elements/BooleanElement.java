package com.raffaeleconforti.logic.solver.elements;

/**
 * Created by conforti on 6/08/15.
 */
public class BooleanElement implements LogicElement {

    private boolean value;

    public BooleanElement(boolean value) {
        this.value = value;
    }

    @Override
    public BooleanElement clone() {
        return new BooleanElement(value);
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof BooleanElement) {
            BooleanElement booleanElement = (BooleanElement) object;
            return booleanElement.value == value;
        }
        return false;
    }

    @Override
    public boolean contains(LogicElement logicElement) {
        return equals(logicElement);
    }

    @Override
    public boolean containsInAND(LogicElement logicElement) {
        return equals(logicElement);
    }

    @Override
    public boolean containsInOR(LogicElement logicElement) {
        return equals(logicElement);
    }

    @Override
    public boolean isValue() {
        return value;
    }

    @Override
    public String toString() {
        return Boolean.toString(value).toUpperCase();
    }

    @Override
    public LogicElement reduce() {
        return this;
    }
}
