/*
 *  Copyright (C) 2018 Raffaele Conforti (www.raffaeleconforti.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
