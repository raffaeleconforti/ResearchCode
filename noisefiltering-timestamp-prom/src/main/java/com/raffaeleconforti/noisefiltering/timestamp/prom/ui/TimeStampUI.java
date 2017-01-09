package com.raffaeleconforti.noisefiltering.timestamp.prom.ui;

import org.deckfour.uitopia.api.event.TaskListener;
import org.processmining.contexts.uitopia.UIPluginContext;

import java.util.concurrent.CancellationException;

/**
 * Created by conforti on 26/02/15.
 */
public class TimeStampUI {

    public int showGUI(UIPluginContext context) {

        TimeStampResult timeStampResult = new TimeStampResult();
        TaskListener.InteractionResult guiResult = context.showWizard("Select TimeStampResult Level",
                true, true, timeStampResult);
        if (guiResult == TaskListener.InteractionResult.CANCEL) {
            context.getFutureResult(0).cancel(true);
            throw new CancellationException("The wizard has been cancelled.");
        }

        return timeStampResult.getApproach();

    }

}
