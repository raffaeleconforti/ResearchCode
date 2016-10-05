package com.raffaeleconforti.noisefiltering.event.optimization.lpsolve;

import com.raffaeleconforti.automaton.Automaton;
import com.raffaeleconforti.automaton.Edge;
import com.raffaeleconforti.automaton.Node;
import com.raffaeleconforti.foreignkeydiscovery.Couple;
import com.raffaeleconforti.noisefiltering.event.optimization.InfrequentBehaviourSolver;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;

import java.util.*;

/**
 * Created by conforti on 2/04/15.
 */
public class LPSolveInfrequentBehaviourSolver<T> implements InfrequentBehaviourSolver<T> {

    final Automaton<T> automaton;
    final Set<Edge<T>> infrequentEdges;
    final Set<Node<T>> requiredStatus;
    final Map<Object, Integer> mapVar;

    public LPSolveInfrequentBehaviourSolver(Automaton<T> automaton, Set<Edge<T>> infrequentEdges, Set<Node<T>> requiredStates) {
        this.automaton = automaton;
        this.infrequentEdges = infrequentEdges;
        this.requiredStatus = requiredStates;
        mapVar = new UnifiedMap<>();

        try {
            System.loadLibrary("lpsolve55");
            System.loadLibrary("lpsolve55j");
        } catch (Exception e) {
            System.err.println("Unable to load required libraries for ILP solver.");
            System.err.println("Exception thrown: "+e);
            System.err.println("Please obtain a copy of 'lpsolve' and make the libraries available");
            System.err.println("to InfrequentBehaviourFilter on the java library path:");
            System.err.println("  java <params> -Djava.library.path=path/to/lpsolve/libs");
        }
    }

