package com.raffaeleconforti.noisefiltering.timestamp.noise.prom.ui;

import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.raffaeleconforti.noisefiltering.timestamp.noise.selection.TimeStampNoiseResult;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by conforti on 26/02/15.
 */
public class TimeStampNoise extends ProMPropertiesPanel {

    private static final long serialVersionUID = 1L;

    private static final String DIALOG_NAME = "Select TimeStampResult Level";

    final TimeStampNoiseResult result;

    JComboBox comboBox;

    NiceIntegerSlider percentageTraces;
    NiceIntegerSlider percentageUniqueTraces;
    NiceIntegerSlider percentageEvents;

    NiceIntegerSlider totalGaps;
    NiceIntegerSlider minGapLength;
    NiceIntegerSlider maxGapLength;
    NiceIntegerSlider averageGapLength;
    NiceIntegerSlider minGapNumber;
    NiceIntegerSlider maxGapNumber;
    NiceIntegerSlider averageGapNumber;

    public TimeStampNoise() {
        super(DIALOG_NAME);

        result = new TimeStampNoiseResult();
        result.setPercentageTraces(0.0);
        result.setPercentageEvents(0.0);

        UpdateDataListener udl = new UpdateDataListener();
        comboBox = SlickerFactory.instance().createComboBox(new Object[] {"Traces", "UniqueTraces", "Other"});
        comboBox.addActionListener(udl);
        add(comboBox);

        percentageTraces = SlickerFactory.instance().createNiceIntegerSlider("Percentage of Traces", 0, 100, 0, NiceSlider.Orientation.HORIZONTAL);
        percentageTraces.addChangeListener(udl);
        add(percentageTraces);

        percentageUniqueTraces = SlickerFactory.instance().createNiceIntegerSlider("Percentage of Unique Traces", 0, 100, 0, NiceSlider.Orientation.HORIZONTAL);
        percentageUniqueTraces.setEnabled(false);
        percentageUniqueTraces.addChangeListener(udl);
        add(percentageUniqueTraces);

        percentageEvents = SlickerFactory.instance().createNiceIntegerSlider("Percentage of Events", 0, 100, 0, NiceSlider.Orientation.HORIZONTAL);
        percentageEvents.addChangeListener(udl);
        add(percentageEvents);
    }

    public TimeStampNoiseResult getSelections() {
        return result;
    }

    private class UpdateDataListener implements ChangeListener, ActionListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            if (e.getSource() instanceof JSlider) {
                JSlider jSlider = (JSlider) e.getSource();
                if(jSlider == percentageTraces.getSlider()) {
                    double value = ((double) (percentageTraces.getSlider()).getValue()) / 100.0;
                    result.setPercentageTraces(value);
                    result.setPercentageUniqueTraces(null);
                }else if(jSlider == percentageUniqueTraces.getSlider()) {
                    double value = ((double) (percentageUniqueTraces.getSlider()).getValue()) / 100.0;
                    result.setPercentageUniqueTraces(value);
                    result.setPercentageTraces(null);
                }else if(jSlider == percentageEvents.getSlider()) {
                    double value = ((double) (percentageEvents.getSlider()).getValue()) / 100.0;
                    result.setPercentageEvents(value);
                }
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() instanceof JComboBox) {
                JComboBox comboBox = (JComboBox) e.getSource();
                int index = comboBox.getSelectedIndex();
                switch (index) {
                    case 0: percentageTraces.setEnabled(true);
                            percentageUniqueTraces.setEnabled(false);
                            break;
                    case 1: percentageTraces.setEnabled(false);
                            percentageUniqueTraces.setEnabled(true);
                            break;
                    case 2:
                }
            }
        }
    }

}
