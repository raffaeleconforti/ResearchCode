package com.raffaeleconforti.noisefiltering.timestamp.prom;

import com.raffaeleconforti.noisefiltering.timestamp.TimeStampFilterMarker;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

/**
 * Created by conforti on 7/02/15.
 */

@Plugin(name = "Timestamp Filter Marker", parameterLabels = {"Noisy Log", "Correct Log"},
        returnLabels = {"Marked Log"},
        returnTypes = {XLog.class})

public class TimeStampFilterMarkerPlugin {

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@unimelb.edu.au",
            pack = "Noise Filtering (raffaele.conforti@unimelb.edu.au)")
    @PluginVariant(variantLabel = "Timestamp Filter Marker", requiredParameterLabels = {0, 1})
    public XLog mark(final UIPluginContext context, XLog noisyLog, XLog correctLog) {
        TimeStampFilterMarker timeStampFilterMarker = new TimeStampFilterMarker();
        return timeStampFilterMarker.check(noisyLog, correctLog);
    }


}
