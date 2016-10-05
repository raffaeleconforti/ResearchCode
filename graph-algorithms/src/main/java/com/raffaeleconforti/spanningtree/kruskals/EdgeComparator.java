package com.raffaeleconforti.spanningtree.kruskals;

import java.util.Comparator;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 16/02/2016.
 */
public class EdgeComparator implements Comparator<Edge> {
    @Override
    public int compare(Edge edge1, Edge edge2) {
        if (edge1.getWeight() < edge2.getWeight())
            return -1;
        if (edge1.getWeight() > edge2.getWeight())
            return 1;
        return 0;
    }
}
