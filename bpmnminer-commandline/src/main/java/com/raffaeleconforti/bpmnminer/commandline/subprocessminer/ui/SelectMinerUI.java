package com.raffaeleconforti.bpmnminer.commandline.subprocessminer.ui;

import com.raffaeleconforti.bpmnminer.subprocessminer.selection.SelectMinerResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raffaele Conforti on 20/02/14.
 */

public class SelectMinerUI {

    public SelectMinerResult showGUI() {

        List<String> allAttributes = new ArrayList<String>();
        allAttributes.add(SelectMinerResult.HM5);
        allAttributes.add(SelectMinerResult.IM);
        allAttributes.add(SelectMinerResult.ALPHA);
        allAttributes.add(SelectMinerResult.ILP);
        allAttributes.add(SelectMinerResult.SM);
        allAttributes.add(SelectMinerResult.HM6);

        // show ui to user to confirm/select primary keys
        SelectMiner ignoreGui = new SelectMiner(allAttributes);

        return ignoreGui.getSelectedAlgorithm();

    }

}
