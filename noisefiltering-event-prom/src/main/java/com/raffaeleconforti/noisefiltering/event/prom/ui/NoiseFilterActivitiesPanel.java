package com.raffaeleconforti.noisefiltering.event.prom.ui;

import com.raffaeleconforti.automaton.Node;
import com.raffaeleconforti.context.FakeProMPropertiesPanel;
import com.raffaeleconforti.noisefiltering.event.selection.NoiseFilterResult;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

/**
 * Created by conforti on 17/09/15.
 */
public class NoiseFilterActivitiesPanel extends FakeProMPropertiesPanel {

    private static final long serialVersionUID = 1L;

    private static final String DIALOG_NAME = "Select Required States";

    private final NoiseFilterResult result;
    private final JCheckBox[] possibleStatesCheckBox;
    private final JCheckBox allStates;
    private final Node<String>[] possibleStates;
    private Node<String> artificialStart;
    private Node<String> artificialEnd;

    public NoiseFilterActivitiesPanel(Set<Node<String>> states) {
        super(DIALOG_NAME);

        result = new NoiseFilterResult();
        result.setRequiredStates(states);

        UpdateDataListener udl = new UpdateDataListener();

        int i = 0;
        possibleStatesCheckBox = new JCheckBox[states.size()];
        possibleStates = states.toArray(new Node[states.size()]);
        Arrays.sort(possibleStates, new Comparator<Node<String>>() {
            @Override
            public int compare(Node<String> o1, Node<String> o2) {
                int val = Double.valueOf(o1.getFrequency()).compareTo(o2.getFrequency());
                if(val == 0) val = o1.getData().compareTo(o2.getData());
                return -val;
            }
        });

        allStates = addCheckBox("Select/Deselect All", true);
        allStates.addChangeListener(udl);

        double total = 0.0;
        DecimalFormat decimalFormat = new DecimalFormat("##0.000");
        for(Node<String> state : possibleStates) {
            if(!state.getData().equals("ArtificialStartEvent") && !state.getData().equals("ArtificialEndEvent")) {
                total += state.getFrequency();
            }
        }

        for(Node<String> state : possibleStates) {
            if(state.getData().equals("ArtificialStartEvent")) {
                artificialStart = state;
            }else if(state.getData().equals("ArtificialEndEvent")) {
                artificialEnd = state;
            }else {
                possibleStatesCheckBox[i] = addCheckBox(state.getData() + ", frequency: " + decimalFormat.format(state.getFrequency() / total) + "%", true);
                possibleStatesCheckBox[i].addChangeListener(udl);
                i++;
            }
        }
    }

    public NoiseFilterResult getSelections() {
        return result;
    }

    private class UpdateDataListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            Set<Node<String>> selectedStates = new UnifiedSet<Node<String>>();
            selectedStates.add(artificialStart);
            selectedStates.add(artificialEnd);
            if(e.getSource().equals(allStates)) {
                if(allStates.isSelected()) {
                    for (int i = 0; i < possibleStatesCheckBox.length - 2; i++) {
                        possibleStatesCheckBox[i].setSelected(true);
                        selectedStates.add(possibleStates[i]);
                    }
                }else {
                    for (int i = 0; i < possibleStatesCheckBox.length - 2; i++) {
                        possibleStatesCheckBox[i].setSelected(false);
                    }
                }
            }else {
                for (int i = 0; i < possibleStatesCheckBox.length - 2; i++) {
                    if (possibleStatesCheckBox[i].isSelected()) {
                        selectedStates.add(possibleStates[i]);
                    }
                }
            }
            result.setRequiredStates(selectedStates);
        }
    }
}
