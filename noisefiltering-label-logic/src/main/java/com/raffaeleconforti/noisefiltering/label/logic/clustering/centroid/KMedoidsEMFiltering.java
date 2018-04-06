package com.raffaeleconforti.noisefiltering.label.logic.clustering.centroid;

import com.raffaeleconforti.noisefiltering.label.logic.FilteringResult;
import com.raffaeleconforti.noisefiltering.label.logic.clustering.AbstractClustering;
import com.raffaeleconforti.noisefiltering.label.logic.clustering.SkipClusterException;
import de.lmu.ifi.dbs.elki.algorithm.clustering.ClusteringAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.KMedoidsEM;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.KMedoidsInitialization;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.model.PrototypeModel;
import de.lmu.ifi.dbs.elki.distance.distancefunction.NumberVectorDistanceFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 26/12/17.
 */
public class KMedoidsEMFiltering<T> extends AbstractClustering<T> {

    public KMedoidsEMFiltering(T[] key, double[][] data, int maxClts) {
        super(key, data, maxClts);
    }

    public List<FilteringResult<T>> getOutliers() {
        List<FilteringResult<T>> outliers = new ArrayList<>();

        for (int l = 0; l < kMedoidsInitializations.length; l++) {
            KMedoidsInitialization kMedoidsInitialization = kMedoidsInitializations[l];
            for (int d = 0; d < distanceFunctions.length; d++) {
                NumberVectorDistanceFunction distanceFunction = distanceFunctions[d];
                ClusteringAlgorithm<Clustering<PrototypeModel>> clusteringMethod = new KMedoidsEM(
                        distanceFunction,
                        maxClts /* k - number of partitions */, //
                        0 /* maximum number of iterations: no limit */,
                        kMedoidsInitialization
                );
                try {
                    String technique = "KMedoidsEM - (" +
                            distanceFunction.getClass().getCanonicalName().substring(distanceFunction.getClass().getCanonicalName().lastIndexOf(".") + 1) +
                            " " +
                            kMedoidsInitialization.getClass().getCanonicalName().substring(kMedoidsInitialization.getClass().getCanonicalName().lastIndexOf(".") + 1) +
                            ")";
                    outliers.add(
                            extractOutliers(
                                    clusteringMethod,
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
