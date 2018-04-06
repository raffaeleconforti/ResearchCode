package com.raffaeleconforti.bpmnminer.commandline.preprocessing.functionaldependencies.ui;

import com.raffaeleconforti.bpmnminer.preprocessing.functionaldependencies.DiscoverERmodel;
import com.raffaeleconforti.foreignkeydiscovery.functionaldependencies.NoEntityException;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class DiscoverERmodel_UI_primaryKeys {

    private List<DiscoverERmodel.PrimaryKeyData> data;
    private int chosenKeysIndex[];

    public DiscoverERmodel_UI_primaryKeys(List<DiscoverERmodel.PrimaryKeyData> data) throws NoEntityException {
        this.data = data;

        chosenKeysIndex = new int[data.size()];

        String token;
        Scanner console = new Scanner(System.in);

        int dataIndex = 0;
        // create a new property item for each event type
        for (DiscoverERmodel.PrimaryKeyData currentData : data) {

            chosenKeysIndex[dataIndex] = 0;

            // build a list of all keys (sets of attribute names)
            Integer value = null;

            if(currentData.primaryKeys.length > 1) {
                System.out.println("Select primary key " + currentData.name + ":");
                String[] keyList = new String[currentData.primaryKeys.length]; //change if user can select any attributes for identifiers
                for (int i = 0; i < currentData.primaryKeys.length; i++) {
                    UnifiedSet<String> attr = currentData.primaryKeys[i];
                    keyList[i] = DiscoverERmodel.keyToString(attr);
                    System.out.println((i + 1) + ") " + keyList[i]);
                }

                while (value == null) {
                    token = console.nextLine();
                    try {
                        value = Integer.parseInt(token) - 1;
                        if (value < 0 || value >= keyList.length) {
                            System.out.println("Select a number between 1 and " + keyList.length);
                            value = null;
                        } else {
                            chosenKeysIndex[dataIndex] = value;
                        }
                    } catch (NumberFormatException nfe) {
                        System.out.println("Select a number between 1 and " + keyList.length);
                        value = null;
                    }
                }
            }else {
                chosenKeysIndex[dataIndex] = 0;
            }

            dataIndex++;
        }
    }

    /**
     * @param set
     * @return string to render list of attributes in a tooltip
     */
    protected static String attributesToToolTipString(String set[]) {
        StringBuilder keyString = new StringBuilder("(");
        for (int i = 0; i < set.length; i++) {
            keyString.append(set[i]).append(i == set.length - 1 ? ") " : ",\n");
        }
        return keyString.toString();
    }

    /**
     * Accumlate the selection and return a mapping from each primary key to the
     * set of event names which have this primary key.
     *
     * @return
     */
    public Map<Set<String>, Set<String>> getSelection() {
        Map<Set<String>, Set<String>> group = new UnifiedMap<Set<String>, Set<String>>();

        for (int dataIndex = 0; dataIndex < data.size(); dataIndex++) {
            Set<String> primaryKey = data.get(dataIndex).primaryKeys[chosenKeysIndex[dataIndex]];
            Set<String> set;
            if ((set = group.get(primaryKey)) == null) {
                set = new UnifiedSet<String>();
                group.put(primaryKey, set);
            }
            set.add(data.get(dataIndex).name);
        }

        return group;
    }


}
