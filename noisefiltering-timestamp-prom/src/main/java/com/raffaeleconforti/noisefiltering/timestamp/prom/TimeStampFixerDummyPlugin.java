package com.raffaeleconforti.noisefiltering.timestamp.prom;

import com.raffaeleconforti.noisefiltering.timestamp.TimeStampFixerDummyExecutor;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

/**
 * Created by conforti on 7/02/15.
 */

@Plugin(name = "Timestamp Filter Dummy", parameterLabels = {"Log"},
        returnLabels = {"XLog"},
        returnTypes = {XLog.class})
public class TimeStampFixerDummyPlugin {

    private TimeStampFixerDummyExecutor timeStampFixerExecutor = new TimeStampFixerDummyExecutor(false, false);

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@unimelb.edu.au",
            pack = "TimeStampResult Filtering (raffaele.conforti@unimelb.edu.au)")
    @PluginVariant(variantLabel = "Timestamp Filter Dummy", requiredParameterLabels = {0})//, 1, 2, 3 })
    public XLog fixTimeStamp(final UIPluginContext context, XLog log) {
        return timeStampFixerExecutor.filterLog(log);
    }

}
