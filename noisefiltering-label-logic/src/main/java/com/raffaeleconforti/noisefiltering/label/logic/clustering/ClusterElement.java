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

package com.raffaeleconforti.noisefiltering.label.logic.clustering;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 29/12/17.
 */
public class ClusterElement<T> {

    private double cost;
    private Map<T, double[]> elements;

    public ClusterElement(double cost) {
        this.cost = cost;
        elements = new HashMap<>();
    }

    public void addElement(T element, double[] cost) {
        elements.put(element, cost);
    }

    public double getCost() {
        return cost;
    }

    public Map<T, double[]> getElements() {
        return elements;
    }

    @Override
    public String toString() {
        return "Cost: " + cost + "\n" + elements.toString();
    }
}
