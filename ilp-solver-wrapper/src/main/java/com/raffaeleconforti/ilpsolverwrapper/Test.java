package com.raffaeleconforti.ilpsolverwrapper;

import com.raffaeleconforti.ilpsolverwrapper.impl.gurobi.Gurobi_Solver;
import com.raffaeleconforti.ilpsolverwrapper.impl.lpsolve.LPSolve_Solver;

import java.util.Arrays;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/4/17.
 */
public class Test {

    public static void main(String[] args) {
        ILPSolver solver = new Gurobi_Solver();
        solver.createModel();
        ILPSolverVariable x = solver.addVariable(Double.MIN_VALUE, Double.MAX_VALUE, 1, ILPSolver.VariableType.CONTINUOUS, "x");
        ILPSolverVariable y = solver.addVariable(Double.MIN_VALUE, Double.MAX_VALUE, 1, ILPSolver.VariableType.CONTINUOUS, "y");

        solver.integrateVariables();

        ILPSolverExpression objectiveFunction = solver.createExpression();
        objectiveFunction.addTerm(x, -143);
        objectiveFunction.addTerm(y, -60);

        solver.setObjectiveFunction(objectiveFunction);

        ILPSolverExpression expression1 = solver.createExpression();
        expression1.addTerm(x, 120);
        expression1.addTerm(y, 210);
        ILPSolverConstraint constraint1 = solver.addConstraint(expression1, ILPSolver.Operator.LESS_EQUAL, 15000, "");

        ILPSolverExpression expression2 = solver.createExpression();
        expression2.addTerm(x, 110);
        expression2.addTerm(y, 30);
        ILPSolverConstraint constraint2 = solver.addConstraint(expression2, ILPSolver.Operator.LESS_EQUAL, 4000, "");

        ILPSolverExpression expression3 = solver.createExpression();
        expression3.addTerm(x, 1);
        expression3.addTerm(y, 1);
        ILPSolverConstraint constraint3 = solver.addConstraint(expression3, ILPSolver.Operator.LESS_EQUAL, 75, "");

        solver.solve();
        System.out.println(solver.getSolutionValue());
        System.out.println(Arrays.toString(solver.getSolutionVariables(new ILPSolverVariable[] {y})));
    }

}
