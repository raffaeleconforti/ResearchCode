package com.raffaeleconforti.logic.solver;

import com.raffaeleconforti.logic.solver.elements.*;
import com.raffaeleconforti.logic.solver.exception.LogicElementValueNotAssigned;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by conforti on 6/08/15.
 */
public class ConjunctiveNormalFormBuilder {

    public static void main(String[] args) throws LogicElementValueNotAssigned {
//        String s = "( ( ( a OR b ) AND ( c OR d ) ) AND ( e OR f ) )";

//        String s = "( ( ( a OR ( b AND a ) ) OR ( b AND a ) ) OR ( ( ( a AND b ) AND ( a OR ( b AND a ) ) ) OR ( a AND b ) ) )";
//        LogicElementBuilder dnf = new LogicElementBuilder(s);
//        LogicElement e = dnf.parseExpression();

        LogicExpression expression = new LogicExpression(new LogicFunctionBookmark("g()"), LogicOperator.AND, new AtomicElement("a"));
        LogicExpression expression1 = new LogicExpression(expression, LogicOperator.OR, new AtomicElement("b"));
        LogicFunction function = new LogicFunction("f()", expression1);

        LogicExpression expression2 = new LogicExpression(new LogicFunctionBookmark("d()"), LogicOperator.AND, new AtomicElement("a"));
        LogicExpression expression3 = new LogicExpression(expression2, LogicOperator.OR, new AtomicElement("b"));
        LogicFunction function2 = new LogicFunction("g()", expression3);

        LogicExpression expression4 = new LogicExpression(new LogicFunctionBookmark("f()"), LogicOperator.AND, new AtomicElement("a"));
        LogicExpression expression5 = new LogicExpression(expression4, LogicOperator.OR, new AtomicElement("b"));
        LogicFunction function3 = new LogicFunction("d()", expression5);

        LogicSystem logicSystem = new LogicSystem(function, function2, function3);
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

    private static LogicElement convertToDNF(LogicElement logicElement) throws LogicElementValueNotAssigned {
        if(logicElement instanceof LogicExpression) {
            LogicExpression logicExpression = (LogicExpression) logicElement;
            logicExpression.setLeftLogicElement(convertToDNF(logicExpression.getLeftLogicElement()).reduce());
            logicExpression.setRightLogicElement(convertToDNF(logicExpression.getRightLogicElement()).reduce());

            if (LogicOperator.isOR(logicExpression.getLogicOperator())) {
                if (logicExpression.getLeftLogicElement() instanceof LogicExpression && logicExpression.getRightLogicElement() instanceof LogicExpression) {
                    LogicExpression leftExpression = convertElement((LogicExpression) logicExpression.getLeftLogicElement(), ((LogicExpression) logicExpression.getRightLogicElement()).getLeftLogicElement());
                    LogicExpression rightExpression = convertElement((LogicExpression) logicExpression.getLeftLogicElement(), ((LogicExpression) logicExpression.getRightLogicElement()).getRightLogicElement());

                    LogicElement left = convertToDNF(leftExpression).reduce();
                    LogicElement right = convertToDNF(rightExpression).reduce();
                    return  new LogicExpression(left, ((LogicExpression) logicExpression.getRightLogicElement()).getLogicOperator(), right);
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
        LogicExpression leftExpression = new LogicExpression(expression.getLeftLogicElement(), LogicOperator.OR, element);
        LogicExpression rightExpression = new LogicExpression(expression.getRightLogicElement(), LogicOperator.OR, element);

        LogicElement left = convertToDNF(leftExpression);
        LogicElement right = convertToDNF(rightExpression);
        return new LogicExpression(left, expression.getLogicOperator(), right);
    }

    private static String removeParenthesys(String result) {
        List<String> list = new ArrayList<String>();
        StringTokenizer stringTokenizer = new StringTokenizer(result, " ");

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
            }else if(token.equals("OR") && list.get(list.size() - 1).equals(")")) {
                positions.remove(positions.size() - 1);
                list.add(token);
            }else {
                list.add(token);
            }
        }

        return list.toString().replace(",", "").replace("[", "").replace("]", "");
//        String s = list.toString().replace(",", "").replace("[", "").replace("]", "");

//        stringTokenizer = new StringTokenizer(s, " ");
//
//        Set<String> set = new UnifiedSet<String>();
//
//        while (stringTokenizer.hasMoreTokens()) {
//            String token = stringTokenizer.nextToken();
//            if(token.equals("(")) {
//                list.add(token);
//                positions.add(list.size() - 1);
//            }else if(token.equals("AND") && list.get(list.size() - 1).equals(")") && !list.get(list.size() - 2).equals(")")) {
//                int pos = positions.remove(positions.size() - 1);
//                list.remove(list.size() - 1);
//                list.remove(pos);
//                list.add(token);
//            }else if(token.equals("OR") && list.get(list.size() - 1).equals(")")) {
//                positions.remove(positions.size() - 1);
//                list.add(token);
//            }else {
//                list.add(token);
//            }
//        }

    }
}
