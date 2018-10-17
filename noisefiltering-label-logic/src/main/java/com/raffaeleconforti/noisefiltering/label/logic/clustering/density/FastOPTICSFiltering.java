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

package com.raffaeleconforti.noisefiltering.label.logic.clustering.density;

import com.raffaeleconforti.noisefiltering.label.logic.FilteringResult;
import com.raffaeleconforti.noisefiltering.label.logic.clustering.SkipClusterException;
import de.lmu.ifi.dbs.elki.algorithm.clustering.optics.FastOPTICS;
import de.lmu.ifi.dbs.elki.algorithm.clustering.optics.OPTICSTypeAlgorithm;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.index.preprocessed.fastoptics.RandomProjectedNeighborsAndDensities;
import de.lmu.ifi.dbs.elki.math.random.RandomFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 26/12/17.
 */
public class FastOPTICSFiltering<T> extends DensityBasedClustering<T> {

    public FastOPTICSFiltering(T[] key, double[][] data, int maxClts) {
        super(key, data, maxClts);
    }

    public List<FilteringResult<T>> getOutliers() {
        List<FilteringResult<T>> outliers = new ArrayList<>();
        RandomProjectedNeighborsAndDensities distanceFunction = new RandomProjectedNeighborsAndDensities(RandomFactory.DEFAULT);

        OPTICSTypeAlgorithm clusteringMethod = new FastOPTICS<NumberVector>(minPts, distanceFunction);
        try {
            String technique = "FastOPTICS - (" +
                    distanceFunction.getClass().getCanonicalName().substring(distanceFunction.getClass().getCanonicalName().lastIndexOf(".") + 1) +
                    ")";
            outliers.add(
                    extractOutliers(
                            wrapClusteringTechnique(clusteringMethod),
                            technique
                    )
            );
        } catch (SkipClusterException e) {
        }

        return outliers;

    }

}
