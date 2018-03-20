package com.raffaeleconforti.bpmnminer.converter;

import com.raffaeleconforti.bpmn.util.BPMNCleaner;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

/**
 * Created by conforti on 12/11/2014.
 */
@Plugin(name = "Clean BPMN Model", parameterLabels = {"BPMNDiagram"},
        returnLabels = {"BPMNDiagram"},
        returnTypes = {BPMNDiagram.class})
public class BPMNCleanerPlugin {

    public static BPMNDiagram simplify(UIPluginContext context, BPMNDiagram diagram) {
        return BPMNCleaner.clean(diagram);

    }

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@unimelb.edu.au",
            pack = "bpmnminer")
    @PluginVariant(variantLabel = "Clean BPMN Model", requiredParameterLabels = {})
    public BPMNDiagram convert(final UIPluginContext context) {//, BPMNDiagram diagram) {
        return simplify(context, null);
    }

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@unimelb.edu.au",
            pack = "bpmnminer")
    @PluginVariant(variantLabel = "Clean BPMN Model", requiredParameterLabels = {0})
    public BPMNDiagram convert(final UIPluginContext context, BPMNDiagram diagram) {
        return simplify(context, diagram);
    }

}
