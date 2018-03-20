package com.raffaeleconforti.noisefiltering.event.prom;

import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.noisefiltering.event.FrequentBehaviourFilter;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

/**
 * Created by conforti on 7/02/15.
 */

@Plugin(name = "Frequent Behaviour Filter", parameterLabels = {"Log", "Log"},
        returnLabels = {"BPMNModel"},
        returnTypes = {BPMNDiagram.class})

public class FrequentBehaviourFilterPlugin {

    private final FrequentBehaviourFilter frequentBehaviourFilter = new FrequentBehaviourFilter();

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@unimelb.edu.au",
            pack = "BPMNMiner (raffaele.conforti@unimelb.edu.au)")
    @PluginVariant(variantLabel = "Frequent Behaviour Filter Single Log", requiredParameterLabels = {0})//, 1, 2, 3 })
    public BPMNDiagram generateDiagram(XLog rawlog) {
        return frequentBehaviourFilter.generateDiagram(new FakePluginContext(), rawlog);
    }

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@unimelb.edu.au",
            pack = "BPMNMiner (raffaele.conforti@unimelb.edu.au)")
    @PluginVariant(variantLabel = "Frequent Behaviour Filter Compare Two Logs", requiredParameterLabels = {0, 1})
    public BPMNDiagram generateDiagramTwoLogs(XLog rawlog1, XLog rawlog2) {
        return frequentBehaviourFilter.generateDiagramTwoLogs(new FakePluginContext(), rawlog1, rawlog2);
    }

}