    public Set<Edge<T>> identifyRemovableEdges() {
        Set<Edge<T>> removable = new UnifiedSet<Edge<T>>();
        List<Edge<T>> edgeList = new ArrayList<Edge<T>>(automaton.getEdges());
        List<Node<T>> nodeList = new ArrayList<Node<T>>(automaton.getNodes());

        try {
            LpSolve lp; //INCOMPLETE

            int numberOfVariables = countVariables(edgeList, nodeList);

            /* create space large enough for one row */
            int[] colno = new int[numberOfVariables];
            double[] row = new double[numberOfVariables];

            lp = LpSolve.makeLp(0, numberOfVariables);

            int count = 0;
            int offset = 1;
            // Create variables
            for(int i = 0; i < edgeList.size(); i++) {
                lp.setColName(count + offset, "Edge" + count);
                lp.setBinary(count + offset, true);
                setEdgeValue(i, count);
                colno[count] = count + offset;
                count++;
            }

            for(int i = 0; i < nodeList.size(); i++) {
                lp.setColName(count + offset, "NodeFromSource" + count);
                lp.setBinary(count + offset, true);
                setSourceToNodeValue(i, count);
                colno[count] = count + offset;
                count++;
            }

            for(int i = 0; i < nodeList.size(); i++) {
                Set<Integer> connectedFrom = new UnifiedSet<Integer>();
                for(int j = 0; j < nodeList.size(); j++) {
                    if(i != j) {
                        for (Edge<T> edge : edgeList) {
                            if (edge.getTarget().equals(nodeList.get(i)) && edge.getSource().equals(nodeList.get(j))) {
                                connectedFrom.add(j);
                            }
                        }
                    }
                }

                for(int j = 0; j < nodeList.size(); j++) {
                    if(connectedFrom.contains(j)) {
                        lp.setColName(count + offset, "Node" + i + "Node" + j);
                        lp.setInt(count + offset, true);
                        setFromSourceMapVar(i, j, count);
                        colno[count] = count + offset;
                        count++;
                    }
                }
            }

            for(int i = 0; i < nodeList.size(); i++) {
                lp.setColName(count + offset, "NodeToTarget" + count);
                lp.setBinary(count + offset, true);
                setNodeToTargetValue(i, count);
                colno[count] = count + offset;
                count++;
            }

            for(int i = 0; i < nodeList.size(); i++) {
                Set<Integer> connectedTo = new UnifiedSet<Integer>();
                for(int j = 0; j < nodeList.size(); j++) {
                    if(i != j) {
                        for (Edge<T> edge : edgeList) {
                            if (edge.getSource().equals(nodeList.get(i)) && edge.getTarget().equals(nodeList.get(j))) {
                                connectedTo.add(j);
                            }
                        }
                    }
                }

                for(int j = 0; j < nodeList.size(); j++) {
                    if(connectedTo.contains(j)) {
                        lp.setColName(count + offset, "Node" + i + "Node" + j);
                        lp.setInt(count + offset, true);
                        setToTargetMapVar(i, j, count);
                        colno[count] = count + offset;
                        count++;
                    }
                }
            }

            // Set objective: summation of all edges
            for(int i = 0; i < edgeList.size(); i++) {
                row[getEdgeValue(i)] = 1.0;
            }
            lp.setObjFnex(numberOfVariables, row, colno);
            lp.setMinim();

            lp.setAddRowmode(true);
            // Add constraint: set mandatory edges
            for(int i = 0; i < edgeList.size(); i++) {
                if(!infrequentEdges.contains(edgeList.get(i))) {
                    row = new double[numberOfVariables];
                    row[getEdgeValue(i)] = 1.0;
                    lp.addConstraintex(numberOfVariables, row, colno, LpSolve.EQ, 1);
                }
            }

            Set<Integer> sources = new UnifiedSet<Integer>();
            // Add constraint: source is connected to source
            for(int i = 0; i < nodeList.size(); i++) {
                if(automaton.getAutomatonStart().contains(nodeList.get(i))) {
                    row = new double[numberOfVariables];
                    row[getSourceToNodeValue(i)] = 1.0;
                    lp.addConstraintex(numberOfVariables, row, colno, LpSolve.EQ, 1);
                    sources.add(i);
                }
            }

            // Add constraint: node is connected from source 1
            for(int i = 0; i < nodeList.size(); i++) {
                if(!sources.contains(i)) {
                    for (int j = 0; j < nodeList.size(); j++) {
                        if (j != i) {
                            for (int k = 0; k < edgeList.size(); k++) {
                                if (edgeList.get(k).getSource().equals(nodeList.get(j)) && edgeList.get(k).getTarget().equals(nodeList.get(i))) {
                                    row = new double[numberOfVariables];
                                    row[getSourceToNodeValue(j)] = -1;
                                    row[getEdgeValue(k)] = -1;
                                    row[getFromSourceMapVar(i, j)] = 2;
                                    lp.addConstraintex(numberOfVariables, row, colno, LpSolve.LE, 0);


                                    row = new double[numberOfVariables];
                                    row[getSourceToNodeValue(j)] = 1;
                                    row[getEdgeValue(k)] = 1;
                                    row[getFromSourceMapVar(i, j)] = -2;
                                    lp.addConstraintex(numberOfVariables, row, colno, LpSolve.LE, 1);
                                }
                            }
                        }
                    }
                }
            }

            // Add constraint: node is connected from source 2
            for(int i = 0; i < nodeList.size(); i++) {
                if(!sources.contains(i)) {
                    row = new double[numberOfVariables];
                    for (int j = 0; j < nodeList.size(); j++) {
                        if (getFromSourceMapVar(i, j) != null) {
                            row[getFromSourceMapVar(i, j)] = -1;
                        }
                    }
                    row[getSourceToNodeValue(i)] = 1;
                    lp.addConstraintex(numberOfVariables, row, colno, LpSolve.LE, 0);


                    row = new double[numberOfVariables];
                    for (int j = 0; j < nodeList.size(); j++) {
                        if (getFromSourceMapVar(i, j) != null) {
                            row[getFromSourceMapVar(i, j)] = 1;
                        }
                    }
                    row[getSourceToNodeValue(i)] = -Double.MAX_VALUE;
                    lp.addConstraintex(numberOfVariables, row, colno, LpSolve.LE, 0);
                }
            }

            // Add constraint: node is connected from source 3
            for(int i = 0; i < nodeList.size(); i++) {
                if(requiredStatus.contains(nodeList.get(i))) {
                    row = new double[numberOfVariables];
                    row[getSourceToNodeValue(i)] = 1;
                    lp.addConstraintex(numberOfVariables, row, colno, LpSolve.EQ, 1);
                }
            }

            Set<Integer> sinks = new UnifiedSet<Integer>();
            // Add constraint: source is connected to target
            for(int i = 0; i < nodeList.size(); i++) {
                if(automaton.getAutomatonEnd().contains(nodeList.get(i))) {
                    row = new double[numberOfVariables];
                    row[getNodeToTargetValue(i)] = 1;
                    lp.addConstraintex(numberOfVariables, row, colno, LpSolve.EQ, 1);
                    sinks.add(i);
                }
            }

            // Add constraint: node is connected to target 1
            for(int i = 0; i < nodeList.size(); i++) {
                if(!sinks.contains(i)) {
                    for (int j = 0; j < nodeList.size(); j++) {
                        if (j != i) {
                            for (int k = 0; k < edgeList.size(); k++) {
                                if (edgeList.get(k).getTarget().equals(nodeList.get(j)) && edgeList.get(k).getSource().equals(nodeList.get(i))) {
                                    row = new double[numberOfVariables];
                                    row[getNodeToTargetValue(j)] = -1;
                                    row[getEdgeValue(k)] = -1;
                                    row[getToTargetMapVar(i, j)] = 2;
                                    lp.addConstraintex(numberOfVariables, row, colno, LpSolve.LE, 0);

                                    row = new double[numberOfVariables];
                                    row[getNodeToTargetValue(j)] = 1;
                                    row[getEdgeValue(k)] = 1;
                                    row[getToTargetMapVar(i, j)] = -2;
                                    lp.addConstraintex(numberOfVariables, row, colno, LpSolve.LE, 1);
                                }
                            }
                        }
                    }
                }
            }

            // Add constraint: node is connected to target 2
            for(int i = 0; i < nodeList.size(); i++) {
                if(!sinks.contains(i)) {
                    row = new double[numberOfVariables];
                    for (int j = 0; j < nodeList.size(); j++) {
                        if (getToTargetMapVar(i, j) != null) {
                            row[getToTargetMapVar(i, j)] = -1;
                        }
                    }
                    row[getNodeToTargetValue(i)] = 1;
                    lp.addConstraintex(numberOfVariables, row, colno, LpSolve.LE, 0);


                    row = new double[numberOfVariables];
                    for (int j = 0; j < nodeList.size(); j++) {
                        if (getToTargetMapVar(i, j) != null) {
                            row[getToTargetMapVar(i, j)] = 1;
                        }
                    }
                    row[getNodeToTargetValue(i)] = -Double.MAX_VALUE;
                    lp.addConstraintex(numberOfVariables, row, colno, LpSolve.LE, 0);
                }
            }

            // Add constraint: node is connected to target 3
            for(int i = 0; i < nodeList.size(); i++) {
                if(requiredStatus.contains(nodeList.get(i))) {
                    row = new double[numberOfVariables];
                    row[getNodeToTargetValue(i)] = 1;
                    lp.addConstraintex(numberOfVariables, row, colno, LpSolve.EQ, 1);
                }
            }

            // Optimize model
            lp.setAddRowmode(false);
            lp.setVerbose(LpSolve.IMPORTANT);

            int status = lp.solve();
            if (status == LpSolve.UNBOUNDED) {
                System.out.println("The model cannot be solved "
                        + "because it is unbounded");
            }
            if (status == LpSolve.OPTIMAL) {
                System.out.println("The optimal objective is " +
                        lp.getObjective());
            }
            if (status == LpSolve.INFEASIBLE) {
                System.out.println("The model is infeasible");
            }
            if (status != LpSolve.UNBOUNDED && status != LpSolve.OPTIMAL && status != LpSolve.INFEASIBLE) {
                System.out.println("Other status " + status);
            }

            row = new double[numberOfVariables];
            lp.getVariables(row);
            // Identify Removable Arcs
            for(int i = 0; i < edgeList.size(); i++) {
                if(row[getEdgeValue(i)] == 0) {
                    removable.add(edgeList.get(i));
                }
            }

            // Dispose of model and environment
            lp.deleteLp();

            } catch (LpSolveException e) {
            System.out.println("Error code: " + e.getMessage());
        }

        return removable;
    }

