package com.raffaeleconforti.splitminer.splitminer.dfgp;

import com.raffaeleconforti.automaton.Automaton;
import com.raffaeleconforti.automaton.AutomatonFactory;
import com.raffaeleconforti.automaton.Edge;
import com.raffaeleconforti.automaton.Node;
import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.ilpsolverwrapper.ILPSolver;
import com.raffaeleconforti.ilpsolverwrapper.impl.gurobi.Gurobi_Solver;
import com.raffaeleconforti.ilpsolverwrapper.impl.lpsolve.LPSolve_Solver;
import com.raffaeleconforti.noisefiltering.event.InfrequentBehaviourFilter;
import com.raffaeleconforti.noisefiltering.event.infrequentbehaviour.automaton.AutomatonInfrequentBehaviourDetector;
import com.raffaeleconforti.noisefiltering.event.optimization.wrapper.WrapperInfrequentBehaviourSolver;
import com.raffaeleconforti.noisefiltering.event.selection.NoiseFilterResult;
import com.raffaeleconforti.splitminer.log.LogParser;
import com.raffaeleconforti.splitminer.log.SimpleLog;
import com.raffaeleconforti.splitminer.log.graph.LogNode;
import org.apache.commons.lang3.StringUtils;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramImpl;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.Event;

import java.util.*;

/**
 * Created by Adriano on 24/10/2016.
 */
public abstract class DirectlyFollowGraphPlus {

    private SimpleLog log;
    protected int startcode;
    protected int endcode;

    protected Set<DFGEdge> edges;
    protected Map<Integer, DFGNode> nodes;
    protected Map<Integer, HashSet<DFGEdge>> outgoings;
    protected Map<Integer, HashSet<DFGEdge>> incomings;
    private Map<Integer, HashMap<Integer, DFGEdge>> dfgp;

    protected Set<Integer> loopsL1;
    protected Set<DFGEdge> loopsL2;
    private Map<Integer, HashSet<Integer>> parallelisms;
    private Set<DFGEdge> bestEdges;

    protected double percentileFrequencyThreshold;
    private double parallelismsThreshold;
    private int filterThreshold;
    //    private boolean percentileOnBest;
    private boolean parallelismsFirst;

//    public DirectlyFollowGraphPlus(SimpleLog log) {
//        this(log, DFGPUIResult.FREQUENCY_THRESHOLD, DFGPUIResult.PARALLELISMS_THRESHOLD, DFGPUIResult.STD_FILTER, DFGPUIResult.PARALLELISMS_FIRST);
//    }

    public DirectlyFollowGraphPlus(SimpleLog log, double percentileFrequencyThreshold, double parallelismsThreshold, boolean parallelismsFirst) {
        this.log = log;
        this.startcode = log.getStartcode();
        this.endcode = log.getEndcode();
        this.percentileFrequencyThreshold = percentileFrequencyThreshold;
        this.parallelismsThreshold = parallelismsThreshold;
//        this.percentileOnBest = percentileOnBest;
        this.parallelismsFirst = parallelismsFirst;
    }

    public BPMNDiagram getDFG() {
        buildDirectlyFollowsGraph();
        return getDFGP(true);
    }

    public BPMNDiagram getDFGP(boolean labels) {
        Map<Integer, String> events = log.getEvents();
        BPMNDiagram diagram = new BPMNDiagramImpl("DFGP-diagram");
        HashMap<Integer, BPMNNode> mapping = new HashMap<>();
        String label;
        Activity task;
        BPMNNode src, tgt;

        for (int event : nodes.keySet()) {
            label = events.get(event) + "\n(" + nodes.get(event).getFrequency() + ")";
            task = diagram.addActivity((labels ? label : Integer.toString(event)), false, false, false, false, false);
            mapping.put(event, task);
        }

        for (DFGEdge edge : edges) {
            src = mapping.get(edge.getSourceCode());
            tgt = mapping.get(edge.getTargetCode());
            diagram.addFlow(src, tgt, edge.toString());
        }

        return diagram;
    }

    public BPMNDiagram convertIntoBPMNDiagram() {
        BPMNDiagram diagram = new BPMNDiagramImpl("eDFGP-diagram");
        HashMap<Integer, BPMNNode> mapping = new HashMap<>();
        String label;
        BPMNNode node;
        BPMNNode src, tgt;

        for (int event : nodes.keySet()) {
            label = Integer.toString(event);

            if (event == startcode || event == endcode)
                node = diagram.addEvent(label, (event == startcode ? Event.EventType.START : Event.EventType.END), Event.EventTrigger.NONE, (event == startcode ? Event.EventUse.CATCH : Event.EventUse.THROW), true, null);
            else
                node = diagram.addActivity(label, loopsL1.contains(event), false, false, false, false);

            mapping.put(event, node);
        }

        for (DFGEdge edge : edges) {
            src = mapping.get(edge.getSourceCode());
            tgt = mapping.get(edge.getTargetCode());
            diagram.addFlow(src, tgt, edge.toString());
        }

        return diagram;
    }

