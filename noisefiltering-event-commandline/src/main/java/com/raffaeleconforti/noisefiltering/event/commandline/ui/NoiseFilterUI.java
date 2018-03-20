package com.raffaeleconforti.noisefiltering.event.commandline.ui;

import com.raffaeleconforti.automaton.Node;
import com.raffaeleconforti.noisefiltering.event.commandline.InfrequentBehaviourFilterCommandLine;
import com.raffaeleconforti.noisefiltering.event.selection.NoiseFilterResult;

import java.util.Set;

/**
 * Created by conforti on 26/02/15.
 */
public class NoiseFilterUI {

    public NoiseFilterResult showGUI(InfrequentBehaviourFilterCommandLine infrequentBehaviourFilterCommandLine, double[] arcs, Set<Node<String>> states) {

        NoiseFilterPanel noiseFilterUI = new NoiseFilterPanel(infrequentBehaviourFilterCommandLine, arcs);
        NoiseFilterActivitiesPanel noiseFilterActivities = new NoiseFilterActivitiesPanel(states);
        noiseFilterUI.getSelections().setRequiredStates(noiseFilterActivities.getSelections().getRequiredStates());
        return noiseFilterUI.getSelections();

    }

}