    private int countVariables(List<Edge<T>> edgeList, List<Node<T>> nodeList) {
        int count = edgeList.size() + (2 * nodeList.size());

        Set<Integer> connectedFrom;
        for(int i = 0; i < nodeList.size(); i++) {
            connectedFrom = new UnifiedSet<Integer>();
            for(int j = 0; j < nodeList.size(); j++) {
                if(i != j) {
                    for (Edge<T> edge : edgeList) {
                        if (edge.getTarget().equals(nodeList.get(i)) && edge.getSource().equals(nodeList.get(j))) {
                            connectedFrom.add(j);
                        }
                    }
                }
            }

            for(int j = 0; j < nodeList.size(); j++) {
                if(connectedFrom.contains(j)) {
                    count++;
                }
            }
        }

        Set<Integer> connectedTo;
        for(int i = 0; i < nodeList.size(); i++) {
            connectedTo = new UnifiedSet<Integer>();
            for(int j = 0; j < nodeList.size(); j++) {
                if(i != j) {
                    for (Edge<T> edge : edgeList) {
                        if (edge.getSource().equals(nodeList.get(i)) && edge.getTarget().equals(nodeList.get(j))) {
                            connectedTo.add(j);
                        }
                    }
                }
            }

            for(int j = 0; j < nodeList.size(); j++) {
                if(connectedTo.contains(j)) {
                    count++;
                }
            }
        }

        return count;
    }

