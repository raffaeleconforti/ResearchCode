package com.raffaeleconforti.ilpsolverwrapper.impl.gurobi;

import com.raffaeleconforti.ilpsolverwrapper.ILPSolver.VariableType;
import com.raffaeleconforti.ilpsolverwrapper.ILPSolverVariable;
import gurobi.GRBVar;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/4/17.
 */
public class Gurobi_Variable implements ILPSolverVariable {

    private GRBVar variable;
    private double lowerBound;
    private double upperBound;
    private VariableType variableType;
    private String variableName;

    public Gurobi_Variable(GRBVar variable,
                           double lowerBound,
                           double upperBound,
                           VariableType variableType,
                           String variableName) {
        this.variable = variable;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.variableType = variableType;
        this.variableName = variableName;
    }

    public GRBVar getVariable() {
        return variable;
    }

    @Override
    public double getLowerBound() {
        return lowerBound;
    }

    @Override
    public double getUpperBound() {
        return upperBound;
    }

    @Override
    public VariableType getVariableType() {
        return variableType;
    }

    @Override
    public String getVariableName() {
        return variableName;
    }

}
