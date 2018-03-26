package com.raffaeleconforti.noisefiltering.label.logic.clustering.density;

import com.raffaeleconforti.noisefiltering.label.logic.clustering.AbstractClustering;
import de.lmu.ifi.dbs.elki.algorithm.clustering.ClusteringAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.clustering.optics.OPTICSTypeAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.clustering.optics.OPTICSXi;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.model.AbstractModel;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 27/12/17.
 */
public abstract class DensityBasedClustering<T> extends AbstractClustering<T> {

    public DensityBasedClustering(T[] key, double[][] data, int maxClts) {
        super(key, data, maxClts);
    }

    public ClusteringAlgorithm<? extends Clustering<? extends AbstractModel>> wrapClusteringTechnique(OPTICSTypeAlgorithm clusteringMethod) {
        return new OPTICSXi(clusteringMethod, 0.1);
    }

}
