/*
 *  Copyright (C) 2018 Raffaele Conforti (www.raffaeleconforti.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.raffaeleconforti.kernelestimation.distribution.kernel;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Created by conforti on 29/01/2016.
 */
public class KernelDensityEstimator implements UnivariateFunction {

    private final Kernel kernel;
    private final double[] data;
    private final NormalDistribution[] distributions;

    private final double h;
    private final int n;

    public KernelDensityEstimator(Kernel kernel, double[] data) {
        this.kernel = kernel;
        this.data = data;
        this.distributions = new NormalDistribution[data.length];

        this.n = data.length;
        this.h = estimateBandwidthUsingSilvermanRuleOfThumb();

        for(int i = 0; i < n; i++) {
            distributions[i] = new NormalDistribution(data[i], h);
        }
    }

    public double estimate(double x) {
        double k = 0;
        for(int i = 0; i < n; i++) {
            k += kernel.getKernel((data[i] - x) / h);
        }
        return k / (n * h);
    }

    public double cumulativeProbability(double x) {
        double k = 0;
        for(int i = 0; i < n; i++) {
            k += distributions[i].cumulativeProbability(x);
        }
        return k / n;
    }

    public double getMean() {
        double mean = 0;
        for(int i = 0; i < data.length; i++) {
            mean += data[i];
        }
        return mean / data.length;
    }

    public double getStdDeviation() {
        double mean = getMean();
        double stdDeviation = 0;
        for(int i = 0; i < data.length; i++) {
            stdDeviation += Math.pow((mean - data[i]), 2);
        }
        return Math.sqrt(stdDeviation / data.length);
    }

    private double estimateBandwidthUsingSilvermanRuleOfThumb() {
        double stdDeviation = getStdDeviation();
        return 1.06 * stdDeviation * Math.pow(data.length, -0.2);
    }

    @Override
    public double value(double v) {
        return estimate(v);
    }
}



