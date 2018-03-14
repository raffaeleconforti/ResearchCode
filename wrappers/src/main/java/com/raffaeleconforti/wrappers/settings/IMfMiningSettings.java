package com.raffaeleconforti.wrappers.settings;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Adriano on 28/06/2017.
 */
public class IMfMiningSettings extends MiningSettings {
    private Set<String> settingLabels;

    public IMfMiningSettings() {
        super();
        settingLabels = new HashSet<>();
        settingLabels.add("noiseThresholdIMf");
    }

    public Set<String> getSettingLabels() { return new HashSet<String>(settingLabels); }
}
