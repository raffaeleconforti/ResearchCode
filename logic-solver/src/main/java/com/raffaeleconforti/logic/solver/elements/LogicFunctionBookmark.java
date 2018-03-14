package com.raffaeleconforti.logic.solver.elements;

import com.raffaeleconforti.logic.solver.exception.LogicElementValueNotAssigned;

/**
 * Created by conforti on 6/08/15.
 */
public class LogicFunctionBookmark implements LogicElement {

    private final String name;

    public LogicFunctionBookmark(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean isValue() throws LogicElementValueNotAssigned {
        throw new LogicElementValueNotAssigned();
    }

    @Override
    public LogicElement reduce() throws LogicElementValueNotAssigned {
        throw new LogicElementValueNotAssigned();
    }

    @Override
    public LogicFunctionBookmark clone() {
        return new LogicFunctionBookmark(name);
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
        if(object instanceof LogicFunctionBookmark) {
            LogicFunctionBookmark logicFunctionBookmark = (LogicFunctionBookmark) object;
            return logicFunctionBookmark.getName().equals(name);
        }
        return false;
    }

    @Override
    public String toString() {
        return name;
    }
}
