package com.raffaeleconforti.bpmnminer.prom.preprocessing.functionaldependencies.ui;

import com.raffaeleconforti.bpmnminer.preprocessing.functionaldependencies.DiscoverERmodel;
import com.raffaeleconforti.foreignkeydiscovery.conceptualmodels.Attribute;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.widgets.WidgetColors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.List;

public class DiscoverERmodel_UI_foreignKey extends ProMPropertiesPanel {

    private static final long serialVersionUID = 1L;

    private static final String DIALOG_NAME = "Foreign Key-Primary Key Relations";

    private boolean[] selectedForeignKeys;

    public DiscoverERmodel_UI_foreignKey(List<DiscoverERmodel.ForeignKeyData> data) {
        super(DIALOG_NAME);

        selectedForeignKeys = new boolean[data.size()];
        for(int i = 0; i < selectedForeignKeys.length; i++) {
            selectedForeignKeys[i] = true;
        }

        int fkNumIndex = 0;
        for (DiscoverERmodel.ForeignKeyData currentData : data) {
            JLabel l = addProperty("from " + currentData.e1.getName() + " to " + currentData.e2.getName(), new JLabel());
            // resize the label for a better layout
            l.getParent().getComponents()[1].setPreferredSize(new Dimension(800, 30));
            // remove mouse listeners on this one: passive element
            l.getParent().setBackground(WidgetColors.HEADER_COLOR);
            for (MouseListener ml : l.getParent().getMouseListeners())
                l.getParent().removeMouseListener(ml);

            JCheckBox cb = addCheckBox("    " + getKeyString(currentData.e1_foreignKey) + " is a foreign key to " + getKeyString(currentData.e2_primaryKey),
                    selectedForeignKeys[fkNumIndex]);
            cb.setSelected(true);
            cb.addActionListener(new UpdateDataListener(fkNumIndex));

            // resize the label for a better layout
            cb.getParent().getComponents()[1].setPreferredSize(new Dimension(800, 30));

            fkNumIndex++;
        }
    }

    private static String getKeyString(List<Attribute> key) {
        StringBuilder result = new StringBuilder("(");
        for (int i = 0; i < key.size(); i++) {
            if (i > 0) result.append(", ");
            result.append(key.get(i).getName());
        }
        result.append(")");
        return result.toString();
    }

    public boolean[] getSelection() {
        return selectedForeignKeys;
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
                    && e.getSource() instanceof JCheckBox) {
                selectedForeignKeys[index] = ((JCheckBox) e.getSource()).isSelected();
            }
        }
    }
}
