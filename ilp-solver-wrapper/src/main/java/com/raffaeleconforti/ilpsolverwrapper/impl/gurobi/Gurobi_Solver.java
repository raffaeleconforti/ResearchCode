package com.raffaeleconforti.ilpsolverwrapper.impl.gurobi;

import com.raffaeleconforti.ilpsolverwrapper.ILPSolver;
import com.raffaeleconforti.ilpsolverwrapper.ILPSolverConstraint;
import com.raffaeleconforti.ilpsolverwrapper.ILPSolverExpression;
import com.raffaeleconforti.ilpsolverwrapper.ILPSolverVariable;
import gurobi.*;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/4/17.
 */
public class Gurobi_Solver implements ILPSolver {

    private GRBEnv env;
    private GRBModel model;
    private boolean minimize = true;

    @Override
    public double getInfinity() {
        return GRB.INFINITY;
    }

    @Override
    public void createModel() {
        try {
            env = new GRBEnv("qp.noisefiltering");
            model = new GRBModel(env);
            model.getEnv().set(GRB.IntParam.LogToConsole, 0);
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ILPSolverVariable addVariable(double lowerBound, double upperBound, double objectiveCoefficient, VariableType variableType, String variableName) {
        try {
            char gurobiVariableType = GRB.CONTINUOUS;
            switch (variableType) {
                case BINARY     : gurobiVariableType = GRB.BINARY;
                                  break;
                case INTEGER    : gurobiVariableType = GRB.INTEGER;
                                  break;
                case CONTINUOUS : gurobiVariableType = GRB.CONTINUOUS;
            }

            return new Gurobi_Variable(model.addVar(lowerBound, upperBound, objectiveCoefficient, gurobiVariableType, variableName));
        } catch (GRBException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ILPSolverExpression createExpression() {
        return new Gurobi_Expression(new GRBLinExpr());
    }

    @Override
    public ILPSolverConstraint addConstraint(ILPSolverExpression expression, Operator operator, double coefficient, String constraintName) {
        try {
            char gurobiOperator = GRB.EQUAL;
            switch (operator) {
                case LESS_EQUAL     : gurobiOperator = GRB.LESS_EQUAL;
                    break;
                case EQUAL          : gurobiOperator = GRB.EQUAL;
                    break;
                case GREATER_EQUAL  : gurobiOperator = GRB.GREATER_EQUAL;
            }

            return new Gurobi_Constraint(model.addConstr(((Gurobi_Expression) expression).getLinearExpression(), gurobiOperator, coefficient, constraintName));
        } catch (GRBException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setObjectiveFunction(ILPSolverExpression objectiveFunction) {
        try {
            model.setObjective(((Gurobi_Expression) objectiveFunction).getLinearExpression(), minimize?GRB.MINIMIZE:GRB.MAXIMIZE);
        } catch (GRBException e) {
            e.printStackTrace();
        }
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
            model.update();
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void solve() {
        try {
            model.optimize();
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public double[] getSolutionVariables(ILPSolverVariable[] variables) {
        try {
            double[] solutions = new double[variables.length];
            for(int i = 0; i < variables.length; i++) {
                solutions[i] = ((Gurobi_Variable) variables[i]).getVariable().get(GRB.DoubleAttr.X);
            }
            return solutions;
        } catch (GRBException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public double getSolutionValue() {
        try {
            return model.get(GRB.DoubleAttr.ObjVal);
        } catch (GRBException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public Status getStatus() {
        try {
            int status = model.get(GRB.IntAttr.Status);
            if(status == GRB.OPTIMAL) return Status.OPTIMAL;
            else if(status == GRB.INFEASIBLE) return Status.INFEASIBLE;
            else if(status == GRB.UNBOUNDED) return Status.UNBOUNDED;
            else return Status.ERROR;
        } catch (GRBException e) {
            e.printStackTrace();
        }
        return Status.ERROR;
    }

    @Override
    public void dispose() {
        try {
            model.dispose();
            env.dispose();
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

}
