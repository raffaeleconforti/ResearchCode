package com.raffaeleconforti.noisefiltering.timestamp.permutation.permutators;

import com.raffaeleconforti.ilpsolverwrapper.ILPSolver;
import com.raffaeleconforti.ilpsolverwrapper.ILPSolverExpression;
import com.raffaeleconforti.ilpsolverwrapper.ILPSolverVariable;
import com.raffaeleconforti.ilpsolverwrapper.impl.lpsolve.LPSolve_Solver;
import com.raffaeleconforti.kernelestimation.distribution.EventDistributionCalculator;
import com.raffaeleconforti.kernelestimation.distribution.impl.EventDistributionCalculatorImpl;
import com.raffaeleconforti.noisefiltering.timestamp.permutation.PermutationTechnique;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.*;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 14/07/2016.
 */
public class ILPApproach implements PermutationTechnique {

    private boolean debug_mode = false;
    private EventDistributionCalculator eventDistributionCalculator;
    private XEvent[] eventsArray;
    private double[][] likeloods;
    private ILPSolver solver;

    private int numberOfArcs = 0;
    private int numberOfArcswithZero = 0;

    public static void main(String[] args) {
        XFactory factory = new XFactoryNaiveImpl();
        XConceptExtension xce = XConceptExtension.instance();
        XTimeExtension xte = XTimeExtension.instance();

        XEvent a = factory.createEvent();
        xce.assignName(a, "a");

        XEvent b = factory.createEvent();
        xce.assignName(b, "b");

        XEvent c = factory.createEvent();
        xce.assignName(c, "c");

        XEvent d = factory.createEvent();
        xce.assignName(d, "d");

        XTrace trace1 = factory.createTrace();
        trace1.add(a);
        trace1.add(b);
        trace1.add(c);
        trace1.add(d);

        XTrace trace2 = factory.createTrace();
        trace2.add(a);
        trace2.add(c);
        trace2.add(b);
        trace2.add(d);

        XEvent a1 = factory.createEvent();
        xce.assignName(a1, "a");
        xte.assignTimestamp(a1, 1000000);

        XEvent b1 = factory.createEvent();
        xce.assignName(b1, "b");
        xte.assignTimestamp(b1, 2000000);

        XEvent c1 = factory.createEvent();
        xce.assignName(c1, "c");
        xte.assignTimestamp(c1, 3000000);

        XEvent d1 = factory.createEvent();
        xce.assignName(d1, "d");
        xte.assignTimestamp(d1, 4000000);

        XTrace trace3 = factory.createTrace();
        trace3.add(a1);
        trace3.add(b1);
        trace3.add(c1);
        trace3.add(d1);

        XLog log = factory.createLog();
        log.add(trace1);
        log.add(trace2);
        log.add(trace3);

        EventDistributionCalculator eventDistributionCalculator = new EventDistributionCalculatorImpl(log, new XEventNameClassifier(), false);
        eventDistributionCalculator.analyseLog();

        Set<XEvent> events = new UnifiedSet<>();
        events.add(b1);
        events.add(c1);

        ILPApproach ilpApproach = new ILPApproach(events, eventDistributionCalculator, a1, d1, new LPSolve_Solver(), true);
        System.out.println(ilpApproach.findBestStartEnd());
    }

    public ILPApproach(Set<XEvent> events, EventDistributionCalculator eventDistributionCalculator, XEvent start, XEvent end, ILPSolver solver, boolean debug_mode) {
        this.debug_mode = debug_mode;
        this.solver = solver;
        this.eventDistributionCalculator = eventDistributionCalculator;
        this.eventsArray = events.toArray(new XEvent[events.size() + 2]);
        Arrays.sort(eventsArray, new Comparator<XEvent>() {
            XEventClassifier xEventClassifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
            @Override
            public int compare(XEvent o1, XEvent o2) {
                if(o1 != null && o2 != null) return xEventClassifier.getClassIdentity(o1).compareTo(xEventClassifier.getClassIdentity(o2));
                else if(o1 == null) return 1;
                else return -1;
            }
        });
        this.eventsArray[eventsArray.length - 2] = start;
        this.eventsArray[eventsArray.length - 1] = end;
        this.likeloods = new double[events.size() + 2][events.size() + 2];
        computeLikelihoods();
    }

