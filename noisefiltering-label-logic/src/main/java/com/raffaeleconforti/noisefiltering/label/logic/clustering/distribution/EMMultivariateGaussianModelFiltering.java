package com.raffaeleconforti.noisefiltering.label.logic.clustering.distribution;

import com.raffaeleconforti.noisefiltering.label.logic.FilteringResult;
import com.raffaeleconforti.noisefiltering.label.logic.clustering.AbstractClustering;
import com.raffaeleconforti.noisefiltering.label.logic.clustering.SkipClusterException;
import de.lmu.ifi.dbs.elki.algorithm.clustering.ClusteringAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.clustering.em.EM;
import de.lmu.ifi.dbs.elki.algorithm.clustering.em.EMClusterModelFactory;
import de.lmu.ifi.dbs.elki.algorithm.clustering.em.MultivariateGaussianModelFactory;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.KMeansInitialization;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.model.PrototypeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 26/12/17.
 */
public class EMMultivariateGaussianModelFiltering<T> extends AbstractClustering<T> {

    public EMMultivariateGaussianModelFiltering(T[] key, double[][] data, int maxClts) {
        super(key, data, maxClts);
    }

    public List<FilteringResult<T>> getOutliers() {
        List<FilteringResult<T>> outliers = new ArrayList<>();

        for (int l = 0; l < kMeansInitializations.length; l++) {
            KMeansInitialization kMeansInitialization = kMeansInitializations[l];
            EMClusterModelFactory emClusterModelFactory = new MultivariateGaussianModelFactory(kMeansInitialization);
            ClusteringAlgorithm<Clustering<PrototypeModel>> clusteringMethod = new EM<>(
                    maxClts,
                    0.1,
                    emClusterModelFactory,
                    0,
                    true);
            try {
                String technique = "EMMultivariateGaussianModel - (" +
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
