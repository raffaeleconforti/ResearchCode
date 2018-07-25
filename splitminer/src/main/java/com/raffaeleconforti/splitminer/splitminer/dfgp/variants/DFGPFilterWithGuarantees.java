package com.raffaeleconforti.splitminer.splitminer.dfgp.variants;

import com.raffaeleconforti.splitminer.log.SimpleLog;
import com.raffaeleconforti.splitminer.splitminer.dfgp.DFGEdge;
import com.raffaeleconforti.splitminer.splitminer.dfgp.DirectlyFollowGraphPlus;

import java.util.*;

/**
 * Created by Raffaele Conforti on 24/10/2016.
 */
public class DFGPFilterWithGuarantees extends DirectlyFollowGraphPlus {

    private Set<DFGEdge> bestEdges;
    private int filterThreshold;

//    public DFGPFilterWithGuarantees(SimpleLog log) {
//        super(log);
//    }

    public DFGPFilterWithGuarantees(SimpleLog log, double percentileFrequencyThreshold, double parallelismsThreshold, boolean parallelismsFirst) {
        super(log, percentileFrequencyThreshold, parallelismsThreshold, parallelismsFirst);
    }

    @Override
    public void buildDFGP() {
        super.buildDFGP();
        filterWithGuarantees();
    }

    private void filterWithGuarantees() {
        bestEdgesOnMaxFrequencies();
        computeFilterThreshold();

        bestEdgesOnMaxCapacities();
        for (DFGEdge e : new HashSet<>(edges))
            if (!bestEdges.contains(e) && !(e.getFrequency() >= filterThreshold)) removeEdge(e, false);
    }

    private void bestEdgesOnMaxFrequencies() {
        bestEdges = new HashSet<>();

        for (int node : nodes.keySet()) {
            if (node != endcode) bestEdges.add(Collections.max(outgoings.get(node)));
            if (node != startcode) bestEdges.add(Collections.max(incomings.get(node)));
        }
    }

    private void computeFilterThreshold() {
        ArrayList<DFGEdge> frequencyOrderedEdges = new ArrayList<>();
        int i;

        frequencyOrderedEdges.addAll(bestEdges);

        Collections.sort(frequencyOrderedEdges);
        i = (int) Math.round(frequencyOrderedEdges.size() * percentileFrequencyThreshold);
        if (i == frequencyOrderedEdges.size()) i--;
        filterThreshold = frequencyOrderedEdges.get(i).getFrequency();
    }

    private void bestEdgesOnMaxCapacities() {
        int src, tgt, cap, maxCap;
        DFGEdge bp, bs;

        LinkedList<Integer> toVisit = new LinkedList<>();
        Set<Integer> unvisited = new HashSet<>();

        HashMap<Integer, DFGEdge> bestPredecessorFromSource = new HashMap<>();
        HashMap<Integer, DFGEdge> bestSuccessorToSink = new HashMap<>();

        Map<Integer, Integer> maxCapacitiesFromSource = new HashMap<>();
        Map<Integer, Integer> maxCapacitiesToSink = new HashMap<>();

        for (int n : nodes.keySet()) {
            maxCapacitiesFromSource.put(n, 0);
            maxCapacitiesToSink.put(n, 0);
        }

        maxCapacitiesFromSource.put(startcode, Integer.MAX_VALUE);
        maxCapacitiesToSink.put(endcode, Integer.MAX_VALUE);

//      forward exploration
        toVisit.add(startcode);
        unvisited.addAll(nodes.keySet());
        unvisited.remove(startcode);

        while (!toVisit.isEmpty()) {
            src = toVisit.removeFirst();
            cap = maxCapacitiesFromSource.get(src);
            for (DFGEdge oe : outgoings.get(src)) {
                tgt = oe.getTargetCode();
                maxCap = (cap > oe.getFrequency() ? oe.getFrequency() : cap);
                if ((maxCap > maxCapacitiesFromSource.get(tgt))) { //|| ((maxCap == maxCapacitiesFromSource.get(tgt)) && (bestPredecessorFromSource.get(tgt).getFrequency() < oe.getFrequency())) ) {
                    maxCapacitiesFromSource.put(tgt, maxCap);
                    bestPredecessorFromSource.put(tgt, oe);
                    if (!toVisit.contains(tgt)) unvisited.add(tgt);
                }
                if (unvisited.contains(tgt)) {
                    toVisit.addLast(tgt);
                    unvisited.remove(tgt);
                }
            }
        }


//      backward exploration
        toVisit.add(endcode);
        unvisited.clear();
        unvisited.addAll(nodes.keySet());
        unvisited.remove(endcode);

        while (!toVisit.isEmpty()) {
            tgt = toVisit.removeFirst();
            cap = maxCapacitiesToSink.get(tgt);
            for (DFGEdge ie : incomings.get(tgt)) {
                src = ie.getSourceCode();
                maxCap = (cap > ie.getFrequency() ? ie.getFrequency() : cap);
                if ((maxCap > maxCapacitiesToSink.get(src))) { //|| ((maxCap == maxCapacitiesToSink.get(src)) && (bestSuccessorToSink.get(src).getFrequency() < ie.getFrequency())) ) {
                    maxCapacitiesToSink.put(src, maxCap);
                    bestSuccessorToSink.put(src, ie);
                    if (!toVisit.contains(src)) unvisited.add(src);
                }
                if (unvisited.contains(src)) {
                    toVisit.addLast(src);
                    unvisited.remove(src);
                }
            }
        }

        bestEdges = new HashSet<>();
        for (int n : nodes.keySet()) {
            bestEdges.add(bestPredecessorFromSource.get(n));
            bestEdges.add(bestSuccessorToSink.get(n));
        }
        bestEdges.remove(null);

//        for( int n : nodes.keySet() ) {
//            System.out.println("DEBUG - " + n + " : [" + maxCapacitiesFromSource.get(n) + "][" + maxCapacitiesToSink.get(n) + "]");
//        }
    }

}
