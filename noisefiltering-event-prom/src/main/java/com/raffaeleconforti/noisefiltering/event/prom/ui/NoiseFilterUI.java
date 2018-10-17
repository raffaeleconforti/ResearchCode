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
