/*
 *  Copyright (C) 2018 Raffaele Conforti (www.raffaeleconforti.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.raffaeleconforti.bpmnminer.prom.preprocessing.functionaldependencies.ui;

import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class DiscoverERmodel_UI_ignoreAttributes extends ProMPropertiesPanel {

    private static final long serialVersionUID = 1L;

    private static final String DIALOG_NAME = "Attributes to consider for primary key detection";

    private boolean[] selectedAttributes;
    private String[] attributes;

    public DiscoverERmodel_UI_ignoreAttributes(List<String> attributeNames) {
        super(DIALOG_NAME);

        selectedAttributes = new boolean[attributeNames.size()];
        attributes = new String[attributeNames.size()];

        int fkNumIndex = 0;
        for (String attribute : attributeNames) {

            selectedAttributes[fkNumIndex] = true;
            attributes[fkNumIndex] = attribute;

            JCheckBox cb = addCheckBox(attribute, selectedAttributes[fkNumIndex]);
            cb.addActionListener(new UpdateDataListener(fkNumIndex));

            // resize the label for a better layout
            cb.getParent().getComponents()[1].setPreferredSize(new Dimension(800, 30));
            fkNumIndex++;
        }
    }

    public List<String> getIgnoreAttributes() {
        List<String> ignored = new ArrayList<String>();
        for (int i = 0; i < selectedAttributes.length; i++) {
            if (!selectedAttributes[i]) ignored.add(attributes[i]);
        }
        return ignored;
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
                selectedAttributes[index] = ((JCheckBox) e.getSource()).isSelected();
            }
        }
    }
}