    public boolean areConcurrent(int A, int B) {
        return (parallelisms.containsKey(A) && parallelisms.get(A).contains(B));
    }

    public void buildDFGP() {
        System.out.println("DFGP - settings > " + parallelismsThreshold + " : " + percentileFrequencyThreshold + " : " + this.getClass().toString());
//        System.out.println("DFGP - [Settings] parallelisms threshold: " + parallelismsThreshold);
//        System.out.println("DFGP - [Settings] percentile on best: " + percentileOnBest);
//        System.out.println("DFGP - [Settings] parcentile threshold: " + percentileFrequencyThreshold);

//        System.out.println("DEBUG - NO FILTER");

        buildDirectlyFollowsGraph();                //first method to execute
        detectLoops();                              //depends on buildDirectlyFollowsGraph()
        detectParallelisms();                       //depends on detectLoops()
    }

    protected void buildDirectlyFollowsGraph() {
        Map<String, Integer> traces = log.getTraces();
        Map<Integer, String> events = log.getEvents();

        StringTokenizer trace;
        int traceFrequency;

        int event;
        int prevEvent;

        DFGNode node;
        DFGNode prevNode;
        DFGEdge edge;

        DFGNode autogenStart;
        DFGNode autogenEnd;

        nodes = new HashMap<>();
        edges = new HashSet<>();
        outgoings = new HashMap<>();
        incomings = new HashMap<>();
        dfgp = new HashMap<>();

        autogenStart = new DFGNode(events.get(startcode), startcode);
        this.addNode(autogenStart);
//        while parsing the simple log we will always skip the start event,
//        so we set now the maximum frequency because it is an artificial start event
        autogenStart.increaseFrequency(log.size());

        autogenEnd = new DFGNode(events.get(endcode), endcode);
        this.addNode(autogenEnd);

        for (String t : traces.keySet()) {
            trace = new StringTokenizer(t, "::");
            traceFrequency = traces.get(t);

//            consuming the start event that is always 0
            trace.nextToken();
            prevEvent = startcode;
            prevNode = autogenStart;

            while (trace.hasMoreTokens()) {
//                we read the next event of the trace until it is finished
                event = Integer.valueOf(trace.nextToken());

                if (!nodes.containsKey(event)) {
                    node = new DFGNode(events.get(event), event);
                    this.addNode(node);
                } else node = nodes.get(event);

//                  increasing frequency of this event occurrence
                node.increaseFrequency(traceFrequency);

                if (!dfgp.containsKey(prevEvent) || !dfgp.get(prevEvent).containsKey(event)) {
                    edge = new DFGEdge(prevNode, node);
                    this.addEdge(edge);
                }

//                  increasing frequency of this directly following relationship
                dfgp.get(prevEvent).get(event).increaseFrequency(traceFrequency);

                prevEvent = event;
                prevNode = node;
            }
        }
    }

