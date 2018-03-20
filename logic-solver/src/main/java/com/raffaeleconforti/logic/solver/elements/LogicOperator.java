package com.raffaeleconforti.logic.solver.elements;

/**
 * Created by conforti on 5/08/15.
 */
public enum  LogicOperator {

    AND, OR;

    public static boolean isAND(LogicOperator logicOperator) {
        return logicOperator == LogicOperator.AND;
    }

    public static boolean isOR(LogicOperator logicOperator) {
        return logicOperator == LogicOperator.OR;
    }
}


