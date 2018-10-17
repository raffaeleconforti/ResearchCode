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

package com.raffaeleconforti.structuredminer;

import com.raffaeleconforti.conversion.bpmn.BPMNToPetriNetConverter;
import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.structuredminer.miner.StructuredMiner;
import com.raffaeleconforti.structuredminer.ui.SettingsStructuredMiner;
import com.raffaeleconforti.structuredminer.ui.SettingsStructuredMinerUI;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

/**
 * Created by conforti on 19/12/2015.
 */
@Plugin(name = "Structured Miner", parameterLabels = {"Log"},
        returnLabels = {"BPMNModel"},
        returnTypes = {BPMNDiagram.class})

public class StructuredMinerPlugin {

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@unimelb.edu.au",
            pack = "StructuredMiner (raffaele.conforti@unimelb.edu.au)")
    @PluginVariant(variantLabel = "Structured Miner", requiredParameterLabels = {0})//, 1, 2, 3 })
    public BPMNDiagram generateDiagram(final UIPluginContext context, XLog log) {

        SettingsStructuredMinerUI settingsStructuredMinerUI = new SettingsStructuredMinerUI();
        SettingsStructuredMiner settings = settingsStructuredMinerUI.showGUI(context);

        StructuredMiner miner = new StructuredMiner(context, log, settings);
        BPMNDiagram diagram = miner.mine();
        Object[] petrinetWithMarking = BPMNToPetriNetConverter.convert(diagram);

        return PetriNetToBPMNConverter.convert((Petrinet) petrinetWithMarking[0], (Marking) petrinetWithMarking[1], (Marking) petrinetWithMarking[2], true);
    }

}
