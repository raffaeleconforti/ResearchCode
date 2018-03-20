package com.raffaeleconforti.noisefiltering.timestamp.noise.prom;

import com.raffaeleconforti.noisefiltering.timestamp.noise.TimeStampNoiseGenerator;
import com.raffaeleconforti.noisefiltering.timestamp.noise.prom.ui.TimeStampNoiseUI;
import com.raffaeleconforti.noisefiltering.timestamp.noise.selection.TimeStampNoiseResult;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
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

@Plugin(name = "Timestamp Noise Generator", parameterLabels = {"Log"},
        returnLabels = {"XLog"},
        returnTypes = {XLog.class})
public class TimeStampNoiseGeneratorPlugin {

    final XEventClassifier xEventClassifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
    final XConceptExtension xce = XConceptExtension.instance();
    final XTimeExtension xte = XTimeExtension.instance();

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@unimelb.edu.au",
            pack = "TimeStampResult Filtering (raffaele.conforti@unimelb.edu.au)")
    @PluginVariant(variantLabel = "Timestamp Noise Generator", requiredParameterLabels = {0})//, 1, 2, 3 })
    public XLog insertNoise(final UIPluginContext context, XLog log) {


        TimeStampNoiseUI timeStampNoiseUI = new TimeStampNoiseUI();

        TimeStampNoiseResult timeStampNoiseResult = timeStampNoiseUI.showGUI(context);

        TimeStampNoiseGenerator timeStampNoiseGenerator = new TimeStampNoiseGenerator();
        if(timeStampNoiseResult.getPercentageEvents() != null) {
            if(timeStampNoiseResult.getPercentageTraces() != null) {
                System.out.println(timeStampNoiseResult.getPercentageTraces() + " " + timeStampNoiseResult.getPercentageEvents());
                return timeStampNoiseGenerator.insertNoiseTotalTracesEvents(log, timeStampNoiseResult.getPercentageTraces(), timeStampNoiseResult.getPercentageEvents());
            }else {
                return timeStampNoiseGenerator.insertNoiseUniqueTracesEvents(log, timeStampNoiseResult.getPercentageUniqueTraces(), timeStampNoiseResult.getPercentageEvents());
            }
        }else {
            return timeStampNoiseGenerator.insertNoiseGapNumberAndLenght(log, timeStampNoiseResult.getTotalGaps(), timeStampNoiseResult.getMinGapLength(),
                    timeStampNoiseResult.getMaxGapLength(), timeStampNoiseResult.getAverageGapLength(), timeStampNoiseResult.getMinGapNumber(), timeStampNoiseResult.getMaxGapNumber(), timeStampNoiseResult.getAverageGapNumber());
        }
    }



}
