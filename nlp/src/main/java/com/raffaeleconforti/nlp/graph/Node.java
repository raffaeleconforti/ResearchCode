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
public class Node {

    private static int counter = 1;
    private Integer hashCode;
    private int id;
    private String label;

    public static Node getInstance(String label) {
        Node node = new Node(counter, label);
        counter++;
        return node;
    }

    private Node(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Node) {
            Node n = (Node) o;
            return id == n.id && label.equals(n.label);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if(hashCode == null) {
            HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
            hashCode = hashCodeBuilder.append(id).append(label).build();
        }
        return hashCode;
    }

    @Override
    public String toString() {
        return id + "\t" + label;
    }

}
