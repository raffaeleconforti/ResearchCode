package com.raffaeleconforti.foreignkeydiscovery.conceptualmodels;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Defines a list of values belonging to the same attribute with a pointer
 * to the current value which is used in testing inclusion dependencies.
 *
 * @author Viara Popova
 * @author Modified by Raffaele Conforti
 */
public class ValueList {
    private int current = 0;
    private ArrayList<String> values;

    public ValueList() {
        values = new ArrayList<String>();
    }

    public void addValue(String val) {

        int i = 0;
        if (values.size() == 0) {
            values.add(val);
        } else {
            ListIterator<String> itr = values.listIterator();
            while (itr.hasNext()) {
                String v = itr.next();
                int comp = v.compareTo(val); //compares lexicographically
                if (comp > 0) {    //insert value before current element
                    values.add(i, val);
                    break;
                }
                if (comp == 0) {
                    break; //value already in the array
                }
                if (!itr.hasNext()) {
                    values.add(val);
                    break;
                }
                i++;
            }
        }
    }

    public Boolean movePointer() {
        if (current < values.size() - 1) {
            current++;
            return true;
        } else return false;
    }

    public void resetPointer() {
        current = 0;
    }

    public Boolean lastPlace() {
        return current == values.size();
    }

    public String getValue() {
        return values.get(current);
    }

    public int getSize() {
        return values.size();
    }

}
