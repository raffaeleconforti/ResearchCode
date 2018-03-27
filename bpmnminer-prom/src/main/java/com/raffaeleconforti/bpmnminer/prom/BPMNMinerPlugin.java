package com.raffaeleconforti.bpmnminer.prom;

import com.raffaeleconforti.bpmnminer.exception.ExecutionCancelledException;
import com.raffaeleconforti.bpmnminer.prom.preprocessing.functionaldependencies.ui.DiscoverERModel_UI;
import com.raffaeleconforti.bpmnminer.prom.subprocessminer.ui.EntityDiscoverer_UI;
import com.raffaeleconforti.bpmnminer.prom.subprocessminer.ui.SelectMinerUI;
import com.raffaeleconforti.bpmnminer.subprocessminer.BPMNSubProcessMiner;
import com.raffaeleconforti.bpmnminer.subprocessminer.EntityDiscoverer;
import com.raffaeleconforti.bpmnminer.subprocessminer.selection.SelectMinerResult;
import com.raffaeleconforti.foreignkeydiscovery.conceptualmodels.ConceptualModel;
import com.raffaeleconforti.foreignkeydiscovery.conceptualmodels.Entity;
import com.raffaeleconforti.foreignkeydiscovery.functionaldependencies.NoEntityException;
import com.raffaeleconforti.log.util.LogOptimizer;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by conforti on 30/03/15.
 */
@Plugin(name = "BPMN Miner", level = PluginLevel.PeerReviewed, parameterLabels = {"Log"},
        returnLabels = {"BPMNDiagram"},
        returnTypes = {BPMNDiagram.class})
public class BPMNMinerPlugin {

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "R. Conforti, M. Dumas, L. Garcia-Banuelos, M. La Rosa",
            email = "raffaele.conforti@unimelb.edu.au, m.larosa@unimelb.edu.au, marlon.dumas@ut.ee, luciano.garcia@ut.ee",
            pack = "BPMNMiner (raffaele.conforti@unimelb.edu.au)")
    @PluginVariant(variantLabel = "BPMN Miner", requiredParameterLabels = {0})//, 1, 2, 3 })
    public BPMNDiagram mineBPMNModel(final UIPluginContext context, XLog rawlog) {
        try {
            return mineBPMNModelWithException(context, rawlog);
        } catch (ExecutionCancelledException e) {
            context.getFutureResult(0).cancel(true);
            return null;
        }

    }

    public BPMNDiagram mineBPMNModelWithException(final UIPluginContext context, XLog rawlog) throws ExecutionCancelledException {
        BPMNSubProcessMiner subProcessMiner = new BPMNSubProcessMiner();

        LogOptimizer logOptimizer = new LogOptimizer();
        XLog optimizedLog = logOptimizer.optimizeLog(rawlog);
        rawlog = optimizedLog;

        UIPluginContext pluginContext = context;
        pluginContext.getProgress().setIndeterminate(true);
        pluginContext.getProgress().setCaption("BPMN Miner");

        pluginContext.getProgress().setIndeterminate(false);
        pluginContext.getProgress().setMinimum(0);
        pluginContext.getProgress().setMaximum(20);//concModel.getEntities().size() + 2);
        pluginContext.getProgress().setValue(1);

        EntityDiscoverer_UI entityDiscoverer_ui = new EntityDiscoverer_UI();
        EntityDiscoverer entityDiscoverer = entityDiscoverer_ui.getEntityDiscoverer();

        //Select Miner
        SelectMinerUI selectMinerUI = new SelectMinerUI();
        SelectMinerResult guiResult;

        UIPluginContext testContext = pluginContext.createChildContext("Select Mining Algorithm");
        guiResult = selectMinerUI.showGUI(testContext);

        Integer algorithm = 1;

        //discover the ER model
        DiscoverERModel_UI ERmodel = new DiscoverERModel_UI();
        ConceptualModel concModel = null;

        //ui choose artifacts from entities
        //------------------------------------------
        List<Entity> groupEntities = new ArrayList<Entity>();
        List<Entity> candidatesEntities = new ArrayList<Entity>();
        List<Entity> selectedEntities = new ArrayList<Entity>();

        try {
            UIPluginContext newContext = pluginContext.createChildContext("Discover ER Model");
            concModel = ERmodel.showGUI(newContext, rawlog, algorithm);

            if(concModel != null) {
                System.out.println("Discovering groupEntities...");
                groupEntities = entityDiscoverer_ui.discoverGroupEntities(concModel, context, false);
                System.out.println("groupEntities discovered");

                System.out.println("Discovering candidatesEntities...");
                candidatesEntities = entityDiscoverer_ui.discoverCandidatesEntities(concModel, groupEntities);
                System.out.println("candidatesEntities discovered");

                System.out.println("Discovering selectedEntities...");
                selectedEntities = entityDiscoverer_ui.selectEntities(groupEntities, candidatesEntities, false);
                System.out.println("selectedEntities discovered");
            }
        } catch (NoEntityException nee) {}

        StringBuilder sb = new StringBuilder();
        if (groupEntities != null) {
            sb.append("groupEntities:\n");
            for (Entity e : groupEntities) {
                sb.append(e.toString()).append("\n");
            }
        }
        if (candidatesEntities != null) {
            sb.append("candidatesEntities:\n");
            for (Entity e : candidatesEntities) {
                sb.append(e.toString()).append("\n");
            }
        }
        if(selectedEntities != null) {
            sb.append("selectedEntities:\n");
            for (Entity e : selectedEntities) {
                sb.append(e.toString()).append("\n");
            }
        }
        System.out.println(sb.toString());

        BPMNDiagram model = subProcessMiner.mineBPMNModel(context, rawlog, false, guiResult, algorithm, entityDiscoverer, concModel, groupEntities, candidatesEntities, selectedEntities, false);

        return model;

    }

}
