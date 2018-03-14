package com.raffaeleconforti.wrappers.settings;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Adriano on 28/06/2017.
 */
public class HM6MiningSettings extends MiningSettings {
    private Set<String> settingLabels;

    public HM6MiningSettings() {
        super();
        settingLabels = new HashSet<>();
        settingLabels.add("dependencyThresholdHM6");
        settingLabels.add("L1lThresholdHM6");
        settingLabels.add("L2lThresholdHM6");
        settingLabels.add("longDepThresholdHM6");
        settingLabels.add("relativeToBestThresholdHM6");
        settingLabels.add("allConnectedHM6");
        settingLabels.add("longDependencyHM6");
    }

    public Set<String> getSettingLabels() { return new HashSet<String>(settingLabels); }
}
