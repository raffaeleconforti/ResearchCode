package com.raffaeleconforti.logic.solver.elements;

import com.raffaeleconforti.logic.solver.exception.LogicElementValueNotAssigned;

/**
 * Created by conforti on 5/08/15.
 */
public class LogicExpression implements LogicElement, Cloneable {

    private LogicElement leftLogicElement;
    private LogicOperator logicOperator;
    private LogicElement rightLogicElement;

    private Boolean value;

    public static void main(String[] args) throws LogicElementValueNotAssigned {
        LogicExpression expression = new LogicExpression(new BooleanElement(true), LogicOperator.AND, new AtomicElement("a"));
        LogicExpression expression1 = new LogicExpression(expression, LogicOperator.OR, new AtomicElement("b"));
        System.out.println(expression1);
        System.out.println(expression1.reduce());
    }

    public LogicExpression(LogicElement leftLogicElement, LogicOperator logicOperator, LogicElement rightLogicElement) {
        this.leftLogicElement = leftLogicElement;
        this.logicOperator = logicOperator;
        this.rightLogicElement = rightLogicElement;
    }

    public LogicElement getLeftLogicElement() {
        return leftLogicElement;
    }

    public LogicElement getRightLogicElement() {
        return rightLogicElement;
    }

    public LogicOperator getLogicOperator() {
        return logicOperator;
    }

    public void setRightLogicElement(LogicElement rightLogicElement) {
        this.rightLogicElement = rightLogicElement.clone();
    }

    public void setLeftLogicElement(LogicElement leftLogicElement) {
        this.leftLogicElement = leftLogicElement.clone();
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public LogicExpression clone() {
        return new LogicExpression(leftLogicElement.clone(), logicOperator, rightLogicElement.clone());
    }

    @Override
    public boolean contains(LogicElement logicElement) {
        boolean result = equals(logicElement);
        result |= leftLogicElement.contains(logicElement);
        result |= rightLogicElement.contains(logicElement);
        return result;
    }

    @Override
    public boolean containsInAND(LogicElement logicElement) {
        boolean result = equals(logicElement);
        if(LogicOperator.isAND(logicOperator)) {
            result |= leftLogicElement.containsInAND(logicElement);
            result |= rightLogicElement.containsInAND(logicElement);
        }
        return result;
    }

    @Override
    public boolean containsInOR(LogicElement logicElement) {
        boolean result = equals(logicElement);
        if(LogicOperator.isOR(logicOperator)) {
            result |= leftLogicElement.containsInOR(logicElement);
            result |= rightLogicElement.containsInOR(logicElement);
        }
        return result;
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof LogicExpression) {
            LogicExpression logicExpression = (LogicExpression) object;
            boolean result = logicExpression.getLeftLogicElement().equals(leftLogicElement) && logicExpression.getRightLogicElement().equals(rightLogicElement);
            result |= logicExpression.getLeftLogicElement().equals(rightLogicElement) && logicExpression.getRightLogicElement().equals(leftLogicElement);
            return result;
        }
        return false;
    }

    @Override
    public boolean isValue() throws LogicElementValueNotAssigned {
        if(value != null) return value;

        if(LogicOperator.isAND(logicOperator)) return leftLogicElement.isValue() && rightLogicElement.isValue();
        if(LogicOperator.isOR(logicOperator)) return leftLogicElement.isValue() || rightLogicElement.isValue();

        return false;
    }

    @Override
    public String toString() {
        return "( " + leftLogicElement.toString() + " " + logicOperator.toString() + " " + rightLogicElement.toString() + " )";
    }

    @Override
    public LogicElement reduce() throws LogicElementValueNotAssigned {
        leftLogicElement = reduceLeftElement();
        rightLogicElement = reduceRightElement();

        if (containsBooleanElement()) {
            if (LogicOperator.isAND(logicOperator)) {
                return reduceANDBooleanElement();
            } else if (LogicOperator.isOR(logicOperator)) {
                return reduceORBooleanElement();
            }else {
                throw new LogicElementValueNotAssigned();
            }
        }else {
            return reduceSameElement();//DisjunctiveNormalFormBuilder.convertToDNF(reduceSameElement());
        }
    }

    private LogicElement reduceLeftElement() throws LogicElementValueNotAssigned {
        return leftLogicElement.reduce();
    }

    private LogicElement reduceRightElement() throws LogicElementValueNotAssigned {
        return rightLogicElement.reduce();
    }

    private LogicElement reduceANDBooleanElement() throws LogicElementValueNotAssigned {
        if(isLeftElementBooleanElement() && leftLogicElement.isValue()) return rightLogicElement;
        else if(isRightElementBooleanElement() && rightLogicElement.isValue()) return leftLogicElement;
        else return this;
    }

    private LogicElement reduceORBooleanElement() throws LogicElementValueNotAssigned {
        if(isLeftElementBooleanElement() && leftLogicElement.isValue()) return new BooleanElement(true);
        else if(isRightElementBooleanElement() && rightLogicElement.isValue()) return new BooleanElement(true);
        else return this;
    }

