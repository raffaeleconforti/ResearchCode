package com.raffaeleconforti.kernelestimation.distribution.kernel;

import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 * Created by conforti on 29/01/2016.
 */
public interface Kernel extends UnivariateFunction {

    double getKernel(double u);
}
