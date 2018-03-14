package com.raffaeleconforti.dsm.dsmcells;

import com.raffaeleconforti.dsm.DesignStructureMatrixCell;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 1/04/2016.
 */
public abstract class DesignStructureMatrixCellAbstract implements DesignStructureMatrixCell {

    @Override
    public String toString() {
        return getType();
    }

    @Override
    public int hashCode() {
        return getType().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof DesignStructureMatrixCell)) return false;
        DesignStructureMatrixCell dsmc = (DesignStructureMatrixCell) o;
        return getType().equals(dsmc.getType());
    }

}
