package com.raffaeleconforti.noisefiltering.event.noise.commandline.ui;

import com.raffaeleconforti.noisefiltering.event.noise.selection.NoiseResult;

/**
 * Created by conforti on 26/02/15.
 */
public class NoiseUI {

    public NoiseResult showGUI() {

        Noise noise = new Noise();
        return noise.getSelections();

    }

}
