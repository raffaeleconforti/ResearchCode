/*
 *  Copyright (C) 2018 Raffaele Conforti (www.raffaeleconforti.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