    private LogicElement reduceSameElement() {
        if(leftLogicElement.equals(rightLogicElement)) {
            return leftLogicElement;
        }else if(LogicOperator.isAND(logicOperator)) {
            return simplifySharedElementsInAND(leftLogicElement, rightLogicElement, logicOperator);
        }else if(LogicOperator.isOR(logicOperator)) {
            return simplifySharedElementsInOR(leftLogicElement, rightLogicElement, logicOperator);
        }
        return this;
    }

    private boolean isLogicExpression(LogicElement firstLogicElement) {
        return firstLogicElement instanceof LogicExpression;
    }

    private LogicElement simplifySharedElementsInAND(LogicElement firstElement, LogicElement secondElement, LogicOperator logicOperator) {
        if(isLogicExpression(firstElement)) {
            LogicExpression logicExpression = (LogicExpression) firstElement;
            if (logicExpression.containsInAND(secondElement)) {
                return logicExpression;
            }
        }
        if(isLogicExpression(secondElement)) {
            LogicExpression logicExpression = (LogicExpression) secondElement;
            if (logicExpression.containsInAND(firstElement)) {
                return logicExpression;
            }
        }
        if(isLogicExpression(firstElement) && isLogicExpression(secondElement)) {
            LogicExpression firstLogicExpression = (LogicExpression) firstElement;
            LogicExpression secondLogicExpression = (LogicExpression) secondElement;
            if(LogicOperator.isAND(firstLogicExpression.getLogicOperator()) && LogicOperator.isAND(secondLogicExpression.getLogicOperator())) {
                if(firstLogicExpression.containsInAND(secondLogicExpression.getLeftLogicElement())) {
                    return new LogicExpression(firstLogicExpression, LogicOperator.AND, secondLogicExpression.getRightLogicElement());
                }else if(firstLogicExpression.containsInAND(secondLogicExpression.getRightLogicElement())) {
                    return new LogicExpression(firstLogicExpression, LogicOperator.AND, secondLogicExpression.getLeftLogicElement());
                }else if(secondLogicExpression.containsInAND(firstLogicExpression.getLeftLogicElement())) {
                    return new LogicExpression(secondLogicExpression, LogicOperator.AND, firstLogicExpression.getRightLogicElement());
                }else if(secondLogicExpression.containsInAND(firstLogicExpression.getRightLogicElement())) {
                    return new LogicExpression(secondLogicExpression, LogicOperator.AND, firstLogicExpression.getLeftLogicElement());
                }
            }
        }
        return new LogicExpression(firstElement, logicOperator, secondElement);
    }

    private LogicElement simplifySharedElementsInOR(LogicElement firstElement, LogicElement secondElement, LogicOperator logicOperator) {
        if(isLogicExpression(firstElement)) {
            LogicExpression logicExpression = (LogicExpression) firstElement;
            if (logicExpression.contains(secondElement)) {
                return secondElement;
            }
        }
        if(isLogicExpression(secondElement)) {
            LogicExpression logicExpression = (LogicExpression) secondElement;
            if (logicExpression.contains(firstElement)) {
                return firstElement;
            }
        }
        if(isLogicExpression(firstElement) && isLogicExpression(secondElement)) {
            LogicExpression firstLogicExpression = (LogicExpression) firstElement;
            LogicExpression secondLogicExpression = (LogicExpression) secondElement;
            if(LogicOperator.isOR(firstLogicExpression.getLogicOperator()) && LogicOperator.isOR(secondLogicExpression.getLogicOperator())) {
                if(firstLogicExpression.containsInOR(secondLogicExpression.getLeftLogicElement())) {
                    return new LogicExpression(firstLogicExpression, LogicOperator.OR, secondLogicExpression.getRightLogicElement());
                }else if(firstLogicExpression.containsInOR(secondLogicExpression.getRightLogicElement())) {
                    return new LogicExpression(firstLogicExpression, LogicOperator.OR, secondLogicExpression.getLeftLogicElement());
                }else if(secondLogicExpression.containsInOR(firstLogicExpression.getLeftLogicElement())) {
                    return new LogicExpression(secondLogicExpression, LogicOperator.OR, firstLogicExpression.getRightLogicElement());
                }else if(secondLogicExpression.containsInOR(firstLogicExpression.getRightLogicElement())) {
                    return new LogicExpression(secondLogicExpression, LogicOperator.OR, firstLogicExpression.getLeftLogicElement());
                }
            }
        }
        return new LogicExpression(firstElement, logicOperator, secondElement);
    }

    public boolean containsBooleanElement() {
        return isLeftElementBooleanElement() || isRightElementBooleanElement();
    }

    public boolean isLeftElementBooleanElement() {
        return isElementBooleanElement(leftLogicElement);
    }

    public boolean isRightElementBooleanElement() {
        return isElementBooleanElement(rightLogicElement);
    }

    public boolean isElementBooleanElement(LogicElement logicElement) {
        return logicElement instanceof BooleanElement;
    }

}

