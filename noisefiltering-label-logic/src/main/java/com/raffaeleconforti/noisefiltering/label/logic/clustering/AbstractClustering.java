package com.raffaeleconforti.noisefiltering.label.logic.clustering;

import com.raffaeleconforti.noisefiltering.label.logic.FilteringResult;
import de.lmu.ifi.dbs.elki.algorithm.clustering.ClusteringAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.CentroidLinkageMethod;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.LinkageMethod;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.*;
import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.model.AbstractModel;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDRange;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;
import de.lmu.ifi.dbs.elki.distance.distancefunction.NumberVectorDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.random.RandomFactory;
import de.lmu.ifi.dbs.elki.utilities.exceptions.ObjectNotFoundException;

import java.util.*;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 26/12/17.
 */
public abstract class AbstractClustering<T> {

    protected final NumberVectorDistanceFunction[] distanceFunctions = new NumberVectorDistanceFunction[]{
//            HistogramIntersectionDistanceFunction.STATIC,
//            AbsolutePearsonCorrelationDistanceFunction.STATIC,OK
//            PearsonCorrelationDistanceFunction.STATIC,OK
//            SquaredPearsonCorrelationDistanceFunction.STATIC,
//            SquaredUncenteredCorrelationDistanceFunction.STATIC,
//            UncenteredCorrelationDistanceFunction.STATIC,
//            HistogramMatchDistanceFunction.STATIC,
//            KolmogorovSmirnovDistanceFunction.STATIC,
//            EuclideanDistanceFunction.STATIC,
//            ManhattanDistanceFunction.STATIC,
//            MaximumDistanceFunction.STATIC,
//            MinimumDistanceFunction.STATIC,
            SquaredEuclideanDistanceFunction.STATIC,
//            ChiSquaredDistanceFunction.STATIC,
//            HellingerDistanceFunction.STATIC,
//            JeffreyDivergenceDistanceFunction.STATIC,
//            JensenShannonDivergenceDistanceFunction.STATIC,
//            KullbackLeiblerDivergenceAsymmetricDistanceFunction.STATIC,
//            KullbackLeiblerDivergenceReverseAsymmetricDistanceFunction.STATIC,
//            SqrtJensenShannonDivergenceDistanceFunction.STATIC,
//            HammingDistanceFunction.STATIC,
//            new JaccardSimilarityDistanceFunction()
    };

    protected final LinkageMethod[] linkageMethods = new LinkageMethod[]{
            CentroidLinkageMethod.STATIC,
//            CompleteLinkageMethod.STATIC,
//            GroupAverageLinkageMethod.STATIC,
//            MedianLinkageMethod.STATIC,
//            SingleLinkageMethod.STATIC,
//            WardLinkageMethod.STATIC,
//            WeightedAverageLinkageMethod.STATIC
    };

    protected final KMeansInitialization[] kMeansInitializations = new KMeansInitialization[]{
//            new FarthestPointsInitialMeans(RandomFactory.DEFAULT, true),
//            new FarthestPointsInitialMeans(RandomFactory.DEFAULT, false),
//            new FarthestSumPointsInitialMeans(RandomFactory.DEFAULT, true),
//            new FarthestSumPointsInitialMeans(RandomFactory.DEFAULT, false),
//            new FirstKInitialMeans(),
            new KMeansPlusPlusInitialMeans(RandomFactory.DEFAULT),
//            new RandomlyChosenInitialMeans(RandomFactory.DEFAULT),
//            new RandomlyGeneratedInitialMeans(RandomFactory.DEFAULT)
    };


    protected final KMedoidsInitialization[] kMedoidsInitializations = new KMedoidsInitialization[]{
//            new FarthestPointsInitialMeans(RandomFactory.DEFAULT, true),
//            new FarthestPointsInitialMeans(RandomFactory.DEFAULT, false),
//            new FarthestSumPointsInitialMeans(RandomFactory.DEFAULT, true),
//            new FarthestSumPointsInitialMeans(RandomFactory.DEFAULT, false),
//            new FirstKInitialMeans(),
//            new KMeansPlusPlusInitialMeans(RandomFactory.DEFAULT),
            new RandomlyChosenInitialMeans(RandomFactory.DEFAULT)
    };

    protected Database db;
    protected T[] key;
    protected DBIDRange ids;
    //    protected Map<String, Double>[] maps;
    protected double[][] data;

    protected int maxClts;
    protected int minPts;
    protected int min_clusters = 2;
    protected int min_size_top_cluster = 1;

    public AbstractClustering(T[] key, double[][] data, int maxClts) {
        this.key = key;
        this.data = data;
        // Adapter to load data from an existing array.
        DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data);
        db = new StaticArrayDatabase(dbc, null);
        db.initialize();
        // Relation containing the number vectors:
        Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);
        ids = (DBIDRange) rel.getDBIDs();

        this.maxClts = maxClts; //(int) Math.ceil(Math.sqrt(key.length / 2));
        minPts = 1;//key.length / maxClts;
    }

    public FilteringResult<T> extractOutliers(ClusteringAlgorithm<? extends Clustering<? extends AbstractModel>> clusteringMethod, String technique) throws SkipClusterException {
        Set<T> outliers = new HashSet<>();
        List<? extends Cluster<? extends AbstractModel>> clusters;
        try {
            clusters = clusteringMethod.run(db).getAllClusters();
        } catch (ObjectNotFoundException onfe) {
            throw new SkipClusterException();
        }

        List<ClusterElement<T>> clusterElements = getClusterElements(clusters);

//        if ((clusterElements.size() > min_clusters) || (clusterElements.size() == min_clusters && clusterElements.get(1).getElements().size() > min_size_top_cluster)) {
        if ((clusterElements.size() >= min_clusters)) {
            ClusterElement<T> clu = clusterElements.get(0);
            for (T outlier : clu.getElements().keySet()) {
                if (clu.getElements().get(outlier)[2] > 1 && clu.getElements().get(outlier)[3] > 1) {
                    outliers.add(outlier);
                }
            }
        }
        return new FilteringResult<>(technique, outliers);
    }

    private double getClusterCost(Cluster<? extends AbstractModel> o1) {
        double avg1 = 0;
        int count = 0;
        for (DBIDIter it = o1.getIDs().iter(); it.valid(); it.advance()) {
            avg1 += getElementCost(ids.getOffset(it));
            count++;
        }
        avg1 /= count;

        return avg1;
    }

    private double getElementCost(int elementOffset) {
        double tmp_avg = 0;
        for (int i = 0; i < data[0].length; i++) {
            tmp_avg += data[elementOffset][i];
        }
        return (tmp_avg / data[0].length);
    }

    private List<ClusterElement<T>> getClusterElements(List<? extends Cluster<? extends AbstractModel>> clusters) {
        List<ClusterElement<T>> clusterElements = new ArrayList<>();

        for (Cluster<? extends AbstractModel> cluster : clusters) {
            ClusterElement<T> clusterElement = new ClusterElement<>(getClusterCost(cluster));
            for (DBIDIter it = cluster.getIDs().iter(); it.valid(); it.advance()) {
                int offset = ids.getOffset(it);
                clusterElement.addElement(key[offset], data[offset]);//getElementCost(offset));
            }
            clusterElements.add(clusterElement);
        }

        Collections.sort(clusterElements, new Comparator<ClusterElement<T>>() {
            @Override
            public int compare(ClusterElement<T> o1, ClusterElement<T> o2) {
                return Double.compare(o1.getCost(), o2.getCost());
            }
        });

        return clusterElements;
    }

    public abstract List<FilteringResult<T>> getOutliers();

}