    private void setEdgeValue(Integer value, Integer key) {
        mapVar.put(value, key);
    }

    private void setSourceToNodeValue(Integer value, Integer key) {
        mapVar.put(value + automaton.getEdges().size(), key);
    }

    private void setNodeToTargetValue(Integer value, Integer key) {
        mapVar.put(value + automaton.getEdges().size() + automaton.getNodes().size(), key);
    }

    private void setFromSourceMapVar(Integer value1, Integer value2, Integer key) {
        mapVar.put(new Couple<Integer, Integer>(value1 + automaton.getEdges().size(), value2 + automaton.getEdges().size()), key);
    }

    private void setToTargetMapVar(Integer value1, Integer value2, Integer key) {
        mapVar.put(new Couple<Integer, Integer>(value1 + automaton.getEdges().size() + automaton.getNodes().size(), value2 + automaton.getEdges().size() + automaton.getNodes().size()), key);
    }

    private Integer getEdgeValue(Integer value) {
        return mapVar.get(value);
    }

    private Integer getSourceToNodeValue(Integer value) {
        return mapVar.get(value + automaton.getEdges().size());
    }

    private Integer getNodeToTargetValue(Integer value) {
        return mapVar.get(value + automaton.getEdges().size() + automaton.getNodes().size());
    }

    private Integer getFromSourceMapVar(Integer value1, Integer value2) {
        return mapVar.get(new Couple<Integer, Integer>(value1 + automaton.getEdges().size(), value2 + automaton.getEdges().size()));
    }

    private Integer getToTargetMapVar(Integer value1, Integer value2) {
        return mapVar.get(new Couple<Integer, Integer>(value1 + automaton.getEdges().size() + automaton.getNodes().size(), value2 + automaton.getEdges().size() + automaton.getNodes().size()));
    }
}
