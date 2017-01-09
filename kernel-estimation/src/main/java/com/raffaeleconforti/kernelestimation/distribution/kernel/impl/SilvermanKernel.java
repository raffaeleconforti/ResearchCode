package com.raffaeleconforti.kernelestimation.distribution.kernel.impl;

import com.raffaeleconforti.kernelestimation.distribution.kernel.Kernel;

/**
 * Created by conforti on 29/01/2016.
 */
public class SilvermanKernel implements Kernel {

    @Override
    public double getKernel(double u) {
        return (0.5 * Math.exp(-(Math.abs(u) / Math.sqrt(2))) * Math.sin((Math.abs(u) / Math.sqrt(2)) + (Math.PI / 4)));
    }

    @Override
    public double value(double v) {
        return getKernel(v);
    }
}
