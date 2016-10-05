package com.raffaeleconforti.keithshwarz.algorithms.maximummatchings.edmonds;

/**
 * Created by conforti on 26/11/14.
 */

public class Edge implements Comparable<Edge> {

    final Node from;
    final Node to;
    final int weight;

    public Edge(final Node argFrom, final Node argTo, final int argWeight){
        from = argFrom;
        to = argTo;
        weight = argWeight;
    }

    public int compareTo(final Edge argEdge){
        return weight - argEdge.weight;
    }

    public Node getFrom() {
        return from;
    }

    public Node getTo() {
        return to;
    }
}