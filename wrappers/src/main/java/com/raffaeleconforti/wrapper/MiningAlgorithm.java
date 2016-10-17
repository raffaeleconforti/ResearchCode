package com.raffaeleconforti.wrapper;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

/**
 * Created by conforti on 20/02/15.
 */
public interface MiningAlgorithm {

    PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log, boolean structure);
    BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, boolean structure);
    String getAlgorithmName();

}
