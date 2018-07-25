package com.raffaeleconforti.splitminer.splitminer.dfgp.variants;

import com.raffaeleconforti.splitminer.log.SimpleLog;
import com.raffaeleconforti.splitminer.splitminer.dfgp.DirectlyFollowGraphPlus;

public class DFGPNoFilter extends DirectlyFollowGraphPlus {

    public DFGPNoFilter(SimpleLog log, double percentileFrequencyThreshold, double parallelismsThreshold, boolean parallelismsFirst) {
        super(log, percentileFrequencyThreshold, parallelismsThreshold, parallelismsFirst);
    }
}
