package com.raffaeleconforti.noisefiltering.label.logic.clustering.distribution;

import com.raffaeleconforti.noisefiltering.label.logic.FilteringResult;
import com.raffaeleconforti.noisefiltering.label.logic.clustering.AbstractClustering;
import com.raffaeleconforti.noisefiltering.label.logic.clustering.SkipClusterException;
import de.lmu.ifi.dbs.elki.algorithm.clustering.ClusteringAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.clustering.em.DiagonalGaussianModelFactory;
import de.lmu.ifi.dbs.elki.algorithm.clustering.em.EM;
import de.lmu.ifi.dbs.elki.algorithm.clustering.em.EMClusterModelFactory;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.KMeansInitialization;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.model.PrototypeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 26/12/17.
 */
public class EMDiagonalGaussianModelFiltering<T> extends AbstractClustering<T> {

    public EMDiagonalGaussianModelFiltering(T[] key, double[][] data, int maxClts) {
        super(key, data, maxClts);
    }

    public List<FilteringResult<T>> getOutliers() {
        List<FilteringResult<T>> outliers = new ArrayList<>();

        for (int l = 0; l < kMeansInitializations.length; l++) {
            KMeansInitialization kMeansInitialization = kMeansInitializations[l];
            EMClusterModelFactory emClusterModelFactory = new DiagonalGaussianModelFactory(kMeansInitialization);
            ClusteringAlgorithm<Clustering<PrototypeModel>> clusteringMethod = new EM<>(
                    maxClts,
                    0.1,
                    emClusterModelFactory,
                    0,
                    true);
            try {
                String technique = "EMDiagonalGaussianModel - (" +
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

        return outliers;

    }

}
