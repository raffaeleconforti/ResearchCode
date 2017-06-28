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
import com.raffaeleconforti.wrapper.settings.MiningSettings;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

/**
 * Created by Raffaele Conforti on 20/02/14.
 */
public class BPMNMiner {

    public BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, String startEndEventPreName, int selectedAlgorithm, MiningSettings params, boolean clean, boolean commandline) throws ExecutionCancelledException {
        BPMNDiagram result = null;

        if (selectedAlgorithm == SelectMinerResult.ALPHAPOS) {
            System.out.println("Using Alpha Miner");
            result = new AlphaPlusWrapper().mineBPMNDiagram(context, log, false, params);
        } else if (selectedAlgorithm == SelectMinerResult.ILPPOS) {
            System.out.println("Using ILP Miner");
            result = new ILPAlgorithmWrapper().mineBPMNDiagram(context, log, false, params);
        } else if (selectedAlgorithm == SelectMinerResult.IMPOS) {
            System.out.println("Using Inductive Miner");
            result = new InductiveMinerIMfWrapper().mineBPMNDiagram(context, log, false, params);
        } else if (selectedAlgorithm == SelectMinerResult.HMPOS5) {
            System.out.println("Using Heuristics Miner 5.2");
            result = new Heuristics52AlgorithmWrapper().mineBPMNDiagram(context, log, false, params);
        } else if (selectedAlgorithm == SelectMinerResult.SMPOS) {
            System.out.println("Using Split Miner");
            result = new SplitMinerWrapper().mineBPMNDiagram(context, log, false, params);
        } else if (selectedAlgorithm == SelectMinerResult.HMPOS6) {
            System.out.println("Using Heuristics Miner 6");
            result = new HeuristicsAlgorithmWrapper().mineBPMNDiagram(context, log, false, params);
        }
        result = BPMNCleaner.clean(result);
        result = BPMNSimplifier.renameStartAndEndEvents(result, startEndEventPreName);
        result = BPMNSimplifier.renameGateways(result, startEndEventPreName);
        result = BPMNSimplifier.simplify(result);

        return result;
    }
}
