package com.raffaeleconforti.ilpsolverwrapper.impl.gurobi;

import com.raffaeleconforti.ilpsolverwrapper.ILPSolverExpression;
import com.raffaeleconforti.ilpsolverwrapper.ILPSolverVariable;
import gurobi.GRBLinExpr;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/4/17.
 */
public class Gurobi_Expression implements ILPSolverExpression {

    private GRBLinExpr linearExpression;

    public Gurobi_Expression(GRBLinExpr linearExpression) {
        this.linearExpression = linearExpression;
    }

    public GRBLinExpr getLinearExpression() {
        return linearExpression;
    }

    @Override
    public void addTerm(ILPSolverVariable variable, double coefficient) {
        linearExpression.addTerm(coefficient, ((Gurobi_Variable) variable).getVariable());
    }

}
