package com.raffaeleconforti.kernelestimation.distribution.kernel.impl;

import com.raffaeleconforti.kernelestimation.distribution.kernel.Kernel;

/**
 * Created by conforti on 29/01/2016.
 */
public class GaussianKernel implements Kernel {

    @Override
    public double getKernel(double u) {
        return (1 / (Math.sqrt(2 * Math.PI))) * Math.exp(-0.5 * Math.pow(u, 2));
    }

    @Override
    public double value(double v) {
        return getKernel(v);
    }
}
