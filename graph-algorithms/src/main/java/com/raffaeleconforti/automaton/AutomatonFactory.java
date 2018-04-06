package com.raffaeleconforti.automaton;

import com.raffaeleconforti.log.util.NameExtractor;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by conforti on 14/02/15.
 */
public class AutomatonFactory {

    private final NameExtractor nameExtractor;

    public AutomatonFactory(XEventClassifier xEventClassifier) {
        nameExtractor = new NameExtractor(xEventClassifier);
    }

    public Automaton<Integer> generate(List<IntArrayList> log) {
        Automaton<Integer> automaton = new Automaton<>();

        Map<Node, Node> map = new UnifiedMap<Node, Node>();
        for(IntArrayList t : log) {
            for (int i = 0; i < t.size(); i++) {
                int e = t.get(i);
                Node n;
                if((n = map.get(new Node<>(e))) == null) {
                    n = new Node<>(e);
                    map.put(n, n);
                }
                automaton.addNode(n);
            }
        }

        for(IntArrayList t : log) {
            for (int i = 0; i < t.size(); i++) {
                int e = t.get(i);
                if(i < t.size() - 1) {
                    int e1 = t.get(i + 1);
                    Node n = map.get(new Node<>(e));
                    Node n1 = map.get(new Node<>(e1));

                    automaton.addEdge(n, n1);
                }
            }
        }

        return automaton;
    }

    public Automaton generate(XLog log) {
        Automaton<String> automaton = new Automaton<String>();

        Map<Node, Node> map = new UnifiedMap<Node, Node>();
        for(XTrace t : log) {
            for (int i = 0; i < t.size(); i++) {
                XEvent e = t.get(i);
                Node n;
                if((n = map.get(new Node<>(nameExtractor.getEventName(e)))) == null) {
                    n = new Node<>(nameExtractor.getEventName(e));
                    map.put(n, n);
                }
                automaton.addNode(n);
            }
        }

        for(XTrace t : log) {
            for (int i = 0; i < t.size(); i++) {
                XEvent e = t.get(i);
                if(i < t.size() - 1) {
                    XEvent e1 = t.get(i + 1);
                    Node n = map.get(new Node<>(nameExtractor.getEventName(e)));
                    Node n1 = map.get(new Node<>(nameExtractor.getEventName(e1)));

                    automaton.addEdge(n, n1);
                }
            }
        }

        return automaton;
    }

    public Automaton generateForTimeFilter(XLog log, Map<String, Set<String>> duplicatedEvents) {
        Automaton<String> automaton = new Automaton<>();

        Map<Node, Node> map = new UnifiedMap<>();
        for(XTrace t : log) {
            for(XEvent e : t) {
                if(duplicatedEvents.get(nameExtractor.getTraceName(t)) == null || !duplicatedEvents.get(nameExtractor.getTraceName(t)).contains(nameExtractor.getEventName(e))) {
                    Node n;
                    if ((n = map.get(new Node(nameExtractor.getEventName(e)))) == null) {
                        n = new Node(nameExtractor.getEventName(e));
                        map.put(n, n);
                    }
                    automaton.addNode(n);
                }
            }
        }

        for(XTrace t : log) {
            for (int i = 0; i < t.size(); i++) {
                XEvent e = t.get(i);
                if(duplicatedEvents.get(nameExtractor.getTraceName(t)) == null || !duplicatedEvents.get(nameExtractor.getTraceName(t)).contains(nameExtractor.getEventName(e))) {
                    if (i < t.size() - 1) {
                        XEvent e1 = t.get(i + 1);
                        if(duplicatedEvents.get(nameExtractor.getTraceName(t)) == null || !duplicatedEvents.get(nameExtractor.getTraceName(t)).contains(nameExtractor.getEventName(e1))) {
                            Node n = map.get(new Node(nameExtractor.getEventName(e)));
                            Node n1 = map.get(new Node(nameExtractor.getEventName(e1)));

                            automaton.addEdge(n, n1);
                        }
                    }
                }
            }
        }

        return automaton;
    }

}
