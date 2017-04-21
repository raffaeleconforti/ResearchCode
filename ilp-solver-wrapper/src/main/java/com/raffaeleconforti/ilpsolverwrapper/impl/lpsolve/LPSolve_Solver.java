package com.raffaeleconforti.ilpsolverwrapper.impl.lpsolve;

import com.raffaeleconforti.ilpsolverwrapper.ILPSolver;
import com.raffaeleconforti.ilpsolverwrapper.ILPSolverConstraint;
import com.raffaeleconforti.ilpsolverwrapper.ILPSolverExpression;
import com.raffaeleconforti.ilpsolverwrapper.ILPSolverVariable;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/4/17.
 */
public class LPSolve_Solver implements ILPSolver {


    public static final double INFINITY = 1.0E25D;//1.0E100D;

    private LpSolve lp;
    private List<LPSolve_Variable> variables = new ArrayList<>();
    private List<LPSolve_Constraint> constraints = new ArrayList<>();
    private LPSolve_Constraint objectiveFunction;
    private boolean minimize = true;
    private int status;
    private String problem;

    @Override
    public double getInfinity() {
        return INFINITY;
    }

    @Override
    public void createModel() {

    }

    @Override
    public ILPSolverVariable addVariable(double lowerBound, double upperBound, double objectiveCoefficient, VariableType variableType, String variableName) {
        LPSolve_Variable variable = new LPSolve_Variable(variables.size(), lowerBound, upperBound, variableType, variableName);
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

        List<ILPSolverVariable> expression_variables = ((LPSolve_Expression) expression).getVariables();
        List<Double> expression_coefficients = ((LPSolve_Expression) expression).getCoefficients();

        for(int i = 0; i < expression_variables.size(); i++) {
            LPSolve_Variable variable = (LPSolve_Variable) expression_variables.get(i);
            double variable_coefficient = expression_coefficients.get(i);

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

        List<ILPSolverVariable> function_variables = ((LPSolve_Expression) objectiveFunction).getVariables();
        List<Double> function_coefficients = ((LPSolve_Expression) objectiveFunction).getCoefficients();

        for(int i = 0; i < function_variables.size(); i++) {
            LPSolve_Variable variable = (LPSolve_Variable) function_variables.get(i);
            double variable_coefficient = function_coefficients.get(i);

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
                if(variable.getVariableType() == VariableType.INTEGER || variable.getVariableType() == VariableType.BINARY) {
                    lp.setInt(variable.getVariablePosition() + 1, true);
//                }else if(variable.getVariableType() == VariableType.BINARY) {
//                    lp.setBinary(variable.getVariablePosition() + 1, true);
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

//            lp.resizeLp(constraints.size(), lp.getNcolumns());
            lp.setAddRowmode(true);
            for(LPSolve_Variable variable : variables) {
                int[] colno = new int[variables.size()];
                double[] row = new double[variables.size()];
                colno[variable.getVariablePosition()] = variable.getVariablePosition() + 1;
                row[variable.getVariablePosition()] = 1;
                double diff = (variable.getVariableType() == VariableType.CONTINUOUS)?Double.MIN_VALUE:1;

                if(variable.getLowerBound() != -getInfinity()) {
//                    lp.addConstraintex(variables.size(), row, colno, LpSolve.GE, variable.getLowerBound() + diff);
                    lp.addConstraintex(variables.size(), row, colno, LpSolve.GE, variable.getLowerBound());
                }
                if(variable.getUpperBound() != getInfinity()) {
//                    lp.addConstraintex(variables.size(), row, colno, LpSolve.LE, variable.getUpperBound() - diff);
                    lp.addConstraintex(variables.size(), row, colno, LpSolve.LE, variable.getUpperBound());
                }
            }

            for(LPSolve_Constraint constraint : constraints) {
                lp.addConstraintex(constraint.getSize(), constraint.getRow(), constraint.getColno(), constraint.getLpSolveOperator(), constraint.getCoefficient());
            }

            lp.setAddRowmode(false);
            problem = saveProblem();

            lp.setVerbose(LpSolve.DETAILED);
//            lp.defaultBasis();
            lp.setSimplextype(LpSolve.SIMPLEX_DUAL_DUAL);
//            lp.
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
    public String printProblem() {
        return problem;
    }

    private String saveProblem() {
        try {
            String file = "problem.lp";
            lp.writeLp(file);
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            int count = 0;
            boolean constrain = false;
            boolean print = true;
            while(line != null) {
                if(line.equalsIgnoreCase("/* Objective function */")) {
                    line = br.readLine();
                    if (line.startsWith("min:")) {
                        sb.append("Minimize\n").append(standardize(line.replaceAll("min:", " "), count, constrain)).append("\n");
                    } else {
                        sb.append("Maximize\n").append(standardize(line.replaceAll("max:", " "), count, constrain)).append("\n");
                    }
                }else if(line.equalsIgnoreCase("/* Constraints */")) {
                    sb.append("\nSubject To\n");
                    constrain = true;
                    print = true;
                }else if(line.equalsIgnoreCase("/* Variable bounds */")) {
                    print = false;
                }else if(line.equalsIgnoreCase("/* Integer definitions */")) {
                    sb.append("\nGenerals\n");
                    constrain = false;
                    line = br.readLine();
                    line = line.substring(3);
                    line = standardize(line, count, constrain);
                    sb.append(line.replaceAll(",", " ")).append("\n");
                }else if(line.isEmpty()) {

                }else {
                    if(print) sb.append(standardize(line, count, constrain));
                    if(constrain && !line.startsWith(" ")) count++;
                }
                line = br.readLine();
            }
            sb.append("End\n");
            return sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LpSolveException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String standardize(String line, int count, boolean constrain) {
        line = line.replaceAll("-", "- ");
        line = line.replaceAll("[+]", "+ ");
        line = line.replaceAll("1e[+] ", "1e+");
        line = line.replaceAll(";", "");
        if(line.startsWith("R")) line = line.substring(line.indexOf(":") + 2);
        if(constrain) {
//            System.out.println(line);
            if(line.startsWith("+")) line = line.substring(2);
            if(!line.startsWith(" ")) line = "\n R" + count + ": " + line;
        }
        return line;
    }

    @Override
    public void dispose() {
        lp.deleteLp();
    }

}
