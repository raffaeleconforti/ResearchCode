package com.raffaeleconforti.bpmnminer.converter;

import com.raffaeleconforti.bpmn.util.BPMNSimplifier;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

/**
 * Created by Raffaele Conforti on 28/02/14.
 */

@Plugin(name = "Simplify BPMN Model", parameterLabels = {"BPMNDiagram"},
        returnLabels = {"BPMNDiagram"},
        returnTypes = {BPMNDiagram.class})
public class BPMNSimplifierPlugin {

    public static BPMNDiagram simplify(BPMNDiagram diagram) {
        int oldSize = 0;
        int size = diagram.getActivities().size() + diagram.getEvents().size() + diagram.getGateways().size() + diagram.getFlows().size();
        while (oldSize != size) {
            oldSize = size;
            BPMNSimplifier.removeConnectedStartEndEvent(diagram);
            BPMNSimplifier.removeHTMLfromAllActivitiesProcess(diagram);
            BPMNSimplifier.removeArtificialNodes(diagram);
            BPMNSimplifier.removeLoopedXOR(diagram);
            BPMNSimplifier.replaceShortLoopsWithSelfLoops(diagram);
            BPMNSimplifier.fixANDGateway(diagram);
            BPMNSimplifier.removeUselessSubProcesses(diagram);
            BPMNSimplifier.removeGatewaysUseless(diagram, diagram.getGateways());
            BPMNSimplifier.removeDuplicateArcs(diagram);
            BPMNSimplifier.removeEmptyActivities(diagram);
            size = diagram.getActivities().size() + diagram.getEvents().size() + diagram.getGateways().size() + diagram.getFlows().size();
        }
        return diagram;

    }

    public static BPMNDiagram basicSimplification(BPMNDiagram diagram) {
        int oldSize = 0;
        int size = diagram.getActivities().size() + diagram.getEvents().size() + diagram.getGateways().size() + diagram.getFlows().size();
        while (oldSize != size) {
            oldSize = size;
            BPMNSimplifier.removeConnectedStartEndEvent(diagram);
            BPMNSimplifier.removeHTMLfromAllActivitiesProcess(diagram);
            BPMNSimplifier.insertStartAndEndEventsIfMissing(diagram);
            BPMNSimplifier.removeArtificialNodes(diagram);
//            BPMNSimplifier.removeLoopedXOR(diagram);
            BPMNSimplifier.fixANDGateway(diagram);
            BPMNSimplifier.removeGatewaysUseless(diagram, diagram.getGateways());
            size = diagram.getActivities().size() + diagram.getEvents().size() + diagram.getGateways().size() + diagram.getFlows().size();
        }
        return diagram;

    }

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@unimelb.edu.au",
            pack = "bpmnminer")
    @PluginVariant(variantLabel = "Simplify BPMN Model", requiredParameterLabels = {0})
    public BPMNDiagram convert(final UIPluginContext context, BPMNDiagram diagram) {
        return simplify(diagram);
    }
}
