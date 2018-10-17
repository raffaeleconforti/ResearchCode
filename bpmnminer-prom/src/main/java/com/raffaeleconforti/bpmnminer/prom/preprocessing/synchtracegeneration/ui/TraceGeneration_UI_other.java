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

package com.raffaeleconforti.bpmnminer.prom.preprocessing.synchtracegeneration.ui;

import com.raffaeleconforti.foreignkeydiscovery.conceptualmodels.Entity;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class TraceGeneration_UI_other extends ProMPropertiesPanel {

    private static final long serialVersionUID = 1L;

    private static final String DIALOG_NAME = "Associate Other Entities to Selected SubProcesses";

    private List<Entity> other;
    private List<Entity> artifacts;
    private int chosenArtifactsIndex[];

    public TraceGeneration_UI_other(List<Entity> other, List<Entity> artifacts, boolean all) {
        super(DIALOG_NAME);
        this.other = other;
        this.artifacts = artifacts;
        chosenArtifactsIndex = new int[other.size()];

        String[] keyList = new String[artifacts.size()];
        // create a new property item for each event type
        for (int i = 0; i < artifacts.size(); i++) {
            // build a list of all artifacts (sets of attribute names)
            keyList[i] = artifacts.get(i).getName();
        }

        for (int i = 0; i < other.size(); i++) {
            chosenArtifactsIndex[i] = 0;
            // and create a combobox having all keys as an available option
            JComponent box;
            box = addComboBox(other.get(i).getName(), keyList);
            ((JComboBox) box).addActionListener(new UpdateDataListener(i));

            // resize the combobox and the label for a better layout
            box.setPreferredSize(new Dimension(500, 30));
            for (Component c : box.getParent().getComponents()) {
                if (c instanceof JLabel) {
                    c.setPreferredSize(new Dimension(500, 30));
                }
            }
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

    public List<Entity> getSelection() {
        List<Entity> selected = new ArrayList<Entity>();
        for (int dataIndex = 0; dataIndex < other.size(); dataIndex++) {
            if (chosenArtifactsIndex[dataIndex] == 0)
                selected.add(dataIndex, artifacts.get(chosenArtifactsIndex[dataIndex]));
        }
        return selected;
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
                chosenArtifactsIndex[index] = ((JComboBox) e.getSource()).getSelectedIndex();
            }
        }
    }


}
