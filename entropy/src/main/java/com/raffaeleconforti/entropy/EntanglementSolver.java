package com.raffaeleconforti.entropy;

import com.raffaeleconforti.automaton.Automaton;
import com.raffaeleconforti.automaton.AutomatonFactory;
import com.raffaeleconforti.automaton.Edge;
import com.raffaeleconforti.automaton.Node;
import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.log.util.LogModifier;
import com.raffaeleconforti.log.util.LogOptimizer;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 25/08/2016.
 */
public class EntanglementSolver {

    private Automaton automaton;
    private int nodes;
    private Map<Node, Integer> nodeIntegerMap = new UnifiedMap<>();
    private IntObjectHashMap intObjectMap = new IntObjectHashMap();

    private Set<EntanglementState> nonCapturingStates = new UnifiedSet<>();
    private Set<EntanglementState> capturingStates = new UnifiedSet<>();
    private Set<EntanglementState> possibleStates = new UnifiedSet<>();

    private Map<EntanglementState, Set<EntanglementState>> previousStatesCache = new UnifiedMap<>();
    private Map<EntanglementState, Set<EntanglementState>> nextStatesCache = new UnifiedMap<>();
    private Map<EntanglementState, Set<EntanglementState>> sameSourceStatesCache = new UnifiedMap<>();

//    private Set<Set<EntanglementState>> capturingSets = new UnifiedSet<>();

    private Set<EntanglementState> signatureStates = new UnifiedSet<>();

//    public static void main(String[] args) throws Exception {
//        XLog original = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/Dropbox/LaTex/2016/Timestamp Repair/Logs/Experiments/TimeExperimentSimulation.xes.gz");
//        String[] types = new String[] {"Event", "Trace", "UniqueTrace"};
//        String[] levels = new String[] {"05", "10", "15", "20", "25", "30", "35", "40"};
//        String[] names = new String[] {"", "BNN", "D", " ILP", "R"};
//
//
//        TimeStampFilterChecker timeStampFilterChecker = new TimeStampFilterChecker();
//        for(int t = 0; t < types.length; t++) {
//            for (int l = 0; l < levels.length; l++) {
//                XLog noise = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/Dropbox/LaTex/2016/Timestamp Repair/Logs/Experiments/" + types[t] + "/" + types[t] + "0." + levels[l] + ".xes.gz");
//                for (int n = 0; n < names.length; n++) {
//                    XLog fix = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/Dropbox/LaTex/2016/Timestamp Repair/Logs/Experiments/" + types[t] + "/" + types[t] + "0." + levels[l] + names[n] + ".xes.gz");
//                    PrintWriter out = new PrintWriter("/Volumes/Data/Dropbox/LaTex/2016/Timestamp Repair/Logs/Experiments/" + types[t] + "/" + types[t] + "0." + levels[l] + names[n] + ".txt");
//                    String s = timeStampFilterChecker.check(fix, noise, original);
//                    s = s.replace("<html><table width=\"400\"><tr><td width=\"33%\"></td><td width=\"33%\"><table>", "");
//                    s = s.replace("</table></td><td width=\"33%\"></td></tr></table></html>", "");
//                    s = s.replace("<tr>", "");
//                    s = s.replace("<td>", "");
//                    s = s.replace("</td>", "\t");
//                    s = s.replace("</tr>", "\n");
//                    s = s.replace("<html><p>", "");
//                    s = s.replace("<br>", "\n");
//                    s = s.replace("</p></html>", "");
//                    out.write(s);
//                    out.flush();
//                    out.close();
//                    System.out.println(types[t] + "/" + types[t] + "0." + levels[l] + names[n]);
//                }
//            }
//        }
//
//    }

//    public static void main(String[] args) throws Exception {
//        XLog log = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/SharedFolder/Logs/BPI2014 (Incident Activity).xes.gz");
//
//        for (XTrace trace : log) {
//            Collections.sort(trace, new Comparator<XEvent>() {
//                @Override
//                public int compare(XEvent o1, XEvent o2) {
//                    if (XTimeExtension.instance().extractTimestamp(o1).equals(XTimeExtension.instance().extractTimestamp(o2))) {
//                        return ((XAttributeLiteral) o1.getAttributes().get("IncidentActivity_Number")).getValue().compareTo(((XAttributeLiteral) o2.getAttributes().get("IncidentActivity_Number")).getValue());
//                    }
//                    return XTimeExtension.instance().extractTimestamp(o1).compareTo(XTimeExtension.instance().extractTimestamp(o2));
//                }
//            });
//        }
//
//        LogImporter.exportToFile("/Volumes/Data/SharedFolder/Logs/", "BPI2014 (Incident Activity) - Sorted.xes.gz", log);
//        if (true) return;
//    }

