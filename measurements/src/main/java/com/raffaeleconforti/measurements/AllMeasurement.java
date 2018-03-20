package com.raffaeleconforti.measurements;

import com.raffaeleconforti.measurements.ui.allmeasurement.SelectMinerUIAM;
import com.raffaeleconforti.measurements.ui.allmeasurement.SelectMinerUIResultAM;
import com.raffaeleconforti.measurements.ui.computemeasurement.SelectMinerUIResultCM;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

/**
 * Created by conforti on 23/02/15.
 */
@Plugin(name = "Compute Measurements Multiple Algorithms", parameterLabels = {"Training Log", "Validating Log"},
        returnLabels = {"Measurement"},
        returnTypes = {String.class})
public class AllMeasurement {
    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@unimelb.edu.au",
            pack = "BPMNMiner (raffaele.conforti@unimelb.edu.au)")
    @PluginVariant(variantLabel = "Compute Measurements Multiple Algorithms", requiredParameterLabels = {0})
    public String execute(UIPluginContext context, XLog trainingLog) {
        return execute(context, trainingLog, trainingLog);
    }

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@unimelb.edu.au",
            pack = "BPMNMiner (raffaele.conforti@unimelb.edu.au)")
    @PluginVariant(variantLabel = "Compute Measurements Multiple Algorithms with test Log", requiredParameterLabels = {0, 1})
    public String execute(UIPluginContext context, XLog trainingLog, XLog validatingLog) {

        SelectMinerUIAM selectMiner = new SelectMinerUIAM();
        SelectMinerUIResultAM result = selectMiner.showGUI(context, true);

        StringBuilder sb = new StringBuilder();
        sb.append("<html><table width=\"400\"><tr><td width=\"33%\"></td><td width=\"33%\"><table>");
        sb.append("<tr><td>" + context.getGlobalContext().getResourceManager().getResourceForInstance(trainingLog).getName() + "</td><td></td><td></td></tr>");
        sb.append("<tr><td></td><td></td><td></td></tr>");

        SelectMinerUIResultCM resultUICM = new SelectMinerUIResultCM(0);
        resultUICM.setFold(10);
        resultUICM.setFitness(result.isFitness());
        resultUICM.setPrecision(result.isPrecision());
        resultUICM.setGeneralization(result.isGeneralization());
        resultUICM.setNoise(result.isNoise());
        resultUICM.setSemplicity(result.isSemplicity());

        ComputeMeasurment cm = new ComputeMeasurment();
        for(int i = 0; i < result.getSelectedAlgorithm().length; i++) {
            if(result.getSelectedAlgorithm()[i]) {
                sb.append("<tr><td>" + cm.getAlgorithmName(i) + "</td><td></td><td></td></tr>");
                cm.clear();

                resultUICM.setSelectedAlgorithm(i);

                String s = cm.execute(context, trainingLog, validatingLog, resultUICM);
                s = s.substring(73, s.indexOf("</table></td><td width=\"33%\"></td></tr></table></html>"));
                sb.append(s).append("<tr><td></td><td></td><td></td></tr>");
            }
        }

        sb.append("</table></td><td width=\"33%\"></td></tr></table></html>");
        return sb.toString();

    }
}
