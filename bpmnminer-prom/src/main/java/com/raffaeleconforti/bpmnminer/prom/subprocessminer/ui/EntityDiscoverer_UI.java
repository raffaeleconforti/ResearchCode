package com.raffaeleconforti.bpmnminer.prom.subprocessminer.ui;

import com.raffaeleconforti.bpmnminer.prom.preprocessing.synchtracegeneration.ui.TraceGeneration_UI_other;
import com.raffaeleconforti.bpmnminer.prom.preprocessing.synchtracegeneration.ui.TraceGeneration_UI_subprocesses;
import com.raffaeleconforti.bpmnminer.subprocessminer.EntityDiscoverer;
import com.raffaeleconforti.foreignkeydiscovery.conceptualmodels.ConceptualModel;
import com.raffaeleconforti.foreignkeydiscovery.conceptualmodels.Entity;
import com.raffaeleconforti.foreignkeydiscovery.grouping.Group;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.processmining.contexts.uitopia.UIPluginContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 10/03/2016.
 */
public class EntityDiscoverer_UI {

    public EntityDiscoverer getEntityDiscoverer() {
        return entityDiscoverer;
    }

    EntityDiscoverer entityDiscoverer = new EntityDiscoverer();

    public List<Entity> discoverCandidatesEntities(ConceptualModel concModel, List<Entity> groupEntities) {
        return entityDiscoverer.discoverCandidatesEntities(concModel, groupEntities);
    }

    public List<Entity> discoverGroupEntities(ConceptualModel concModel, UIPluginContext context, boolean all) {
        List<Entity> topEnt = entityDiscoverer.discoverTopEntities(concModel);
        List<Entity> nonTopEnt = entityDiscoverer.discoverNonTopEntities(concModel);

        // show ui to user to confirm/select artifacts
        List<Entity> groupEntities = null;
//        if(!all) {
//            if(topEnt.size() > 0 || nonTopEnt.size() > 0) {
//                TraceGeneration_UI_subprocesses artGui = new TraceGeneration_UI_subprocesses(topEnt, nonTopEnt, all);
//                TaskListener.InteractionResult guiResult = context.showWizard("BPMN Miner > Confirm SubProcesses", true, true, artGui);
//                if (guiResult == TaskListener.InteractionResult.CANCEL) {
//                    throw new ExecutionCancelledException();
//                }
//
//                // get user selection
//                groupEntities = artGui.getSelection();
//            }else {
//                groupEntities = new ArrayList<Entity>();
//            }
//        }

//        groupEntities = entityDiscoverer.setGroupEntities(concModel, groupEntities, all);

        if(!all) {
            if(topEnt.size() > 0 || nonTopEnt.size() > 0) {
                TraceGeneration_UI_subprocesses artGui = new TraceGeneration_UI_subprocesses(topEnt, nonTopEnt, all);

                // get user selection
                groupEntities = artGui.getSelection();
            }else {
                groupEntities = new ArrayList<Entity>();
            }
        }

        groupEntities = entityDiscoverer.setGroupEntities(concModel, groupEntities, all);
        return groupEntities;
    }

    public List<Entity> selectEntities(List<Entity> groupEntities, List<Entity> candidatesEntities, boolean all) {
        List<Entity> selectedEntities = null;

        if (groupEntities.size() > 0 && candidatesEntities.size() > 0) {
            // show ui to user to confirm/select artifacts for the remaining entities
            TraceGeneration_UI_other otherGui = new TraceGeneration_UI_other(candidatesEntities, groupEntities, all);
            // get user selection
            selectedEntities = otherGui.getSelection();
        } else {
            for (int i = 0; i < candidatesEntities.size(); i++) {
                selectedEntities.add(i, groupEntities.get(0));
            }
        }

        return selectedEntities;

    }

    public Set<Group> generateArtifactGroup(UIPluginContext context, List<Entity> groupEntities, List<Entity> candidatesEntities, List<Entity> selectedEntities) {
        //preparation for generating artifact logs
        Set<Group> artifacts = new UnifiedSet<Group>();
        Iterator<Entity> entityIt2 = groupEntities.iterator();

        //for each main entity (artifact)
        while (entityIt2.hasNext() && !context.getProgress().isCancelled()) {
            Entity entity = entityIt2.next();
            //String entityLabel = entity.getLabel();
            Group newgroup = new Group(entity);

            //add timestamps of secondary entities
            for (int i = 0; i < candidatesEntities.size(); i++) {
                if (selectedEntities.get(i).equals(entity)) {
                    Entity e = candidatesEntities.get(i);
                    newgroup.addEntity(e);
                }
            }
            artifacts.add(newgroup);
        }

        return artifacts;

    }

}
