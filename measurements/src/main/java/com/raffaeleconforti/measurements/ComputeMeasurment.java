package com.raffaeleconforti.measurements;

import au.edu.qut.bpmn.metrics.ComplexityCalculator;
import au.edu.qut.bpmn.structuring.StructuringService;
import com.raffaeleconforti.conversion.bpmn.BPMNToPetriNetConverter;
import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.measurements.ui.computemeasurement.SelectMinerUICM;
import com.raffaeleconforti.measurements.ui.computemeasurement.SelectMinerUIResultCM;
import com.raffaeleconforti.memorylog.XAttributeLiteralImpl;
import com.raffaeleconforti.noisefiltering.event.prom.InfrequentBehaviourFilterPlugin;
import com.raffaeleconforti.noisefiltering.event.prom.InfrequentBehaviourFilterPluginLPSolve;
import com.raffaeleconforti.wrappers.BPMNMinerAlgorithmWrapper;
import com.raffaeleconforti.wrappers.MiningAlgorithm;
import com.raffaeleconforti.wrappers.PetrinetWithMarking;
import com.raffaeleconforti.wrappers.StructuredMinerAlgorithmWrapper;
import com.raffaeleconforti.wrappers.impl.EvolutionaryTreeMinerWrapper;
import com.raffaeleconforti.wrappers.impl.FodinaAlgorithmWrapper;
import com.raffaeleconforti.wrappers.impl.ILPAlgorithmWrapper;
import com.raffaeleconforti.wrappers.impl.alpha.AlphaAlgorithmWrapper;
import com.raffaeleconforti.wrappers.impl.heuristics.Heuristics52AlgorithmWrapper;
import com.raffaeleconforti.wrappers.impl.heuristics.HeuristicsAlgorithmWrapper;
import com.raffaeleconforti.wrappers.impl.inductive.InductiveMinerIMWrapper;
import nl.tue.astar.AStarException;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.petrinets.PNRepResultAllRequiredParamConnection;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.multietc.plugins.MultiETCPlugin;
import org.processmining.plugins.multietc.res.MultiETCResult;
import org.processmining.plugins.multietc.sett.MultiETCSettings;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteMarkEquation;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * Created by conforti on 20/02/15.
 */

@Plugin(name = "Compute Measurements", parameterLabels = {"Training Log", "Validating Log", "Petri Net", "Initial Marking", "Final Marking"},
        returnLabels = {"Measurement"},
        returnTypes = {String.class})
public class ComputeMeasurment {

    private final Random r = new Random(123456789);
    private int fold;
    private XLog trainingLog;
    private XLog validatingLog;
    private final XFactory factory = new XFactoryNaiveImpl();
    private int numberTraces;

    private IPNReplayParameter parameters;
    private TransEvClassMapping mapping;
    private IPNReplayParameter finalParameters;
    private TransEvClassMapping finalMapping;
    private PetrinetWithMarking petrinetWithMarking;
    private PNRepResult pnRepResult;
    private MultiETCResult multiETCResult;

    private MiningAlgorithm[] miningAlgorithms;

    private static final XEventClassifier xEventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
    private final XConceptExtension xce = XConceptExtension.instance();
    private final XTimeExtension xte = XTimeExtension.instance();
    private final XLifecycleExtension xle = XLifecycleExtension.instance();