    private void computeLikelihoods() {
        List<XEvent> list = new ArrayList<>(2);
        for(int i = 0; i < eventsArray.length; i++) {
            for(int j = 0; j < eventsArray.length; j++) {
                if(i != j) {
                    list.clear();
                    list.add(0, eventsArray[i]);
                    list.add(1, eventsArray[j]);
                    likeloods[i][j] = eventDistributionCalculator.computeLikelihood(list);
                    if(i == eventsArray.length - 1 && j == eventsArray.length - 2) {
                        likeloods[i][j] = 1.0;
                    }
                    if(likeloods[i][j] > 0.0) {
                        numberOfArcs++;
//                        likeloods[i][j] *= 10.0;
                    }
                    numberOfArcswithZero++;
                }else {
                    likeloods[i][j] = 0.0;
                }
            }
        }
    }

    private void computeEnrichedLikelihoods() {
        numberOfArcs = 0;
        numberOfArcswithZero = 0;
        List<XEvent> list = new ArrayList<>(2);
        for(int i = 0; i < eventsArray.length; i++) {
            for(int j = 0; j < eventsArray.length; j++) {
                if(i != j) {
                    list.clear();
                    list.add(0, eventsArray[i]);
                    list.add(1, eventsArray[j]);
                    likeloods[i][j] = eventDistributionCalculator.computeEnrichedLikelihood(list);
                    if(i == eventsArray.length - 1 && j == eventsArray.length - 2) {
                        likeloods[i][j] = 1.0;
                    }
                    if(likeloods[i][j] > 0.0) {
                        numberOfArcs++;
//                        likeloods[i][j] *= 10.0;
                    }
                    numberOfArcswithZero++;
                }else {
                    likeloods[i][j] = 0.0;
                }
            }
        }
    }

    public Set<List<XEvent>> findBestStartEnd() {
        return findBestStartEnd(false);
    }

