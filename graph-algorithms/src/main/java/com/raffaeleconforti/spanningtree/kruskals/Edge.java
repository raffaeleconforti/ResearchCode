package com.raffaeleconforti.spanningtree.kruskals;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 16/02/2016.
 */
public class Edge {
    private int sourcevertex;
    private int destinationvertex;
    private int weight;

    public int getSourcevertex() {
        return sourcevertex;
    }

    public void setSourcevertex(int sourcevertex) {
        this.sourcevertex = sourcevertex;
    }

    public int getDestinationvertex() {
        return destinationvertex;
    }

    public void setDestinationvertex(int destinationvertex) {
        this.destinationvertex = destinationvertex;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
