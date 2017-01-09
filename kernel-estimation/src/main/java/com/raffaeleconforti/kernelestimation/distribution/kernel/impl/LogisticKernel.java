package com.raffaeleconforti.kernelestimation.distribution.kernel.impl;

import com.raffaeleconforti.kernelestimation.distribution.kernel.Kernel;

/**
 * Created by conforti on 29/01/2016.
 */
public class LogisticKernel implements Kernel {

    @Override
    public double getKernel(double u) {
        return (1 / (Math.exp(u) + 2 + Math.exp(-u)));
    }

    @Override
    public double value(double v) {
        return getKernel(v);
    }
}
