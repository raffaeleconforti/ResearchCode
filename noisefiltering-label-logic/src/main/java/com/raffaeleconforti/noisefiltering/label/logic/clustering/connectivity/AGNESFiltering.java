package com.raffaeleconforti.noisefiltering.label.logic.clustering.connectivity;

import com.raffaeleconforti.noisefiltering.label.logic.FilteringResult;
import com.raffaeleconforti.noisefiltering.label.logic.clustering.SkipClusterException;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.AGNES;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.HierarchicalClusteringAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.LinkageMethod;
import de.lmu.ifi.dbs.elki.distance.distancefunction.NumberVectorDistanceFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 26/12/17.
 */
public class AGNESFiltering<T> extends ConnectivityBasedClustering<T> {

    public AGNESFiltering(T[] key, double[][] data, int maxClts) {
        super(key, data, maxClts);
    }

    public List<FilteringResult<T>> getOutliers() {
        List<FilteringResult<T>> outliers = new ArrayList<>();
        for (int l = 0; l < linkageMethods.length; l++) {
            LinkageMethod linkageMethod = linkageMethods[l];
            for (int d = 0; d < distanceFunctions.length; d++) {
                NumberVectorDistanceFunction distanceFunction = distanceFunctions[d];
                HierarchicalClusteringAlgorithm clusteringMethod = new AGNES<>(distanceFunction, linkageMethod);
                try {
                    String technique = "AGNES - (" +
                            distanceFunction.getClass().getCanonicalName().substring(distanceFunction.getClass().getCanonicalName().lastIndexOf(".") + 1) +
                            " " +
                            linkageMethod.getClass().getCanonicalName().substring(linkageMethod.getClass().getCanonicalName().lastIndexOf(".") + 1) +
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
        }

        return outliers;

    }

}