    protected void detectLoops() {
        Map<String, Integer> traces = log.getTraces();
        HashSet<DFGEdge> removableLoopEdges = new HashSet();

        DFGEdge e2;
        int src;
        int tgt;

        String src2tgt_loop2Pattern;
        String tgt2src_loop2Pattern;

        int src2tgt_loop2Frequency;
        int tgt2src_loop2Frequency;

        int loop2score;

        loopsL1 = new HashSet<>();
        loopsL2 = new HashSet<>();

//        System.out.println("DFGP - evaluating loops length ONE ...");
        for (DFGEdge e : edges) {
            src = e.getSourceCode();
            tgt = e.getTargetCode();
            if (src == tgt) {
                loopsL1.add(src);
                removableLoopEdges.add(e);
            }
        }

//        we removed the loop length 1 edges, because late we will just mark them as self-loop activities
//        System.out.println("DFGP - loops length ONE found: " + loopsL1.size());
        for (DFGEdge e : removableLoopEdges) this.removeEdge(e, false);

//        System.out.println("DEBUG - found " + loopsL1.size() + " self-loops:");
//        for( int code : loopsL1 ) System.out.println("DEBUG - self-loop: " + code);

        for (DFGEdge e1 : edges) {
            src = e1.getSourceCode();
            tgt = e1.getTargetCode();

//            if src OR tgt are length 1 loops, we do not evaluate length 2 loops for this edge,
//            because a length 1 loop in parallel with something else
//            can generate pattern of the type [src :: tgt :: src] OR [tgt :: src :: tgt]
            if (!loopsL2.contains(e1) && dfgp.get(tgt).containsKey(src) && !loopsL1.contains(src) && !loopsL1.contains(tgt)) {
                e2 = dfgp.get(tgt).get(src);

                src2tgt_loop2Pattern = "::" + src + "::" + tgt + "::" + src + "::";
                tgt2src_loop2Pattern = "::" + tgt + "::" + src + "::" + tgt + "::";
                src2tgt_loop2Frequency = 0;
                tgt2src_loop2Frequency = 0;

                for (String trace : traces.keySet()) {
                    src2tgt_loop2Frequency += (StringUtils.countMatches(trace, src2tgt_loop2Pattern) * traces.get(trace));
                    tgt2src_loop2Frequency += (StringUtils.countMatches(trace, tgt2src_loop2Pattern) * traces.get(trace));
                }

                loop2score = src2tgt_loop2Frequency + tgt2src_loop2Frequency;

//                if the loop2score is not zero, it means we found patterns of the type:
//                [src :: tgt :: src] OR [tgt :: src :: tgt], so we set both edges as short-loops
                if (loop2score != 0) {
                    loopsL2.add(e1);
                    loopsL2.add(e2);
                }
            }
        }

//        System.out.println("DFGP - loops length TWO found: " + loopsL2.size()/2);
    }

    protected void detectParallelisms() {
        int totalParallelisms = 0;
        int confirmedParallelisms = 0;
        boolean priorityCheck;

        DFGEdge e2;
        int src;
        int tgt;

        int src2tgt_frequency;
        int tgt2src_frequency;
        double parallelismScore;

        HashSet<DFGEdge> removableEdges = new HashSet<>();

        parallelisms = new HashMap<>();

        for (DFGEdge e1 : edges) {
            src = e1.getSourceCode();
            tgt = e1.getTargetCode();

            if (parallelismsFirst) priorityCheck = !loopsL2.contains(e1);
            else priorityCheck = !loopsL1.contains(src) && !loopsL1.contains(tgt);

            if (dfgp.get(tgt).containsKey(src) && priorityCheck && !removableEdges.contains(e1)) {
//                this means: src || tgt is candidate parallelism
                e2 = dfgp.get(tgt).get(src);

                src2tgt_frequency = e1.getFrequency();
                tgt2src_frequency = e2.getFrequency();
                parallelismScore = (double) (src2tgt_frequency - tgt2src_frequency) / (src2tgt_frequency + tgt2src_frequency);

                if (Math.abs(parallelismScore) < parallelismsThreshold) {
//                    if parallelismScore is less than the threshold epslon,
//                    we set src || tgt and vice-versa, and we remove e1 and e2
                    if (!parallelisms.containsKey(src)) parallelisms.put(src, new HashSet<Integer>());
                    parallelisms.get(src).add(tgt);
                    if (!parallelisms.containsKey(tgt)) parallelisms.put(tgt, new HashSet<Integer>());
                    parallelisms.get(tgt).add(src);
                    removableEdges.add(e1);
                    removableEdges.add(e2);
                    totalParallelisms++;
                } else {
//                    otherwise we remove the least frequent edge, e1 or e2
                    if (parallelismScore > 0) removableEdges.add(e2);
                    else removableEdges.add(e1);
                }
            }
        }

        ArrayList<DFGEdge> orderedRemovableEdges = new ArrayList<>(removableEdges);
        Collections.sort(orderedRemovableEdges);
        while (!orderedRemovableEdges.isEmpty()) {
            DFGEdge re = orderedRemovableEdges.remove(0);
            if (!this.removeEdge(re, true)) {
//                System.out.println("DEBUG - impossible remove: " + re.print());
                src = re.getSourceCode();
                tgt = re.getTargetCode();
                if (parallelisms.containsKey(src)) parallelisms.get(src).remove(tgt);
                if (parallelisms.containsKey(tgt)) parallelisms.get(tgt).remove(src);
                if ((re = dfgp.get(tgt).get(src)) != null) this.removeEdge(re, true);
            } else {
                confirmedParallelisms++;
            }
        }

        System.out.println("DFGP - parallelisms found (total, confirmed): (" + totalParallelisms + " , " + confirmedParallelisms + ")");
    }


    /* data objects management */

