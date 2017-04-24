package com.raffaeleconforti.ilpsolverwrapper;

import com.raffaeleconforti.ilpsolverwrapper.ILPSolver.VariableType;
/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/4/17.
 */
public interface ILPSolverVariable {

    double getLowerBound();
    double getUpperBound();
    VariableType getVariableType();
    String getVariableName();
}