    public static void main(String[] args) throws Exception {
        XFactory factory = new XFactoryNaiveImpl();
        LogOptimizer logOptimizer = new LogOptimizer();
//        XLog log = LogImporter.importFromFile(factory, "/Volumes/Data/SharedFolder/Logs/repairExample_complete_lifecycle_only.xes");
        XLog log = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/SharedFolder/Logs/BPI2012 (SimpleFiltered).xes.gz");
//        XLog log = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/SharedFolder/Logs/Windscreen-GIOManual.xes.gz");
//        XLog log = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/SharedFolder/Logs/Commercial-Subprocesses.xes.gz");
        LogModifier logModifier = new LogModifier(factory, XConceptExtension.instance(), XTimeExtension.instance(), logOptimizer);
        log = logModifier.insertArtificialStartAndEndEvent(log);
        EntanglementSolver entanglementSolver = new EntanglementSolver(log);


        Automaton automaton = new Automaton<>();

        Node start = new Node("start");
        Node a = new Node("a");
        Node b = new Node("b");
        Node c = new Node("c");
//        Node d = new Node("d");

        automaton.addNode(start);
        automaton.addNode(a);
        automaton.addNode(b);
        automaton.addNode(c);
//        automaton.addNode(d);

        automaton.addEdge(start, a);
        automaton.addEdge(a, b);
        automaton.addEdge(b, a);
        automaton.addEdge(a, c);
        automaton.addEdge(c, a);
        automaton.addEdge(b, c);
        automaton.addEdge(c, b);

//        automaton.addEdge(a, a);
//        automaton.addEdge(b, b);
//        automaton.addEdge(c, c);
//        automaton.addEdge(d, a);

//        entanglementSolver = new EntanglementSolver(automaton);

        System.out.println(entanglementSolver.solve());
    }

    public EntanglementSolver(XLog log) {
        XEventClassifier xEventClassifier = new XEventAndClassifier(new XEventNameClassifier());
        AutomatonFactory automatonFactory = new AutomatonFactory(xEventClassifier);
        this.automaton = automatonFactory.generate(log);
        initialize();
    }

    public EntanglementSolver(Automaton automaton) {
        this.automaton = automaton;
        initialize();
    }

    private void initialize() {
        simplifyAutomaton();
        this.nodes = automaton.getNodes().size();
        int pos = 2;
        Set<Node> nodes = automaton.getNodes();
        for(Node node : nodes) {
            if(!automaton.getAutomatonStart().contains(node)) {
                nodeIntegerMap.put(node, pos);
                intObjectMap.put(pos, node);
                pos++;
            }else {
                nodeIntegerMap.put(node, 1);
                intObjectMap.put(1, node);
            }
        }
    }

    private void simplifyAutomaton() {
        Iterator<Node> nodes = automaton.getNodes().iterator();
        Set<Edge> edges = automaton.getEdges();

        while(nodes.hasNext()) {
            Node node = nodes.next();
            int input = 0;
            int output = 0;
            Edge inputEdge = null;
            Edge outputEdge = null;
            for(Edge edge : edges) {
                if(edge.getSource().equals(node)) {
                    output++;
                    outputEdge = edge;
                }
                if(edge.getTarget().equals(node)) {
                    input++;
                    inputEdge = edge;
                }
            }
            if(input == 1 && output == 1) {
                automaton.addEdge(inputEdge.getSource(), outputEdge.getTarget());
                automaton.removeEdge(inputEdge);
                automaton.removeEdge(outputEdge);
                automaton.removeNode(node);

                nodes = automaton.getNodes().iterator();
                edges = automaton.getEdges();

                System.out.println("removed");
            }
        }
    }

