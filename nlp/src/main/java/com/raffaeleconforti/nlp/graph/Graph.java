package com.raffaeleconforti.nlp.graph;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 18/1/17.
 */
public class Graph {

    Set<Node> nodes = new HashSet<>();
    Set<Edge> edges = new HashSet<>();

    public Node addNode(String node_label) {
        Node node = getNode(node_label);
        if(node == null) {
            node = Node.getInstance(node_label);
            addNode(node);
        }
        return node;
    }

    public Edge addEdge(String source_node_label, String target_node_label) {
        Edge edge = getEdge(source_node_label, target_node_label);
        if(edge == null) {
            edge = new Edge(getNode(source_node_label), getNode(target_node_label), 1);
            addEdge(edge);
        }else {
            edge.incrementWeight();
        }
        return edge;
    }

    private Node addNode(Node node) {
        nodes.add(node);
        return node;
    }

    private Edge addEdge(Edge edge) {
        edges.add(edge);
        return edge;
    }

    public Node getNode(String node_label) {
        for(Node node : nodes) {
            if(node.getLabel().equals(node_label)) {
                return node;
            }
        }
        return null;
    }

    public Edge getEdge(String source_node_label, String target_node_label) {
        for(Edge edge : edges) {
            if(edge.getSource().getLabel().equals(source_node_label) && edge.getTarget().getLabel().equals(target_node_label)) {
                return edge;
            }
        }
        return null;
    }

}
