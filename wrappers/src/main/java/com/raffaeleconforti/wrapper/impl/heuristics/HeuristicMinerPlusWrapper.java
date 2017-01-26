package com.raffaeleconforti.wrapper.impl.heuristics;

import au.edu.qut.processmining.miners.heuristic.HeuristicMinerPlus;
import au.edu.qut.processmining.miners.heuristic.ui.miner.HMPlusUIResult;
import au.edu.qut.promplugins.HeuristicMinerPlusPlugin;
import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.conversion.bpmn.BPMNToPetriNetConverter;
import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import com.raffaeleconforti.marking.MarkingDiscoverer;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Created by Adriano on 17/01/2017.
 */
@Plugin(name = "Heuristic Miner Plus Wrapper",
        parameterLabels = {"Event Log"},
        returnLabels = {"HM+ BPMN model output"},
        returnTypes = {BPMNDiagram.class})
public class HeuristicMinerPlusWrapper implements MiningAlgorithm {

    @Override
    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log, boolean structure) {
        BPMNDiagram diagram = this.mineBPMNDiagram(context, log, structure);

        Object[] result = BPMNToPetriNetConverter.convert(diagram);

        if(result[1] == null) result[1] = PetriNetToBPMNConverter.guessInitialMarking((Petrinet) result[0]);
        if(result[2] == null) result[2] = PetriNetToBPMNConverter.guessFinalMarking((Petrinet) result[0]);

        if(result[1] == null) result[1] = MarkingDiscoverer.constructInitialMarking(context, (Petrinet) result[0]);
        else MarkingDiscoverer.createInitialMarkingConnection(context, (Petrinet) result[0], (Marking) result[1]);

        if(result[2] == null) result[2] = MarkingDiscoverer.constructFinalMarking(context, (Petrinet) result[0]);
        else MarkingDiscoverer.createFinalMarkingConnection(context, (Petrinet) result[0], (Marking) result[1]);
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

        return new PetrinetWithMarking((Petrinet) result[0], (Marking) result[1], (Marking) result[2]);
    }

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Adriano Augusto",
            email = "adriano.august@ut.ee",
            pack = "bpmntk-osgi")
    @PluginVariant(variantLabel = "Heuristic Miner Plus Wrapper", requiredParameterLabels = {0})
    public BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, boolean structure) {
        BPMNDiagram output ;

        if(context instanceof FakePluginContext) {
            HeuristicMinerPlus heuristicMinerPlus = new HeuristicMinerPlus();
            output = heuristicMinerPlus.mineBPMNModel(log, 0.95, 0.00, 1.00, true, HMPlusUIResult.StructuringTime.NONE);
        } else {
            output = HeuristicMinerPlusPlugin.mineBPMNModelWithHMP(context, log);
        }
        return output;
    }

    @Override
    public String getAlgorithmName() {
        return "Heuristic Miner Plus";
    }

    @Override
    public String getAcronym() {
        return "HM+";
    }
}
