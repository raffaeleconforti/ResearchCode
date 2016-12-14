package com.raffaeleconforti.structuredminer.miner;

import au.edu.qut.promplugins.StructureDiagramPlugin;
import au.edu.qut.bpmn.structuring.StructuringService;
import au.edu.qut.bpmn.structuring.ui.iBPStructUIResult;
import com.raffaeleconforti.bpmn.util.BPMNCleaner;
import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.structuredminer.ui.SettingsStructuredMiner;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.impl.FodinaAlgorithmWrapper;
import com.raffaeleconforti.wrapper.impl.heuristics.Heuristics52AlgorithmWrapper;
import com.raffaeleconforti.wrapper.impl.heuristics.HeuristicsAlgorithmWrapper;
import com.raffaeleconforti.wrapper.impl.heuristics.HeuristicsDollarAlgorithmWrapper;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

/**
 * Created by conforti on 20/12/2015.
 */
public class StructuredMiner {

    private final UIPluginContext context;
    private final XLog log;
    private final SettingsStructuredMiner settings;
    private iBPStructUIResult settingStructuring;

    private final StructuringService structuring = new StructuringService();

    public StructuredMiner(UIPluginContext context, XLog log, SettingsStructuredMiner settings) {
        this.context = context;
        this.log = log;
        this.settings = settings;
    }

    public StructuredMiner(UIPluginContext context, XLog log, SettingsStructuredMiner settings, iBPStructUIResult settingStructuring) {
        this.context = context;
        this.log = log;
        this.settings = settings;
        this.settingStructuring = settingStructuring;
    }

    public BPMNDiagram mine() {
        MiningAlgorithm miningAlgorithm = retreiveMiningAlgorithm();

//        PetrinetWithMarking petrinet = miningAlgorithm.minePetrinet(context, log, false);

//        BPMNDiagram diagram = PetriNetToBPMNConverter.convert(petrinet.getPetrinet(), petrinet.getInitialMarking(), petrinet.getFinalMarking(), true);
        BPMNDiagram diagram = miningAlgorithm.mineBPMNDiagram(context, log, false);
        diagram = BPMNCleaner.clean(diagram);
        diagram = structureDiagram(diagram);

        return diagram;
    }

    private MiningAlgorithm retreiveMiningAlgorithm() {
        switch (settings.getSelectedAlgorithm()) {
            case SettingsStructuredMiner.HMPOS : return new HeuristicsAlgorithmWrapper();
            case SettingsStructuredMiner.HMPOS52 : return new Heuristics52AlgorithmWrapper();
            case SettingsStructuredMiner.HDMPOS : return new HeuristicsDollarAlgorithmWrapper();
            case SettingsStructuredMiner.FODINAPOS : return new FodinaAlgorithmWrapper();
        }
        return null;
    }

    private BPMNDiagram structureDiagram(BPMNDiagram diagram) {
        if(settings.isStructured()) {
            try {
//                Object[] res = BPMNToPetriNetConverter.convert(diagram);
//                BPMNDiagram diagram1 = PetriNetToBPMNConverter.convert((Petrinet) res[0], (Marking) res[1], (Marking) res[2], true);
                if(settingStructuring == null && context instanceof FakePluginContext) return structuring.structureDiagram(diagram);
                else if(settingStructuring == null) return StructureDiagramPlugin.structureDiagram(context, diagram);
                else return structuring.structureDiagram(diagram,
                        settingStructuring.getPolicy().toString(),
                        settingStructuring.getMaxDepth(),
                        settingStructuring.getMaxSol(),
                        settingStructuring.getMaxChildren(),
                        settingStructuring.getMaxStates(),
                        settingStructuring.getMaxMinutes(),
                        settingStructuring.isTimeBounded(),
                        settingStructuring.isKeepBisimulation(),
                        settingStructuring.isForceStructuring());
            } catch (Exception e) {
                e.printStackTrace();
                return diagram;
            }
        }
        return diagram;
    }

}
