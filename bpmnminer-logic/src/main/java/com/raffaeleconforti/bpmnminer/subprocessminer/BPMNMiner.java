package com.raffaeleconforti.bpmnminer.subprocessminer;

import com.raffaeleconforti.bpmn.util.BPMNCleaner;
import com.raffaeleconforti.bpmn.util.BPMNSimplifier;
import com.raffaeleconforti.bpmnminer.exception.ExecutionCancelledException;
import com.raffaeleconforti.bpmnminer.subprocessminer.selection.SelectMinerResult;
import com.raffaeleconforti.wrapper.impl.ILPAlgorithmWrapper;
import com.raffaeleconforti.wrapper.impl.SplitMinerWrapper;
import com.raffaeleconforti.wrapper.impl.alpha.AlphaPlusWrapper;
import com.raffaeleconforti.wrapper.impl.heuristics.Heuristics52AlgorithmWrapper;
import com.raffaeleconforti.wrapper.impl.heuristics.HeuristicsAlgorithmWrapper;
import com.raffaeleconforti.wrapper.impl.inductive.InductiveMinerIMfWrapper;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

/**
 * Created by Raffaele Conforti on 20/02/14.
 */
public class BPMNMiner {

    public BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, String startEndEventPreName, int selectedAlgorithm, boolean clean, boolean commandline) throws ExecutionCancelledException {
        BPMNDiagram result = null;

        if (selectedAlgorithm == SelectMinerResult.ALPHAPOS) {
            result = new AlphaPlusWrapper().mineBPMNDiagram(context, log, false);
        } else if (selectedAlgorithm == SelectMinerResult.ILPPOS) {
            result = new ILPAlgorithmWrapper().mineBPMNDiagram(context, log, false);
        } else if (selectedAlgorithm == SelectMinerResult.IMPOS) {
            result = new InductiveMinerIMfWrapper().mineBPMNDiagram(context, log, false);
        } else if (selectedAlgorithm == SelectMinerResult.HMPOS5) {
            result = new Heuristics52AlgorithmWrapper().mineBPMNDiagram(context, log, false);
        } else if (selectedAlgorithm == SelectMinerResult.SMPOS) {
            result = new SplitMinerWrapper().mineBPMNDiagram(context, log, false);
        } else if (selectedAlgorithm == SelectMinerResult.HMPOS6) {
            result = new HeuristicsAlgorithmWrapper().mineBPMNDiagram(context, log, false);
        }
        result = BPMNCleaner.clean(result);
        result = BPMNSimplifier.renameStartAndEndEvents(result, startEndEventPreName);
        result = BPMNSimplifier.renameGateways(result, startEndEventPreName);
        result = BPMNSimplifier.simplify(result);

        return result;
    }
}
