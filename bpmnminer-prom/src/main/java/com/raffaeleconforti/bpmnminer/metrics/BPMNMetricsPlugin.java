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