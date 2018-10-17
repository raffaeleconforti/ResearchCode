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
