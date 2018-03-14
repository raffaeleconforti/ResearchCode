package com.raffaeleconforti.measurements.ui.computemeasurement;

import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.factory.SlickerFactory;
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
public class SelectMinerCM extends ProMPropertiesPanel {

    private static final long serialVersionUID = 1L;

    private static final String DIALOG_NAME = "Select Mining Algorithm";

    private final int[] selectedAttributes;
    private SelectMinerUIResultCM result = null;

    private final JCheckBox fitness;
    private final JCheckBox precision;
    private final JCheckBox generalization;
    private final JCheckBox simplicity;
    private final JCheckBox noise;

    public SelectMinerCM(List<String> attributeNames) {
        super(DIALOG_NAME);

        result = new SelectMinerUIResultCM(0);

        selectedAttributes = new int[attributeNames.size()];

        JComboBox cb = addComboBox("Mining Algorithm", attributeNames);
        UpdateDataListener udl = new UpdateDataListener();
        cb.addActionListener(udl);

        fitness = addCheckBox("Fitness", true);
        fitness.addChangeListener(udl);

        precision = addCheckBox("Precision", true);
        precision.addChangeListener(udl);

        generalization = addCheckBox("Generalization", true);
        generalization.addChangeListener(udl);

        noise = addCheckBox("Filter Noise", false);
        noise.addChangeListener(udl);

        simplicity = addCheckBox("Simplicity", true);
        simplicity.addChangeListener(udl);

        NiceIntegerSlider fold = SlickerFactory.instance().createNiceIntegerSlider("Number Folds", 1, 100, 10, NiceSlider.Orientation.HORIZONTAL);
        fold.addChangeListener(udl);
        add(fold);

    }

    public SelectMinerUIResultCM getSelections() {
        return result;
    }

    /**
     * Listener to copy the selection from the comboboxes to a readable data-structure.
     *
     * @author dfahland
     */
    private class UpdateDataListener implements ActionListener, ChangeListener {

        public void actionPerformed(ActionEvent e) {
            if (e.getID() == ActionEvent.ACTION_PERFORMED && e.getSource() instanceof JComboBox) {
                result.setSelectedAlgorithm(((JComboBox) e.getSource()).getSelectedIndex());
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if(e.getSource() instanceof JSlider) {
                result.setFold(((JSlider) e.getSource()).getValue());
            }else {
                JCheckBox jCheckBox = (JCheckBox) e.getSource();
                if(e.getSource().equals(fitness)) {
                    result.setFitness(jCheckBox.isSelected());
                }else if(e.getSource().equals(precision)) {
                    result.setPrecision(jCheckBox.isSelected());
                }else if(e.getSource().equals(generalization)) {
                    result.setGeneralization(jCheckBox.isSelected());
                }else if(e.getSource().equals(noise)) {
                    result.setNoise(jCheckBox.isSelected());
                }else if(e.getSource().equals(simplicity)) {
                    result.setSemplicity(jCheckBox.isSelected());
                }
            }
        }
    }
}
