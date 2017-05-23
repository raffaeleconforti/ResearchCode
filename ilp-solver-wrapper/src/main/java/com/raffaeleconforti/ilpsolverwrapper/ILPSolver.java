package com.raffaeleconforti.ilpsolverwrapper;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/4/17.
 */
public interface ILPSolver {

    enum VariableType {CONTINUOUS, BINARY, INTEGER}
    enum Operator {LESS_EQUAL, EQUAL, GREATER_EQUAL}
    enum Status {OPTIMAL, INFEASIBLE, UNBOUNDED, ERROR}

    double getInfinity();
    void setAlwaysFeasible(boolean isAlwaysFeasible);
    void createModel();
    ILPSolverVariable addVariable(double lowerBound, double upperBound, double objectiveCoefficient, VariableType variableType, String variableName);
    ILPSolverExpression createExpression();
    ILPSolverConstraint addConstraint(ILPSolverExpression expression, Operator operator, double coefficient, String constraintName);
    void setObjectiveFunction(ILPSolverExpression objectiveFunction);
    void setMaximize();
    void setMinimize();
    void integrateVariables();
    void solve();
    double[] getSolutionVariables(ILPSolverVariable[] variables);
    double getSolutionValue();
    Status getStatus();
    String printProblem();
    void dispose();
}
