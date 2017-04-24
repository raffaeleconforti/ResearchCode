package com.raffaeleconforti.structuredminer.ui;


import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Created by Raffaele Conforti on 20/02/14.
 */
public class SettingsStructuredMinerFrame extends ProMPropertiesPanel {

    private static final long serialVersionUID = 1L;

    private static final String DIALOG_NAME = "Select Mining Algorithm";

    final int[] selectedAttributes;
    final SettingsStructuredMiner settings = new SettingsStructuredMiner(0);

//    JCheckBox uma;
//    JCheckBox simplify;
final JCheckBox structured;

    public SettingsStructuredMinerFrame(List<String> attributeNames) {
        super(DIALOG_NAME);

        selectedAttributes = new int[attributeNames.size()];

        JComboBox cb = addComboBox("Mining Algorithm", attributeNames);
        UpdateDataListener udl = new UpdateDataListener();
        cb.addActionListener(udl);

//        uma = addCheckBox("Repair with Uma", true);
//        uma.addChangeListener(udl);
//
//        simplify = addCheckBox("Simplify with Uma", true);
//        simplify.addChangeListener(udl);

        structured = addCheckBox("Structured", true);
        structured.addChangeListener(udl);
    }

    public SettingsStructuredMiner getSelections() {
        return settings;
    }

    /**
     * Listener to copy the selection from the comboboxes to a readable data-structure.
     *
     * @author dfahland
     */
    private class UpdateDataListener implements ActionListener, ChangeListener {

        public void actionPerformed(ActionEvent e) {
            if (e.getID() == ActionEvent.ACTION_PERFORMED && e.getSource() instanceof JComboBox) {
                settings.setSelectedAlgorithm(((JComboBox) e.getSource()).getSelectedIndex());
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            JCheckBox jCheckBox = (JCheckBox) e.getSource();
//            if(e.getSource().equals(uma)) {
//                settings.setUma(jCheckBox.isSelected());
//            }else if(e.getSource().equals(simplify)) {
//                settings.setSimplify(jCheckBox.isSelected());
//            }else
            if(e.getSource().equals(structured)) {
                settings.setStructured(jCheckBox.isSelected());
            }
        }
    }
}
