package com.raffaeleconforti.noisefiltering.event.optimization.wrapper;

import com.raffaeleconforti.automaton.Automaton;
import com.raffaeleconforti.automaton.Edge;
import com.raffaeleconforti.automaton.Node;
import com.raffaeleconforti.ilpsolverwrapper.ILPSolver;
import com.raffaeleconforti.ilpsolverwrapper.ILPSolverExpression;
import com.raffaeleconforti.ilpsolverwrapper.ILPSolverVariable;
import com.raffaeleconforti.ilpsolverwrapper.impl.gurobi.Gurobi_Solver;
import com.raffaeleconforti.noisefiltering.event.optimization.InfrequentBehaviourSolver;
import gurobi.*;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by conforti on 2/04/15.
 */
public class WrapperInfrequentBehaviourSolver<T> {

    private final Automaton<T> automaton;
    private final Set<Edge<T>> infrequentEdges;
    private final Set<Node<T>> requiredStatus;

    public WrapperInfrequentBehaviourSolver(Automaton<T> automaton, Set<Edge<T>> infrequentEdges, Set<Node<T>> requiredStates) {
        this.automaton = automaton;
        this.infrequentEdges = infrequentEdges;
        this.requiredStatus = requiredStates;
    }

    public Set<Edge<T>> identifyRemovableEdges(ILPSolver solver) {
        Set<Edge<T>> removable = new UnifiedSet<Edge<T>>();
        List<Edge<T>> edgeList = new ArrayList<Edge<T>>(automaton.getEdges());
        List<Node<T>> nodeList = new ArrayList<Node<T>>(automaton.getNodes());

        solver.createModel();

        // Create variables
        ILPSolverVariable[] edges = new ILPSolverVariable[edgeList.size()];
        for(int i = 0; i < edges.length; i++) {
            edges[i] = solver.addVariable(0.0, 1.0, 0.0, ILPSolver.VariableType.BINARY, edgeList.get(i).toString());
        }

        ILPSolverVariable[] connectedSourceList = new ILPSolverVariable[nodeList.size()];
        for(int i = 0; i < connectedSourceList.length; i++) {
            connectedSourceList[i] = solver.addVariable(0.0, 1.0, 0.0, ILPSolver.VariableType.BINARY, nodeList.get(i).toString());
        }

        ILPSolverVariable[][] subconnectedSourceList = new ILPSolverVariable[nodeList.size()][nodeList.size()];
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
                    subconnectedSourceList[i][j] = solver.addVariable(0.0, Double.MAX_VALUE, 0.0, ILPSolver.VariableType.INTEGER, nodeList.get(i).toString()+nodeList.get(j).toString());
                }
            }
        }

        ILPSolverVariable[] connectedTargetList = new ILPSolverVariable[nodeList.size()];
        for(int i = 0; i < connectedTargetList.length; i++) {
            connectedTargetList[i] = solver.addVariable(0.0, 1.0, 0.0, ILPSolver.VariableType.BINARY, nodeList.get(i).toString());
        }

        ILPSolverVariable[][] subconnectedTargetList = new ILPSolverVariable[nodeList.size()][nodeList.size()];
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
                    subconnectedTargetList[i][j] = solver.addVariable(0.0, Double.MAX_VALUE, 0.0, ILPSolver.VariableType.INTEGER, nodeList.get(i).toString()+nodeList.get(j).toString());
                }
            }
        }

        // Integrate new variables
        solver.integrateVariables();

        // Set objective: summation of all edges
        ILPSolverExpression obj = solver.createExpression();
        for(int i = 0; i < edges.length; i++) {
            obj.addTerm(edges[i],1.0);
        }
        solver.setObjectiveFunction(obj);

        // Add constraint: set mandatory edges
        for(int i = 0; i < edgeList.size(); i++) {
            if(!infrequentEdges.contains(edgeList.get(i))) {
                ILPSolverExpression expr = solver.createExpression();
                expr.addTerm(edges[i], 1.0);
                solver.addConstraint(expr, ILPSolver.Operator.EQUAL, 1.0, "edge"+i);
            }
        }

        Set<Integer> sources = new UnifiedSet<Integer>();
        // Add constraint: source is connected to source
        for(int i = 0; i < nodeList.size(); i++) {
            if(automaton.getAutomatonStart().contains(nodeList.get(i))) {
                ILPSolverExpression expr = solver.createExpression();
                expr.addTerm(connectedSourceList[i], 1.0);
                solver.addConstraint(expr, ILPSolver.Operator.EQUAL, 1.0, "Start" + i);
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
                                ILPSolverExpression expr1 = solver.createExpression();
                                expr1.addTerm(connectedSourceList[j], -1.0);
                                expr1.addTerm(edges[k], -1.0);
                                expr1.addTerm(subconnectedSourceList[i][j], 2.0);
                                solver.addConstraint(expr1, ILPSolver.Operator.LESS_EQUAL, 0, "");

                                ILPSolverExpression expr2 = solver.createExpression();
                                expr2.addTerm(connectedSourceList[j], 1.0);
                                expr2.addTerm(edges[k], 1.0);
                                expr2.addTerm(subconnectedSourceList[i][j], -2);
                                solver.addConstraint(expr2, ILPSolver.Operator.LESS_EQUAL, 1.0, "");
                            }
                        }
                    }
                }
            }
        }

        // Add constraint: node is connected from source 2
        for(int i = 0; i < nodeList.size(); i++) {
            if(!sources.contains(i)) {
                ILPSolverExpression expr1 = solver.createExpression();
                for (int j = 0; j < nodeList.size(); j++) {
                    if (subconnectedSourceList[i][j] != null) {
                        expr1.addTerm(subconnectedSourceList[i][j], -1.0);
                    }
                }
                expr1.addTerm(connectedSourceList[i], 1.0);
                solver.addConstraint(expr1, ILPSolver.Operator.LESS_EQUAL, 0, "");

                ILPSolverExpression expr2 = solver.createExpression();
                for (int j = 0; j < nodeList.size(); j++) {
                    if (subconnectedSourceList[i][j] != null) {
                        expr2.addTerm(subconnectedSourceList[i][j], 1.0);
                    }
                }
                expr2.addTerm(connectedSourceList[i], -solver.getInfinity());
                solver.addConstraint(expr2, ILPSolver.Operator.LESS_EQUAL, 0.0, "");
            }
        }

        // Add constraint: node is connected from source 3
        for(int i = 0; i < nodeList.size(); i++) {
            if(requiredStatus.contains(nodeList.get(i))) {
                ILPSolverExpression expr = solver.createExpression();
                expr.addTerm(connectedSourceList[i], 1.0);
                solver.addConstraint(expr, ILPSolver.Operator.EQUAL, 1.0, "");
            }
        }

        Set<Integer> sinks = new UnifiedSet<Integer>();
        // Add constraint: source is connected to target
        for(int i = 0; i < nodeList.size(); i++) {
            if(automaton.getAutomatonEnd().contains(nodeList.get(i))) {
                ILPSolverExpression expr = solver.createExpression();
                expr.addTerm(connectedTargetList[i], 1.0);
                solver.addConstraint(expr, ILPSolver.Operator.EQUAL, 1.0, "End" + i);
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
                                ILPSolverExpression expr1 = solver.createExpression();
                                expr1.addTerm(connectedTargetList[j], -1.0);
                                expr1.addTerm(edges[k], -1.0);
                                expr1.addTerm(subconnectedTargetList[i][j], 2.0);
                                solver.addConstraint(expr1, ILPSolver.Operator.LESS_EQUAL, 0, "");

                                ILPSolverExpression expr2 = solver.createExpression();
                                expr2.addTerm(connectedTargetList[j], 1.0);
                                expr2.addTerm(edges[k], 1.0);
                                expr2.addTerm(subconnectedTargetList[i][j], -2);
                                solver.addConstraint(expr2, ILPSolver.Operator.LESS_EQUAL, 1.0, "");
                            }
                        }
                    }
                }
            }
        }

        // Add constraint: node is connected to target 2
        for(int i = 0; i < nodeList.size(); i++) {
            if(!sinks.contains(i)) {
                ILPSolverExpression expr1 = solver.createExpression();
                for (int j = 0; j < nodeList.size(); j++) {
                    if (subconnectedTargetList[i][j] != null) {
                        expr1.addTerm(subconnectedTargetList[i][j], -1.0);
                    }
                }
                expr1.addTerm(connectedTargetList[i], 1.0);
                solver.addConstraint(expr1, ILPSolver.Operator.LESS_EQUAL, 0, "");

                ILPSolverExpression expr2 = solver.createExpression();
                for (int j = 0; j < nodeList.size(); j++) {
                    if (subconnectedTargetList[i][j] != null) {
                        expr2.addTerm(subconnectedTargetList[i][j], 1.0);
                    }
                }
                expr2.addTerm(connectedTargetList[i], -solver.getInfinity());
                solver.addConstraint(expr2, ILPSolver.Operator.LESS_EQUAL, 0.0, "");
            }
        }

        // Add constraint: node is connected to target 3
        for(int i = 0; i < nodeList.size(); i++) {
            if(requiredStatus.contains(nodeList.get(i))) {
                ILPSolverExpression expr = solver.createExpression();
                expr.addTerm(connectedTargetList[i], 1.0);
                solver.addConstraint(expr, ILPSolver.Operator.EQUAL, 1.0, "");
            }
        }

        // Optimize model
        solver.solve();
        ILPSolver.Status status = solver.getStatus();

        if (status == ILPSolver.Status.OPTIMAL) {
            System.out.println("The optimal objective is " +
                    solver.getSolutionValue());

            // Identify Removable Arcs
            double[] sol = solver.getSolutionVariables(edges);
            for (int i = 0; i < edges.length; i++) {
                if (sol[i] == 0) {
                    removable.add(edgeList.get(i));
                }
            }
        }else {
            if (status == ILPSolver.Status.UNBOUNDED) {
                System.out.println("The model cannot be solved "
                        + "because it is unbounded");
            }
            if (status == ILPSolver.Status.INFEASIBLE) {
                System.out.println("The model is infeasible");
            }
        }

        // Dispose of model and environment
        solver.dispose();

        return removable;
    }
}