    public int solve() {
        int cops = 1;
        boolean increase = false;
        while(!increase && cops < nodes) {
            increase = solve(cops);
            System.out.println("Cops " + cops + " = " + increase);
            if(!increase) {
                previousStatesCache.clear();
                nextStatesCache.clear();
                sameSourceStatesCache.clear();

                nonCapturingStates.clear();
                signatureStates.clear();
                cops++;
            }
        }
        return cops;
    }

    private boolean solve(int cops) {

        System.out.println(intObjectMap);
        capturingStates = discoverCapturingStates(cops);
        System.out.println("capturing " + capturingStates);
        possibleStates = discoverPossibleStates(cops);
        System.out.println("possible " + possibleStates);

        extendCapturingStates();

        boolean captured = capturingStates.size() == possibleStates.size();

        System.out.println("new capturing " + capturingStates);
        System.out.println("nonCapturing " + nonCapturingStates);
        return captured;
    }

    public Set<EntanglementState> discoverPossibleStates(int cops) {

        Set<EntanglementState> visited = new UnifiedSet<>();
        Set<EntanglementState> toVisit = new UnifiedSet<>();

        for(EntanglementState state : possibleStates) {
            int[] uniqueStage = new int[cops + 1];
            for(int i = 0; i < uniqueStage.length - 1; i++) {
                uniqueStage[i] = state.get(i);
            }
            EntanglementState start = new EntanglementState(uniqueStage);
            toVisit.add(start.getSignatureState());
        }

        if(toVisit.size() == 0) {
            EntanglementState start = new EntanglementState(new int[cops + 1]);
            start.set(0, nodeIntegerMap.get(automaton.getAutomatonStart().iterator().next()));
            toVisit.add(start);
        }

        Iterator<EntanglementState> toVisitIterator = toVisit.iterator();
        EntanglementState currentState;
        while (toVisitIterator.hasNext()) {
            currentState = toVisitIterator.next();
            toVisitIterator.remove();
            visited.add(currentState);

            Set<EntanglementState> nextStates = discoverNextStates(currentState);
            for(EntanglementState nextState : nextStates) {
                if (!visited.contains(nextState)) {
                    toVisit.add(nextState);
                    toVisitIterator = toVisit.iterator();
                }
            }
        }

        return visited;
    }

    public Set<EntanglementState> discoverCapturingStates(int cops) {
        Set<EntanglementState> visited = new UnifiedSet<>();
        Set<EntanglementState> set = discoverPossibleStates(cops);

        Set<Edge> edges = automaton.getEdges();
        System.out.println(edges);

        for (EntanglementState state : set) {
            for(int i = 1; i < state.size(); i++) {
                if(state.get(0) == state.get(i)) {
                    visited.add(state);
                }
            }
        }
        return visited;
    }

    public boolean areCapturingStatesAvoidable(Set<EntanglementState> nonCapturingStates, Map<EntanglementState, Integer> visited, EntanglementState currentState) {
        Map<EntanglementState, Integer> currentVisited = new UnifiedMap(visited);
        Integer timeVisit = currentVisited.get(currentState);
        currentVisited.put(currentState, timeVisit==null?1:timeVisit+1);

        Set<EntanglementState> states = discoverNextStates(currentState);

        if(capturingStates.contains(currentState)) {
            return false;
        }else if(nonCapturingStates.contains(currentState)) {
            return true;
        }

        if(states.size() == 0) {
            capturingStates.add(currentState);
            return false;
        }

        if (capturingStates.containsAll(states)) {
            capturingStates.add(currentState);
            return false;
        }

        Set<EntanglementState> possibleEscapeStates = new UnifiedSet<>();
        for(EntanglementState nextState : states) {
            if(!capturingStates.contains(nextState)) {
                if (currentVisited.containsKey(nextState)) {
                    currentVisited.put(nextState, currentVisited.get(nextState) + 1);
                    boolean possibleEscape = false;

                    for (EntanglementState possibleNextState : states) {
                        boolean escapeOptions = true;
                        for (int i = 1; i < nextState.size(); i++) {
                            if (possibleNextState.get(0) == currentState.get(i)) {
                                escapeOptions = false;
                                break;
                            }
                        }
                        possibleEscape |= escapeOptions;
                    }

                    for (int i = 1; i < nextState.size(); i++) {
                        if (nextState.get(i) == 0) {
                            possibleEscape = false;
                            break;
                        }
                    }

                    if (possibleEscape && !nextState.isSkip()) {
                        if(nonCapturingStates.contains(nextState)) {
                            nonCapturingStates.add(currentState);
                            return true;
                        }
                        possibleEscapeStates.add(nextState);
                    }
                } else {
                    if(areCapturingStatesAvoidable(nonCapturingStates, currentVisited, nextState)) {
                        if(nonCapturingStates.contains(nextState)) {
                            nonCapturingStates.add(currentState);
                            return true;
                        }
                        possibleEscapeStates.add(nextState);
                    }
                }
            }
        }

        for(EntanglementState escapeState : possibleEscapeStates) {
            boolean safe = false;
            if(nonCapturingStates.contains(escapeState)) {
                nonCapturingStates.add(currentState);
                return true;
            }

            for(EntanglementState nextState : states) {
                if(!escapeState.equals(nextState) && nextState.get(0) == escapeState.get(0) && capturingStates.contains(nextState)) {
                    safe = true;
                    break;
                }
            }
            if(!safe) {
                if(visited.get(escapeState) != null && visited.get(escapeState) > 3){//automaton.getNodes().size()) {
                    nonCapturingStates.add(escapeState);
                    nonCapturingStates.add(currentState);
                }
                return true;
            }
        }

        capturingStates.add(currentState);
        return false;
    }

