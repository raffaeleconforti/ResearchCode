package com.raffaeleconforti.noisefiltering.event.prom.ui;

import com.raffaeleconforti.automaton.Node;
import com.raffaeleconforti.noisefiltering.event.prom.InfrequentBehaviourFilterPlugin;
import com.raffaeleconforti.noisefiltering.event.selection.NoiseFilterResult;
import org.deckfour.uitopia.api.event.TaskListener;
import org.processmining.contexts.uitopia.UIPluginContext;

import java.util.Set;
import java.util.concurrent.CancellationException;

/**
 * Created by conforti on 26/02/15.
 */
public class NoiseFilterUI {

    public NoiseFilterResult showGUI(UIPluginContext context, InfrequentBehaviourFilterPlugin infrequentBehaviourFilterPlugin, double[] arcs, Set<Node<String>> states) {

        NoiseFilterPanel noiseFilterUI = new NoiseFilterPanel(infrequentBehaviourFilterPlugin, arcs);
        TaskListener.InteractionResult guiResult = context.showWizard("Select Noise Level",
                true, false, noiseFilterUI);
        if (guiResult == TaskListener.InteractionResult.CANCEL) {
            context.getFutureResult(0).cancel(true);
            throw new CancellationException("The wizard has been cancelled.");
        }

        NoiseFilterActivitiesPanel noiseFilterActivities = new NoiseFilterActivitiesPanel(states);
        guiResult = context.showWizard("Select Noise Level", false, true, noiseFilterActivities);
        if (guiResult == TaskListener.InteractionResult.CANCEL) {
            context.getFutureResult(0).cancel(true);
            throw new CancellationException("The wizard has been cancelled.");
        }

        noiseFilterUI.getSelections().setRequiredStates(noiseFilterActivities.getSelections().getRequiredStates());
        return noiseFilterUI.getSelections();

    }

}
