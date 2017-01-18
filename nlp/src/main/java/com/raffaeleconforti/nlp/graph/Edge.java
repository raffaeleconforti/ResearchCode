package com.raffaeleconforti.nlp.graph;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 18/1/17.
 */
public class Edge {

    private Integer hashCode;
    private Node source;
    private Node target;
    private int weight;

    public Edge(Node source, Node target, int weight) {
        this.source = source;
        this.target = target;
        this.weight = weight;
    }

    public Node getSource() {
        return source;
    }

    public Node getTarget() {
        return target;
    }

    public int incrementWeight() {
        weight++;
        return weight;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Edge) {
            Edge e = (Edge) o;
            return source.equals(e.source) && target.equals(e.target);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if(hashCode == null) {
            HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
            hashCode = hashCodeBuilder.append(source).append(target).build();
        }
        return hashCode;
    }

    @Override
    public String toString() {
        return source.getId() + "\t" + target.getId() + "\t" + weight;
    }

}
