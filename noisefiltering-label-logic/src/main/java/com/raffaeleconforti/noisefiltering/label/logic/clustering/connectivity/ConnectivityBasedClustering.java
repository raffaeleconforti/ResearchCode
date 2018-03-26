package com.raffaeleconforti.noisefiltering.label.logic.clustering.connectivity;

import com.raffaeleconforti.noisefiltering.label.logic.clustering.AbstractClustering;
import de.lmu.ifi.dbs.elki.algorithm.clustering.ClusteringAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.HierarchicalClusteringAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.extraction.SimplifiedHierarchyExtraction;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.model.AbstractModel;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 27/12/17.
 */
public abstract class ConnectivityBasedClustering<T> extends AbstractClustering<T> {

    public ConnectivityBasedClustering(T[] key, double[][] data, int maxClts) {
        super(key, data, maxClts);
    }

    public ClusteringAlgorithm<? extends Clustering<? extends AbstractModel>> wrapClusteringTechnique(HierarchicalClusteringAlgorithm clusteringMethod) {
        return new SimplifiedHierarchyExtraction(clusteringMethod, 2);
    }

}
