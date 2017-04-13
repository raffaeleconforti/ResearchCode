package com.raffaeleconforti.ilpsolverwrapper.impl.lpsolve;

import com.raffaeleconforti.ilpsolverwrapper.ILPSolverExpression;
import com.raffaeleconforti.ilpsolverwrapper.ILPSolverVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/4/17.
 */
public class LPSolve_Expression implements ILPSolverExpression {

    private List<ILPSolverVariable> variables = new ArrayList<>();
    private List<Double> coefficients = new ArrayList<>();

    @Override
    public void addTerm(ILPSolverVariable variable, double coefficient) {
        variables.add(variable);
        coefficients.add(coefficient);
    }

    public List<ILPSolverVariable> getVariables() {
        return variables;
    }

    public List<Double> getCoefficients() {
        return coefficients;
    }

}
