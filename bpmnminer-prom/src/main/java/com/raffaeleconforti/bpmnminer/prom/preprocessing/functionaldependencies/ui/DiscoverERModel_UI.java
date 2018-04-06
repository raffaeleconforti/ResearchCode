package com.raffaeleconforti.bpmnminer.prom.preprocessing.functionaldependencies.ui;

import com.raffaeleconforti.bpmnminer.exception.ExecutionCancelledException;
import com.raffaeleconforti.bpmnminer.preprocessing.functionaldependencies.DiscoverERmodel;
import com.raffaeleconforti.bpmnminer.preprocessing.inclusiondependencies.InclusionDependencies;
import com.raffaeleconforti.foreignkeydiscovery.conceptualmodels.Cardinality;
import com.raffaeleconforti.foreignkeydiscovery.conceptualmodels.ConceptualModel;
import com.raffaeleconforti.foreignkeydiscovery.conceptualmodels.Relationship;
import com.raffaeleconforti.foreignkeydiscovery.functionaldependencies.Data;
import com.raffaeleconforti.foreignkeydiscovery.functionaldependencies.NoEntityException;
import com.raffaeleconforti.foreignkeydiscovery.functionaldependencies.TANEjava;
import org.deckfour.uitopia.api.event.TaskListener;
import org.deckfour.xes.model.XLog;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.widgets.ProMTextField;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 10/03/2016.
 */
public class DiscoverERModel_UI {

    DiscoverERmodel discoverERmodel = new DiscoverERmodel();

