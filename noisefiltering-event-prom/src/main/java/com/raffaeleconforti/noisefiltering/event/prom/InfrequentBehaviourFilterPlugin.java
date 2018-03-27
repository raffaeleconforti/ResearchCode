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
