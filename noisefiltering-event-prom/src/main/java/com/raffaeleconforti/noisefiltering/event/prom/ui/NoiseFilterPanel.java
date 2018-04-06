package com.raffaeleconforti.noisefiltering.event.prom.ui;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.raffaeleconforti.noisefiltering.event.infrequentbehaviour.automaton.AutomatonInfrequentBehaviourDetector;
import com.raffaeleconforti.noisefiltering.event.prom.InfrequentBehaviourFilterPlugin;
import com.raffaeleconforti.noisefiltering.event.selection.NoiseFilterResult;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by conforti on 26/02/15.
 */
public class NoiseFilterPanel extends ProMPropertiesPanel {

    private static final long serialVersionUID = 1L;

    private static final String DIALOG_NAME = "Select Noise Threshold";

    private final InfrequentBehaviourFilterPlugin infrequentBehaviourFilterPlugin;
    private final double[] arcs;

    private final NoiseFilterResult result;
    private final NiceDoubleSlider percentile;
    private final NiceDoubleSlider noiseLevel;
    private final JCheckBox repeated;
    private final JCheckBox fix;

    public NoiseFilterPanel(InfrequentBehaviourFilterPlugin infrequentBehaviourFilterPlugin, double[] arcs) {
        super(DIALOG_NAME);

        this.infrequentBehaviourFilterPlugin = infrequentBehaviourFilterPlugin;
        this.arcs = arcs;
        double noiseLevelValue = infrequentBehaviourFilterPlugin.discoverThreshold(arcs, 0.125);

        result = new NoiseFilterResult();
        result.setRepeated(true);
        result.setFixLevel(false);
        result.setNoiseLevel(noiseLevelValue);
        result.setPercentile(0.125);

        UpdateDataListener udl = new UpdateDataListener();

        percentile = SlickerFactory.instance().createNiceDoubleSlider("Maximum Percentile", 0.0, 1.0, 0.125, NiceSlider.Orientation.HORIZONTAL);
        percentile.addChangeListener(udl);
        add(percentile);

        repeated = addCheckBox("Repeated", true);
        repeated.addChangeListener(udl);

        fix = addCheckBox("Use fix noise level", false);
        fix.addChangeListener(udl);

        noiseLevel = SlickerFactory.instance().createNiceDoubleSlider("Noise Threshold", 0.0, 1.0, noiseLevelValue, NiceSlider.Orientation.HORIZONTAL);
        noiseLevel.setEnabled(false);
        noiseLevel.addChangeListener(udl);
        add(noiseLevel);

        JComboBox cb = addComboBox("Infrequent Arcs Criteria", new String[] {"AVERAGE", "MAX", "MIN"});
        result.setApproach(AutomatonInfrequentBehaviourDetector.AVE);
        cb.addActionListener(udl);
    }

    public NoiseFilterResult getSelections() {
        return result;
    }

    private class UpdateDataListener implements ActionListener, ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            if(e.getSource() instanceof JCheckBox) {
                JCheckBox box = (JCheckBox) e.getSource();
                if(box == fix) {
                    result.setFixLevel(box.isSelected());
                    if (result.isFixLevel()) {
                        percentile.setEnabled(false);
                        noiseLevel.setEnabled(true);
                    } else {
                        percentile.setEnabled(true);
                        noiseLevel.setEnabled(false);
                    }
                }else {
                    result.setRepeated(box.isSelected());
                }
            }else if(e.getSource() instanceof JSlider) {
                JSlider slider = (JSlider) e.getSource();
                if(slider == noiseLevel.getSlider()) {
                    double value = ((double) slider.getValue()) / 10000.0;
                    result.setNoiseLevel(value);
                }else if(slider == percentile.getSlider()){
                    double value = ((double) slider.getValue()) / 10000.0;
                    result.setPercentile(value);
                    double noise = infrequentBehaviourFilterPlugin.discoverThreshold(arcs, value);
                    noiseLevel.setValue(noise);
                    result.setNoiseLevel(noise);
                }
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            result.setApproach(((JComboBox) e.getSource()).getSelectedIndex());
        }
    }

}
