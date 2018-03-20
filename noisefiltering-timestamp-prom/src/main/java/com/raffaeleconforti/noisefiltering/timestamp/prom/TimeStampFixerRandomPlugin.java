package com.raffaeleconforti.noisefiltering.timestamp.prom;

import com.raffaeleconforti.noisefiltering.timestamp.TimeStampFixerRandomExecutor;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

/**
 * Created by conforti on 7/02/15.
 */

@Plugin(name = "Timestamp Filter Random", parameterLabels = {"Log"},
        returnLabels = {"XLog"},
        returnTypes = {XLog.class})
public class TimeStampFixerRandomPlugin {

    private final TimeStampFixerRandomExecutor timeStampRandomFixerExecutor = new TimeStampFixerRandomExecutor(false, false);

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@unimelb.edu.au",
            pack = "TimeStampResult Filtering (raffaele.conforti@unimelb.edu.au)")
    @PluginVariant(variantLabel = "Timestamp Filter Random", requiredParameterLabels = {0})//, 1, 2, 3 })
    public XLog fixTimeStamp(final UIPluginContext context, XLog log) {
        return timeStampRandomFixerExecutor.filterLog(log);
    }

}
