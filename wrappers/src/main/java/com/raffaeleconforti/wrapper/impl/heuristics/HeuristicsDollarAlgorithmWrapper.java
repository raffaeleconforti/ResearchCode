package com.raffaeleconforti.wrapper.impl.heuristics;

import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.heuristicsdollarminer.HeuristicsDollarMiner;
import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.log.util.LogReaderClassic;
import com.raffaeleconforti.wrapper.LogPreprocessing;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import com.raffaeleconforti.marking.MarkingDiscoverer;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.log.LogFile;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;

import org.eclipse.collections.impl.map.mutable.UnifiedMap;

/**
 * Created by conforti on 20/02/15.
 */
@Plugin(name = "Heuristics Dollar Algorithm Wrapper", parameterLabels = {"Log"},
        returnLabels = {"PetrinetWithMarking"},
        returnTypes = {PetrinetWithMarking.class})
public class HeuristicsDollarAlgorithmWrapper implements MiningAlgorithm {

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@qut.edu.au",
            pack = "Noise Filtering")
    @PluginVariant(variantLabel = "Heuristics Dollar Algorithm Wrapper", requiredParameterLabels = {0})
    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log) {
        return minePetrinet(context, log, false);
    }

    @Override
    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log, boolean structure) {
        LogPreprocessing logPreprocessing = new LogPreprocessing();
        log = logPreprocessing.preprocessLog(context, log);

        try {
            LogImporter.exportToFile("", "tmpLog.mxml.gz", log);
        } catch (Exception e) {
            e.printStackTrace();
        }

        LogFile lf = LogFile.getInstance("tmpLog.mxml.gz");
        PetriNet result = new HeuristicsDollarMiner().mine(LogReaderClassic.createInstance(null, lf));
        Petrinet petrinet = getPetrinet(result);
        logPreprocessing.removedAddedElements(petrinet);

        return new PetrinetWithMarking(petrinet, MarkingDiscoverer.constructInitialMarking(context, petrinet), MarkingDiscoverer.constructFinalMarking(context, petrinet));
    }

    @Override
    public BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, boolean structure) {
        PetrinetWithMarking petrinetWithMarking = minePetrinet(context, log, structure);
        return PetriNetToBPMNConverter.convert(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking(), true);
    }

    private Petrinet getPetrinet(PetriNet result) {
        Petrinet petrinet = new PetrinetImpl("Alpha Dollar");
        UnifiedMap<org.processmining.framework.models.petrinet.Transition, Transition> transitionUnifiedMap = new UnifiedMap<>();
        UnifiedMap<org.processmining.framework.models.petrinet.Place, Place> placeUnifiedMap = new UnifiedMap<>();

        for(org.processmining.framework.models.petrinet.Transition t : result.getTransitions()) {
            Transition transition = petrinet.addTransition(t.getLogEvent().getModelElementName());
            transition.setInvisible(t.isInvisibleTask());
            transitionUnifiedMap.put(t, transition);
        }

        for(org.processmining.framework.models.petrinet.Place p : result.getPlaces()) {
            Place place = petrinet.addPlace(p.getName());
            placeUnifiedMap.put(p, place);
        }

        for(org.processmining.framework.models.petrinet.Transition t : result.getTransitions()) {
            for(org.processmining.framework.models.petrinet.Place p : result.getPlaces()) {
                Transition transition = transitionUnifiedMap.get(t);
                Place place = placeUnifiedMap.get(p);
                if(result.findEdge(t, p) != null) {
                    petrinet.addArc(transition, place);
                }
                if(result.findEdge(p, t) != null) {
                    petrinet.addArc(place, transition);
                }
            }
        }

        return petrinet;
    }

    @Override
    public String getAlgorithmName() {
        return "Heuristics Dollar";
    }

    @Override
    public String getAcronym() { return "HM$";}
}
