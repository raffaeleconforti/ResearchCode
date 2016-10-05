package com.raffaeleconforti.noisefiltering.event.optimization.gurobi;

import com.raffaeleconforti.automaton.Automaton;
import com.raffaeleconforti.automaton.Edge;
import com.raffaeleconforti.automaton.Node;
import com.raffaeleconforti.noisefiltering.event.optimization.InfrequentBehaviourSolver;
import gurobi.*;

import java.util.ArrayList;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import java.util.List;
import java.util.Set;

/**
 * Created by conforti on 2/04/15.
 */
public class GurobiInfrequentBehaviourSolver<T> implements InfrequentBehaviourSolver<T> {

    private final Automaton<T> automaton;
    private final Set<Edge<T>> infrequentEdges;
    private final Set<Node<T>> requiredStatus;

    public GurobiInfrequentBehaviourSolver(Automaton<T> automaton, Set<Edge<T>> infrequentEdges, Set<Node<T>> requiredStates) {
        this.automaton = automaton;
        this.infrequentEdges = infrequentEdges;
        this.requiredStatus = requiredStates;
    }

    public Set<Edge<T>> identifyRemovableEdges() {
        Set<Edge<T>> removable = new UnifiedSet<Edge<T>>();
        List<Edge<T>> edgeList = new ArrayList<Edge<T>>(automaton.getEdges());
        List<Node<T>> nodeList = new ArrayList<Node<T>>(automaton.getNodes());

        try {
            GRBEnv env = new GRBEnv("qp.noisefiltering");

            GRBModel model = new GRBModel(env);
            model.getEnv().set(GRB.IntParam.LogToConsole, 0);

            // Create variables
            GRBVar[] edges = new GRBVar[edgeList.size()];
            for(int i = 0; i < edges.length; i++) {
                edges[i] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, edgeList.get(i).toString());
            }

            GRBVar[] connectedSourceList = new GRBVar[nodeList.size()];
            for(int i = 0; i < connectedSourceList.length; i++) {
                connectedSourceList[i] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, nodeList.get(i).toString());
            }

