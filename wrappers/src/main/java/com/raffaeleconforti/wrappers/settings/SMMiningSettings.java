package com.raffaeleconforti.wrappers.settings;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Adriano on 28/06/2017.
 */
public class SMMiningSettings extends MiningSettings {
        private Set<String> settingLabels;

        public SMMiningSettings() {
            super();
            settingLabels = new HashSet<>();
            settingLabels.add("epsilonSM");
            settingLabels.add("etaSM");
            settingLabels.add("replaceORsSM");
        }

        public Set<String> getSettingLabels() { return new HashSet<String>(settingLabels); }
    }
