package com.raffaeleconforti.noisefiltering.event.noise.prom;

import com.raffaeleconforti.noisefiltering.event.noise.NoiseGenerator;
import com.raffaeleconforti.noisefiltering.event.noise.prom.ui.NoiseUI;
import com.raffaeleconforti.noisefiltering.event.noise.selection.NoiseResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

/**
 * Created by conforti on 26/02/15.
 */
@Plugin(name = "Noise Generator", parameterLabels = {"Log"},
        returnLabels = {"Noise Log"},
        returnTypes = {XLog.class})
public class NoiseGeneratorPlugin {

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@unimelb.edu.au",
            pack = "BPMNMiner (raffaele.conforti@unimelb.edu.au)")
    @PluginVariant(variantLabel = "Noise Generator", requiredParameterLabels = {0})
    public XLog generateNoise(UIPluginContext context, XLog log) {
        NoiseGenerator noiseGenerator = new NoiseGenerator(log);
        NoiseUI noiseUI = new NoiseUI();
        NoiseResult result = noiseUI.showGUI(context);
        return noiseGenerator.insertNoise(result.getNoiseLevel());
    }
}
