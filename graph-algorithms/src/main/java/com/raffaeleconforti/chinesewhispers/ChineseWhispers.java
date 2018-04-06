package com.raffaeleconforti.chinesewhispers;

//import org.jgrapht.WeightedGraph;
//import org.jgrapht.experimental.permutation.CollectionPermutationIter;

/**
 * Created by andreas on 2/25/17.
 */
public class ChineseWhispers<V extends Comparable<V>,E> {

    private static final int MAX_ITERATIONS = 100;
    private final long seed;

    public ChineseWhispers(long seed){
        this.seed = seed;
    }


//    public Collection<Set<V>> getClustering(WeightedGraph<V,E> graph){
//        ArrayList<V> vertices = new ArrayList<V>(graph.vertexSet());
//        Collections.sort(vertices);
//        CollectionPermutationIter<V> permutationIter = new CollectionPermutationIter(vertices);
//        // assign each node a unique label
//        int i = 0;
//        Map<V, Integer> classAssignment = new THashMap<>();
//        for (V vertex : permutationIter.getNextArray()){
//            classAssignment.put(vertex,i++);
//        }
//
//        // do whispers:
//        boolean converged = false;
//        for (int iter = 0; iter < MAX_ITERATIONS && ! converged; iter++) {
//            Map<Integer, Double> weights = new THashMap<>();
//            converged = true;
//            for (V vertex : permutationIter.getNextArray()) {
//                double currentHighestWeight = 0;
//                int currentHighestClass = classAssignment.get(vertex);
//
//                Set<E> edges = graph.edgesOf(vertex);
//
//                for (E edge : edges) {
//                    V target = graph.getEdgeTarget(edge);
//                    V source = graph.getEdgeSource(edge);
//                    V other = (target == vertex ? source : target);
//                    int otherClass = classAssignment.get(other);
//                    if (currentHighestClass != otherClass) {
//                        double newWeight = weights.containsKey(otherClass) ? weights.get(otherClass) : 0;
//                        newWeight += graph.getEdgeWeight(edge);
//                        weights.put(otherClass, newWeight);
//                        if (newWeight > currentHighestWeight) {
//                            currentHighestWeight = newWeight;
//                            currentHighestClass = otherClass;
//                        }
//                    }
//                }
//                if (currentHighestWeight > 0 && currentHighestClass != classAssignment.get(vertex)) {
//                    converged = false;
//                    classAssignment.put(vertex, currentHighestClass);
//                }
//            }
////            if (converged){
////                System.out.println("converged after "+iter+" iterations.");
////            }
//        }
//
//        // read out the clusters:
//        Map<Integer, Set<V>> clusters = new THashMap<>();
//        for (Map.Entry<V,Integer> entry : classAssignment.entrySet()){
//            if (!clusters.containsKey(entry.getValue())) {
//                clusters.put(entry.getValue(), new THashSet<V>());
//            }
//            clusters.get(entry.getValue()).add(entry.getKey());
//        }
//        return clusters.values();
//    }
}
