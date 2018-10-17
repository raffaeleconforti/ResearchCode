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

package com.raffaeleconforti.noisefiltering.timestamp;

import com.raffaeleconforti.automaton.Automaton;
import com.raffaeleconforti.automaton.AutomatonFactory;
import com.raffaeleconforti.automaton.Edge;
import com.raffaeleconforti.automaton.Node;
import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.log.util.LogCloner;
import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.log.util.LogModifier;
import com.raffaeleconforti.log.util.LogOptimizer;
import com.raffaeleconforti.noisefiltering.timestamp.permutation.PermutationTechnique;
import com.raffaeleconforti.noisefiltering.timestamp.traceselector.AutomatonBestTraceMatchSelector;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
import org.eclipse.collections.api.multimap.Multimap;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.multimap.set.UnifiedSetMultimap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Event;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;
import org.processmining.plugins.bpmn.Bpmn;
import org.processmining.plugins.bpmn.plugins.BpmnImportPlugin;
import org.processmining.plugins.bpmn.plugins.BpmnSelectDiagramPlugin;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by conforti on 7/02/15.
 */

public class TimeStampFixerSmartExecutor {

    private final XEventClassifier xEventClassifier = new XEventAndClassifier(new XEventNameClassifier());

//    private AutomatonInfrequentBehaviourDetector automatonInfrequentBehaviourDetector = new AutomatonInfrequentBehaviourDetector(AutomatonInfrequentBehaviourDetector.MAX);

    private final AutomatonFactory automatonFactory = new AutomatonFactory(xEventClassifier);

    private final SimpleDateFormat dateFormatSeconds = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private final boolean debug_mode;
    private final boolean useGurobi;
    private final boolean useArcsFrequency;

    public TimeStampFixerSmartExecutor(boolean useGurobi, boolean useArcsFrequency, boolean debug_mode) {
        this.debug_mode = debug_mode;
        this.useGurobi = useGurobi;
        this.useArcsFrequency = useArcsFrequency;
    }

    public static void main(String[] args) throws Exception {
        String dir = "/Volumes/MobileData/Logs/Consultancies/UoM/";
        FakePluginContext fakePluginContext = new FakePluginContext();
        BpmnImportPlugin importPlugin = new BpmnImportPlugin();
        BpmnSelectDiagramPlugin selectPlugin = new BpmnSelectDiagramPlugin();
        BPMNDiagram diagram = selectPlugin.selectDefault(
                fakePluginContext,
                (Bpmn) importPlugin.importFile(
                        fakePluginContext,
                        new File(dir + "UM Overall.bpmn")
                ));

        XConceptExtension xce = XConceptExtension.instance();
        XTimeExtension xte = XTimeExtension.instance();
        TimeStampFixerSmartExecutor t = new TimeStampFixerSmartExecutor(false, false, false);
        Automaton<String> a = t.convertToAutomaton(diagram);
        for (Edge<String> edge : a.getEdges()) {
            a.addEdge(edge, 1);
        }
        XLog log = LogImporter.importFromFile(new XFactoryNaiveImpl(), dir + "Version 5/UoM1.xes.gz");
        for (XTrace trace : log) {
            for (XEvent event : trace) {
//                XAttributeTimestamp time = (XAttributeTimestamp) event.getAttributes().remove("time:timestamp");
//                event.getAttributes().put("time", new XAttributeTimestampImpl("time", time.getValue()));
//                Calendar calendar = Calendar.getInstance();
//                calendar.setTime(time.getValue());
//                calendar.set(Calendar.HOUR_OF_DAY, 0);
//                calendar.set(Calendar.MINUTE, 0);
//                calendar.set(Calendar.SECOND, 0);
//                xte.assignTimestamp(event, calendar.getTime());
                xce.assignName(event, event.getAttributes().get("REP_LEVEL_1").toString());
                Iterator<String> iterator = event.getAttributes().keySet().iterator();
                Set<String> remove = new HashSet<>();
                while (iterator.hasNext()) {
                    String name = iterator.next();
                    if (name.startsWith("(case)")) {
                        remove.add(name);
                    }
                }
                for (String name : remove) {
                    event.getAttributes().remove(name);
                }
            }
        }
        XLog filtered = t.filterLog(log, a, 11, PermutationTechnique.ILP_GUROBI, true, false, false);
        for (XTrace trace : filtered) {
            for (XEvent event : trace) {
                xce.assignName(event, event.getAttributes().get("Rep_level_2_Mod").toString());
            }
        }
        filtered = t.filterLog(filtered, a, 11, PermutationTechnique.ILP_GUROBI, true, false, false);
//        for (XTrace trace : filtered) {
//            for (XEvent event : trace) {
//                XAttributeTimestamp timestamp = (XAttributeTimestamp) event.getAttributes().remove("time");
//                xte.assignTimestamp(event, timestamp.getValue());
//            }
//        }
        LogImporter.exportToFile(dir + "Version 5/UoM2.xes.gz", filtered);
    }

