package com.raffaeleconforti.noisefiltering.timestamp.prom;

import com.raffaeleconforti.noisefiltering.timestamp.TimeStampFixerSmartExecutor;
import com.raffaeleconforti.noisefiltering.timestamp.permutation.PermutationTechnique;
import com.raffaeleconforti.noisefiltering.timestamp.prom.ui.TimeStampUI;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

/**
 * Created by conforti on 7/02/15.
 */

@Plugin(name = "Timestamp Filter", parameterLabels = {"Log"},
        returnLabels = {"XLog"},
        returnTypes = {XLog.class})
public class TimeStampFixerPlugin {

    private TimeStampFixerSmartExecutor timeStampFixerSmartExecutor;

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@qut.edu.au",
            pack = "TimeStampResult Filtering (raffaele.conforti@qut.edu.au)")
    @PluginVariant(variantLabel = "Timestamp Filter", requiredParameterLabels = {0})//, 1, 2, 3 })
    public XLog fixTimeStamp(final UIPluginContext context, XLog log) {
        int limitExtensive = 11;
        TimeStampUI timeStampUI = new TimeStampUI();
        int approach = timeStampUI.showGUI(context);
        boolean useGurobi = (approach == PermutationTechnique.ILP_GUROBI) ? true : false;
        timeStampFixerSmartExecutor = new TimeStampFixerSmartExecutor(useGurobi);
        return timeStampFixerSmartExecutor.filterLog(log, limitExtensive, approach);
    }

}
