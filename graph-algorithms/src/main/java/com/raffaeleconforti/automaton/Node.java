package com.raffaeleconforti.automaton;

/**
 * Created by conforti on 14/02/15.
 */
public class Node<T> {

    private T data;
    private double frequency;

    public Node(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public double getFrequency() {
        return frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return data != null ? data.equals(node.data) : node.data == null;
    }

    @Override
    public int hashCode() {
        int result = data != null ? data.hashCode() : 0;
        return result;
    }

    @Override
    public String toString() {
        return data.toString();
    }

}
