package com.raffaeleconforti.noisefiltering.event.noise.prom.ui;

import com.raffaeleconforti.noisefiltering.event.noise.selection.NoiseResult;
import org.deckfour.uitopia.api.event.TaskListener;
import org.processmining.contexts.uitopia.UIPluginContext;

import java.util.concurrent.CancellationException;

/**
 * Created by conforti on 26/02/15.
 */
public class NoiseUI {

    public NoiseResult showGUI(UIPluginContext context) {

        Noise noise = new Noise();
        TaskListener.InteractionResult guiResult = context.showWizard("Select Noise Level",
                true, true, noise);
        if (guiResult == TaskListener.InteractionResult.CANCEL) {
            context.getFutureResult(0).cancel(true);
            throw new CancellationException("The wizard has been cancelled.");
        }

        return noise.getSelections();

    }

}
