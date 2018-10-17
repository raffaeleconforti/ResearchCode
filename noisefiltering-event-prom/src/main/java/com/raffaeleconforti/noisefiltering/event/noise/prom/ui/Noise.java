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

package com.raffaeleconforti.noisefiltering.event.noise.prom.ui;

import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.raffaeleconforti.noisefiltering.event.noise.selection.NoiseResult;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Created by conforti on 26/02/15.
 */
public class Noise extends ProMPropertiesPanel {

    private static final long serialVersionUID = 1L;

    private static final String DIALOG_NAME = "Select Noise Level";

    final NoiseResult result;

    public Noise() {
        super(DIALOG_NAME);

        result = new NoiseResult();
        result.setNoiseLevel(0.1);

        UpdateDataListener udl = new UpdateDataListener();
        NiceIntegerSlider fold = SlickerFactory.instance().createNiceIntegerSlider("Percentage of Noise", 1, 100, 10, NiceSlider.Orientation.HORIZONTAL);
        fold.addChangeListener(udl);
        add(fold);
    }

    public NoiseResult getSelections() {
        return result;
    }

    private class UpdateDataListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            if (e.getSource() instanceof JSlider) {
                double value = ((double) ((JSlider) e.getSource()).getValue()) / 100.0;
                result.setNoiseLevel(value);
            }
        }
    }

}
