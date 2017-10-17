package com.raffaeleconforti.wrapper;

import com.raffaeleconforti.wrapper.settings.MiningSettings;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.processtree.ProcessTree;

/**
 * Created by conforti on 20/02/15.
 */
public interface MiningAlgorithm {

    boolean canMineProcessTree();
    ProcessTree mineProcessTree(UIPluginContext context, XLog log, boolean structure, MiningSettings params);
    PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log, boolean structure, MiningSettings params);
    BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, boolean structure, MiningSettings params);
    String getAlgorithmName();
    String getAcronym();

}
