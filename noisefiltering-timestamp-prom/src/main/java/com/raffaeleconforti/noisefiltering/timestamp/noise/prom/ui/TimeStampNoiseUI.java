package com.raffaeleconforti.noisefiltering.timestamp.noise.prom.ui;

import com.raffaeleconforti.noisefiltering.timestamp.noise.selection.TimeStampNoiseResult;
import org.deckfour.uitopia.api.event.TaskListener;
import org.processmining.contexts.uitopia.UIPluginContext;

import java.util.concurrent.CancellationException;

/**
 * Created by conforti on 26/02/15.
 */
public class TimeStampNoiseUI {

    public TimeStampNoiseResult showGUI(UIPluginContext context) {

        TimeStampNoise timeStampNoise = new TimeStampNoise();
        TaskListener.InteractionResult guiResult = context.showWizard("Select TimeStampResult Level",
                true, true, timeStampNoise );
        if (guiResult == TaskListener.InteractionResult.CANCEL) {
            context.getFutureResult(0).cancel(true);
            throw new CancellationException("The wizard has been cancelled.");
        }

        return timeStampNoise .getSelections();

    }

}
