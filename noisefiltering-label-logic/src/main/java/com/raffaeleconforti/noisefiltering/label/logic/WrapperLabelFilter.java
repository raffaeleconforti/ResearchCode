package com.raffaeleconforti.noisefiltering.label.logic;

import com.raffaeleconforti.automaton.Automaton;
import com.raffaeleconforti.automaton.AutomatonFactory;
import com.raffaeleconforti.automaton.Edge;
import com.raffaeleconforti.automaton.Node;
import com.raffaeleconforti.ilpsolverwrapper.ILPSolver;
import com.raffaeleconforti.ilpsolverwrapper.ILPSolverExpression;
import com.raffaeleconforti.ilpsolverwrapper.ILPSolverVariable;
import com.raffaeleconforti.ilpsolverwrapper.impl.gurobi.Gurobi_Solver;
import com.raffaeleconforti.log.util.LogImporter;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.*;

/**
 * Created by conforti on 2/04/15.
 */
public class WrapperLabelFilter<T> {

    private final Automaton<T> automaton;
    private final Map<Node<T>, Double> benefits;
    private final Map<Node<T>, Double> drawbacks;
    private XEventClassifier classifier = new XEventNameClassifier();

    public static void main(String[] args) throws Exception {
        XLog log = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/SharedFolder/Logs/ArtificialLess.xes.gz");

        XEventClassifier classifier = new XEventNameClassifier();
        AutomatonFactory automatonFactory = new AutomatonFactory(classifier);
        Automaton<String> automaton = automatonFactory.generate(log);
        WrapperLabelFilter wrapperLabelFilter = new WrapperLabelFilter(automaton, log);
        Set<Node<String>> nodes = wrapperLabelFilter.identifyRemovableNodes(new Gurobi_Solver());
    }

    public WrapperLabelFilter(Automaton<T> automaton, XLog log) {
        this.automaton = automaton;
        this.benefits = discoverBenefits(automaton, log);
        this.drawbacks = discoverDrawbacks(automaton, log);
    }

    private Map<Node<T>, Double> discoverBenefits(Automaton<T> automaton, XLog log) {
        Map<Node<T>, Double> benefits = new HashMap<>();

        for(XTrace trace : log) {
            for(int event_pos = 1; event_pos < trace.size() - 1; event_pos++) {
                String name_previous = classifier.getClassIdentity(trace.get(event_pos - 1));
                String name_current = classifier.getClassIdentity(trace.get(event_pos));
                String name_next = classifier.getClassIdentity(trace.get(event_pos + 1));

                Node<T> node_previous = null;
                Node<T> node_current = null;
                Node<T> node_next = null;
                for(Node<T> node : automaton.getNodes()) {
                    if(node.getData().equals(name_previous)) {
                        node_previous = node;
                    }
                    if(node.getData().equals(name_current)) {
                        node_current = node;
                    }
                    if(node.getData().equals(name_next)) {
                        node_next = node;
                    }
                }

                for(Edge<T> edge : automaton.getEdges()) {
                    Double val;
                    if(edge.getSource().equals(node_previous) && edge.getTarget().equals(node_current)) {
                        if((val = benefits.get(node_current)) == null) {
                            val = 0.0;
                        }
                        val++;
                        benefits.put(node_current, val);
                    }
                    if(edge.getSource().equals(node_current) && edge.getTarget().equals(node_next)) {
                        if((val = benefits.get(node_current)) == null) {
                            val = 0.0;
                        }
                        val++;
                        benefits.put(node_current, val);
                    }
                }
            }
        }

        return benefits;
    }

