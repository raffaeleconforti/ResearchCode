package com.raffaeleconforti.ilpsolverwrapper.impl.gurobi;

import com.raffaeleconforti.ilpsolverwrapper.ILPSolverVariable;
import gurobi.GRBVar;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/4/17.
 */
public class Gurobi_Variable implements ILPSolverVariable {

    private GRBVar variable;

    public Gurobi_Variable(GRBVar variable) {
        this.variable = variable;
    }

    public GRBVar getVariable() {
        return variable;
    }

}