    public ConceptualModel showGUI(UIPluginContext context, XLog log, int algorithm) throws NoEntityException, ExecutionCancelledException {

        List<String> allAttributes = new ArrayList<String>(discoverERmodel.getLogAttributes(log));
        allAttributes.remove("concept:name");
        allAttributes.remove("time:timestamp");
        allAttributes.remove("lifecycle:transition");
        allAttributes.remove("org:resource");

        // show ui to user to confirm/select primary keys
        if(allAttributes.size() == 0) {
            return null;
        }

        DiscoverERmodel_UI_ignoreAttributes ignoreGui = new DiscoverERmodel_UI_ignoreAttributes(allAttributes);
        TaskListener.InteractionResult guiResult = context.showWizard("Discover ER Model > Select Attributes for Discovery", true, true, ignoreGui);
        if (guiResult == TaskListener.InteractionResult.CANCEL) {
            throw new ExecutionCancelledException();
        }

        context.getProgress().setIndeterminate(true);
        context.getProgress().setCaption("Processing Raw Logs");

        UnifiedMap<String, Data> data = discoverERmodel.transferData(log, ignoreGui.getIgnoreAttributes());

        context.getProgress().setIndeterminate(false);
        context.getProgress().setMinimum(0);
        context.getProgress().setMaximum(data.size() + 1);
        context.getProgress().setValue(1);

        //find functional dependencies and keys
        Data currentData;
        TANEjava tane;
        for (Data data1 : data.values()) {
            currentData = data1;

            try {
                tane = new TANEjava(currentData);
                tane.setConsoleOutput(false);
                tane.getFD();
                tane.getKeys();
            } catch (Exception e) {
                e.printStackTrace();
            }
            context.getProgress().inc();
        }

		/*
         * Step 1: discover primary keys
		 */
        List<DiscoverERmodel.PrimaryKeyData> pKeyData = DiscoverERmodel.PrimaryKeyData.getData(data);

        // show ui to user to confirm/select primary keys
        Map<Set<String>, Set<String>> group;
        if(pKeyData.size() > 0) {
            DiscoverERmodel_UI_primaryKeys pKeyGui = new DiscoverERmodel_UI_primaryKeys(pKeyData);
            guiResult = context.showWizard("Discover ER Model > Select Primary Keys of Event Types",
                    true, true, pKeyGui);
            if (guiResult == TaskListener.InteractionResult.CANCEL) {
                throw new ExecutionCancelledException();
            }

            // get user selection
            group = pKeyGui.getSelection();
        }else {
            group = new UnifiedMap<Set<String>, Set<String>>();
        }
		/*
         * Step 1b: show entities and allow user to give each entity a name
		 */
        Map<Set<String>, ProMTextField> entityName_newValues = new UnifiedMap<Set<String>, ProMTextField>();
        ProMPropertiesPanel overView = new ProMPropertiesPanel("Discovered ER Model");
        ProMTextField tf;
        DefaultListModel attributes;
        ProMList attributeList;
        for (Map.Entry<Set<String>, Set<String>> setSetEntry : group.entrySet()) {
            tf = overView.addTextField("entity name", DiscoverERmodel.keyToString(setSetEntry.getKey()));
            entityName_newValues.put(setSetEntry.getKey(), tf);

            attributes = new DefaultListModel();
            for (String ts : setSetEntry.getValue()) {
                attributes.addElement(ts);
            }
            attributeList = new ProMList(null, attributes);

            overView.addProperty("events", attributeList);
        }

        // store names of entities
        Map<Set<String>, String> primaryKeys_entityName = new UnifiedMap<Set<String>, String>();
        for (Map.Entry<Set<String>, ProMTextField> setProMTextFieldEntry : entityName_newValues.entrySet()) {
            primaryKeys_entityName.put(setProMTextFieldEntry.getKey(), setProMTextFieldEntry.getValue().getText());
        }

        // and initialize conceptual model with entities
        discoverERmodel.setPrimaryKeysEntityName(primaryKeys_entityName);
        ConceptualModel concModel = discoverERmodel.createConceptualModel(group, data);

		/*
         * Step 2: discover relationships (foreign key-primary key)
		 */
        if(algorithm == 1) {
            InclusionDependencies d = new InclusionDependencies(concModel, discoverERmodel.getAllInstances());
            List<? extends List<? extends List<Object>>> candidateFKeys = d.getDependencies(concModel, discoverERmodel.getAllInstances());
            // show user a dialog to select which relation ships to use
            List<DiscoverERmodel.ForeignKeyData> fkeyData = DiscoverERmodel.ForeignKeyData.getForeignKeyData(candidateFKeys);

            DiscoverERmodel.ForeignKeyData fKey;
            Relationship r;
            for (int i = 0; i < fkeyData.size(); i++) {
                // define mappings from foreign-key attributes to primary-key attributes
                fKey = fkeyData.get(i);
                for (int j = 0; j < fKey.e1_foreignKey.size(); j++) {
                    fKey.e1.makeForeignKey(fKey.e1_foreignKey.get(j), fKey.e2, fKey.e2_primaryKey.get(j));
                }

                // define relationship between entities with basic cardinality
                r = concModel.getRelationship(fKey.e1, fKey.e2);
                if (r == null) { //adding a new relationship
                    concModel.addRelationship(new Relationship(fKey.e1, fKey.e2, Cardinality.ZERO_OR_ONE,
                            Cardinality.ZERO_OR_ONE));
                }
            }
        }

		/*
         * Step 3: discover cardinalities
		 */
        concModel.findCardinalitites(discoverERmodel.getAllInstances());

        concModel.findHorizon();
        concModel.findTopEntities(discoverERmodel.getAllInstances());

        return concModel;
    }

    public Map<Set<String>, String> generateEntitiesNames(Map<Set<String>, Set<String>> group) throws NoEntityException {
        Map<Set<String>, String> primaryKeys_entityName = new UnifiedMap<Set<String>, String>();
        Map<Set<String>, ProMTextField> entityName_newValues = new UnifiedMap<Set<String>, ProMTextField>();
        ProMPropertiesPanel overView = new ProMPropertiesPanel("Discovered ER Model");
        ProMTextField tf;
        DefaultListModel attributes;
        ProMList attributeList;
        for (Map.Entry<Set<String>, Set<String>> setSetEntry : group.entrySet()) {
            tf = overView.addTextField("entity name", DiscoverERmodel.keyToString(setSetEntry.getKey()));
            entityName_newValues.put(setSetEntry.getKey(), tf);

            attributes = new DefaultListModel();
            for (String ts : setSetEntry.getValue()) {
                attributes.addElement(ts);
            }
            attributeList = new ProMList(null, attributes);

            overView.addProperty("events", attributeList);
        }

        // store names of entities
        for (Map.Entry<Set<String>, ProMTextField> setProMTextFieldEntry : entityName_newValues.entrySet()) {
            primaryKeys_entityName.put(setProMTextFieldEntry.getKey(), setProMTextFieldEntry.getValue().getText());
        }

        return primaryKeys_entityName;
    }
}
