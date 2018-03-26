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
