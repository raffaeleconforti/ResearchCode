package org.processmining.plugins.bpmnminer.prom.subprocessminer.ui;

import com.raffaeleconforti.bpmnminer.exception.ExecutionCancelledException;
import com.raffaeleconforti.bpmnminer.subprocessminer.selection.SelectMinerResult;
import org.deckfour.uitopia.api.event.TaskListener;
import org.processmining.contexts.uitopia.UIPluginContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raffaele Conforti on 20/02/14.
 */

public class SelectMinerUI {

    public SelectMinerResult showGUI(UIPluginContext context) throws ExecutionCancelledException {

        List<String> allAttributes = new ArrayList<String>();
        allAttributes.add(SelectMinerResult.HMWO5);
        allAttributes.add(SelectMinerResult.IM);
        allAttributes.add(SelectMinerResult.ALPHA);
        allAttributes.add(SelectMinerResult.ILP);
        allAttributes.add(SelectMinerResult.HMW5);
        allAttributes.add(SelectMinerResult.HM6);

        // show ui to user to confirm/select primary keys
        SelectMiner ignoreGui = new SelectMiner(allAttributes, false);
        TaskListener.InteractionResult guiResult = context.showWizard("Select Miner", true, true, ignoreGui);
        if (guiResult == TaskListener.InteractionResult.CANCEL) {
            throw new ExecutionCancelledException();
        }

        return ignoreGui.getSelections();

    }

}
