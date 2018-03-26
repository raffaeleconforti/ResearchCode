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
