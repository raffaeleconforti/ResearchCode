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
import com.raffaeleconforti.log.util.LogModifier;
import com.raffaeleconforti.log.util.LogOptimizer;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
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

    public static void main(String[] args) {

        XLog log = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/SharedFolder/Logs/BPI2012.xes.gz");
        LogModifier logModifier = new LogModifier(new XFactoryNaiveImpl(), XConceptExtension.instance(), XTimeExtension.instance(), new LogOptimizer());
        log = logModifier.insertArtificialStartAndEndEvent(log);

        XEventClassifier classifier = new XEventNameClassifier();
        AutomatonFactory automatonFactory = new AutomatonFactory(classifier);

        boolean removed = true;
        while (removed) {
            removed = false;
            Automaton<String> automaton = automatonFactory.generate(log);
            WrapperLabelFilter wrapperLabelFilter = new WrapperLabelFilter(automaton, log);
            Set<Node<String>> nodes = wrapperLabelFilter.identifyRemovableNodes(new Gurobi_Solver());

            for (XTrace trace : log) {
                Iterator<XEvent> iterator = trace.iterator();
                while (iterator.hasNext()) {
                    XEvent event = iterator.next();
                    String name = classifier.getClassIdentity(event);
                    for (Node<String> node : nodes) {
                        if (node.getData().equals(name)) {
                            iterator.remove();
                            removed = true;
                            break;
                        }
                    }
                }
            }
        }

        log = logModifier.removeArtificialStartAndEndEvent(log);

        LogImporter.exportToFile("/Volumes/Data/SharedFolder/Logs/BPI2012 (SimpleFiltered)2.xes.gz", log);
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
//            for(int event_pos = 1; event_pos < trace.size(); event_pos++) {
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

                Double val;
                if((val = benefits.get(node_current)) == null) {
                    val = 0.0;
                    benefits.put(node_current, val);
                }

                double found = 0;
                for(Edge<T> edge : automaton.getEdges()) {
                    if(edge.getSource().equals(node_previous) && edge.getTarget().equals(node_current)) {
                        found += automaton.getEdgeFrequency(edge);
                    }
                    if(edge.getSource().equals(node_current) && edge.getTarget().equals(node_next)) {
                        found = automaton.getEdgeFrequency(edge);
                    }
                }
                if(found > 0) {
                    val = benefits.get(node_current);
                    val += found;
                    benefits.put(node_current, val);
                }

//                for(Edge<T> edge : automaton.getEdges()) {
//                    if(edge.getSource().equals(node_previous) && edge.getTarget().equals(node_current)) {
//                        val = benefits.get(node_current);
//                        val++;
//                        benefits.put(node_current, val);
//                    }
//                    if(edge.getSource().equals(node_current) && edge.getTarget().equals(node_next)) {
//                        val = benefits.get(node_current);
//                        val++;
//                        benefits.put(node_current, val);
//                    }
//                }
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

                Double val;
                if((val = drawbacks.get(node_current)) == null) {
                    val = 0.0;
                    drawbacks.put(node_current, val);
                }

                double found = 0.0;
                for(Edge<T> edge : automaton.getEdges()) {
                    if(edge.getSource().equals(node_previous) && edge.getTarget().equals(node_next)) {
                        found = automaton.getEdgeFrequency(edge);
                        break;
                    }
                }
                if(found > 0) {
                    val = drawbacks.get(node_current);
                    val += found;
                    drawbacks.put(node_current, val);
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
            if(benefits.containsKey(nodeList.get(i))) {
                obj.addTerm(nodes[i], benefits.get(nodeList.get(i)));
                obj.addTerm(negNodes[i], drawbacks.get(nodeList.get(i)));
            }
        }
        solver.setObjectiveFunction(obj);
        solver.setMaximize();

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

        for(int i = 0; i < nodeList.size(); i++) {
            int input = 0;
            int output = 0;
            for(int k = 0; k < edgeList.size(); k++) {
                if(edgeList.get(k).getSource().equals(nodeList.get(i))) output++;
                if(edgeList.get(k).getTarget().equals(nodeList.get(i))) input++;
            }

            if(input == 1 && output == 1 && nodeList.get(i).getFrequency() > 1) {
                ILPSolverExpression expr = solver.createExpression();
                expr.addTerm(nodes[i], 1.0);
                solver.addConstraint(expr, ILPSolver.Operator.EQUAL, 1.0, "");
            }
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
