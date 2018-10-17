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

package com.raffaeleconforti.measurements;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Adriano on 22/11/2016.
 */
public class Measure {
    private Map<String, String> measures;
    private double value;

    public Measure() {
        measures = new HashMap<>();
        value = Double.NaN;
    }

    public Measure(double value) {
        measures = new HashMap<>();
        setValue(value);
    }

    public Measure(String metric, String value) {
        measures = new HashMap<>();
        measures.put(metric, value);
        try { this.value = Double.valueOf(value); }
        catch( NumberFormatException nfe ) { this.value = Double.NaN; }
    }

    public Measure(String metric, double value) {
        measures = new HashMap<>();
        measures.put(metric, Double.toString(value));
        this.value = value;
    }

    public void addMeasure(String metric, String value) {
        measures.put(metric, value);
        try { if( this.value == Double.NaN ) this.value = Double.valueOf(value); }
        catch( NumberFormatException nfe ) { this.value = Double.NaN; }
    }

    public void addMeasure(String metric, double value) {
        measures.put(metric, Double.toString(value));
        if( this.value == Double.NaN ) this.value = value;
    }

    public void setValue(double value) {
        this.value = value;
        addMeasure("", value);
    }

    public double getValue() { return this.value; }

    public String toString() {
        String s = "";
        for(String k : measures.keySet()) {
            s += k + " : " + measures.get(k) + "\n";
        }
        return s;
    }

//    public Map<String, String> getMeasures() { return measures; }

    public Set<String> getMetrics() { return measures.keySet(); }

    public String getMetricValue(String metric) { return measures.get(metric); }

}