    public Automaton<String> convertToAutomaton(BPMNDiagram bpmnDiagram) {
        //create an object of class: au.edu.qut.processmining.miners.splitminer.dfgp.DirectlyFollowGraphPlus
        //line 180: change from private to public the following method, call it.
        //private void buildDirectlyFollowsGraph() {...}

        Map<String, Node<String>> labels = new UnifiedMap<>();
        Automaton<String> automaton = new Automaton<>();
        Map<BPMNNode, Node> map = new UnifiedMap<>();
        UnifiedSetMultimap<Gateway, BPMNNode> incomingToGateway = new UnifiedSetMultimap();
        UnifiedSetMultimap<Gateway, BPMNNode> outgoingFromGateway = new UnifiedSetMultimap();

        Node start = new Node("ArtificialStartEvent");
        Node end = new Node("ArtificialEndEvent");

        automaton.addNode(start);
        automaton.addNode(end);
        for (Event event : bpmnDiagram.getEvents()) {
            if (event.getEventType() == Event.EventType.START) map.put(event, start);
            else if (event.getEventType() == Event.EventType.END) map.put(event, end);
        }
        for (BPMNNode bpmnNode : bpmnDiagram.getActivities()) {
            if (!labels.containsKey(bpmnNode.getLabel())) {
                Node node = new Node(bpmnNode.getLabel());
                automaton.addNode(node);
                map.put(bpmnNode, node);
                labels.put(bpmnNode.getLabel(), node);
            } else {
                map.put(bpmnNode, labels.get(bpmnNode.getLabel()));
            }
        }
        for (Gateway gateway : bpmnDiagram.getGateways()) {
            for (Flow flow : bpmnDiagram.getFlows()) {
                if (flow.getSource().equals(gateway)) {
                    outgoingFromGateway.put(gateway, flow.getTarget());
                } else if (flow.getTarget().equals(gateway)) {
                    incomingToGateway.put(gateway, flow.getSource());
                }
            }
        }
        for (Flow flow : bpmnDiagram.getFlows()) {
            BPMNNode source = flow.getSource();
            BPMNNode target = flow.getTarget();
            if (source instanceof Event) {
                for (Node<String> node : getNode(target, map, outgoingFromGateway)) {
                    automaton.addEdge(start, node);
                }
            } else if (target instanceof Event) {
                for (Node<String> node : getNode(source, map, incomingToGateway)) {
                    automaton.addEdge(node, end);
                }
            } else {
                for (Node<String> node1 : getSourceNode(source, map, incomingToGateway)) {
                    for (Node<String> node2 : getTargetNode(target, map, outgoingFromGateway)) {
                        automaton.addEdge(node1, node2);
                    }
                }
            }
        }
        automaton.getAutomatonStart();
        automaton.getAutomatonEnd();
//        List<Edge<String>> edges = new ArrayList<>(automaton.getEdges());
//        Collections.sort(edges, new Comparator<Edge<String>>() {
//            @Override
//            public int compare(Edge<String> o1, Edge<String> o2) {
//                return o1.toString().compareTo(o2.toString());
//            }
//        });
//        System.out.println(edges);
        return automaton;
    }

    private Set<Node<String>> getSourceNode(BPMNNode node, Map<BPMNNode, Node> map, Multimap<Gateway, BPMNNode> gateway) {
        Set<Node<String>> nodes = getNode(node, map, gateway);
        return nodes;
    }

    private Set<Node<String>> getTargetNode(BPMNNode node, Map<BPMNNode, Node> map, Multimap<Gateway, BPMNNode> gateway) {
        Set<Node<String>> nodes = getNode(node, map, gateway);
        return nodes;
    }

    private Set<Node<String>> getNode(BPMNNode node, Map<BPMNNode, Node> map, Multimap<Gateway, BPMNNode> gateway) {
        Set<Node<String>> nodes = new UnifiedSet<>();
        if (node instanceof Gateway) {
            for (BPMNNode node1 : gateway.get((Gateway) node)) {
                nodes.addAll(getNode(node1, map, gateway));
            }
        } else {
            nodes.add(map.get(node));
        }
        return nodes;
    }

