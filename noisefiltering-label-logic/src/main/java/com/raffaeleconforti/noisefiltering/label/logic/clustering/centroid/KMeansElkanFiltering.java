package com.raffaeleconforti.noisefiltering.label.logic.clustering.centroid;

import com.raffaeleconforti.noisefiltering.label.logic.FilteringResult;
import com.raffaeleconforti.noisefiltering.label.logic.clustering.AbstractClustering;
import com.raffaeleconforti.noisefiltering.label.logic.clustering.SkipClusterException;
import de.lmu.ifi.dbs.elki.algorithm.clustering.ClusteringAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.KMeansElkan;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.KMeansInitialization;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.model.PrototypeModel;
import de.lmu.ifi.dbs.elki.distance.distancefunction.NumberVectorDistanceFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 26/12/17.
 */
public class KMeansElkanFiltering<T> extends AbstractClustering<T> {

    public KMeansElkanFiltering(T[] key, double[][] data, int maxClts) {
        super(key, data, maxClts);
    }

    public List<FilteringResult<T>> getOutliers() {
        List<FilteringResult<T>> outliers = new ArrayList<>();

        for (int l = 0; l < kMeansInitializations.length; l++) {
            KMeansInitialization kMeansInitialization = kMeansInitializations[l];
            for (int d = 0; d < distanceFunctions.length; d++) {
                NumberVectorDistanceFunction distanceFunction = distanceFunctions[d];
                ClusteringAlgorithm<Clustering<PrototypeModel>> clusteringMethod = new KMeansElkan(
                        distanceFunction,
                        maxClts /* k - number of partitions */, //
                        0 /* maximum number of iterations: no limit */,
                        kMeansInitialization,
                        false
                );
                try {
                    String technique = "KMeansElkan - (" +
                            distanceFunction.getClass().getCanonicalName().substring(distanceFunction.getClass().getCanonicalName().lastIndexOf(".") + 1) +
                            " " +
                            kMeansInitialization.getClass().getCanonicalName().substring(kMeansInitialization.getClass().getCanonicalName().lastIndexOf(".") + 1) +
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
