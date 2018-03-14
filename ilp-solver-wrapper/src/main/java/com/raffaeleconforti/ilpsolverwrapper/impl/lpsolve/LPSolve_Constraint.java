package com.raffaeleconforti.ilpsolverwrapper.impl.lpsolve;

import com.raffaeleconforti.ilpsolverwrapper.ILPSolverConstraint;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/4/17.
 */
public class LPSolve_Constraint implements ILPSolverConstraint {

    private int size;
    private double[] row;
    private int[] colno;
    private int lpSolveOperator;
    private double coefficient;

    public LPSolve_Constraint(int size, double[] row, int[] colno, int lpSolveOperator, double coefficient) {
        this.size = size;
        this.row = row;
        this.colno = colno;
        this.lpSolveOperator = lpSolveOperator;
        this.coefficient = coefficient;
    }


    public int getSize() {
        return size;
    }

    public double[] getRow() {
        return row;
    }

    public int[] getColno() {
        return colno;
    }

    public int getLpSolveOperator() {
        return lpSolveOperator;
    }

    public double getCoefficient() {
        return coefficient;
    }

}
