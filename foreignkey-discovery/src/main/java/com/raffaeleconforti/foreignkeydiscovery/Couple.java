package com.raffaeleconforti.foreignkeydiscovery;

/**
 * Created by Raffaele Conforti on 14/10/14.
 */
public class Couple<T extends Comparable, D extends Comparable> {

    private T firstElement;
    private D secondElement;
    private Integer hashCode;

    public Couple(T firstElement, D secondElement) {
        this.firstElement = firstElement;
        this.secondElement = secondElement;
    }

    public T getFirstElement() {
        return firstElement;
    }

    public D getSecondElement() {
        return secondElement;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Couple) {
            Couple c = (Couple) o;
            if(this.hashCode() == c.hashCode()) {
                return c.firstElement.equals(firstElement) && c.secondElement.equals(secondElement);
            }
            return false;
        }
        return false;
    }

    @Override
    public int hashCode() {
        if(hashCode == null) {
            if(firstElement instanceof String && secondElement instanceof String) {
                hashCode = (firstElement + " " + secondElement).hashCode();
            }else {
                hashCode = firstElement.hashCode() + secondElement.hashCode();
            }
        }
        return hashCode;
    }

    @Override
    public String toString() {
        return "Element 1 "+firstElement+" Element 2 "+secondElement;
    }

}