    protected void addNode(DFGNode n) {
        int code = n.getCode();

        nodes.put(code, n);
        if (!incomings.containsKey(code)) incomings.put(code, new HashSet<DFGEdge>());
        if (!outgoings.containsKey(code)) outgoings.put(code, new HashSet<DFGEdge>());
        if (!dfgp.containsKey(code)) dfgp.put(code, new HashMap<Integer, DFGEdge>());
    }

    protected void removeNode(int code) {
        HashSet<DFGEdge> removable = new HashSet<>();
        nodes.remove(code);
        for (DFGEdge e : incomings.get(code)) removable.add(e);
        for (DFGEdge e : outgoings.get(code)) removable.add(e);
        for (DFGEdge e : removable) removeEdge(e, false);
    }

    protected void addEdge(DFGEdge e) {
        int src = e.getSourceCode();
        int tgt = e.getTargetCode();

        edges.add(e);
        incomings.get(tgt).add(e);
        outgoings.get(src).add(e);
        dfgp.get(src).put(tgt, e);

//        System.out.println("DEBUG - added edge: " + src + " -> " + tgt);
    }

    protected boolean removeEdge(DFGEdge e, boolean safe) {
        int src = e.getSourceCode();
        int tgt = e.getTargetCode();
        if (safe && ((incomings.get(tgt).size() == 1) || (outgoings.get(src).size() == 1))) return false;
        incomings.get(tgt).remove(e);
        outgoings.get(src).remove(e);
        dfgp.get(src).remove(tgt);
        edges.remove(e);
        return true;
//        System.out.println("DEBUG - removed edge: " + src + " -> " + tgt);
    }


    /* DEBUG methods */

    private void printEdges() {
        for (DFGEdge e : edges)
            System.out.println("DEBUG - edge : " + e.print());
    }

    public void printNodes() {
        for (DFGNode n : nodes.values())
            System.out.println("DEBUG - node : " + n.print());
    }

    public void printParallelisms() {
        System.out.println("DEBUG - printing parallelisms:");
        for (int A : parallelisms.keySet()) {
            System.out.print("DEBUG - " + A + " || ");
            for (int B : parallelisms.get(A)) System.out.print(B + ",");
            System.out.println();
        }
    }


    /* OTHER methods (experimental) */

    private void noiseFilter(boolean gurobi) {
        System.out.println("DEBUG - edges before filtering: " + edges.size());
        ILPSolver ilp_solver;
        Automaton<String> automaton = new Automaton<>();
        Set<Edge<String>> removable = new HashSet<>();
        LogNode src, tgt;
        int srcID, tgtID;

        Map<Integer, Node<String>> anodes = new HashMap<>();
        Map<Edge<String>, DFGEdge> aedges = new HashMap<>();

        Node<String> srcANode, tgtANode;
        Edge<String> aedge;

        for (DFGEdge e : edges) {
            src = e.getSource();
            tgt = e.getTarget();

            srcID = src.getCode();
            tgtID = tgt.getCode();

            if ((srcANode = anodes.get(srcID)) == null) {
                srcANode = new Node<>(Integer.toString(srcID));
                srcANode.setFrequency(src.getFrequency());
                anodes.put(srcID, srcANode);
                automaton.addNode(srcANode);
            }


            if (!anodes.containsKey(tgtID)) {
                tgtANode = new Node<>(Integer.toString(tgtID));
                tgtANode.setFrequency(tgt.getFrequency());
                anodes.put(tgtID, tgtANode);
                automaton.addNode(tgtANode);
            } else tgtANode = anodes.get(tgtID);

            aedge = new Edge<>(srcANode, tgtANode);
            automaton.addEdge(aedge, e.getFrequency());
            aedges.put(aedge, e);
            if (!(bestEdges.contains(e) || (e.getFrequency() > filterThreshold))) {
                aedge.setInfrequent(true);
                removable.add(aedge);
            }
        }

//        System.out.println("DEBUG - automaton start: " + automaton.getAutomatonStart());
//        System.out.println("DEBUG - automaton end: " + automaton.getAutomatonEnd());

        automaton.getAutomatonStart();
        automaton.getAutomatonEnd();
        automaton.createDirectedGraph();

        for (Edge<String> e : automaton.getEdges())
            e.setFrequency(getFrequency(automaton, e, AutomatonInfrequentBehaviourDetector.AVE));

        if (gurobi) ilp_solver = new Gurobi_Solver();
        else ilp_solver = new LPSolve_Solver();
        WrapperInfrequentBehaviourSolver<String> solver;

        solver = new WrapperInfrequentBehaviourSolver<>(automaton, removable, automaton.getNodes(), true);
        removable = solver.identifyRemovableEdges(ilp_solver);

        for (Edge<String> ae : removable) {
//            System.out.println("DEBUG - removing edge: " + ae.getSource().getData() + " > " + ae.getTarget().getData());
            edges.remove(aedges.get(ae));
        }
        System.out.println("DEBUG - edges after filtering: " + edges.size());
    }

