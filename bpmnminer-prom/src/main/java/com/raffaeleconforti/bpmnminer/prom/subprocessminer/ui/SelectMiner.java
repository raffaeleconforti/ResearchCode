package com.raffaeleconforti.bpmnminer.prom.subprocessminer.ui;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.raffaeleconforti.bpmnminer.subprocessminer.selection.SelectMinerResult;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Raffaele Conforti on 20/02/14.
 */
public class SelectMiner extends ProMPropertiesPanel {

    private static final long serialVersionUID = 1L;

    private static final String DIALOG_NAME = "Select Mining Algorithm";

    boolean[] selectedAttributes;
    NiceDoubleSlider interruptingEventToleranceTextField;
    NiceDoubleSlider multiInstancePercentageTextField;
    NiceDoubleSlider multiInstanceToleranceTextField;
    NiceDoubleSlider timerEventPercentageTextField;
    NiceDoubleSlider timerEventToleranceTextField;
	NiceDoubleSlider noiseThresholdTextField;

	private Integer selectedAlgorithm;
    private Double interruptingEventTolerance;
    private Double multiInstancePercentage;
    private Double multiInstanceTolerance;
    private Double timerEventPercentage;
    private Double timerEventTolerance;
	private Double noiseThreshold;
    SelectMinerResult result = null;
    Scanner console = new Scanner(System.in);

    public SelectMiner(List<String> attributeNames, boolean commandLine) {
        super(DIALOG_NAME);

        double initialEventTolerance = 0.0;
        double initialMultiinstancePercentage = 0.0;
        double initialMultiinstanceTolerance = 0.5;
        double initialTimerPercentage = 0.0;
        double initialTimerTolerance = 0.0;
		double initialNoiseThreshold = 0.3;

        result = new SelectMinerResult(0, null, initialEventTolerance, initialMultiinstancePercentage, initialMultiinstanceTolerance, initialTimerPercentage, initialTimerTolerance, initialNoiseThreshold);

		selectedAttributes = new boolean[attributeNames.size()];

		JComboBox cb = addComboBox("Mining Algorithm", attributeNames);
		UpdateDataListener udl = new UpdateDataListener();
		cb.addActionListener(udl);

		interruptingEventToleranceTextField = SlickerFactory.instance().createNiceDoubleSlider("InterruptingEvent Tolerance Value", 0.0, 1.0, initialEventTolerance, NiceSlider.Orientation.HORIZONTAL);
		interruptingEventToleranceTextField.addChangeListener(udl);
		add(interruptingEventToleranceTextField);

		multiInstancePercentageTextField = SlickerFactory.instance().createNiceDoubleSlider("MultiInstance Percentage Value", 0.0, 1.0, initialMultiinstancePercentage, NiceSlider.Orientation.HORIZONTAL);
		multiInstancePercentageTextField.addChangeListener(udl);
		add(multiInstancePercentageTextField);

		multiInstanceToleranceTextField = SlickerFactory.instance().createNiceDoubleSlider("MultiInstance Tolerance Value", 0.0, 1.0, initialMultiinstanceTolerance, NiceSlider.Orientation.HORIZONTAL);
		multiInstanceToleranceTextField.addChangeListener(udl);
		add(multiInstanceToleranceTextField);

		timerEventPercentageTextField = SlickerFactory.instance().createNiceDoubleSlider("TimerEvent Percentage Value", 0.0, 1.0, initialTimerPercentage, NiceSlider.Orientation.HORIZONTAL);
		timerEventPercentageTextField.addChangeListener(udl);
		add(timerEventPercentageTextField);

		timerEventToleranceTextField = SlickerFactory.instance().createNiceDoubleSlider("TimerEvent Tolerance Value", 0.0, 1.0, initialTimerTolerance, NiceSlider.Orientation.HORIZONTAL);
		timerEventToleranceTextField.addChangeListener(udl);
		add(timerEventToleranceTextField);

		noiseThresholdTextField = SlickerFactory.instance().createNiceDoubleSlider("Noise Threshold Value", 0.0, 1.0, initialNoiseThreshold, NiceSlider.Orientation.HORIZONTAL);
		noiseThresholdTextField.addChangeListener(udl);
		add(noiseThresholdTextField);

    }

    public SelectMinerResult getSelectedAlgorithm() {
        result = new SelectMinerResult(selectedAlgorithm, null, interruptingEventTolerance, multiInstancePercentage, multiInstanceTolerance, timerEventPercentage, timerEventTolerance, noiseThreshold);
        return result;
    }

	public SelectMinerResult getSelections() {
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
            if (e.getSource() instanceof JSlider) {
                NiceDoubleSlider nds = (NiceDoubleSlider) ((JSlider) e.getSource()).getParent();

                if (nds == interruptingEventToleranceTextField) {
                    result.setInterruptingEventTolerance(nds.getValue());
                } else if (nds == multiInstancePercentageTextField) {
                    result.setMultiInstancePercentage(((JSlider) e.getSource()).getValue());
                } else if (nds == multiInstanceToleranceTextField) {
                    result.setMultiInstanceTolerance(((JSlider) e.getSource()).getValue());
                } else if (nds == timerEventPercentageTextField) {
                    result.setTimerEventPercentage(((JSlider) e.getSource()).getValue());
                } else if (nds == timerEventToleranceTextField) {
                    result.setTimerEventTolerance(((JSlider) e.getSource()).getValue());
				} else if (nds == noiseThresholdTextField) {
					result.setNoiseThreshold(((JSlider) e.getSource()).getValue());
				}
            }
        }
    }
}
