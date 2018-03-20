package com.raffaeleconforti.noisefiltering.timestamp.prom;

import com.raffaeleconforti.noisefiltering.timestamp.TimeStampFilterChecker;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

/**
 * Created by conforti on 7/02/15.
 */

@Plugin(name = "Timestamp Filter Checker", parameterLabels = {"Filtered Log", "Noisy Log", "Correct Log"},
        returnLabels = {"Result"},
        returnTypes = {String.class})

public class TimeStampFilterCheckerPlugin {

    XConceptExtension xce = XConceptExtension.instance();
    XTimeExtension xte = XTimeExtension.instance();

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@unimelb.edu.au",
            pack = "Noise Filtering (raffaele.conforti@unimelb.edu.au)")
    @PluginVariant(variantLabel = "Timestamp Filter Checker", requiredParameterLabels = {0, 1, 2})
    public String check(final UIPluginContext context, XLog filteredLog, XLog noisyLog, XLog correctLog) {
        TimeStampFilterChecker timeStampFilterChecker = new TimeStampFilterChecker();
        return timeStampFilterChecker.check(filteredLog, noisyLog, correctLog);
    }


}
