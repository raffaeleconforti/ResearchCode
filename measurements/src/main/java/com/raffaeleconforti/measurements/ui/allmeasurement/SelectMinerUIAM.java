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

package com.raffaeleconforti.measurements.ui.allmeasurement;

import org.deckfour.uitopia.api.event.TaskListener;
import org.processmining.contexts.uitopia.UIPluginContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

/**
 * Created by Raffaele Conforti on 20/02/14.
 */

public class SelectMinerUIAM {

    public static final String BPMN = "BPMN Miner";
    public static final String ALPHA = "Alpha Algorithm";
    public static final String ILP = "ILP Miner";
    public static final String HM = "Heuristics Miner";
    public static final String HM52 = "Heuristics Miner 5.2";
    public static final String FODINA = "Fodina Miner";
    public static final String IM = "Inductive Miner";
    public static final String ETM = "Evolutionary Tree Miner";
    public static final String SM = "Structured Miner";

    public SelectMinerUIResultAM showGUI(UIPluginContext context, boolean discoverModel) {

        List<String> allAttributes = new ArrayList<String>();
        allAttributes.add(BPMN);
        allAttributes.add(IM);
        allAttributes.add(HM);
        allAttributes.add(FODINA);
        allAttributes.add(ILP);
        allAttributes.add(ALPHA);
        allAttributes.add(HM52);
        allAttributes.add(ETM);
        allAttributes.add(SM);

        // show UI to user to confirm/select primary keys
        SelectMinerAM ignoreGui = new SelectMinerAM(allAttributes, discoverModel);
        TaskListener.InteractionResult guiResult = context.showWizard("Select Miner",
                true, true, ignoreGui);
        if (guiResult == TaskListener.InteractionResult.CANCEL) {
            context.getFutureResult(0).cancel(true);
            throw new CancellationException("The wizard has been cancelled.");
        }

        return ignoreGui.getSelections();

    }

}
