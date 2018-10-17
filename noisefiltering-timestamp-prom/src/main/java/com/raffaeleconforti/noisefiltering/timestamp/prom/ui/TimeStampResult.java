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

package com.raffaeleconforti.noisefiltering.timestamp.prom.ui;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.raffaeleconforti.noisefiltering.timestamp.permutation.PermutationTechnique;
import com.raffaeleconforti.noisefiltering.timestamp.permutation.PermutationTechniqueFactory;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by conforti on 26/02/15.
 */
public class TimeStampResult extends ProMPropertiesPanel {

    private static final long serialVersionUID = 1L;

    private static final String DIALOG_NAME = "Select Timestamp Filtering Approach";

    private int approach;

    JComboBox comboBox;

    public TimeStampResult() {
        super(DIALOG_NAME);

        UpdateDataListener udl = new UpdateDataListener();
        comboBox = SlickerFactory.instance().createComboBox(new Object[] {
                PermutationTechniqueFactory.getPermutationTechniqueName(PermutationTechnique.ILP_GUROBI),
                PermutationTechniqueFactory.getPermutationTechniqueName(PermutationTechnique.ILP_GUROBI_ARCS),
                PermutationTechniqueFactory.getPermutationTechniqueName(PermutationTechnique.ILP_LPSOLVE),
                PermutationTechniqueFactory.getPermutationTechniqueName(PermutationTechnique.ILP_LPSOLVE_ARCS),
                PermutationTechniqueFactory.getPermutationTechniqueName(PermutationTechnique.HEURISTICS_BEST),
                PermutationTechniqueFactory.getPermutationTechniqueName(PermutationTechnique.HEURISTICS_SET)
        });
        comboBox.addActionListener(udl);
        add(comboBox);
    }

    public int getApproach() {
        return approach;
    }

    private class UpdateDataListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() instanceof JComboBox) {
                JComboBox comboBox = (JComboBox) e.getSource();
                approach = comboBox.getSelectedIndex();
            }
        }
    }

}
