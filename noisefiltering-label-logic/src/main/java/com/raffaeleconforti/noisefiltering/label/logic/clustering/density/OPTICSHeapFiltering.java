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
import de.lmu.ifi.dbs.elki.algorithm.clustering.optics.OPTICSHeap;
import de.lmu.ifi.dbs.elki.algorithm.clustering.optics.OPTICSTypeAlgorithm;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 26/12/17.
 */
public class OPTICSHeapFiltering<T> extends DensityBasedClustering<T> {

    public OPTICSHeapFiltering(T[] key, double[][] data, int maxClts) {
        super(key, data, maxClts);
    }

    public List<FilteringResult<T>> getOutliers() {
        List<FilteringResult<T>> outliers = new ArrayList<>();

        for (int l = 0; l < distanceFunctions.length; l++) {
            DistanceFunction distanceFunction = distanceFunctions[l];
//            for(int i = 0; i < 5; i++) {
            double epsilon = 1.0D / 0.0;//((double) i / 10);
            OPTICSTypeAlgorithm clusteringMethod = new OPTICSHeap<NumberVector>(distanceFunction, epsilon, minPts);
            try {
                String technique = "OPTICSHeap - (" +
                        distanceFunction.getClass().getCanonicalName().substring(distanceFunction.getClass().getCanonicalName().lastIndexOf(".") + 1) +
                        " " +
                        epsilon +
                        ")";
                outliers.add(
                        extractOutliers(
                                wrapClusteringTechnique(clusteringMethod),
                                technique
                        )
                );
            } catch (SkipClusterException e) {
            }
//            }
        }

        return outliers;

    }

}
