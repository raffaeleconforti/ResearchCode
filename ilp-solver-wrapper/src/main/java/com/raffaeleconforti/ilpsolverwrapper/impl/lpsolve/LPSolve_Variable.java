package com.raffaeleconforti.ilpsolverwrapper.impl.lpsolve;

import com.raffaeleconforti.ilpsolverwrapper.ILPSolver.VariableType;
import com.raffaeleconforti.ilpsolverwrapper.ILPSolverVariable;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/4/17.
 */
public class LPSolve_Variable implements ILPSolverVariable {

    private double lowerBound;
    private double upperBound;
    private VariableType variableType;
    private String variableName;
    private int variablePosition;

    public LPSolve_Variable(int variablePosition,
                            double lowerBound,
                            double upperBound,
                            VariableType variableType,
                            String variableName) {
        this.variablePosition = variablePosition;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.variableType = variableType;
        this.variableName = variableName;
    }

    public int getVariablePosition() {
        return variablePosition;
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
