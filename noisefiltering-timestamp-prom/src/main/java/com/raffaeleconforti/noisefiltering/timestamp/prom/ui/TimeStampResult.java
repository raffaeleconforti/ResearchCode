package com.raffaeleconforti.noisefiltering.timestamp.prom.ui;

import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.raffaeleconforti.noisefiltering.timestamp.noise.selection.TimeStampNoiseResult;
import com.raffaeleconforti.noisefiltering.timestamp.permutation.PermutationTechnique;
import com.raffaeleconforti.noisefiltering.timestamp.permutation.PermutationTechniqueFactory;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
                PermutationTechniqueFactory.getPermutationTechniqueName(PermutationTechnique.ILP_LPSOLVE),
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