    private double getFrequency(Automaton<String> automaton, Edge<String> edge, int approach) {
        if (approach == AutomatonInfrequentBehaviourDetector.MIN) {
            return automaton.getEdgeFrequency(edge) / (Math.min(automaton.getNodeFrequency(edge.getSource()), automaton.getNodeFrequency(edge.getTarget())));
        } else if (approach == AutomatonInfrequentBehaviourDetector.MAX) {
            return automaton.getEdgeFrequency(edge) / (Math.max(automaton.getNodeFrequency(edge.getSource()), automaton.getNodeFrequency(edge.getTarget())));
        } else if (approach == AutomatonInfrequentBehaviourDetector.AVE) {
            return automaton.getEdgeFrequency(edge) / ((automaton.getNodeFrequency(edge.getSource()) + automaton.getNodeFrequency(edge.getTarget())) / 2);
        }
        return 0.0;
    }

    private void generateNoiseFilteredDFG() {
        double percentile = percentileFrequencyThreshold;

        System.out.println("DEBUG - node before: " + nodes.size());
        System.out.println("DEBUG - edge before: " + edges.size());
        Map<Integer, String> oldEvents = log.getEvents();
        XLog xlog = log.getXLog();
        XEventClassifier classifier = new XEventNameClassifier();
        InfrequentBehaviourFilter filter = new InfrequentBehaviourFilter(classifier);
        AutomatonFactory automatonFactory = new AutomatonFactory(classifier);

        Automaton<String> automatonOriginal = automatonFactory.generate(xlog);
        NoiseFilterResult noisefilterParams = new NoiseFilterResult();
        noisefilterParams.setRepeated(true);
        noisefilterParams.setFixLevel(false);
        noisefilterParams.setPercentile(percentile);
        noisefilterParams.setNoiseLevel(filter.discoverThreshold(filter.discoverArcs(automatonOriginal, 1.0), percentile));
        noisefilterParams.setApproach(AutomatonInfrequentBehaviourDetector.AVE);
        noisefilterParams.setRequiredStates(automatonOriginal.getNodes());

        XLog fxLog = filter.filterLog(new FakePluginContext(), xlog, noisefilterParams);
        log = LogParser.getSimpleLog(fxLog, classifier);
        buildDirectlyFollowsGraph();
        alignParallelisms(oldEvents);
        System.out.println("DEBUG - node after: " + nodes.size());
        System.out.println("DEBUG - edge after: " + edges.size());
    }

    private void alignParallelisms(Map<Integer, String> oldEvents) {
        Map<Integer, String> events = log.getEvents();
        Map<Integer, HashSet<Integer>> oldParallelisms = this.parallelisms;
        int newSRC, newTGT;
        String srcLabel, tgtLabel;

        int totalParallelisms = 0;

        detectLoops();
        detectParallelisms();

        for (int src : oldParallelisms.keySet())
            for (int tgt : oldParallelisms.get(src)) {
                srcLabel = oldEvents.get(src);
                tgtLabel = oldEvents.get(tgt);
                newTGT = startcode;
                newSRC = endcode;

                for (int k : events.keySet()) {
                    if (events.get(k).equalsIgnoreCase(srcLabel)) newSRC = k;
                    if (events.get(k).equalsIgnoreCase(tgtLabel)) newTGT = k;
                }

                if ((newTGT != startcode) && (newSRC != endcode)) {
                    if (!parallelisms.containsKey(newSRC)) parallelisms.put(newSRC, new HashSet<Integer>());
                    parallelisms.get(newSRC).add(newTGT);
                    if (!parallelisms.containsKey(newTGT)) parallelisms.put(newTGT, new HashSet<Integer>());
                    parallelisms.get(newTGT).add(newSRC);
                    if (dfgp.containsKey(newSRC) && dfgp.get(newSRC).containsKey(newTGT))
                        removeEdge(dfgp.get(newSRC).get(newTGT), true);
                    if (dfgp.containsKey(newTGT) && dfgp.get(newTGT).containsKey(newSRC))
                        removeEdge(dfgp.get(newTGT).get(newSRC), true);
                    totalParallelisms++;
                }
            }

        System.out.println("DEBUG - old parallelisms added: " + totalParallelisms);
    }
}