    private Map<Node<T>, Double> discoverDrawbacks(Automaton<T> automaton, XLog log) {
        Map<Node<T>, Double> drawbacks = new HashMap<>();

        for(XTrace trace : log) {
            for(int event_pos = 1; event_pos < trace.size() - 1; event_pos++) {
                String name_previous = classifier.getClassIdentity(trace.get(event_pos - 1));
                String name_current = classifier.getClassIdentity(trace.get(event_pos));
                String name_next = classifier.getClassIdentity(trace.get(event_pos + 1));

                Node<T> node_previous = null;
                Node<T> node_current = null;
                Node<T> node_next = null;
                for(Node<T> node : automaton.getNodes()) {
                    if(node.getData().equals(name_previous)) {
                        node_previous = node;
                    }
                    if(node.getData().equals(name_current)) {
                        node_current = node;
                    }
                    if(node.getData().equals(name_next)) {
                        node_next = node;
                    }
                }

                for(Edge<T> edge : automaton.getEdges()) {
                    Double val;
                    if(edge.getSource().equals(node_previous) && edge.getTarget().equals(node_next)) {
                        if((val = drawbacks.get(node_current)) == null) {
                            val = 0.0;
                        }
                        val++;
                        drawbacks.put(node_current, val);
                    }
                }
            }
        }

        return drawbacks;
    }

    public WrapperLabelFilter(Automaton<T> automaton, Map<Node<T>, Double> benefits, Map<Node<T>, Double> drawbacks) {
        this.automaton = automaton;
        this.benefits = benefits;
        this.drawbacks = drawbacks;
    }

    public Set<Node<T>> identifyRemovableNodes(ILPSolver solver) {
        Set<Node<T>> removable = new UnifiedSet<Node<T>>();
        List<Edge<T>> edgeList = new ArrayList<Edge<T>>(automaton.getEdges());
        List<Node<T>> nodeList = new ArrayList<Node<T>>(automaton.getNodes());

        solver.createModel();

        // Create variables
        ILPSolverVariable[] nodes = new ILPSolverVariable[nodeList.size()];
        ILPSolverVariable[] negNodes = new ILPSolverVariable[nodeList.size()];
        for(int i = 0; i < nodes.length; i++) {
            nodes[i] = solver.addVariable(0.0, 1.0, 1.0, ILPSolver.VariableType.BINARY, nodeList.get(i).toString().replaceAll("-","_").replaceAll(" ",""));
            negNodes[i] = solver.addVariable(0.0, 1.0, 1.0, ILPSolver.VariableType.BINARY, "NEG_" + nodeList.get(i).toString().replaceAll("-","_").replaceAll(" ",""));
        }

        // Integrate new variables
        solver.integrateVariables();

        // Set objective: summation of all edges (Equation 1 Paper)
        ILPSolverExpression obj = solver.createExpression();
        for(int i = 0; i < nodes.length; i++) {
            obj.addTerm(nodes[i], benefits.get(nodeList.get(i)));
            obj.addTerm(negNodes[i], drawbacks.get(nodeList.get(i)));
        }
        solver.setObjectiveFunction(obj);

        for(int k = 0; k < edgeList.size(); k++) {
            for(int i = 0; i < nodeList.size(); i++) {
                if(edgeList.get(k).getSource().equals(nodeList.get(i))) {
                    for (int j = 0; j < nodeList.size(); j++) {
                        if (edgeList.get(k).getTarget().equals(nodeList.get(j))) {
                            ILPSolverExpression expr = solver.createExpression();
                            expr.addTerm(nodes[i], 1.0);
                            expr.addTerm(nodes[j], 1.0);
                            solver.addConstraint(expr, ILPSolver.Operator.GREATER_EQUAL, 1.0, "");
                            break;
                        }
                    }
                    break;
                }
            }
        }

        for(int i = 0; i < nodeList.size(); i++) {
            ILPSolverExpression expr = solver.createExpression();
            expr.addTerm(nodes[i], 1.0);
            expr.addTerm(negNodes[i], 1.0);
            solver.addConstraint(expr, ILPSolver.Operator.EQUAL, 1.0, "");
        }

        // Optimize model
        solver.solve();
//        System.out.println(solver.printProblem());
        ILPSolver.Status status = solver.getStatus();

        if (status == ILPSolver.Status.OPTIMAL) {
            System.out.println("The optimal objective is " +
                    solver.getSolutionValue());

            // Identify Removable Arcs
            double[] sol = solver.getSolutionVariables(nodes);
            for (int i = 0; i < nodes.length; i++) {
                if (sol[i] == 0) {
                    removable.add(nodeList.get(i));
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
