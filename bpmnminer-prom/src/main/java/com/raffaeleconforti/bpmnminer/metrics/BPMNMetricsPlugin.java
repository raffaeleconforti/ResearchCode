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

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

/**
 * Created by Raffaele Conforti on 17/03/14.
 */

@Plugin(name = "Calculate BPMN Metrics", parameterLabels = {"BPMNDiagram"},
        returnLabels = {"Metrics"},
        returnTypes = {String.class})

public class BPMNMetricsPlugin {

    @UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Raffaele Conforti", email = "raffaele.conforti@unimelb.edu.au")
    @PluginVariant(variantLabel = "Calculate BPMN Metricst", requiredParameterLabels = {0})//, 1, 2, 3 })
    public String calculate(final UIPluginContext context, BPMNDiagram diagram) {
        return BPMNMetrics.calulate(diagram);
    }
}