package com.raffaeleconforti.foreignkeydiscovery;

import java.util.Arrays;

/**
 * Created by Raffaele Conforti on 17/10/14.
 */
public class Cell implements Comparable<Cell>{

    private int[] cellPosition;
    private Integer hashCode;

    public Cell(int[] cellPosition) {
        this.cellPosition = Arrays.copyOf(cellPosition, cellPosition.length);
    }

    public int[] getCellPosition() {
        return cellPosition;
    }

    @Override
    public String toString() {
        return Arrays.toString(cellPosition);
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Cell) {
            Cell c = (Cell) o;
            for(int i = 0; i < cellPosition.length; i++) {
                if(c.cellPosition[i] != this.cellPosition[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        if(hashCode == null) {
            hashCode = Arrays.hashCode(cellPosition);
        }
        return hashCode;
    }

    @Override
    public int compareTo(Cell o) {
        for(int i = 0; i < cellPosition.length; i++) {
            int result = Integer.valueOf(cellPosition[i]).compareTo(o.cellPosition[i]);
            if(result != 0) {
                return result;
            }
        }
        return 0;
    }
}
