package com.raffaeleconforti.ilpsolverwrapper.impl.lpsolve;

import com.raffaeleconforti.ilpsolverwrapper.ILPSolver.VariableType;
import com.raffaeleconforti.ilpsolverwrapper.ILPSolverVariable;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/4/17.
 */
public class LPSolve_Variable implements ILPSolverVariable {

    private VariableType variableType;
    private String variableName;
    private int variablePosition;

    public LPSolve_Variable(VariableType variableType, String variableName, int variablePosition) {
        this.variableType = variableType;
        this.variableName = variableName;
        this.variablePosition = variablePosition;
    }

    public VariableType getVariableType() {
        return variableType;
    }

    public String getVariableName() {
        return variableName;
    }

    public int getVariablePosition() {
        return variablePosition;
    }

}