            GRBVar[][] subconnectedSourceList = new GRBVar[nodeList.size()][nodeList.size()];
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
                        subconnectedSourceList[i][j] = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.INTEGER, nodeList.get(i).toString()+nodeList.get(j).toString());
                    }
                }
            }

            GRBVar[] connectedTargetList = new GRBVar[nodeList.size()];
            for(int i = 0; i < connectedTargetList.length; i++) {
                connectedTargetList[i] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, nodeList.get(i).toString());
            }

            GRBVar[][] subconnectedTargetList = new GRBVar[nodeList.size()][nodeList.size()];
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
                        subconnectedTargetList[i][j] = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.INTEGER, nodeList.get(i).toString()+nodeList.get(j).toString());
                    }
                }
            }

            // Integrate new variables
            model.update();

            // Set objective: summation of all edges
            GRBLinExpr obj = new GRBLinExpr();
            for(int i = 0; i < edges.length; i++) {
                obj.addTerm(1.0, edges[i]);
            }
            model.setObjective(obj);

            // Add constraint: set mandatory edges
            for(int i = 0; i < edgeList.size(); i++) {
                if(!infrequentEdges.contains(edgeList.get(i))) {
                    GRBLinExpr expr = new GRBLinExpr();
                    expr.addTerm(1.0, edges[i]);
                    model.addConstr(expr, GRB.EQUAL, 1.0, "edge"+i);
                }
            }

            Set<Integer> sources = new UnifiedSet<Integer>();
            // Add constraint: source is connected to source
            for(int i = 0; i < nodeList.size(); i++) {
                if(automaton.getAutomatonStart().contains(nodeList.get(i))) {
                    GRBLinExpr expr = new GRBLinExpr();
                    expr.addTerm(1.0, connectedSourceList[i]);
                    model.addConstr(expr, GRB.EQUAL, 1.0, "Start" + i);
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
                                    GRBLinExpr linExpr = new GRBLinExpr();
                                    linExpr.addTerm(-1.0, connectedSourceList[j]);
                                    linExpr.addTerm(-1.0, edges[k]);
                                    linExpr.addConstant(-2.0);
                                    linExpr.addTerm(2.0, subconnectedSourceList[i][j]);
                                    model.addConstr(linExpr, GRB.LESS_EQUAL, -2.0, "");

                                    linExpr = new GRBLinExpr();
                                    linExpr.addTerm(1.0, connectedSourceList[j]);
                                    linExpr.addTerm(1.0, edges[k]);
                                    linExpr.addTerm(-2, subconnectedSourceList[i][j]);
                                    model.addConstr(linExpr, GRB.LESS_EQUAL, 1.0, "");
                                }
                            }
                        }
                    }
                }
            }

            // Add constraint: node is connected from source 2
            for(int i = 0; i < nodeList.size(); i++) {
                if(!sources.contains(i)) {
                    GRBLinExpr linExpr = new GRBLinExpr();
                    for (int j = 0; j < nodeList.size(); j++) {
                        if (subconnectedSourceList[i][j] != null) {
                            linExpr.addTerm(-1.0, subconnectedSourceList[i][j]);
                        }
                    }
                    linExpr.addConstant(-1.0);
                    linExpr.addTerm(1.0, connectedSourceList[i]);
                    model.addConstr(linExpr, GRB.LESS_EQUAL, -1.0, "");

                    linExpr = new GRBLinExpr();
                    for (int j = 0; j < nodeList.size(); j++) {
                        if (subconnectedSourceList[i][j] != null) {
                            linExpr.addTerm(1.0, subconnectedSourceList[i][j]);
                        }
                    }
                    linExpr.addTerm(-GRB.INFINITY, connectedSourceList[i]);
                    model.addConstr(linExpr, GRB.LESS_EQUAL, 0.0, "");
                }
            }

            // Add constraint: node is connected from source 3
            for(int i = 0; i < nodeList.size(); i++) {
                if(requiredStatus.contains(nodeList.get(i))) {
                    GRBLinExpr linExpr = new GRBLinExpr();
                    linExpr.addTerm(1.0, connectedSourceList[i]);
                    model.addConstr(linExpr, GRB.EQUAL, 1.0, "");
                }
            }

            Set<Integer> sinks = new UnifiedSet<Integer>();
            // Add constraint: source is connected to target
            for(int i = 0; i < nodeList.size(); i++) {
                if(automaton.getAutomatonEnd().contains(nodeList.get(i))) {
                    GRBLinExpr expr = new GRBLinExpr();
                    expr.addTerm(1.0, connectedTargetList[i]);
                    model.addConstr(expr, GRB.EQUAL, 1.0, "End" + i);
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
                                    GRBLinExpr linExpr = new GRBLinExpr();
                                    linExpr.addTerm(-1.0, connectedTargetList[j]);
                                    linExpr.addTerm(-1.0, edges[k]);
                                    linExpr.addConstant(-2.0);
                                    linExpr.addTerm(2.0, subconnectedTargetList[i][j]);
                                    model.addConstr(linExpr, GRB.LESS_EQUAL, -2.0, "");

                                    linExpr = new GRBLinExpr();
                                    linExpr.addTerm(1.0, connectedTargetList[j]);
                                    linExpr.addTerm(1.0, edges[k]);
                                    linExpr.addTerm(-2, subconnectedTargetList[i][j]);
                                    model.addConstr(linExpr, GRB.LESS_EQUAL, 1.0, "");
                                }
                            }
                        }
                    }
                }
            }

            // Add constraint: node is connected to target 2
            for(int i = 0; i < nodeList.size(); i++) {
                if(!sinks.contains(i)) {
                    GRBLinExpr linExpr = new GRBLinExpr();
                    for (int j = 0; j < nodeList.size(); j++) {
                        if (subconnectedTargetList[i][j] != null) {
                            linExpr.addTerm(-1.0, subconnectedTargetList[i][j]);
                        }
                    }
                    linExpr.addConstant(-1.0);
                    linExpr.addTerm(1.0, connectedTargetList[i]);
                    model.addConstr(linExpr, GRB.LESS_EQUAL, -1.0, "");

                    linExpr = new GRBLinExpr();
                    for (int j = 0; j < nodeList.size(); j++) {
                        if (subconnectedTargetList[i][j] != null) {
                            linExpr.addTerm(1.0, subconnectedTargetList[i][j]);
                        }
                    }
                    linExpr.addTerm(-GRB.INFINITY, connectedTargetList[i]);
                    model.addConstr(linExpr, GRB.LESS_EQUAL, 0.0, "");
                }
            }

            // Add constraint: node is connected to target 3
            for(int i = 0; i < nodeList.size(); i++) {
                if(requiredStatus.contains(nodeList.get(i))) {
                    GRBLinExpr linExpr = new GRBLinExpr();
                    linExpr.addTerm(1.0, connectedTargetList[i]);
                    model.addConstr(linExpr, GRB.EQUAL, 1.0, "");
                }
            }

            // Optimize model
            model.optimize();
            int status = model.get(GRB.IntAttr.Status);
            if (status == GRB.Status.UNBOUNDED) {
                System.out.println("The model cannot be solved "
                        + "because it is unbounded");
            }
            if (status == GRB.Status.OPTIMAL) {
                System.out.println("The optimal objective is " +
                        model.get(GRB.DoubleAttr.ObjVal));
            }
            if (status == GRB.Status.INFEASIBLE) {
                System.out.println("The model is infeasible");
            }

            // Identify Removable Arcs
            for(int i = 0; i < edges.length; i++) {
                GRBVar edge = edges[i];
                if(edge.get(GRB.DoubleAttr.X) == 0) {
                    removable.add(edgeList.get(i));
                }
            }

            // Dispose of model and environment

            model.dispose();
            env.dispose();

            } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " +
                    e.getMessage());
        }

        return removable;
    }
}
