package com.raffaeleconforti.logic.solver.elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by conforti on 6/08/15.
 */
public class LogicSystem {

    List<LogicFunction> logicFunctions;

    public static void main(String[] args) {
        LogicExpression expression = new LogicExpression(new LogicFunctionBookmark("g()"), LogicOperator.AND, new AtomicElement("a"));
        LogicExpression expression1 = new LogicExpression(expression, LogicOperator.OR, new BooleanElement(true));
        LogicFunction function = new LogicFunction("f()", expression1);

        LogicExpression expression2 = new LogicExpression(new LogicFunctionBookmark("f()"), LogicOperator.AND, new AtomicElement("a"));
        LogicExpression expression3 = new LogicExpression(expression2, LogicOperator.OR, new AtomicElement("b"));
        LogicFunction function2 = new LogicFunction("g()", expression3);

        LogicExpression expression4 = new LogicExpression(new LogicFunctionBookmark("f()"), LogicOperator.AND, new AtomicElement("a"));
        LogicExpression expression5 = new LogicExpression(expression4, LogicOperator.OR, new AtomicElement("b"));
        LogicFunction function3 = new LogicFunction("d()", expression5);

        LogicSystem logicSystem = new LogicSystem(function, function2);
        System.out.println(logicSystem);
        logicSystem.reduce();
        System.out.println(logicSystem);
    }

    public LogicSystem() {
        logicFunctions = new ArrayList<LogicFunction>();
    }

    public LogicSystem(LogicFunction... logicFunctions) {
        this.logicFunctions = new ArrayList<LogicFunction>();
        for(LogicFunction logicFunction : logicFunctions) {
            this.logicFunctions.add(logicFunction);
        }
    }

    public LogicSystem(List<LogicFunction> logicFunctions) {
        this.logicFunctions = logicFunctions;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for(LogicFunction logicFunction : logicFunctions) {
            stringBuilder.append(logicFunction.toString());
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public LogicExpression transformToLogicExpression() {
        LogicExpression logicExpression = new LogicExpression(new BooleanElement(true), LogicOperator.AND, logicFunctions.get(0).getElement());
        for(int i = 1; i < logicFunctions.size(); i++) {
            logicExpression = new LogicExpression(logicExpression, LogicOperator.AND, logicFunctions.get(i).getElement());
        }
        return logicExpression;
    }

    public void reduce() {
        reduceLogicFunctions();
        replaceLogicFunctions();
    }

    private void replaceLogicFunctions() {
        for(int i = 0; i < logicFunctions.size(); i++) {
            LogicFunction logicFunctionRefenence = logicFunctions.get(i);
            for(int j = 0; j < logicFunctions.size(); j++) {
                if(i != j) {
                    LogicFunction logicFunctionToBeModified = logicFunctions.get(j);
                    logicFunctionToBeModified.replaceFunctionBookmarks(logicFunctionRefenence);
                    logicFunctionToBeModified.reduce();
                }
            }
        }
    }

    public void reduceLogicFunctions() {
        for(LogicFunction logicFunction : logicFunctions) {
            logicFunction.reduce();
        }
    }
}
