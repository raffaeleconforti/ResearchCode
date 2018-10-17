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

package com.raffaeleconforti.noisefiltering.event.prom;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.annotations.Plugin;

/**
 * Created by conforti on 7/02/15.
 */

@Plugin(name = "Infrequent Behaviour Filter", parameterLabels = {"Log"},//, "PetriNet", "Marking", "Log" },
        returnLabels = {"FilteredLog"},
        returnTypes = {XLog.class})
public abstract class InfrequentBehaviourFilterPlugin {

    public abstract double discoverThreshold(double[] arcs, double initialPercentile);

    public abstract XLog filterLog(XLog log1);
}
