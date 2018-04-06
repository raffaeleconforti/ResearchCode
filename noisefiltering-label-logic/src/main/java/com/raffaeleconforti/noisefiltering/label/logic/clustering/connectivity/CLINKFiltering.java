package com.raffaeleconforti.noisefiltering.label.logic.clustering.connectivity;

import com.raffaeleconforti.noisefiltering.label.logic.FilteringResult;
import com.raffaeleconforti.noisefiltering.label.logic.clustering.SkipClusterException;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.CLINK;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.distance.distancefunction.NumberVectorDistanceFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 26/12/17.
 */
public class CLINKFiltering<T> extends ConnectivityBasedClustering<T> {

    public CLINKFiltering(T[] key, double[][] data, int maxClts) {
        super(key, data, maxClts);
    }

    public List<FilteringResult<T>> getOutliers() {
        List<FilteringResult<T>> outliers = new ArrayList<>();
        for (int d = 0; d < distanceFunctions.length; d++) {
            NumberVectorDistanceFunction distanceFunction = distanceFunctions[d];
            CLINK<NumberVector> clusteringMethod = new CLINK<>(distanceFunction);
            try {
                String technique = "CLINK - (" +
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
        }

        return outliers;

    }

}
