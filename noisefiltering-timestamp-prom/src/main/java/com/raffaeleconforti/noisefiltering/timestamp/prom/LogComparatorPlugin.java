package com.raffaeleconforti.noisefiltering.timestamp.prom;

import com.raffaeleconforti.noisefiltering.timestamp.LogComparator;
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

@Plugin(name = "Log Comparator", parameterLabels = {"Log 1", "Log 2"},
        returnLabels = {"Result"},
        returnTypes = {String.class})

public class LogComparatorPlugin {

    XConceptExtension xce = XConceptExtension.instance();
    XTimeExtension xte = XTimeExtension.instance();

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@unimelb.edu.au",
            pack = "Noise Filtering (raffaele.conforti@unimelb.edu.au)")
    @PluginVariant(variantLabel = "Log Comparator", requiredParameterLabels = {0, 1})
    public String check(final UIPluginContext context, XLog log1, XLog log2) {
        LogComparator logComparator = new LogComparator();
        return logComparator.check(log1, log2);
    }


}
