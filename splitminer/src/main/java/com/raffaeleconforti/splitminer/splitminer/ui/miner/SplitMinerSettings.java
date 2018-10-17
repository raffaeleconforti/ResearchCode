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

package com.raffaeleconforti.splitminer.splitminer.ui.miner;

import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

/**
 * Created by Adriano on 29/02/2016.
 */
public class SplitMinerSettings extends ProMPropertiesPanel {

    private static final long serialVersionUID = 1L;
    private static final String DIALOG_NAME = "Select Split Miner Params";

    final SplitMinerUIResult result;

    JCheckBox replaceIORs;
    JCheckBox removeSelfLoops;
//    ProMComboBox structuring;

    public SplitMinerSettings() {
        super(DIALOG_NAME);

        SMPItemListener smpil = new SMPItemListener();

        LinkedList<String> structuringTime = new LinkedList<>();
        structuringTime.addLast("NONE");
        structuringTime.addLast("PRE");
        structuringTime.addLast("POST");

        result = new SplitMinerUIResult();

//        structuring = this.addComboBox("Structuring Time", structuringTime);
//        structuring.addActionListener(smpil);

        replaceIORs = this.addCheckBox("Remove OR-joins", false);
        replaceIORs.addChangeListener(smpil);

        removeSelfLoops = this.addCheckBox("Remove Self-loops", true);
        removeSelfLoops.addChangeListener(smpil);

        result.setReplaceIORs(replaceIORs.isSelected());
        result.setRemoveSelfLoops(removeSelfLoops.isSelected());
        result.setStructuringTime(SplitMinerUIResult.STRUCT_POLICY);
    }

    public SplitMinerUIResult getSelections() {
        return result;
    }

    private class SMPItemListener implements ChangeListener, ActionListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            result.setReplaceIORs(replaceIORs.isSelected());
            result.setRemoveSelfLoops(removeSelfLoops.isSelected());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof JComboBox) {
                switch (((JComboBox) e.getSource()).getSelectedIndex()) {
                    case 0:
                        result.setStructuringTime(SplitMinerUIResult.StructuringTime.NONE);
                        break;
                    case 1:
                        result.setStructuringTime(SplitMinerUIResult.StructuringTime.PRE);
                        break;
                    case 2:
                        result.setStructuringTime(SplitMinerUIResult.StructuringTime.POST);
                        break;
                }
            }
        }
    }

}