    public Set<EntanglementState> discoverNextStates(EntanglementState currentState) {
        Set<EntanglementState> result;

        if((result = nextStatesCache.get(currentState)) == null) {
            result = new UnifiedSet<>();
            Set<Edge> edges = automaton.getEdges();
            Set<EntanglementState> toVisit = new UnifiedSet<>();

            EntanglementState nextStateNoCop = new EntanglementState(currentState);
            nextStateNoCop.skip();
            boolean found = false;
            for (Edge robberEdge : edges) {
                if (nodeIntegerMap.get(robberEdge.getSource()) == nextStateNoCop.get(0)) {
                    found = true;
                    EntanglementState nextStateRobber = new EntanglementState(nextStateNoCop);
                    nextStateRobber.set(0, nodeIntegerMap.get(robberEdge.getTarget()));

                    if (!toVisit.contains(nextStateRobber)) {
                        toVisit.add(nextStateRobber);
                    }
                }
            }

            if (!found) {
                if (!toVisit.contains(nextStateNoCop)) {
                    toVisit.add(nextStateNoCop);
                }
            }

            for (int i = 1; i < currentState.size(); i++) {
                EntanglementState nextStateCop = new EntanglementState(currentState);
                nextStateCop.set(i, nextStateCop.get(0));

                boolean skip = false;
                for (int j = 1; j < nextStateCop.size(); j++) {
                    if (j != i && nextStateCop.get(j) == nextStateCop.get(i)) {
                        skip = true;
                        break;
                    }
                }

                if (!skip) {
                    found = false;
                    for (Edge robberEdge : edges) {
                        if (nodeIntegerMap.get(robberEdge.getSource()) == nextStateCop.get(0)) {
                            found = true;
                            EntanglementState nextStateRobber = new EntanglementState(nextStateCop);
                            nextStateRobber.set(0, nodeIntegerMap.get(robberEdge.getTarget()));

                            if (!toVisit.contains(nextStateRobber)) {
                                toVisit.add(nextStateRobber);
                            }
                        }
                    }

                    if (!found) {
                        if (!toVisit.contains(nextStateNoCop)) {
                            toVisit.add(nextStateNoCop);
                        }
                    }
                }
            }

            for (EntanglementState state : toVisit) {
                result.add(state.getSignatureState());
            }

            nextStatesCache.put(currentState, result);
        }

        return result;
    }

