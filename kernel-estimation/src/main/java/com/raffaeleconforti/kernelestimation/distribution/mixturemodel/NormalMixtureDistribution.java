package com.raffaeleconforti.kernelestimation.distribution.mixturemodel;

import com.raffaeleconforti.kernelestimation.distribution.kernel.Kernel;
import com.raffaeleconforti.kernelestimation.distribution.kernel.KernelDensityEstimator;
import com.raffaeleconforti.kernelestimation.distribution.kernel.impl.LogisticKernel;
import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.random.Well19937c;

/**
 * Created by conforti on 29/01/2016.
 */
public class NormalMixtureDistribution extends AbstractRealDistribution {


    private final Kernel kernel = new LogisticKernel();
    private final KernelDensityEstimator kernelDensityEstimator;

    public NormalMixtureDistribution(double[] data) {
        super(new Well19937c());
        kernelDensityEstimator = new KernelDensityEstimator(kernel, data);
    }

    @Override
    public double density(double v) {
        return kernelDensityEstimator.estimate(v);
    }

    @Override
    public double cumulativeProbability(double v) {
        return kernelDensityEstimator.cumulativeProbability(v);
    }

    @Override
    public double getNumericalMean() {
        return kernelDensityEstimator.getMean();
    }

    @Override
    public double getNumericalVariance() {
        return Math.pow(kernelDensityEstimator.getStdDeviation(), 2);
    }

    @Override
    public double getSupportLowerBound() {
        return -1.0D / 0.0;
    }

    @Override
    public double getSupportUpperBound() {
        return 1.0D / 0.0;
    }

    @Override
    public boolean isSupportLowerBoundInclusive() {
        return false;
    }

    @Override
    public boolean isSupportUpperBoundInclusive() {
        return false;
    }

    @Override
    public boolean isSupportConnected() {
        return true;
    }
}
