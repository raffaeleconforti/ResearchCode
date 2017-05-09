package com.raffaeleconforti.ilpsolverwrapper;

import com.raffaeleconforti.ilpsolverwrapper.impl.gurobi.Gurobi_Solver;
import com.raffaeleconforti.ilpsolverwrapper.impl.lpsolve.LPSolve_Solver;

import java.util.Arrays;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/4/17.
 */
public class Test {

    public static void main(String[] args) {
//        test(new Gurobi_Solver());
        test(new LPSolve_Solver());
    }

    private static void test(ILPSolver solver) {
        solver.createModel();
        ILPSolverVariable x = solver.addVariable(1, solver.getInfinity(), 1, ILPSolver.VariableType.INTEGER, "x");
        ILPSolverVariable y = solver.addVariable(0, 10, 1, ILPSolver.VariableType.INTEGER, "y");

        int total = 50;
        ILPSolverVariable[] ys = new ILPSolverVariable[total];
        for(int i = 0; i < total; i++) {
            ys[i] = solver.addVariable(0, 1, 1, ILPSolver.VariableType.BINARY, "y"+i);
        }

        solver.integrateVariables();

        ILPSolverExpression objectiveFunction = solver.createExpression();
        objectiveFunction.addTerm(x, -143);
        objectiveFunction.addTerm(y, -60);
        for(int i = 0; i < total; i++) {
            objectiveFunction.addTerm(ys[i], -1);
        }

        solver.setObjectiveFunction(objectiveFunction);

        ILPSolverExpression expression1 = solver.createExpression();
        expression1.addTerm(x, 120);
        expression1.addTerm(y, 210);
        ILPSolverConstraint constraint1 = solver.addConstraint(expression1, ILPSolver.Operator.LESS_EQUAL, 15000, "");

        ILPSolverExpression expression2 = solver.createExpression();
        expression2.addTerm(x, 110);
        expression2.addTerm(y, 30);
        ILPSolverConstraint constraint2 = solver.addConstraint(expression2, ILPSolver.Operator.EQUAL, 4000, "");

        ILPSolverExpression expression3 = solver.createExpression();
        expression3.addTerm(x, solver.getInfinity());
        expression3.addTerm(y, 1);
        ILPSolverConstraint constraint3 = solver.addConstraint(expression3, ILPSolver.Operator.LESS_EQUAL, 75, "");

        for(int i = 0; i < total - 1; i++) {
            ILPSolverExpression expression = solver.createExpression();
            expression.addTerm(ys[i], -1);
            expression.addTerm(ys[i + 1], 1);
            solver.addConstraint(expression, ILPSolver.Operator.LESS_EQUAL, 75, "");
        }

        solver.solve();
        System.out.println(solver.printProblem());
        System.out.println(solver.getStatus());
        System.out.println(solver.getSolutionValue());
        System.out.println(Arrays.toString(solver.getSolutionVariables(new ILPSolverVariable[] {y})));
    }

}