    private final StructuringService structuring = new StructuringService();

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@unimelb.edu.au",
            pack = "BPMNMiner (raffaele.conforti@unimelb.edu.au)")
    @PluginVariant(variantLabel = "Compute Measurements", requiredParameterLabels = {0})
    public String execute(UIPluginContext context, XLog trainingLog) {
        return execute(context, trainingLog, trainingLog);
    }

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@unimelb.edu.au",
            pack = "BPMNMiner (raffaele.conforti@unimelb.edu.au)")
    @PluginVariant(variantLabel = "Compute Measurements with test noisefiltering", requiredParameterLabels = {0, 1})
    public String execute(UIPluginContext context, XLog trainingLog, XLog validatingLog) {
        return execute(context, trainingLog, validatingLog, true);
    }

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@unimelb.edu.au",
            pack = "BPMNMiner (raffaele.conforti@unimelb.edu.au)")
    @PluginVariant(variantLabel = "Compute Measurements with Model", requiredParameterLabels = {1, 2, 3, 4})
    public String execute(UIPluginContext context, XLog validatingLog, Petrinet petrinet, Marking initialMarking, Marking finalMarking) {

        SelectMinerUICM selectMiner = new SelectMinerUICM(true);
        SelectMinerUIResultCM result = selectMiner.showGUI(context);

        StringBuilder sb = new StringBuilder();
        sb.append("<html><table width=\"400\"><tr><td width=\"33%\"></td><td width=\"33%\"><table>");
        sb.append("<tr><td>" + context.getGlobalContext().getResourceManager().getResourceForInstance(validatingLog).getName() + "</td><td></td><td></td></tr>");
        sb.append("<tr><td></td><td></td><td></td></tr>");

        PetrinetWithMarking petrinetWithMarking = new PetrinetWithMarking(petrinet, initialMarking, finalMarking);
        ComputeMeasurment cm = new ComputeMeasurment();

        sb.append("<tr><td>Model</td><td></td><td></td></tr>");
        cm.clear();
        String s = cm.execute(context, petrinetWithMarking, validatingLog, result);
        s = s.substring(73, s.indexOf("</table></td><td width=\"33%\"></td></tr></table></html>"));
        sb.append(s).append("<tr><td></td><td></td><td></td></tr>");

        sb.append("</table></td><td width=\"33%\"></td></tr></table></html>");
        return sb.toString();

    }

    private String execute(UIPluginContext context, XLog trainingLog, XLog validatingLog, boolean showAlgo) {
        SelectMinerUICM selectMiner = new SelectMinerUICM(showAlgo);
        SelectMinerUIResultCM result = selectMiner.showGUI(context);

        return execute(context, trainingLog, validatingLog, result);

    }

    public String execute(UIPluginContext context, XLog trainingLog, XLog validatingLog, SelectMinerUIResultCM result) {

        int fold = result.getFold();
        int miningAlgorithm = result.getSelectedAlgorithm();

        ArrayList<String> classifiers = new ArrayList<String>();
        for(XEventClassifier c : trainingLog.getClassifiers()) {
            for(String s : c.getDefiningAttributeKeys()) {
                classifiers.add(s);
            }
        }
        for(XEventClassifier c : validatingLog.getClassifiers()) {
            for(String s : c.getDefiningAttributeKeys()) {
                classifiers.add(s);
            }
        }

//        start = createStartEvent(classifiers);
//        end = createEndEvent(classifiers);

        this.fold = fold;
        this.trainingLog = trainingLog;
        this.validatingLog = validatingLog;
        numberTraces = trainingLog.size() / fold;

        miningAlgorithms = new MiningAlgorithm[] {new BPMNMinerAlgorithmWrapper(), new InductiveMinerIMWrapper(),
                new HeuristicsAlgorithmWrapper(), new FodinaAlgorithmWrapper(), new ILPAlgorithmWrapper(),
                new AlphaAlgorithmWrapper(), new Heuristics52AlgorithmWrapper(), new EvolutionaryTreeMinerWrapper(),
                new StructuredMinerAlgorithmWrapper()};

        petrinetWithMarking = discoverPetrinet(context, miningAlgorithms[miningAlgorithm], trainingLog, false, xEventClassifier);
        Petrinet petrinet = petrinetWithMarking.getPetrinet();

        return compute(context, result, petrinet, miningAlgorithm);
    }

    private String execute(UIPluginContext context, PetrinetWithMarking petrinetWithMarking, XLog validatingLog, SelectMinerUIResultCM result) {

        int fold = result.getFold();
        int miningAlgorithm = result.getSelectedAlgorithm();

        ArrayList<String> classifiers = new ArrayList<String>();
        for(XEventClassifier c : validatingLog.getClassifiers()) {
            for(String s : c.getDefiningAttributeKeys()) {
                classifiers.add(s);
            }
        }

//        start = createStartEvent(classifiers);
//        end = createEndEvent(classifiers);

        this.fold = fold;
        this.validatingLog = validatingLog;
        this.trainingLog = validatingLog;
        numberTraces = trainingLog.size() / fold;

//        miningAlgorithms = new MiningAlgorithm[] {null};
        miningAlgorithms = new MiningAlgorithm[] {new BPMNMinerAlgorithmWrapper(), new InductiveMinerIMWrapper(),
                new HeuristicsAlgorithmWrapper(), new FodinaAlgorithmWrapper(), new ILPAlgorithmWrapper(),
                new AlphaAlgorithmWrapper(), new Heuristics52AlgorithmWrapper(), new EvolutionaryTreeMinerWrapper(),
                new StructuredMinerAlgorithmWrapper()};

        this.petrinetWithMarking = petrinetWithMarking;
        Petrinet petrinet = petrinetWithMarking.getPetrinet();

        return compute(context, result, petrinet, miningAlgorithm);

    }

    private String compute(UIPluginContext context, SelectMinerUIResultCM result, Petrinet petrinet, int miningAlgorithm) {

        boolean isFitness = result.isFitness();
        boolean isPrecision = result.isPrecision();
        boolean isGeneralization = result.isGeneralization();
        boolean isNoise = result.isNoise();
//        boolean isStructured = result.isStructured();
        boolean isSemplicity = result.isSemplicity();

        BPMNDiagram diagram1 = PetriNetToBPMNConverter.convert(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking(), true);

        context.getProvidedObjectManager().createProvidedObject("Petri Net", petrinetWithMarking.getPetrinet(), Petrinet.class, context);
        context.getGlobalContext().getResourceManager().getResourceForInstance(petrinetWithMarking.getPetrinet()).setFavorite(true);
        context.getProvidedObjectManager().createProvidedObject("BPMN Diagram", diagram1, BPMNDiagram.class, context);
        context.getGlobalContext().getResourceManager().getResourceForInstance(diagram1).setFavorite(true);

        String a = computeMeasurements(context, petrinet, miningAlgorithm, isFitness, isPrecision, isGeneralization, isNoise, false, isSemplicity);
        a = a.replace("<html><table width=\"400\"><tr><td width=\"33%\"></td><td width=\"33%\"><table>", "<html><table width=\"400\"><tr><td width=\"33%\"></td><td width=\"33%\"><table><tr><td>Original</td><td></td><td></td></tr>");

        return a;
    }

    private PetrinetWithMarking structurePetriNet(UIPluginContext context, PetrinetWithMarking petrinetWithMarking) {

        try {
            BPMNDiagram diagram1 = PetriNetToBPMNConverter.convert(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking(), true);

            context.getProvidedObjectManager().createProvidedObject("ORIGINAL DIAGRAM", diagram1, BPMNDiagram.class, context);
            context.getGlobalContext().getResourceManager().getResourceForInstance(diagram1).setFavorite(true);

            BPMNDiagram diagram = PetriNetToBPMNConverter.convert(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking(), true);
            diagram = structuring.structureDiagram(diagram);

            context.getProvidedObjectManager().createProvidedObject("STRUCTURED DIAGRAM", diagram, BPMNDiagram.class, context);
            context.getGlobalContext().getResourceManager().getResourceForInstance(diagram).setFavorite(true);

            Object[] objects = BPMNToPetriNetConverter.convert(diagram);
            context.addConnection(new InitialMarkingConnection((Petrinet) objects[0], (Marking) objects[1]));
            context.addConnection(new FinalMarkingConnection((Petrinet) objects[0], (Marking) objects[2]));
            return new PetrinetWithMarking((Petrinet) objects[0], (Marking) objects[1], (Marking) objects[2]);
        } catch (Exception e) {
            e.printStackTrace();
            return  petrinetWithMarking;
        }

    }

    private String computeMeasurements(UIPluginContext context, Petrinet petrinet, int miningAlgorithm, boolean isFitness, boolean isPrecision, boolean isGeneralization, boolean isNoise, boolean isStructured, boolean isSemplicity) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><table width=\"400\"><tr><td width=\"33%\"></td><td width=\"33%\"><table>");

        Double fitness = null;
        if(isFitness) {
            fitness = computeFitness(context, petrinet, miningAlgorithm, isStructured);
            sb.append("<tr><td>Fitness (Alignment) </td><td>:</td><td>").append(fitness).append("</td></tr>");

            System.out.println("Fitness: " + fitness);
        }

        Double precision = null;
        if(isPrecision) {
            precision = computePrecision(context, petrinet, miningAlgorithm, isStructured);
            sb.append("<tr><td>Precision (Aligned-ETC)</td><td>:</td><td>").append(precision).append("</td></tr>");

            System.out.println("Precision: " + precision);
        }

        if(fitness != null && precision != null) {
            Double f_measure = 2 * (fitness * precision) / (fitness + precision);
            sb.append("<tr><td>F-Measure</td><td>:</td><td>").append(f_measure).append("</td></tr>");

            System.out.println("F-Score: " + f_measure);
        }

        if(isGeneralization) {
            if(!isNoise) {
                Double generalization = computeXFoldGeneralizationNoise(context, miningAlgorithms[miningAlgorithm], isStructured);
                if (generalization == -1.0) generalization = Double.NaN;

                sb.append("<tr><td>Generalization (").append(fold).append("-Fold)</td><td>:</td><td>").append(generalization).append("</td></tr>");

                System.out.println("Generalization (" + fold + "-Fold): " + generalization);
            }else {
                Double generalization = computeXFoldGeneralization(context, miningAlgorithms[miningAlgorithm], isStructured);
                if(generalization == -1.0) generalization = Double.NaN;

                sb.append("<tr><td>GeneralizationFiltered (").append(fold).append("-Fold)</td><td>:</td><td>").append(generalization).append("</td></tr>");

                System.out.println("GeneralizationFiltered (" + fold + "-Fold): " + generalization);
            }
        }

        if(isSemplicity) {
            sb.append("<tr><td>Size</td><td>:</td><td>").append(calculateSize(petrinet)).append("</td></tr>");
            sb.append("<tr><td>CFC</td><td>:</td><td>").append(calculateCFC(petrinet)).append("</td></tr>");
            sb.append("<tr><td>ACD</td><td>:</td><td>").append(calculateACD(petrinet)).append("</td></tr>");
            sb.append("<tr><td>MCD</td><td>:</td><td>").append(calculateMCD(petrinet)).append("</td></tr>");
            sb.append("<tr><td>CNC</td><td>:</td><td>").append(calculateCNC(petrinet)).append("</td></tr>");
            sb.append("<tr><td>Density</td><td>:</td><td>").append(calculateDensity(petrinet)).append("</td></tr>");

            String result = ComplexityCalculator.computeComplexity(context, PetriNetToBPMNConverter.convert(petrinet, petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking(), false));
            StringTokenizer st = new StringTokenizer(result, " :\n\t\r", true);

            int pos = 0;

            sb.append("<tr></tr><tr><td>Semplicity on BPMN</td><td></td><td></td></tr>");

            while(st.hasMoreTokens()) {
                String token = st.nextToken();
                if(!(token.equals(" ") || token.equals("\t") || token.equals("\r") || token.equals("\n"))) {
                    if(pos == 0) {
                        sb.append("<tr></tr><tr><td>").append(token);
                        pos++;
                    }else if(pos == 1) {
                        sb.append("</td><td>:</td><td>");
                        pos++;
                    }else {
                        sb.append(token).append("</td></tr>");
                        pos = 0;
                    }
                }
            }

        }

        sb.append("</table></td><td width=\"33%\"></td></tr></table></html>");
        return sb.toString();
    }

    private Double computeFitness(UIPluginContext context, Petrinet petrinet, int miningAlgorithm, boolean isStructured) {
        Double fitness = computeFitness(context, miningAlgorithms[miningAlgorithm], isStructured);
        if(fitness == -1.0) fitness = Double.NaN;

        context.getProvidedObjectManager().createProvidedObject("Fitness " + getAlgorithmName(miningAlgorithm), pnRepResult, PNRepResult.class, context);
        context.getGlobalContext().getResourceManager().getResourceForInstance(pnRepResult).setFavorite(true);
        context.addConnection(new PNRepResultAllRequiredParamConnection("Fitness", petrinet, validatingLog, finalMapping, new CostBasedCompleteMarkEquation(), finalParameters, pnRepResult));

        return fitness;
    }

    private Double computePrecision(UIPluginContext context, Petrinet petrinet, int miningAlgorithm, boolean isStructured) {
        computePrecision(context, miningAlgorithms[miningAlgorithm], isStructured);

        Double precision = (Double) multiETCResult.getAttribute(MultiETCResult.PRECISION);

        context.getProvidedObjectManager().createProvidedObject("Precision " + getAlgorithmName(miningAlgorithm), multiETCResult, MultiETCResult.class, context);
        context.getGlobalContext().getResourceManager().getResourceForInstance(multiETCResult).setFavorite(true);

        return precision;
    }

    private XEvent createStartEvent(ArrayList<String> classifiers) {
        XEvent start = factory.createEvent();
        xce.assignName(start, "###$$$%%%$$$###START###$$$%%%$$$###");
        xte.assignTimestamp(start, 1L);
        xle.assignStandardTransition(start, XLifecycleExtension.StandardModel.COMPLETE);
        for(String s : classifiers) {
            XAttributeLiteralImpl a = new XAttributeLiteralImpl(s, "###$$$%%%$$$###START###$$$%%%$$$###");
            start.getAttributes().put(s, a);
        }
        return start;
    }

    private XEvent createEndEvent(ArrayList<String> classifiers) {
        XEvent end = factory.createEvent();
        xce.assignName(end, "###$$$%%%$$$###END###$$$%%%$$$###");
        xte.assignTimestamp(end, Long.MAX_VALUE);
        xle.assignStandardTransition(end, XLifecycleExtension.StandardModel.COMPLETE);
        for (String s : classifiers) {
            XAttributeLiteralImpl a = new XAttributeLiteralImpl(s, "###$$$%%%$$$###END###$$$%%%$$$###");
            end.getAttributes().put(s, a);
        }
        return end;
    }

    public void clear() {
        petrinetWithMarking = null;
        pnRepResult = null;
        multiETCResult = null;
    }

    public String getAlgorithmName(int algorithm) {
        switch (algorithm) {
            case SelectMinerUICM.BPMNPOS : return SelectMinerUICM.BPMN;
            case SelectMinerUICM.IMPOS : return SelectMinerUICM.IM;
            case SelectMinerUICM.ALPHAPOS : return SelectMinerUICM.ALPHA;
            case SelectMinerUICM.FODINAPOS : return SelectMinerUICM.FODINA;
            case SelectMinerUICM.HMPOS : return SelectMinerUICM.HM;
            case SelectMinerUICM.ILPPOS : return SelectMinerUICM.ILP;
            case SelectMinerUICM.HMPOS52 : return SelectMinerUICM.HM52;
            case SelectMinerUICM.SMPOS : return SelectMinerUICM.SM;
            case SelectMinerUICM.ETMPOS : return SelectMinerUICM.ETM;
        }
        return "";
    }

    private PetrinetWithMarking discoverPetrinet(UIPluginContext context, MiningAlgorithm miningAlgorithm, XLog log, boolean isStructured, XEventClassifier xEventClassifier) {
        PetrinetWithMarking petrinetWithMarking = miningAlgorithm.minePetrinet(context, log, isStructured, null, xEventClassifier);
        Petrinet petrinet = petrinetWithMarking.getPetrinet();
        Marking initialMarking = petrinetWithMarking.getInitialMarking();
        Marking finalMarking = constructFinalMarking(petrinet);

        for (Transition t : petrinet.getTransitions()) {
            if (t.getLabel().contains("###$$$%%%$$$###START###$$$%%%$$$###")) {
                t.setInvisible(true);
                t.getAttributeMap().put("ProM_Vis_attr_label", "source");
            }else if (t.getLabel().contains("###$$$%%%$$$###END###$$$%%%$$$###")) {
                t.setInvisible(true);
                t.getAttributeMap().put("ProM_Vis_attr_label", "sink");
            }else if (t.getLabel().toLowerCase().endsWith("+complete")) {
                t.getAttributeMap().put("ProM_Vis_attr_label", t.getLabel().substring(0, t.getLabel().toLowerCase().indexOf("+complete")));
            } else if (t.getLabel().toLowerCase().endsWith("+start")) {
                t.getAttributeMap().put("ProM_Vis_attr_label", t.getLabel().substring(0, t.getLabel().toLowerCase().indexOf("+start")));
            }
        }

        context.getProvidedObjectManager().createProvidedObject("PetriNet", petrinet, Petrinet.class, context);
        context.addConnection(new InitialMarkingConnection(petrinet, initialMarking));
        context.addConnection(new FinalMarkingConnection(petrinet, finalMarking));

        return new PetrinetWithMarking(petrinet, initialMarking, finalMarking);
    }

    private double computePrecision(UIPluginContext context, MiningAlgorithm miningAlgorithm, boolean isStructured) {
        MultiETCPlugin multiETCPlugin = new MultiETCPlugin();

        if(petrinetWithMarking == null) petrinetWithMarking = discoverPetrinet(context, miningAlgorithm, trainingLog, isStructured, xEventClassifier);
        Petrinet petrinet = petrinetWithMarking.getPetrinet();
        MultiETCSettings settings = new MultiETCSettings();
        settings.put(MultiETCSettings.ALGORITHM, MultiETCSettings.Algorithm.ALIGN_1);
        settings.put(MultiETCSettings.REPRESENTATION, MultiETCSettings.Representation.ORDERED);

        try {
            if(pnRepResult == null) {
                computeFitness(context, miningAlgorithm, isStructured);
            }
            Object[] res = multiETCPlugin.checkMultiETCAlign1(context, validatingLog, petrinet, settings, pnRepResult);
            multiETCResult = (MultiETCResult) res[0];
            return (Double) (multiETCResult).getAttribute(MultiETCResult.PRECISION);

        } catch (ConnectionCannotBeObtained connectionCannotBeObtained) {
            connectionCannotBeObtained.printStackTrace();
        }
        return 0.0;
    }

    private double computeFitness(UIPluginContext context, MiningAlgorithm miningAlgorithm, boolean isStructured) {
        if(petrinetWithMarking == null) petrinetWithMarking = discoverPetrinet(context, miningAlgorithm, trainingLog, isStructured, xEventClassifier);
        long time = System.nanoTime();
        pnRepResult = computeFitness(context, petrinetWithMarking, validatingLog);
        System.out.println("TIME " + (System.nanoTime() - time));
        Double f = getAlignmentValue(pnRepResult);
        finalMapping = mapping;
        finalParameters = parameters;
        return (f != null)?f:-1.0;
    }

    private double getAlignmentValue(PNRepResult pnRepResult) {
        int unreliable = 0;
        for(SyncReplayResult srp : pnRepResult) {
            if(!srp.isReliable()) {
                unreliable += srp.getTraceIndex().size();
            }
        }
        if(unreliable > pnRepResult.size() / 2) {
            return -1.0;
        }else {
            return (Double) pnRepResult.getInfo().get(PNRepResult.TRACEFITNESS);
        }
    }

    private XLog[] createdXFolds() {

        if(validatingLog.size() < fold) fold = validatingLog.size();
        XLog[] logs = new XLog[fold];

        for(int i = 0; i < fold; i++) {
            logs[i] = factory.createLog(validatingLog.getAttributes());
        }

        if(validatingLog.size() == fold) {
            int pos = 0;
            for (XTrace t : validatingLog) {
                logs[pos].add(t);
                pos++;
            }
        }else {
            boolean finish = false;
            while (!finish) {
                finish = true;
                for (XTrace t : validatingLog) {
                    int pos = r.nextInt(fold);
                    logs[pos].add(t);
                }
                for (int i = 0; i < logs.length; i++) {
                    if (logs[i].size() == 0) {
                        finish = false;
                    }
                }
                if(!finish) {
                    for(int i = 0; i < fold; i++) {
                        logs[i].clear();
                    }
                }
            }
        }

        return logs;
    }

    private double computeXFoldGeneralization(UIPluginContext context, MiningAlgorithm miningAlgorithm, boolean isStructured) {
        double fitness = 0.0;
        XLog[] logs = createdXFolds();

        for(int i = 0; i < fold; i++) {
            PNRepResult result;
            if(fold > 1) {
                XLog log1 = factory.createLog(validatingLog.getAttributes());
                for (int j = 0; j < fold; j++) {
                    if (j != i) {
                        log1.addAll(logs[j]);
                    }
                }
                InfrequentBehaviourFilterPlugin logInspector = new InfrequentBehaviourFilterPluginLPSolve();
                log1 = logInspector.filterLog(log1);

                context.getProvidedObjectManager().createProvidedObject("TrainingLogFold" + i, log1, XLog.class, context);
                context.getGlobalContext().getResourceManager().getResourceForInstance(log1).setFavorite(true);
                context.getProvidedObjectManager().createProvidedObject("ValidatingLogFold" + i, logs[i], XLog.class, context);
                context.getGlobalContext().getResourceManager().getResourceForInstance(logs[i]).setFavorite(true);

                PetrinetWithMarking petrinetWithMarking = discoverPetrinet(context, miningAlgorithm, log1, isStructured, xEventClassifier);
                if(isStructured) {
                    petrinetWithMarking = structurePetriNet(context, petrinetWithMarking);
                }

                result = computeFitness(context, petrinetWithMarking, logs[i]);
            }else {
                result = pnRepResult;
            }

            Double f = getAlignmentValue(result);
            fitness += (f != null)?f:0.0;
        }
        return fitness / (double) fold;
    }

    private double computeXFoldGeneralizationNoise(UIPluginContext context, MiningAlgorithm miningAlgorithm, boolean isStructured) {
        double fitness = 0.0;
        XLog[] logs = createdXFolds();

        for(int i = 0; i < fold; i++) {
            PNRepResult result;
            if(fold > 1) {
                XLog log1 = factory.createLog(validatingLog.getAttributes());
                for (int j = 0; j < fold; j++) {
                    if (j != i) {
                        log1.addAll(logs[j]);
                    }
                }

                PetrinetWithMarking petrinetWithMarking = discoverPetrinet(context, miningAlgorithm, log1, isStructured, xEventClassifier);
                if(isStructured) {
                    petrinetWithMarking = structurePetriNet(context, petrinetWithMarking);
                }

                result = computeFitness(context, petrinetWithMarking, logs[i]);
            }else {
                result = pnRepResult;
            }

            Double f = getAlignmentValue(result);
            fitness += (f != null)?f:0.0;
        }

        return fitness / (double) fold;
    }

    private PNRepResult computeFitness(UIPluginContext context, PetrinetWithMarking petrinetWithMarking, XLog log) {
        Petrinet petrinet = petrinetWithMarking.getPetrinet();
        Marking initialMarking = petrinetWithMarking.getInitialMarking();
        Marking finalMarking = petrinetWithMarking.getFinalMarking();

        context.addConnection(new FinalMarkingConnection(petrinet, finalMarking));

        PetrinetReplayerWithILP replayer = new PetrinetReplayerWithILP();

        XEventClass dummyEvClass = new XEventClass("DUMMY",99999);

        Map<Transition, Integer> transitions2costs = constructTTCMap(petrinet);
        Map<XEventClass, Integer> events2costs = constructETCMap(petrinet, log, dummyEvClass);

        parameters = constructParameters(transitions2costs, events2costs, petrinet, initialMarking, finalMarking);
        mapping = constructMapping(petrinet, log, dummyEvClass);

        try {
            return replayer.replayLog(context, petrinet, log, mapping, parameters);
        } catch (AStarException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        return null;
    }

    private IPNReplayParameter constructParameters(Map<Transition, Integer> transitions2costs, Map<XEventClass, Integer> events2costs, Petrinet petrinet, Marking initialMarking, Marking finalMarking) {
        IPNReplayParameter parameters = new CostBasedCompleteParam(events2costs, transitions2costs);

        parameters.setInitialMarking(initialMarking);
        parameters.setFinalMarkings(finalMarking);
        parameters.setGUIMode(false);
        parameters.setCreateConn(false);
        ((CostBasedCompleteParam) parameters).setMaxNumOfStates(Integer.MAX_VALUE);

        return  parameters;
    }

    private Map<Transition, Integer> constructTTCMap(Petrinet petrinet) {
        Map<Transition, Integer> transitions2costs = new UnifiedMap<Transition, Integer>();

        for(Transition t : petrinet.getTransitions()) {
            if(t.isInvisible()) {
                transitions2costs.put(t, 0);
            }else {
                transitions2costs.put(t, 1);
            }
        }
        return transitions2costs;
    }

    private static Map<XEventClass, Integer> constructETCMap(Petrinet petrinet, XLog log, XEventClass dummyEvClass) {
        Map<XEventClass,Integer> costMOT = new UnifiedMap<XEventClass,Integer>();
        XLogInfo summary = XLogInfoFactory.createLogInfo(log, xEventClassifier);

        for (XEventClass evClass : summary.getEventClasses().getClasses()) {
            int value = 1;
            for(Transition t : petrinet.getTransitions()) {
                if(t.getLabel().equals(evClass.getId())) {
                    value = 1;
                    break;
                }
            }
            costMOT.put(evClass, value);
        }

        costMOT.put(dummyEvClass, 1);

        return costMOT;
    }

    private static TransEvClassMapping constructMapping(Petrinet net, XLog log, XEventClass dummyEvClass) {
        TransEvClassMapping mapping = new TransEvClassMapping(xEventClassifier, dummyEvClass);

        XLogInfo summary = XLogInfoFactory.createLogInfo(log, xEventClassifier);

        for (Transition t : net.getTransitions()) {
            boolean mapped = false;

            for (XEventClass evClass : summary.getEventClasses().getClasses()) {
                String id = evClass.getId();

                if (t.getLabel().equals(id)) {
                    mapping.put(t, evClass);
                    mapped = true;
                    break;
                }
            }

            if (!mapped) {
                mapping.put(t, dummyEvClass);
            }

        }

        return mapping;
    }

    private Marking constructFinalMarking(Petrinet petrinet) {
        Marking finalMarking = new Marking();
        for(Place p : petrinet.getPlaces()) {
            int out = 0;
            for(PetrinetEdge edge : petrinet.getEdges()) {
                if(edge.getSource().equals(p)) {
                    out++;
                }
            }
            if(out == 0) {
                finalMarking.add(p);
            }
        }

        if(finalMarking.size() == 0) {
            for(Place p : petrinet.getPlaces()) {
                if(p.getLabel().equalsIgnoreCase("sink") ||
                        p.getLabel().equalsIgnoreCase("end") ||
                        p.getLabel().equalsIgnoreCase("final")) {
                    finalMarking.add(p);
                }
            }
        }

        if(finalMarking.size() == 0) {
            for(Transition t : petrinet.getTransitions()) {
                if(t.getLabel().toLowerCase().contains("end")) {
                    for(PetrinetEdge e : petrinet.getEdges()) {
                        if(e.getSource().equals(t)) {
                            finalMarking.add((Place) e.getTarget());
                        }
                    }
                }
            }
        }

        return finalMarking;
    }

    private static String calculateDensity(Petrinet petrinet) {
        double arcs = petrinet.getEdges().size();
        double nodes = petrinet.getNodes().size();

        return String.valueOf(arcs / (nodes * (nodes - 1)));
    }

    private static String calculateCNC(Petrinet petrinet) {
        double arcs = petrinet.getEdges().size();
        double nodes = petrinet.getNodes().size();

        return String.valueOf(arcs / nodes);
    }

    private static String calculateMCD(Petrinet petrinet) {
        int size = 0;

        for (PetrinetNode p : petrinet.getNodes()) {
            int count = 0;
            int input = 0;
            int output = 0;
            for (PetrinetEdge f : petrinet.getEdges()) {
                if (f.getSource().equals(p)) {
                    output++;
                    count++;
                }else if(f.getTarget().equals(p)) {
                    input++;
                    count++;
                }
            }
            if(input > 1 || output > 1) {
                size = Math.max(size, count);
            }
        }

        return String.valueOf(size);
    }

    private static String calculateACD(Petrinet petrinet) {
        double size = 0.0;

        int gateways = 0;
        for (PetrinetNode p : petrinet.getNodes()) {
            int count = 0;
            int input = 0;
            int output = 0;
            for (PetrinetEdge f : petrinet.getEdges()) {
                if (f.getSource().equals(p)) {
                    output++;
                    count++;
                }else if(f.getTarget().equals(p)) {
                    input++;
                    count++;
                }
            }
            if(input > 1 || output > 1) {
                size += count;
                gateways++;
            }
        }

        return String.valueOf(size / gateways);
    }

    private static String calculateCFC(Petrinet petrinet) {
        int size = 0;

        for (PetrinetNode p : petrinet.getNodes()) {
            int input = 0;
            int output = 0;
            if(p instanceof Transition) {
                for (PetrinetEdge f : petrinet.getEdges()) {
                    if (f.getSource().equals(p)) {
                        output++;
                    }else if(f.getTarget().equals(p)) {
                        input++;
                    }
                }
                if(input > 1 || output > 1) {
                    size++;
                }
            }else {
                for (PetrinetEdge f : petrinet.getEdges()) {
                    if (f.getSource().equals(p)) {
                        output++;
                    }else if(f.getTarget().equals(p)) {
                        input++;
                    }
                }
                if(input > 1 || output > 1) {
                    size += output;
                }
            }

        }

        return String.valueOf(size);
    }

    private static String calculateSize(Petrinet petrinet) {
        int size = petrinet.getNodes().size();
        return String.valueOf(size);
    }

}
