package com.raffaeleconforti.wrapper.impl;

import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.wrapper.LogPreprocessing;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import com.raffaeleconforti.marking.MarkingDiscoverer;
import org.deckfour.xes.classification.*;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.plugins.etm.ETM;
import org.processmining.plugins.etm.fitness.metrics.EditDistanceWrapperRTEDAbsolute;
import org.processmining.plugins.etm.fitness.metrics.FitnessReplay;
import org.processmining.plugins.etm.fitness.metrics.OverallFitness;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.TreeUtils;
import org.processmining.plugins.etm.model.narytree.conversion.NAryTreeToProcessTree;
import org.processmining.plugins.etm.parameters.ETMParam;
import org.processmining.plugins.etm.parameters.ETMParamFactory;
import org.processmining.plugins.etm.ui.plugins.ETMPlugin;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.TerminationCondition;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.termination.GenerationCount;

import java.util.Iterator;
import java.util.List;

/**
 * Created by conforti on 9/02/2016.
 */
@Plugin(name = "Evolutionary Tree Miner Wrapper", parameterLabels = {"Log"},
        returnLabels = {"PetrinetWithMarking"},
        returnTypes = {PetrinetWithMarking.class})
public class EvolutionaryTreeMinerWrapper implements MiningAlgorithm {

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@qut.edu.au",
            pack = "Noise Filtering")
    @PluginVariant(variantLabel = "Evolutionary Tree Miner Wrapper", requiredParameterLabels = {0})
    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log) {
        return minePetrinet(context, log, false);
    }

    @Override
    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log, boolean structure) {
        try {
            LogPreprocessing logPreprocessing = new LogPreprocessing();
            log = logPreprocessing.preprocessLog(context, log);

//            System.setOut(new PrintStream(new OutputStream() {
//                @Override
//                public void write(int b) throws IOException {}
//            }));

            ProcessTree processTree;
            if(context instanceof FakePluginContext) {
                PluginContext pluginContext = new UIContext().getMainPluginContext();
                ETMParam params = ETMParamFactory.buildStandardParam(log, pluginContext);

                System.out.println(params.getPopulationSize());
                params.setMaxThreads(Runtime.getRuntime().availableProcessors());

                XEventClassifier classifier = new XEventNameClassifier();//new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
                params.getCentralRegistry().updateEventClassifier(classifier);
                params.addTerminationConditionMaxDuration(3600000); //1 hour

//                ETM etm = new ETM(params);
//                System.out.println("Starting the ETM");
//                etm.run();
//                List stopped = etm.getSatisfiedTerminationConditions();
//                Iterator tree = stopped.iterator();
//
//                while(tree.hasNext()) {
//                    TerminationCondition cond = (TerminationCondition)tree.next();
//                    System.out.println(cond.toString());
//                }
//
//                NAryTree tree1 = etm.getResult();
//                System.out.println("Tree: " + TreeUtils.toString(tree1, params.getCentralRegistry().getEventClasses()));
//                System.out.println("Fitness: " + params.getCentralRegistry().getFitness(tree1).fitnessValues);
//                System.out.println("Discovered tree: " + TreeUtils.toString(tree1, params.getCentralRegistry().getEventClasses()));
//                processTree = NAryTreeToProcessTree.convert(params.getCentralRegistry().getEventClasses(), tree1, "Process tree discovered by the ETM algorithm");

                ETMPlugin etmPlugin = new ETMPlugin();
                processTree = etmPlugin.withoutSeedParams(pluginContext, log, params);
            }else {
                ETMPlugin etmPlugin = new ETMPlugin();
                processTree = etmPlugin.withoutSeed(context, log);
            }

//            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
            ProcessTree2Petrinet.PetrinetWithMarkings petrinetWithMarkings = ProcessTree2Petrinet.convert(processTree, true);

            logPreprocessing.removedAddedElements(petrinetWithMarkings.petrinet);

            MarkingDiscoverer.createInitialMarkingConnection(context, petrinetWithMarkings.petrinet, petrinetWithMarkings.initialMarking);
            MarkingDiscoverer.createFinalMarkingConnection(context, petrinetWithMarkings.petrinet, petrinetWithMarkings.finalMarking);
            return new PetrinetWithMarking(petrinetWithMarkings.petrinet, petrinetWithMarkings.initialMarking, petrinetWithMarkings.finalMarking);
        } catch (ProcessTree2Petrinet.InvalidProcessTreeException e) {
            e.printStackTrace();
        } catch (ProcessTree2Petrinet.NotYetImplementedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, boolean structure) {
        PetrinetWithMarking petrinetWithMarking = minePetrinet(context, log, structure);
        return PetriNetToBPMNConverter.convert(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking(), true);
    }

    @Override
    public String getAlgorithmName() {
        return "Evolutionary Tree Miner";
    }

    @Override
    public String getAcronym() { return "ETM";}
}
