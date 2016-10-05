package com.raffaeleconforti.logic.solver.elements;

import com.raffaeleconforti.logic.solver.exception.LogicElementValueNotAssigned;

/**
 * Created by conforti on 5/08/15.
 */
public interface LogicElement extends Cloneable {

    boolean isValue() throws LogicElementValueNotAssigned;
    LogicElement reduce() throws LogicElementValueNotAssigned;
    LogicElement clone();
    boolean contains(LogicElement logicElement);
    boolean containsInAND(LogicElement logicElement);
    boolean containsInOR(LogicElement logicElement);

}
