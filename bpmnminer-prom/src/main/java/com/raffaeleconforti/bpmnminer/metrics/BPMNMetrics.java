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

package com.raffaeleconforti.bpmnminer.metrics;

import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;

/**
 * Created by Raffaele Conforti on 17/03/14.
 */
public class BPMNMetrics {

    public static String calulate(BPMNDiagram diagram) {

        return "<html>" +
                "<table>" +
                "<tr><td>Size</td><td>" + calculateSize(diagram) + "</td></tr>" +
                "<tr><td>CFC</td><td>" + calculateCFC(diagram) + "</td></tr>" +
                "<tr><td>ACD</td><td>" + calculateACD(diagram) + "</td></tr>" +
                "<tr><td>MCD</td><td>" + calculateMCD(diagram) + "</td></tr>" +
                "<tr><td>CNC</td><td>" + calculateCNC(diagram) + "</td></tr>" +
                "<tr><td>Density</td><td>" + calculateDensity(diagram) + "</td></tr>" +
                "</table>" +
                "</html>";
    }

    private static String calculateDensity(BPMNDiagram diagram) {
        double arcs = diagram.getFlows().size();
        double nodes = diagram.getEvents().size();
        nodes += diagram.getActivities().size();
        nodes += diagram.getGateways().size();

        return String.valueOf(arcs / (nodes * (nodes - 1)));
    }

    private static String calculateCNC(BPMNDiagram diagram) {
        double arcs = diagram.getFlows().size();
        double nodes = diagram.getEvents().size();
        nodes += diagram.getActivities().size();
        nodes += diagram.getGateways().size();

        return String.valueOf(arcs / nodes);
    }

    private static String calculateMCD(BPMNDiagram diagram) {
        int size = 0;

        for (Gateway g : diagram.getGateways()) {
            int count = 0;
            for (Flow f : diagram.getFlows()) {
                if (f.getSource().equals(g) || f.getTarget().equals(g)) {
                    count++;
                }
            }
            size = Math.max(size, count);
        }

        return String.valueOf(size);
    }

    private static String calculateACD(BPMNDiagram diagram) {
        double size = 0.0;

        for (Gateway g : diagram.getGateways()) {

            for (Flow f : diagram.getFlows()) {
                if (f.getSource().equals(g) || f.getTarget().equals(g)) {
                    size++;
                }
            }
        }

        return String.valueOf(size / diagram.getGateways().size());
    }

    private static String calculateCFC(BPMNDiagram diagram) {
        int size = 0;

        for (Gateway g : diagram.getGateways()) {
            if (g.getGatewayType().equals(Gateway.GatewayType.PARALLEL)) {
                size++;
            } else if (g.getGatewayType().equals(Gateway.GatewayType.DATABASED) || g.getGatewayType().equals(Gateway.GatewayType.INCLUSIVE)) {
                int count = 0;

                for (Flow f : diagram.getFlows()) {
                    if (f.getSource().equals(g)) {
                        count++;
                    }
                }

                if (g.getGatewayType().equals(Gateway.GatewayType.DATABASED)) {
                    size += count;
                } else {
                    size += Math.pow(2, count) - 1;
                }
            }
        }

        return String.valueOf(size);
    }

    private static String calculateSize(BPMNDiagram diagram) {
        int size = diagram.getEvents().size();
        size += diagram.getActivities().size();
        size += diagram.getGateways().size();
        return String.valueOf(size);
    }
}
