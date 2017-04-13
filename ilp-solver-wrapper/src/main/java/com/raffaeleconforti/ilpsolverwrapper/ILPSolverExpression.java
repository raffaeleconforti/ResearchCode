package com.raffaeleconforti.ilpsolverwrapper;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/4/17.
 */
public interface ILPSolverExpression {

    void addTerm(ILPSolverVariable variable, double coefficient);

}