    public Set<List<XEvent>> findBestStartEnd(boolean includeZero) {
        List<XEvent> list = new ArrayList<>();

        if(includeZero) computeEnrichedLikelihoods();

        if(numberOfArcs > 0) {
            solver.createModel();

            int numberOfU = eventsArray.length - 1;
            int numberOfX = includeZero ? numberOfArcswithZero : numberOfArcs;
            int numberOfVariables = numberOfX + numberOfU;

            // Create variables
            ILPSolverVariable[] vars = new ILPSolverVariable[numberOfVariables];
            int count = 0;
            // Create variables X
            for (int i = 0; i < eventsArray.length; i++) {
                for (int j = 0; j < eventsArray.length; j++) {
                    if (i != j && (includeZero || likeloods[i][j] > 0.0)) {
                        vars[count] = solver.addVariable(0.0, 1.0, 0.0, ILPSolver.VariableType.BINARY, "X_" + i + "_" + j);
                        count++;
                    }
                }
            }

            // Create variables U
            for (int i = 0; i < eventsArray.length; i++) {
                if (i != eventsArray.length - 2) {
                    if(debug_mode) System.out.print("U_" + i + " = " + XConceptExtension.instance().extractName(eventsArray[i]) + ", ");
                    vars[count] = solver.addVariable(0.0, numberOfU, 0.0, ILPSolver.VariableType.INTEGER, "U_" + i);
                    count++;
                }
            }
            if(debug_mode) System.out.println();

            // Integrate new variables
            solver.integrateVariables();

            // Set objective: summation of all edges
            ILPSolverExpression obj = solver.createExpression();
            count = 0;
            int closeLoop = -1;
            for (int i = 0; i < eventsArray.length; i++) {
                for (int j = 0; j < eventsArray.length; j++) {
                    if (i == eventsArray.length - 1 && j == eventsArray.length - 2) {
                        closeLoop = count;
                    }
                    if (i != j && (includeZero || likeloods[i][j] > 0.0)) {
                        obj.addTerm(vars[count], likeloods[i][j] > 0.0 ? likeloods[i][j] : -numberOfU);
                        count++;
                    }
                }
            }
            solver.setMaximize();
            solver.setObjectiveFunction(obj);

            // Add constraint: set Sum X_ij = 1 ForAll i
            count = 0;
            for (int i = 0; i < eventsArray.length; i++) {
                ILPSolverExpression expr = solver.createExpression();
                for (int j = 0; j < eventsArray.length; j++) {
                    if (i != j && (includeZero || likeloods[i][j] > 0.0)) {
                        expr.addTerm(vars[count], 1.0);
                        count++;
                    }
                }
                solver.addConstraint(expr, ILPSolver.Operator.EQUAL, 1.0, "ForAll_" + i + "_j");
            }

            // Add constraint: set Sum X_ij = 1 ForAll j
            for (int k = 0; k < eventsArray.length; k++) {
                ILPSolverExpression expr = solver.createExpression();
                count = 0;
                for (int i = 0; i < eventsArray.length; i++) {
                    for (int j = 0; j < eventsArray.length; j++) {
                        if (i != j && (includeZero || likeloods[i][j] > 0.0)) {
                            if (j == k) {
                                expr.addTerm(vars[count], 1.0);
                            }
                            count++;
                        }
                    }
                }
                solver.addConstraint(expr, ILPSolver.Operator.EQUAL, 1.0, "ForAll_i_" + k);
            }

            // Add constraint: u_i - u_j + NX_ij <= N - 1 ForAll i j with i != 1 and j != 1
            count = 0;
            for (int i = 0; i < eventsArray.length; i++) {
                for (int j = 0; j < eventsArray.length; j++) {
                    if (i != j && (includeZero || likeloods[i][j] > 0.0)) {
                        if (i != eventsArray.length - 2 && j != eventsArray.length - 2) {
                            ILPSolverExpression expr = solver.createExpression();

                            if (i != eventsArray.length - 1) {
                                expr.addTerm(vars[numberOfX + i], 1.0);
                            } else {
                                expr.addTerm(vars[vars.length - 1], 1.0);
                            }

                            if (j != eventsArray.length - 1) {
                                expr.addTerm(vars[numberOfX + j], -1.0);
                            } else {
                                expr.addTerm(vars[vars.length - 1], -1.0);
                            }

                            expr.addTerm(vars[count], numberOfU);
                            solver.addConstraint(expr, ILPSolver.Operator.LESS_EQUAL, numberOfU - 1, "NX_" + i + "_" + j);
                        }
                        count++;
                    }
                }
            }

            ILPSolverExpression expr = solver.createExpression();
            expr.addTerm(vars[closeLoop], 1.0);
            solver.addConstraint(expr, ILPSolver.Operator.EQUAL, 1.0, "ForAll_end_start");

            // Optimize model
            solver.solve();
            ILPSolver.Status status = solver.getStatus();
            if (status == ILPSolver.Status.OPTIMAL) {
    //                for (int i = 0; i < vars.length - numberOfU; i++) {
    //                    System.out.print(vars[i].get(ILPSolver.VariableType.StringAttr.VarName) + "=" + vars[i].get(ILPSolver.VariableType.DoubleAttr.X) + ", ");
    //                }
    //                for (int i = vars.length - numberOfU; i < vars.length; i++) {
    //                    System.out.print(vars[i].get(ILPSolver.VariableType.StringAttr.VarName) + "=" + vars[i].get(ILPSolver.VariableType.DoubleAttr.X) + ", ");
    //                }
    //                System.out.println();

                // Identify Sequence Events
                int correct = 0;
                // Identify Sequence Events
                int currentSource = eventsArray.length - 2;
                double[] solution = solver.getSolutionVariables(vars);
                while (currentSource != eventsArray.length - 1) {
                    count = 0;
                    outter:
                    for (int i = 0; i < eventsArray.length; i++) {
                        for (int j = 0; j < eventsArray.length; j++) {
                            if (i != j && (includeZero || likeloods[i][j] > 0.0)) {
                                if (currentSource == i) {
                                    if (solution[count] > 0) {
                                        correct++;
                                        if (i != eventsArray.length - 2) {
                                            list.add(eventsArray[i]);
                                        }
                                        currentSource = j;
                                        break outter;
                                    }
                                }
                                count++;
                            }
                        }
                    }
                }
                if(debug_mode) {
                    if (correct - 1 != list.size()) System.out.println("error");
                    else System.out.println("Solved with value " + solver.getSolutionValue());
                }
            } else {
                if(debug_mode) {
                    System.out.println("error");
                    if (status == ILPSolver.Status.INFEASIBLE) System.out.println("INFEASIBLE");
                }
            }
            // Dispose of model and environment
            solver.dispose();
        }

        Set<List<XEvent>> set = new UnifiedSet<>();
        if(list.size() > 0) {
            set.add(list);
            List<XEvent> list1 = new ArrayList<>(2);
            for(int i = 0; i < list.size() - 1; i++) {
                list1.clear();
                list1.add(0, list.get(i));
                list1.add(1, list.get(i + 1));
                double likelihood = eventDistributionCalculator.computeLikelihood(list);
                if (likelihood == 0) {
                    if(debug_mode) System.out.println("Updating Enriched Likelihood");
                    eventDistributionCalculator.updateEnrichedLikelihood(list.get(i), list.get(i + 1));
                }
            }
        }

        if(debug_mode) System.out.println("findBestStartEnd " + set.size());
        if(includeZero) return set;
        else return (set.size() > 0) ? set : findBestStartEnd(true);
    }

}
