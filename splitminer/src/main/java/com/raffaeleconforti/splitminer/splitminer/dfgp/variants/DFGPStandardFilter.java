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

package com.raffaeleconforti.splitminer.splitminer.dfgp.variants;

import com.raffaeleconforti.splitminer.log.SimpleLog;
import com.raffaeleconforti.splitminer.splitminer.dfgp.DFGEdge;
import com.raffaeleconforti.splitminer.splitminer.dfgp.DirectlyFollowGraphPlus;

import java.util.*;

public class DFGPStandardFilter extends DirectlyFollowGraphPlus {

    private Set<DFGEdge> bestEdges;

    public DFGPStandardFilter(SimpleLog log, double percentileFrequencyThreshold, double parallelismsThreshold, boolean parallelismsFirst) {
        super(log, percentileFrequencyThreshold, parallelismsThreshold, parallelismsFirst);
    }

    public void buildDFGP() {
        super.buildDFGP();
        standardFilter();
        exploreAndRemove();
    }

    private void standardFilter() {
        int src;
        int tgt;
        DFGEdge recoverableEdge;

        bestEdgesOnMaxFrequencies();
        ArrayList<DFGEdge> frequencyOrderedBestEdges = new ArrayList<>(bestEdges);

        for (DFGEdge e : new HashSet<>(edges)) this.removeEdge(e, false);

        Collections.sort(frequencyOrderedBestEdges);
        for (int i = (frequencyOrderedBestEdges.size() - 1); i >= 0; i--) {
            recoverableEdge = frequencyOrderedBestEdges.get(i);

            src = recoverableEdge.getSourceCode();
            tgt = recoverableEdge.getTargetCode();
            if (outgoings.get(src).isEmpty() || incomings.get(tgt).isEmpty()) this.addEdge(recoverableEdge);
        }
    }

    private void bestEdgesOnMaxFrequencies() {
        bestEdges = new HashSet<>();

        for (int node : nodes.keySet()) {
            if (node != endcode) bestEdges.add(Collections.max(outgoings.get(node)));
            if (node != startcode) bestEdges.add(Collections.max(incomings.get(node)));
        }
    }

    private void exploreAndRemove() {
        int src, tgt;

        LinkedList<Integer> toVisit = new LinkedList<>();
        Set<Integer> unvisited = new HashSet<>();

//      forward exploration
        toVisit.add(startcode);
        unvisited.addAll(nodes.keySet());
        unvisited.remove(startcode);

        while (!toVisit.isEmpty()) {
            src = toVisit.removeFirst();
            for (DFGEdge oe : outgoings.get(src)) {
                tgt = oe.getTargetCode();
                if (unvisited.contains(tgt)) {
                    toVisit.addLast(tgt);
                    unvisited.remove(tgt);
                }
            }
        }

        for (int n : unvisited) {
            System.out.println("DEBUG - fwd removed: " + nodes.get(n).print());
            removeNode(n);
        }

//      backward exploration
        toVisit.add(endcode);
        unvisited.clear();
        unvisited.addAll(nodes.keySet());
        unvisited.remove(endcode);

        while (!toVisit.isEmpty()) {
            tgt = toVisit.removeFirst();
            for (DFGEdge oe : incomings.get(tgt)) {
                src = oe.getSourceCode();
                if (unvisited.contains(src)) {
                    toVisit.addLast(src);
                    unvisited.remove(src);
                }
            }
        }

        for (int n : unvisited) {
            System.out.println("DEBUG - bkw removed: " + nodes.get(n).print());
            removeNode(n);
        }
    }
}
