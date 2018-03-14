package com.raffaeleconforti.logic.solver;

import com.raffaeleconforti.logic.solver.builder.LogicElementBuilder;
import com.raffaeleconforti.logic.solver.elements.*;
import com.raffaeleconforti.logic.solver.exception.LogicElementValueNotAssigned;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by conforti on 6/08/15.
 */
public class DisjunctiveNormalFormBuilder {

    public static void main(String[] args) throws LogicElementValueNotAssigned {
//        String s = "( ( ( a OR b ) AND ( c OR d ) ) AND ( e OR f ) )";

//        String s = "( ( TRUE AND TRUE ) AND ( TRUE AND a ) )";
//        LogicElementBuilder dnf = new LogicElementBuilder();
//        LogicElement e = dnf.parseExpression(s);


        LogicElementBuilder dnf = new LogicElementBuilder();

        String s1 = "TRUE";
        LogicFunction s1f = new LogicFunction("s1()", dnf.parseExpression(s1));

        String s2 = "( ( s1() AND a ) OR ( s3() AND d ) )";
        LogicFunction s2f = new LogicFunction("s2()", dnf.parseExpression(s2));

        String s3 = "( s1() OR s2() )";
        LogicFunction s3f = new LogicFunction("s3()", dnf.parseExpression(s3));

        String s4 = "( ( s2() AND b ) OR ( s3() AND c ) )";
        LogicFunction s4f = new LogicFunction("s4()", dnf.parseExpression(s4));

        LogicSystem logicSystem = new LogicSystem(s1f, s2f, s3f, s4f);
        logicSystem.reduce();
        LogicElement e = logicSystem.transformToLogicExpression();

        System.out.println(e);
        LogicElement ex = convertToDNF(e);
        System.out.println(ex);
//        while (!ex.toString().equals(e.toString())) {
//            e = ex;
//            ex = convertToDNF(e);
//            ex = (LogicExpression) ex.reduce();
//            System.out.println(ex);
//        }
        System.out.println();
        System.out.println(removeParenthesys(ex.toString()));
    }

    public static LogicElement convertToDNF(LogicElement logicElement) throws LogicElementValueNotAssigned {
        logicElement = logicElement.reduce();
        if(logicElement instanceof LogicExpression) {
            LogicExpression logicExpression = (LogicExpression) logicElement;
            logicExpression.setLeftLogicElement(convertToDNF(logicExpression.getLeftLogicElement()).reduce());
            logicExpression.setRightLogicElement(convertToDNF(logicExpression.getRightLogicElement()).reduce());

            if (LogicOperator.isAND(logicExpression.getLogicOperator())) {
                if (logicExpression.getLeftLogicElement() instanceof LogicExpression && logicExpression.getRightLogicElement() instanceof LogicExpression) {
                    LogicExpression firstExpression = (LogicExpression ) logicExpression.getLeftLogicElement();
                    LogicExpression secondExpression = (LogicExpression ) logicExpression.getRightLogicElement();

                    if(LogicOperator.isOR(firstExpression.getLogicOperator()) || LogicOperator.isOR(secondExpression.getLogicOperator())) {
                        LogicExpression leftExpression = convertElement(firstExpression, secondExpression.getLeftLogicElement());
                        LogicExpression rightExpression = convertElement(firstExpression, secondExpression.getRightLogicElement());

                        LogicElement left = convertToDNF(leftExpression).reduce();
                        LogicElement right = convertToDNF(rightExpression).reduce();
                        return new LogicExpression(left, ((LogicExpression) logicExpression.getRightLogicElement()).getLogicOperator(), right);
                    }
                } else if (logicExpression.getLeftLogicElement() instanceof LogicExpression) {
                    return  convertElement((LogicExpression) logicExpression.getLeftLogicElement(), logicExpression.getRightLogicElement());
                } else if (logicExpression.getRightLogicElement() instanceof LogicExpression) {
                    return convertElement((LogicExpression) logicExpression.getRightLogicElement(), logicExpression.getLeftLogicElement());
                }
            }
            return logicExpression;
        }
        return logicElement;
    }

    private static LogicExpression convertElement(LogicExpression expression, LogicElement element) throws LogicElementValueNotAssigned {
        LogicExpression leftExpression = new LogicExpression(expression.getLeftLogicElement(), LogicOperator.AND, element);
        LogicExpression rightExpression = new LogicExpression(expression.getRightLogicElement(), LogicOperator.AND, element);

        LogicElement left = convertToDNF(leftExpression);
        LogicElement right = convertToDNF(rightExpression);
        return new LogicExpression(left, expression.getLogicOperator(), right);
    }

    public static String removeParenthesys(String result) {
        List<String> list = new ArrayList<String>();
        StringTokenizer stringTokenizer = new StringTokenizer(result, " ");

        boolean removeParenthesys = false;
        List<Integer> positions = new ArrayList<Integer>();
        while (stringTokenizer.hasMoreTokens()) {
            String token = stringTokenizer.nextToken();
            if(token.equals("(")) {
                list.add(token);
                positions.add(list.size() - 1);
            }else if(token.equals("AND") && list.get(list.size() - 1).equals(")") && !list.get(list.size() - 2).equals(")")) {
                int pos = positions.remove(positions.size() - 1);
                list.remove(list.size() - 1);
                list.remove(pos);
                list.add(token);
            }else if(token.equals("AND") && list.get(positions.get(positions.size() - 1) - 1).equals("AND")) {
                int pos = positions.remove(positions.size() - 1);
                list.remove(pos);
                list.add(token);
                removeParenthesys = true;
            }else if(token.equals("OR") && list.get(list.size() - 1).equals(")")) {
                positions.remove(positions.size() - 1);
                list.add(token);
            }else if(token.equals(")") && removeParenthesys) {
                removeParenthesys = false;
            }else {
                list.add(token);
            }
        }

        String s = list.toString().replace(",", "").replace("[", "").replace("]", "");

        if(result.equals(s)) return s;
        else return removeParenthesys(s);
    }
}
