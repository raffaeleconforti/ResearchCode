package com.raffaeleconforti.ilpsolverwrapper.impl.lpsolve;

import com.raffaeleconforti.ilpsolverwrapper.ILPSolver;
import com.raffaeleconforti.ilpsolverwrapper.ILPSolverConstraint;
import com.raffaeleconforti.ilpsolverwrapper.ILPSolverExpression;
import com.raffaeleconforti.ilpsolverwrapper.ILPSolverVariable;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/4/17.
 */
public class LPSolve_Solver implements ILPSolver {

    private LpSolve lp;
    private List<LPSolve_Variable> variables = new ArrayList<>();
    private List<LPSolve_Constraint> constraints = new ArrayList<>();
    private LPSolve_Constraint objectiveFunction;
    private boolean minimize = true;
    private int status;

    @Override
    public double getInfinity() {
        return Double.MAX_VALUE;
    }

    @Override
    public void createModel() {

    }

    @Override
    public ILPSolverVariable addVariable(double lowerBound, double upperBound, double objectiveCoefficient, VariableType variableType, String variableName) {
        LPSolve_Variable variable = new LPSolve_Variable(variableType, variableName, variables.size());
        variables.add(variable);
        return variable;
    }

    @Override
    public ILPSolverExpression createExpression() {
        return new LPSolve_Expression();
    }

    @Override
    public ILPSolverConstraint addConstraint(ILPSolverExpression expression, Operator operator, double coefficient, String constraintName) {
        int lpSolveOperator = LpSolve.EQ;
        switch (operator) {
            case LESS_EQUAL     : lpSolveOperator = LpSolve.LE;
                break;
            case EQUAL          : lpSolveOperator = LpSolve.EQ;
                break;
            case GREATER_EQUAL  : lpSolveOperator = LpSolve.GE;
        }

        int[] colno = new int[variables.size()];
        double[] row = new double[variables.size()];

        List<ILPSolverVariable> variables = ((LPSolve_Expression) expression).getVariables();
        List<Double> coefficients = ((LPSolve_Expression) expression).getCoefficients();

        for(int i = 0; i < variables.size(); i++) {
            LPSolve_Variable variable = (LPSolve_Variable) variables.get(i);
            double variable_coefficient = coefficients.get(i);

            colno[variable.getVariablePosition()] = variable.getVariablePosition() + 1;
            row[variable.getVariablePosition()] = variable_coefficient;
        }

        LPSolve_Constraint lpSolveConstraint = new LPSolve_Constraint(variables.size(), row, colno, lpSolveOperator, coefficient);
        constraints.add(lpSolveConstraint);
        return lpSolveConstraint;
    }

    @Override
    public void setObjectiveFunction(ILPSolverExpression objectiveFunction) {
        int[] colno = new int[variables.size()];
        double[] row = new double[variables.size()];

        List<ILPSolverVariable> variables = ((LPSolve_Expression) objectiveFunction).getVariables();
        List<Double> coefficients = ((LPSolve_Expression) objectiveFunction).getCoefficients();

        for(int i = 0; i < variables.size(); i++) {
            LPSolve_Variable variable = (LPSolve_Variable) variables.get(i);
            double variable_coefficient = coefficients.get(i);

            colno[variable.getVariablePosition()] = variable.getVariablePosition() + 1;
            row[variable.getVariablePosition()] = variable_coefficient;
        }

        this.objectiveFunction = new LPSolve_Constraint(variables.size(), row, colno, LpSolve.EQ, 0.0);
    }

    @Override
    public void setMaximize() {
        minimize = false;
    }

    @Override
    public void setMinimize() {
        minimize = true;
    }

    @Override
    public void integrateVariables() {
        try {
            lp = LpSolve.makeLp(0, variables.size());

            for(LPSolve_Variable variable : variables) {
                lp.setColName(variable.getVariablePosition() + 1, variable.getVariableName());
                if(variable.getVariableType() == VariableType.INTEGER) {
                    lp.setInt(variable.getVariablePosition() + 1, true);
                }else if(variable.getVariableType() == VariableType.BINARY) {
                    lp.setBinary(variable.getVariablePosition() + 1, true);
                }
            }

        } catch (LpSolveException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void solve() {
        try {
            lp.setObjFnex(objectiveFunction.getSize(), objectiveFunction.getRow(), objectiveFunction.getColno());
            if(minimize) {
                lp.setMinim();
            }else {
                lp.setMaxim();
            }

            lp.resizeLp(constraints.size(), lp.getNcolumns());
            lp.setAddRowmode(true);
            for(LPSolve_Constraint constraint : constraints) {
                lp.addConstraintex(constraint.getSize(), constraint.getRow(), constraint.getColno(), constraint.getLpSolveOperator(), constraint.getCoefficient());
            }

            lp.setAddRowmode(false);

            lp.setVerbose(LpSolve.IMPORTANT);
            status = lp.solve();
        } catch (LpSolveException e) {
            e.printStackTrace();
        }
    }

    @Override
    public double[] getSolutionVariables(ILPSolverVariable[] variables) {
        try {
            double[] row = new double[this.variables.size()];
            lp.getVariables(row);

            double[] res = new double[variables.length];
            for(int i = 0; i < variables.length; i++) {
                LPSolve_Variable variable = (LPSolve_Variable) variables[i];
                res[i] = row[variable.getVariablePosition()];
            }
            return res;
        } catch (LpSolveException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public double getSolutionValue() {
        try {
            return lp.getObjective();
        } catch (LpSolveException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public Status getStatus() {
        if(status == LpSolve.OPTIMAL) return Status.OPTIMAL;
        else if(status == LpSolve.INFEASIBLE) return Status.INFEASIBLE;
        else if(status == LpSolve.UNBOUNDED) return Status.UNBOUNDED;
        else return Status.ERROR;
    }

    @Override
    public void dispose() {
        lp.deleteLp();
    }

}
