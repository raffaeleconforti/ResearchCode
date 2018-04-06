package com.raffaeleconforti.bpmnminer.prom.preprocessing.functionaldependencies.ui;

import com.raffaeleconforti.bpmnminer.preprocessing.functionaldependencies.DiscoverERmodel;
import com.raffaeleconforti.foreignkeydiscovery.functionaldependencies.NoEntityException;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.widgets.ProMTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DiscoverERmodel_UI_primaryKeys extends ProMPropertiesPanel {

    private static final long serialVersionUID = 1L;

    private static final String DIALOG_NAME = "Event Types and Available Primary Keys";

    private List<DiscoverERmodel.PrimaryKeyData> data;
    private int chosenKeysIndex[];

    public DiscoverERmodel_UI_primaryKeys(List<DiscoverERmodel.PrimaryKeyData> data) throws NoEntityException {
        super(DIALOG_NAME);
        this.data = data;

        chosenKeysIndex = new int[data.size()];

        int dataIndex = 0;
        // create a new property item for each event type
        for (DiscoverERmodel.PrimaryKeyData currentData : data) {

            chosenKeysIndex[dataIndex] = 0;

            // build a list of all keys (sets of attribute names)
            String[] keyList = new String[currentData.primaryKeys.length]; //change if user can select any attributes for identifiers
            for (int i = 0; i < currentData.primaryKeys.length; i++) {
                UnifiedSet<String> attr = currentData.primaryKeys[i];
                keyList[i] = DiscoverERmodel.keyToString(attr);
            }

            // and create a combobox having all keys as an available option
            JComponent box;
            if (keyList.length > 1) {
                box = addComboBox(currentData.name, keyList);
                ((JComboBox) box).addActionListener(new UpdateDataListener(dataIndex));
            } else {
                box = addTextField(currentData.name, keyList[0]);
                ((ProMTextField) box).setEditable(false);
            }

            // resize the combobox and the label for a better layout
            box.setPreferredSize(new Dimension(500, 30));
            for (Component c : box.getParent().getComponents()) {
                if (c instanceof JLabel) {
                    c.setPreferredSize(new Dimension(500, 30));
                }
            }
            // all available attributes of an event type are rendered in a tooltip
            box.setToolTipText("All available attributes:\n" + attributesToToolTipString(currentData.attributes));

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

    /**
     * Listener to copy the selection from the comboboxes to a readable data-structure.
     *
     * @author dfahland
     */
    private class UpdateDataListener implements ActionListener {

        private int index;

        public UpdateDataListener(int index) {
            this.index = index;
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getID() == ActionEvent.ACTION_PERFORMED
                    && e.getSource() instanceof JComboBox) {
                chosenKeysIndex[index] = ((JComboBox) e.getSource()).getSelectedIndex();
            }
        }
    }


}
