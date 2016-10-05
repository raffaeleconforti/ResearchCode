package com.raffaeleconforti.measurements.ui.computemeasurement;

import org.deckfour.uitopia.api.event.TaskListener;
import org.processmining.contexts.uitopia.UIPluginContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

/**
 * Created by Raffaele Conforti on 20/02/14.
 */

public class SelectMinerUICM {

    public static final String BPMN = "BPMN Miner";
    public static final String ALPHA = "Alpha Algorithm";
    public static final String ILP = "ILP Miner";
    public static final String HM = "Heuristics Miner";
    public static final String HM52 = "Heuristics Miner 5.2";
    public static final String FODINA = "Fodina Miner";
    public static final String IM = "Inductive Miner";
    public static final String ETM = "Evolutionary Tree Miner";
    public static final String SM = "Structured Miner";


    public static final int BPMNPOS = 0;
    public static final int IMPOS = 1;
    public static final int HMPOS = 2;
    public static final int FODINAPOS = 3;
    public static final int ILPPOS = 4;
    public static final int ALPHAPOS = 5;
    public static final int HMPOS52 = 6;
    public static final int ETMPOS = 7;
    public static final int SMPOS = 8;

    private boolean showAlgo = false;

    public SelectMinerUICM(boolean showAlgorithm) {
        showAlgo = showAlgorithm;
    }

    public SelectMinerUIResultCM showGUI(UIPluginContext context) {

        List<String> allAttributes = new ArrayList<String>();
        if(showAlgo) {
            allAttributes.add(BPMN);
            allAttributes.add(IM);
            allAttributes.add(HM);
            allAttributes.add(FODINA);
            allAttributes.add(ILP);
            allAttributes.add(ALPHA);
            allAttributes.add(HM52);
            allAttributes.add(ETM);
            allAttributes.add(SM);
        }

        // show UI to user to confirm/select primary keys
        SelectMinerCM ignoreGui = new SelectMinerCM(allAttributes);
        TaskListener.InteractionResult guiResult = context.showWizard("Select Miner",
                true, true, ignoreGui);
        if (guiResult == TaskListener.InteractionResult.CANCEL) {
            context.getFutureResult(0).cancel(true);
            throw new CancellationException("The wizard has been cancelled.");
        }

        return ignoreGui.getSelections();

    }

}
