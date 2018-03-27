package com.raffaeleconforti.logic.solver.elements;

import com.raffaeleconforti.logic.solver.exception.LogicElementValueNotAssigned;

/**
 * Created by conforti on 6/08/15.
 */
public class LogicFunction implements LogicElement, Cloneable {

    private String name;
    private LogicElement element;

    public static void main(String[] args) {
        LogicExpression expression = new LogicExpression(new LogicFunctionBookmark("f()"), LogicOperator.AND, new BooleanElement(true));
        LogicExpression expression1 = new LogicExpression(expression, LogicOperator.OR, new AtomicElement("b"));
        LogicFunction function = new LogicFunction("f()", expression1);
        System.out.println(function);
        System.out.println(function.reduce());
    }

    public LogicFunction(String name, LogicElement element) {
        this.name = name;
        this.element = element;
    }

    public String getName() {
        return name;
    }

    public LogicElement getElement() {
        return element;
    }

    @Override
    public LogicFunction clone() {
        return new LogicFunction(this.name, element.clone());
    }

    @Override
    public boolean contains(LogicElement logicElement) {
        return false;
    }

    @Override
    public boolean containsInAND(LogicElement logicElement) {
        return false;
    }

    @Override
    public boolean containsInOR(LogicElement logicElement) {
        return false;
    }

    @Override
    public boolean isValue() throws LogicElementValueNotAssigned {
        return element.isValue();
    }

    @Override
    public LogicElement reduce() {
        removeItSelf(element);
        return this;
    }

    @Override
    public String toString() {
        return "function " + name +": " + element.toString();
    }

    public void replaceFunctionBookmarks(LogicFunction logicFunction) {
        replaceFunctionBookmarks(logicFunction, element);
    }

    private void replaceFunctionBookmarks(LogicFunction logicFunction, LogicElement logicElement) {
        if(logicElement instanceof LogicExpression) {
            LogicExpression logicExpression = (LogicExpression) logicElement;
            if (isBookmarkOfFunction(logicFunction, logicExpression.getLeftLogicElement())) {
                logicExpression.setLeftLogicElement(logicFunction.element);
            }
            if (isBookmarkOfFunction(logicFunction, logicExpression.getRightLogicElement())) {
                logicExpression.setRightLogicElement(logicFunction.element);
            }

            if (logicExpression.getLeftLogicElement() instanceof LogicExpression) {
                replaceFunctionBookmarks(logicFunction, logicExpression.getLeftLogicElement());
            }
            if (logicExpression.getRightLogicElement() instanceof LogicExpression) {
                replaceFunctionBookmarks(logicFunction, logicExpression.getRightLogicElement());
            }
        }
    }

    private boolean isBookmarkOfFunction(LogicFunction logicFunction, LogicElement logicElement) {
        if(logicElement instanceof LogicFunctionBookmark) {
            LogicFunctionBookmark logicFunctionBookmark = (LogicFunctionBookmark) logicElement;
            return logicFunctionBookmark.getName().equals(logicFunction.getName());
        }
        return false;
    }

    private LogicElement removeItSelf(LogicElement logicElement) {
        if(logicElement instanceof LogicExpression) {
            LogicExpression logicExpression = (LogicExpression) logicElement;
            if (logicExpression.getLeftLogicElement() instanceof LogicExpression) {
                logicExpression.setLeftLogicElement(removeItSelf(logicExpression.getLeftLogicElement()));
            }
            if (logicExpression.getRightLogicElement() instanceof LogicExpression) {
                logicExpression.setRightLogicElement(removeItSelf(logicExpression.getRightLogicElement()));
            }

            if (containsItSelf(logicExpression)) {
                if (LogicOperator.isAND(logicExpression.getLogicOperator())) {
                    if (isLeftElementItSelf(logicExpression)) return logicExpression.getRightLogicElement();
                    else return logicExpression.getLeftLogicElement();
                } else if (LogicOperator.isOR(logicExpression.getLogicOperator())) {
                    return new BooleanElement(true);
                }
            }
        }
        return logicElement;
    }

    private boolean containsItSelf(LogicExpression logicExpression) {
        return isLeftElementItSelf(logicExpression) || isLeftElementItSelf(logicExpression);
    }

    private boolean isLeftElementItSelf(LogicExpression logicExpression) {
        return isElementItSelf(logicExpression.getLeftLogicElement());
    }

    private boolean isRightElementItSelf(LogicExpression logicExpression) {
        return isElementItSelf(logicExpression.getRightLogicElement());
    }

    private boolean isElementItSelf(LogicElement logicElement) {
        if(logicElement instanceof LogicFunctionBookmark) {
            return ((LogicFunctionBookmark) logicElement).getName().equals(name);
        }
        return false;
    }
}