    public void extendCapturingStates() {
        Iterator<EntanglementState> capturingStatesIterator = capturingStates.iterator();
        Set<Edge> edges = automaton.getEdges();

        boolean loop = true;
        while (loop) {
            loop = false;
            while (capturingStatesIterator.hasNext() && capturingStates.size() != possibleStates.size()) {
                boolean increased = false;
                EntanglementState state = capturingStatesIterator.next();

                Set<EntanglementState> possiblePreviousStates = new UnifiedSet<>();
                for (Edge edge : edges) {
                    if (nodeIntegerMap.get(edge.getTarget()) == state.get(0)) {
                        EntanglementState possiblePreviousState = new EntanglementState(state);
                        possiblePreviousState.set(0, nodeIntegerMap.get(edge.getSource()));
                        if (possibleStates.contains(possiblePreviousState.getSignatureState())) {
                            possiblePreviousStates.add(possiblePreviousState.getSignatureState());
                        }
                    }
                }

                for (EntanglementState possiblePreviousState : possiblePreviousStates) {
                    Set<EntanglementState> nextStates = discoverNextStates(possiblePreviousState);
                    if (!capturingStates.contains(possiblePreviousState)) {
                        boolean otherOptions = false;
                        for (EntanglementState nextState : nextStates) {
                            if (nextState.get(0) != state.get(0)) {
                                otherOptions = true;
                                break;
                            }
                        }
                        if (!otherOptions) {
                            capturingStates.add(possiblePreviousState);
                            increased = true;
                        }
                    }
                }

                if (increased) {
                    capturingStatesIterator = capturingStates.iterator();
                    loop = true;
                }
            }

            if(!loop) {
                capturingStatesIterator = capturingStates.iterator();
                while (capturingStatesIterator.hasNext() && capturingStates.size() != possibleStates.size()) {
                    EntanglementState state = capturingStatesIterator.next();
                    Set<EntanglementState> previousStates = getPreviousStatesLeadingToCapture(state);
                    int capturingStatesSize = capturingStates.size();
                    capturingStates.addAll(previousStates);
                    if (capturingStates.size() > capturingStatesSize) {
                        capturingStatesIterator = capturingStates.iterator();
                        loop = true;
                        break;
                    }
                }
            }
        }
    }

    private Set<EntanglementState> getPreviousStatesLeadingToCapture(EntanglementState state) {
        Set<EntanglementState> possiblePreviousStates;
        if((possiblePreviousStates = previousStatesCache.get(state)) == null) {
            possiblePreviousStates = new UnifiedSet<>();
            for (int i = 1; i < state.size(); i++) {
                for (EntanglementState possibleState : possibleStates) {
                    if (possibleState.get(0) == state.get(i)) {
                        int differences = 0;
                        for (int j = 1; j < state.size(); j++) {
                            boolean match = false;
                            for (int k = 1; k < state.size(); k++) {
                                if (possibleState.get(j) == state.get(k)) {
                                    match = true;
                                    break;
                                }
                            }
                            if (!match) differences++;
                        }
                        if (differences <= 1) possiblePreviousStates.add(possibleState);
                    }
                }
            }
            previousStatesCache.put(state, possiblePreviousStates);
        }

        Set<EntanglementState> previousStates = new UnifiedSet<>();
        for(EntanglementState possiblePreviousState : possiblePreviousStates) {
            if(!capturingStates.contains(possiblePreviousState)) {
                Set<EntanglementState> nextStates = discoverNextStates(possiblePreviousState);
                for (EntanglementState nextState : nextStates) {
                    Set<EntanglementState> sameSource = findStatesSameSource(nextState);
                    if(capturingStates.size() * 100 / possibleStates.size() > 50) {
                        if (capturingStates.containsAll(sameSource)) {
                            previousStates.add(possiblePreviousState);
                            break;
                        }
                    }else {
                        boolean containsAll = true;
                        for (EntanglementState same : sameSource) {
                            if (!capturingStates.contains(same)) {
                                containsAll = false;
                                break;
                            }
                        }
                        if (containsAll) {
                            previousStates.add(possiblePreviousState);
                            break;
                        }
                    }
                }
            }
        }
        return previousStates;
    }

    private Set<EntanglementState> findStatesSameSource(EntanglementState state) {
        Set<EntanglementState> sameSource;
        if((sameSource = sameSourceStatesCache.get(state)) == null) {
            sameSource = new UnifiedSet<>();
            for (int i = 1; i < intObjectMap.size(); i++) {
                EntanglementState same = new EntanglementState(state);
                same.set(0, i);
                if (possibleStates.contains(same.getSignatureState())) {
                    sameSource.add(same.getSignatureState());
                }
            }
            sameSourceStatesCache.put(state, sameSource);
        }
        return sameSource;
    }
}
