package com.raffaeleconforti.measurements.ui.allmeasurement;

import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.raffaeleconforti.measurements.ui.computemeasurement.SelectMinerUICM;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Raffaele Conforti on 20/02/14.
 */
public class SelectMinerAM extends ProMPropertiesPanel {

    private static final long serialVersionUID = 1L;

    private static final String DIALOG_NAME = "Select Mining Algorithm";

    private final boolean[] selectedAttributes;
    private SelectMinerUIResultAM result = null;

    private JCheckBox bpmnminer;
    private JCheckBox inductive;
    private JCheckBox heuristics;
    private JCheckBox fodina;
    private JCheckBox ilp;
    private JCheckBox alpha;
    private JCheckBox heuristics52;
    private JCheckBox evolutionary;
    private JCheckBox structured;

    private final JCheckBox fitness;
    private final JCheckBox precision;
    private final JCheckBox generalization;
    private final JCheckBox noise;
    private final JCheckBox simplicity;


    public SelectMinerAM(List<String> attributeNames, boolean discoverModel) {
        super(DIALOG_NAME);

        boolean[] selection = new boolean[9];
        Arrays.fill(selection, true);
        result = new SelectMinerUIResultAM(selection);

        selectedAttributes = new boolean[attributeNames.size()];

        UpdateDataListener udl = new UpdateDataListener();

        if(discoverModel) {
            bpmnminer = addCheckBox("BPMN Miner", true);
            bpmnminer.addChangeListener(udl);

            inductive = addCheckBox("Inductive Miner", true);
            inductive.addChangeListener(udl);

            heuristics = addCheckBox("Heuristics Miner", true);
            heuristics.addChangeListener(udl);

            fodina = addCheckBox("Fodina Miner", true);
            fodina.addChangeListener(udl);

            ilp = addCheckBox("ILP Miner", true);
            ilp.addChangeListener(udl);

            alpha = addCheckBox("Alpha Miner", true);
            alpha.addChangeListener(udl);

            heuristics52 = addCheckBox("Heuristics Miner 5.2", true);
            heuristics52.addChangeListener(udl);

            evolutionary = addCheckBox("Evolutionary Tree Miner", true);
            evolutionary.addChangeListener(udl);

            structured = addCheckBox("Structured Miner", true);
            structured.addChangeListener(udl);
        }

        fitness = addCheckBox("Fitness", true);
        fitness.addChangeListener(udl);

        precision = addCheckBox("Precision", true);
        precision.addChangeListener(udl);

        generalization = addCheckBox("Generalization", true);
        generalization.addChangeListener(udl);

        noise = addCheckBox("Filter Noise", true);
        noise.addChangeListener(udl);

        simplicity = addCheckBox("Simplicity", true);
        simplicity.addChangeListener(udl);

        NiceIntegerSlider fold = SlickerFactory.instance().createNiceIntegerSlider("Number Folds", 1, 100, 10, NiceSlider.Orientation.HORIZONTAL);
        fold.addChangeListener(udl);
        add(fold);

    }

    public SelectMinerUIResultAM getSelections() {
        return result;
    }

    /**
     * Listener to copy the selection from the comboboxes to a readable data-structure.
     *
     * @author dfahland
     */
    private class UpdateDataListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            if(e.getSource() instanceof JSlider) {
                result.setFold(((JSlider) e.getSource()).getValue());
            }else {
                boolean[] alg = result.getSelectedAlgorithm();
                JCheckBox jCheckBox = (JCheckBox) e.getSource();
                if(e.getSource().equals(bpmnminer)) {
                    alg[SelectMinerUICM.BPMNPOS] = jCheckBox.isSelected();
                    result.setSelectedAlgorithm(alg);
                }else if(e.getSource().equals(inductive)) {
                    alg[SelectMinerUICM.IMPOS] = jCheckBox.isSelected();
                    result.setSelectedAlgorithm(alg);
                }else if(e.getSource().equals(heuristics)) {
                    alg[SelectMinerUICM.HMPOS] = jCheckBox.isSelected();
                    result.setSelectedAlgorithm(alg);
                }else if(e.getSource().equals(fodina)) {
                    alg[SelectMinerUICM.FODINAPOS] = jCheckBox.isSelected();
                    result.setSelectedAlgorithm(alg);
                }else if(e.getSource().equals(ilp)) {
                    alg[SelectMinerUICM.ILPPOS] = jCheckBox.isSelected();
                    result.setSelectedAlgorithm(alg);
                }else if(e.getSource().equals(alpha)) {
                    alg[SelectMinerUICM.ALPHAPOS] = jCheckBox.isSelected();
                    result.setSelectedAlgorithm(alg);
                }else if(e.getSource().equals(heuristics52)) {
                    alg[SelectMinerUICM.HMPOS52] = jCheckBox.isSelected();
                    result.setSelectedAlgorithm(alg);
                }else if (e.getSource().equals(evolutionary)) {
                    alg[SelectMinerUICM.ETMPOS] = jCheckBox.isSelected();
                    result.setSelectedAlgorithm(alg);
                }else if (e.getSource().equals(structured)) {
                    alg[SelectMinerUICM.SMPOS] = jCheckBox.isSelected();
                    result.setSelectedAlgorithm(alg);
                }else if(e.getSource().equals(fitness)) {
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