    public XLog filterLog(XLog log, Automaton<String> automaton, int limitExtensive, int approach, boolean debug_mode, boolean self_cleaning, boolean fix_timestamp) {

        XFactory factory = new XFactoryNaiveImpl();//XFactoryMemoryImpl();
        LogCloner logCloner = new LogCloner(factory);

        XLog res = logCloner.cloneLog(log);

        LogOptimizer logOptimizer = new LogOptimizer();
        LogModifier logModifier = new LogModifier(factory, XConceptExtension.instance(), XTimeExtension.instance(), logOptimizer);

        res = logModifier.sortLog(res);
        XLog optimizedLog = logOptimizer.optimizeLog(log);
        optimizedLog = logModifier.insertArtificialStartAndEndEvent(optimizedLog);

        if (debug_mode) {
            System.out.println("Permutations discovery started");
        }
        TimeStampFixer timeStampFixerSmart;
        if (automaton == null)
            timeStampFixerSmart = new TimeStampFixerSmart(factory, logCloner, optimizedLog, xEventClassifier, dateFormatSeconds, limitExtensive, approach, useGurobi, useArcsFrequency, debug_mode, self_cleaning);
        else
            timeStampFixerSmart = new TimeStampFixerSmart(automaton, factory, logCloner, optimizedLog, xEventClassifier, dateFormatSeconds, limitExtensive, approach, useGurobi, useArcsFrequency, debug_mode, self_cleaning);

        List<String> fixedTraces = new ArrayList<String>();

        long start = System.currentTimeMillis();
        res = sortLog(res, logModifier, timeStampFixerSmart, fixedTraces, approach, debug_mode, self_cleaning);
        long middle = System.currentTimeMillis();
        if (fix_timestamp) res = assignTimestamp(res, logModifier, timeStampFixerSmart, fixedTraces, debug_mode);
        long end = System.currentTimeMillis();

        System.out.println("Ordering: " + (middle - start) + " Assignment: " + (end - middle) + " Total: " + (end - start));

        return res;
    }

    public XLog filterLog(XLog log, int limitExtensive, int approach, boolean debug_mode, boolean self_cleaning, boolean fix_timestamp) {
        return filterLog(log, null, limitExtensive, approach, debug_mode, self_cleaning, fix_timestamp);
    }

    public XLog filterLog(XLog log, int limitExtensive, int approach, boolean debug_mode, boolean self_cleaning) {
        return filterLog(log, null, limitExtensive, approach, debug_mode, self_cleaning, true);
    }

    public XLog sortLog(XLog log, LogModifier logModifier, TimeStampFixer timeStampFixerSmart, List<String> fixedTraces, int approach, boolean debug_mode, boolean self_cleaning) {
        int[] fix = new int[]{0};

        XLog permutedLog = timeStampFixerSmart.obtainPermutedLog();
//        permutedLog = logModifier.insertArtificialStartAndEndEvent(permutedLog);

        if (debug_mode) {
            System.out.println("Permutations discovery ended");
            System.out.println(timeStampFixerSmart.getDuplicatedTraces());
        }

        Automaton<String> automaton = automatonFactory.generateForTimeFilter(permutedLog, timeStampFixerSmart.getDuplicatedEvents());

//        InfrequentBehaviourFilter infrequentBehaviourFilter = new InfrequentBehaviourFilter(xEventClassifier);
//        double[] arcs = infrequentBehaviourFilter.discoverArcs(automaton, 1.0);
//        AutomatonInfrequentBehaviourDetector automatonInfrequentBehaviourDetector = new AutomatonInfrequentBehaviourDetector(AutomatonInfrequentBehaviourDetector.MAX);
//        Automaton<String> automatonClean = automatonInfrequentBehaviourDetector.removeInfrequentBehaviour(automaton, automaton.getNodes(), infrequentBehaviourFilter.discoverThreshold(arcs, 0.125), useGurobi, useArcsFrequency);

        if (debug_mode) {
            System.out.println();
            System.out.println("Selection best permutation started");
        }
        AutomatonBestTraceMatchSelector automatonBestTraceMatchSelector = new AutomatonBestTraceMatchSelector(permutedLog, xEventClassifier, automaton, timeStampFixerSmart.getDuplicatedTraces(), timeStampFixerSmart.getPossibleTraces(), timeStampFixerSmart.getFaultyEvents(), log.size());
//        AutomatonBestTraceMatchSelector automatonBestTraceMatchSelector = new AutomatonBestTraceMatchSelector(permutedLog, xEventClassifier, automatonClean, timeStampFixerSmart.getDuplicatedTraces(), timeStampFixerSmart.getPossibleTraces(), timeStampFixerSmart.getFaultyEvents(), log.size());

        log = automatonBestTraceMatchSelector.selectBestMatchingTraces(new FakePluginContext(), fix, fixedTraces, approach, self_cleaning);
        log = logModifier.removeArtificialStartAndEndEvent(log);

        if (debug_mode) {
            System.out.println("Selection best permutation completed");
            System.out.println();
            System.out.println("Timestamps disambiguation started");
        }

        return log;
    }

    public XLog assignTimestamp(XLog log, LogModifier logModifier, TimeStampFixer timeStampFixerSmart, List<String> fixedTraces, boolean debug_mode) {
        TimestampsAssigner timestampsAssigner = new TimestampsAssigner(log, xEventClassifier, dateFormatSeconds, timeStampFixerSmart.getDuplicatedTraces(), timeStampFixerSmart.getDuplicatedEvents(), useGurobi, useArcsFrequency, debug_mode);
        boolean result = timestampsAssigner.assignTimestamps(fixedTraces);

        if(!result) {
            timestampsAssigner.assignTimestamps();
        }
        log = logModifier.sortLog(log);

        if(debug_mode) {
            System.out.println("Timestamps disambiguation completed");
        }

        return log;
    }

}
