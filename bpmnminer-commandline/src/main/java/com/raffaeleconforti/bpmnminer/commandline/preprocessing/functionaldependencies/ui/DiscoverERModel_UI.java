package com.raffaeleconforti.bpmnminer.commandline.preprocessing.functionaldependencies.ui;

import com.raffaeleconforti.bpmnminer.preprocessing.functionaldependencies.DiscoverERmodel;
import com.raffaeleconforti.foreignkeydiscovery.conceptualmodels.ConceptualModel;
import com.raffaeleconforti.foreignkeydiscovery.functionaldependencies.Data;
import com.raffaeleconforti.foreignkeydiscovery.functionaldependencies.NoEntityException;
import org.deckfour.xes.model.XLog;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 10/03/2016.
 */
public class DiscoverERModel_UI {

    DiscoverERmodel discoverERmodel = new DiscoverERmodel();

    public ConceptualModel showGUI(XLog log, int algorithm) throws NoEntityException {

        List<String> allAttributes = discoverERmodel.generateAllAttributes(log);
        DiscoverERmodel_UI_ignoreAttributes ignoreGui = new DiscoverERmodel_UI_ignoreAttributes(allAttributes);

        /*
         * Step 1: discover primary keys
		 */
        UnifiedMap<String, Data> data = discoverERmodel.generateData(log, ignoreGui.getIgnoreAttributes());

        List<DiscoverERmodel.PrimaryKeyData> pKeyData = DiscoverERmodel.PrimaryKeyData.getData(data);
        DiscoverERmodel_UI_primaryKeys pKeyGui = new DiscoverERmodel_UI_primaryKeys(pKeyData);

        // get user selection
        Map<Set<String>, Set<String>> group = pKeyGui.getSelection();

        discoverERmodel.setPrimaryKeysEntityName(generateEntitiesNames(group));
        ConceptualModel concModel = discoverERmodel.createConceptualModel(group, data);

        return discoverERmodel.showGUI(concModel, discoverERmodel.getPrimaryKeys_entityName(), algorithm);
    }

    public Map<Set<String>, String> generateEntitiesNames(Map<Set<String>, Set<String>> group) throws NoEntityException {
        Map<Set<String>, String> primaryKeys_entityName = new UnifiedMap<>();
        Scanner console = new Scanner(System.in);
        boolean changeParameters = false;
        for (Map.Entry<Set<String>, Set<String>> setSetEntry : group.entrySet()) {
            String value = "";
            if (changeParameters) {
                while (value.isEmpty()) {
                    System.out.println("Select Name for " + DiscoverERmodel.keyToString(setSetEntry.getKey()) + " (Type enter to maintain the name)");
                    value = console.nextLine();
                    if (value.isEmpty()) {
                        value = DiscoverERmodel.keyToString(setSetEntry.getKey());
                    }
                }
            } else {
                value = DiscoverERmodel.keyToString(setSetEntry.getKey());
            }
            primaryKeys_entityName.put(setSetEntry.getKey(), value);
        }

        return primaryKeys_entityName;
    }
}
