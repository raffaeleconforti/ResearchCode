package com.raffaeleconforti.foreignkeydiscovery.databasestructure;

import java.util.Arrays;

/**
 * Created by Raffaele Conforti on 14/10/14.
 */
public class Tuple<T extends Comparable> implements Comparable<Tuple<T>>{

    private T[] elements;
    private Integer hashCode;

    public Tuple(T[] elements) {
        this.elements = elements;
    }

    public T[] getElements() {
        return elements;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Tuple) {
            Tuple c = (Tuple) o;
            if (c.elements.length == elements.length) {
                for (int i = 0; i < elements.length; i++) {
                    if(!c.elements[i].equals(elements[i])) return false;
                }
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public int hashCode() {
        if(hashCode == null) {
            hashCode = Arrays.hashCode(elements);
        }
        return hashCode;
    }

    @Override
    public String toString() {
        return Arrays.toString(elements);
    }

    @Override
    public int compareTo(Tuple<T> o) {
        for(int i = 0; i < elements.length; i++) {
            int result = elements[i].compareTo(o.elements[i]);
            if(result != 0) {
                return result;
            }
        }
        return 0;
    }

}
