package com.raffaeleconforti.noisefiltering.timestamp.permutation.permutators;

import com.raffaeleconforti.kernelestimation.distribution.EventDistributionCalculator;
import com.raffaeleconforti.kernelestimation.distribution.impl.EventDistributionCalculatorImpl;
import com.raffaeleconforti.noisefiltering.timestamp.permutation.PermutationTechnique;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;
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
public class ILPApproachLpSolve implements PermutationTechnique {

    private EventDistributionCalculator eventDistributionCalculator;
    private XEvent[] eventsArray;
    private double[][] likeloods;

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

        EventDistributionCalculator eventDistributionCalculator = new EventDistributionCalculatorImpl(log, new XEventNameClassifier());
        eventDistributionCalculator.analyseLog();

        Set<XEvent> events = new UnifiedSet<>();
        events.add(b1);
        events.add(c1);
        System.out.println("OK");
        ILPApproachLpSolve ilpApproach = new ILPApproachLpSolve(events, eventDistributionCalculator, a1, d1);
        System.out.println(ilpApproach.findBestStartEnd());
    }

    public ILPApproachLpSolve(Set<XEvent> events, EventDistributionCalculator eventDistributionCalculator, XEvent start, XEvent end) {
        try {
            System.loadLibrary("lpsolve55");
            System.loadLibrary("lpsolve55j");
        } catch (Exception e) {
            System.err.println("Unable to load required libraries for ILP solver.");
            System.err.println("Exception thrown: "+e);
            //                  0123456789012345678901234567890123456789012345678901234567890123456789
            System.err.println("Please obtain a copy of 'lpsolve' and make the libraries available");
            System.err.println("to Uma on the java library path:");
            System.err.println("  java <params> -Djava.library.path=path/to/lpsolve/libs");
            System.err.println("Uma provides other settings for removing implicit places that do not");
            System.err.println("require an ILP solver.");
        }

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
                        likeloods[i][j] = 1;
                    }
                    if(likeloods[i][j] > 0) numberOfArcs++;
                    numberOfArcswithZero++;
                }else {
                    likeloods[i][j] = 0;
                }
            }
        }
    }

    public Set<List<XEvent>> findBestStartEnd() {
        return findBestStartEnd(false);
    }

    private Set<List<XEvent>> findBestStartEnd(boolean includeZero) {
        List<XEvent> list = new ArrayList<>();
        try {
            LpSolve lp; //INCOMPLETE

            int numberOfU = eventsArray.length - 1;
            int numberOfX = includeZero? numberOfArcswithZero : numberOfArcs;
            int numberOfVariables = numberOfX + numberOfU;

            /* create space large enough for one row */
            int[] colno = new int[numberOfVariables];
            double[] row;

            lp = LpSolve.makeLp(0, numberOfVariables);

            int count = 0;
            // Create variables X
            for(int i = 0; i < eventsArray.length; i++) {
                for(int j = 0; j < eventsArray.length; j++) {
                    if(i != j && (includeZero || likeloods[i][j] > 0.0)) {
                        lp.setBinary(count + 1, true);
                        colno[count] = count + 1;
                        count++;
                    }
                }
            }
            // Create variables U
            for(int i = 0; i < eventsArray.length; i++) {
                if(i != eventsArray.length - 2) {
                    lp.setInt(count + 1, true);
                    colno[count] = count + 1;
                    count++;
                }
            }

            // Set objective: summation of all edges
            row = new double[numberOfVariables];
            count = 0;
            int closeLoop = -1;
            for(int i = 0; i < eventsArray.length; i++) {
                for(int j = 0; j < eventsArray.length; j++) {
                    if(i == eventsArray.length - 1 && j == eventsArray.length - 2) {
                        closeLoop = count;
                    }
                    if(i != j && (includeZero || likeloods[i][j] > 0.0)) {
                        row[count] = likeloods[i][j] > 0 ? likeloods[i][j] : -numberOfU;
                        count++;
                    }
                }
            }
            lp.setObjFnex(numberOfVariables, row, colno);
            lp.setMaxim();

            lp.setAddRowmode(true);
            // Add constraint: set Sum X_ij = 1 ForAll i
            count = 0;
            for(int i = 0; i < eventsArray.length; i++) {
                row = new double[numberOfVariables];
                for (int j = 0; j < eventsArray.length; j++) {
                    if(i != j && (includeZero || likeloods[i][j] > 0.0)) {
                        row[count] = 1.0;
                        count++;
                    }
                }
                lp.addConstraintex(numberOfVariables, row, colno, LpSolve.EQ, 1);
            }

            // Add constraint: set Sum X_ij = 1 ForAll j
            for(int k = 0; k < eventsArray.length; k++) {
                row = new double[numberOfVariables];
                count = 0;
                for (int i = 0; i < eventsArray.length; i++) {
                    for(int j = 0; j < eventsArray.length; j++) {
                        if(i != j && (includeZero || likeloods[i][j] > 0.0)) {
                            if(j == k) {
                                row[count] = 1.0;
                            }
                            count++;
                        }
                    }
                }
                lp.addConstraintex(numberOfVariables, row, colno, LpSolve.EQ, 1);
            }

            // Add constraint: u_i - u_j + NX_ij <= N - 1 ForAll i j with i != 1 and j != 1
            count = 0;
            for (int i = 0; i < eventsArray.length; i++) {
                for(int j = 0; j < eventsArray.length; j++) {
                    if(i != j && (includeZero || likeloods[i][j] > 0.0)) {
                        if (i != eventsArray.length - 2 && j != eventsArray.length - 2) {
                            row = new double[numberOfVariables];

                            if (i != eventsArray.length - 1) {
                                row[numberOfX + i] = 1.0;
                            } else {
                                row[row.length - 1] = 1.0;
                            }

                            if (j != eventsArray.length - 1) {
                                row[numberOfX + j] = -1.0;
                            } else {
                                row[row.length - 1] = -1.0;
                            }

                            row[count] = numberOfU;
                            lp.addConstraintex(numberOfVariables, row, colno, LpSolve.LE, numberOfU - 1);
                        }
                        count++;
                    }
                }
            }

            row = new double[numberOfVariables];
            row[closeLoop] = 1.0;
            lp.addConstraintex(numberOfVariables, row, colno, LpSolve.EQ, 1);

            // Optimize model
            lp.setAddRowmode(false);

            lp.setVerbose(LpSolve.IMPORTANT);
            int status = lp.solve();
            if(status == LpSolve.OPTIMAL) {
                row = new double[numberOfVariables];
                lp.getVariables(row);

                // Identify Sequence Events
                int currentSource = eventsArray.length - 2;
                while (currentSource != eventsArray.length - 1) {
                    count = 0;
                    outter:
                    for (int i = 0; i < eventsArray.length; i++) {
                        for (int j = 0; j < eventsArray.length; j++) {
                            if(i != j && (includeZero || likeloods[i][j] > 0.0)) {
                                if (currentSource == i) {
                                    if (row[count] > 0) {
                                        if(i != eventsArray.length - 2) {
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
                System.out.println("Solved");
            }else {
                System.out.println("error");
                if(status == LpSolve.INFEASIBLE) System.out.println("INFEASIBLE");
            }
            // Dispose of model and environment
            lp.deleteLp();
        } catch (LpSolveException e) {
            System.out.println("Error code: " + e.getMessage());
        }

        Set<List<XEvent>> set = new UnifiedSet<>();
        if(list.size() > 0) set.add(list);

        System.out.println("findBestStartEnd " + set.size());
        if(includeZero) return set;
        else return (set.size() > 0) ? set : findBestStartEnd(true);
    }
}
