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

package com.raffaeleconforti.structuredminer.ui;

import org.deckfour.uitopia.api.event.TaskListener;
import org.processmining.contexts.uitopia.UIPluginContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

/**
 * Created by Raffaele Conforti on 20/02/14.
 */

public class SettingsStructuredMinerUI {

    private static final String HM = "Heuristics Miner";
    private static final String HM52 = "Heuristics Miner 5.2";
    private static final String HDM = "Heuristics Dollar Miner";
    private static final String FODINA = "FODINA";

    public SettingsStructuredMiner showGUI(UIPluginContext context) {

        List<String> allAttributes = new ArrayList<String>();
        allAttributes.add(HM);
        allAttributes.add(HM52);
        allAttributes.add(HDM);
        allAttributes.add(FODINA);

        // show UI to user to confirm/select primary keys
        SettingsStructuredMinerFrame ignoreGui = new SettingsStructuredMinerFrame(allAttributes);
        TaskListener.InteractionResult guiResult = context.showWizard("Select Miner",
                true, true, ignoreGui);
        if (guiResult == TaskListener.InteractionResult.CANCEL) {
            context.getFutureResult(0).cancel(true);
            throw new CancellationException("The wizard has been cancelled.");
        }

        return ignoreGui.getSelections();

    }

}
