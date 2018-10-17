/*
 *  Copyright (C) 2018 Raffaele Conforti (www.raffaeleconforti.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
