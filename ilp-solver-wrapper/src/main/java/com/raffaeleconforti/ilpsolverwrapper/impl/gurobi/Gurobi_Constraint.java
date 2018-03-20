package com.raffaeleconforti.ilpsolverwrapper.impl.gurobi;

import com.raffaeleconforti.ilpsolverwrapper.ILPSolverConstraint;
import gurobi.GRBConstr;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/4/17.
 */
public class Gurobi_Constraint implements ILPSolverConstraint {

    private GRBConstr constraint;

    public Gurobi_Constraint(GRBConstr constraint) {
        this.constraint = constraint;
    }

    public GRBConstr getConstraint() {
        return constraint;
    }
}
