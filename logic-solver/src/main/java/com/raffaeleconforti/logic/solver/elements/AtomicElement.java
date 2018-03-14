package com.raffaeleconforti.logic.solver.elements;

import com.raffaeleconforti.logic.solver.exception.BooleanFormatException;
import com.raffaeleconforti.logic.solver.exception.LogicElementValueNotAssigned;

/**
 * Created by conforti on 5/08/15.
 */
public class AtomicElement implements LogicElement {

    private final String name;
    private Boolean value;

    public AtomicElement(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public AtomicElement clone() {
        return new AtomicElement(name);
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
    public boolean equals(Object object) {
        if(object instanceof AtomicElement) {
            AtomicElement atomicElement = (AtomicElement) object;
            return atomicElement.getName().equals(name);
        }
        return false;
    }

    @Override
    public boolean isValue() throws LogicElementValueNotAssigned {
        if(value != null) {
            return value;
        }else {
            throw new LogicElementValueNotAssigned();
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public LogicElement reduce() {
        return this;
    }

    private boolean guessValue(String value) throws BooleanFormatException {
        if(value.equalsIgnoreCase("true")) {
            return true;
        }else if(value.equalsIgnoreCase("false")) {
            return false;
        }else {
            throw new BooleanFormatException();
        }
    }
}
