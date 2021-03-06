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
    private final JCheckBox removeTraces;
    private final JCheckBox removeNodes;

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

        removeTraces = addCheckBox("Remove unfitting traces", true);
        removeTraces.addChangeListener(udl);

        removeNodes = addCheckBox("Remove infrequent nodes", false);
        removeNodes.addChangeListener(udl);

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
                } else if (box == repeated) {
                    result.setRepeated(box.isSelected());
                } else if (box == removeTraces) {
                    result.setRemoveTraces(box.isSelected());
                } else if (box == removeNodes) {
                    result.setRemoveNodes(box.isSelected());
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
