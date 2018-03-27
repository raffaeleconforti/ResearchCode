package com.raffaeleconforti.logic.solver;

import com.raffaeleconforti.logic.solver.builder.LogicElementBuilder;
import com.raffaeleconforti.logic.solver.elements.LogicElement;
import com.raffaeleconforti.logic.solver.elements.LogicFunction;
import com.raffaeleconforti.logic.solver.elements.LogicSystem;
import com.raffaeleconforti.logic.solver.exception.LogicElementValueNotAssigned;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.Set;
import java.util.StringTokenizer;

/**
 * Created by conforti on 9/08/15.
 */
public class LogicSolver {

    public static void main(String[] args) throws LogicElementValueNotAssigned {

        LogicElementBuilder leb = new LogicElementBuilder();

        String s1 = "TRUE";
        LogicFunction s1f = new LogicFunction("s1()", leb.parseExpression(s1));

        String s2 = "( ( s1() AND a ) OR ( s3() AND d ) )";
        LogicFunction s2f = new LogicFunction("s2()", leb.parseExpression(s2));

        String s3 = "( s1() OR s2() )";
        LogicFunction s3f = new LogicFunction("s3()", leb.parseExpression(s3));

        String s4 = "( ( ( s2() AND b ) AND f ) OR ( s3() AND c ) )";
        LogicFunction s4f = new LogicFunction("s4()", leb.parseExpression(s4));

        LogicSystem logicSystem = new LogicSystem(s1f, s2f, s3f, s4f);
        logicSystem.reduce();
        LogicElement e = logicSystem.transformToLogicExpression();

        solve(e);
    }

    public static LogicElement solve(LogicElement logicElement) throws LogicElementValueNotAssigned {
        LogicElement dnfLogicElement = DisjunctiveNormalFormBuilder.convertToDNF(logicElement);
        System.out.println(dnfLogicElement);
        System.out.println(DisjunctiveNormalFormBuilder.removeParenthesys(dnfLogicElement.toString()));
        System.out.println(idenditySmallestConjunction(DisjunctiveNormalFormBuilder.removeParenthesys(dnfLogicElement.toString())));
        return logicElement;
    }

    private static String idenditySmallestConjunction(String expression) {
        StringTokenizer stringTokenizer = new StringTokenizer(expression, " ");

        Set<String> set = null;
        Set<String> smallest = null;

        while (stringTokenizer.hasMoreTokens()) {
            String token = stringTokenizer.nextToken();
            if(token.equals("(")) {
                set = new UnifiedSet<String>();
            }else if(token.equals(")")) {
                if(checkIfSmallerList(smallest, set)) {
                    smallest = set;
                }
            }else {
                if(!token.equals("AND") && !token.equals("OR")) {
                    set.add(token);
                }
            }
        }

        return smallest.toString();
    }

    private static boolean checkIfSmallerList(Set<String> smallest, Set<String> set) {
        return smallest == null || smallest.size() > set.size();
    }

}
