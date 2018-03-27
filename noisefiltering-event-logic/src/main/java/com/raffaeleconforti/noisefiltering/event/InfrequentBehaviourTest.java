package com.raffaeleconforti.noisefiltering.event;

import com.raffaeleconforti.log.util.LogImporter;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 20/4/17.
 */
public class InfrequentBehaviourTest {

    public static void main(String[] args) throws Exception {
        XLog log = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/SharedFolder/Logs/ArtificialLess.xes.gz");

        XLog filtered1 = test(log, false, false, false);
        LogImporter.exportToFile("/Volumes/Data/SharedFolder/Logs/ArtificialLess1.xes.gz", filtered1);
//        test(log, false);

//        Node<String> a = new Node<>("A");
//        Node<String> b = new Node<>("B");
//        Node<String> c = new Node<>("C");
//        Node<String> d = new Node<>("D");
//
//        Automaton<String> automaton = new Automaton<>();
//        automaton.addNode(a, 4);
//        automaton.addNode(b, 8);
//        automaton.addNode(c, 2);
//        automaton.addNode(d, 4);
//        automaton.addEdge(a, b, 4);
//        automaton.addEdge(b, b, 3);
//        automaton.addEdge(b, c, 2);
//        automaton.addEdge(b, d, 3);
//        automaton.addEdge(c, b, 1);
//        automaton.addEdge(c, d, 1);
//
//        Node<String> a = new Node<>("A");
//        Node<String> b = new Node<>("B");
//        Node<String> c = new Node<>("C");
//
//        Automaton<String> automaton = new Automaton<>();
//        automaton.addNode(a, 4);
//        automaton.addNode(b, 4);
//        automaton.addNode(c, 3);
//        automaton.addEdge(a, b, 4);
//        automaton.addEdge(b, c, 2);
//        automaton.addEdge(a, c, 1);
//
//        automaton.getAutomatonStart();
//        automaton.getAutomatonEnd();
//        automaton.createDirectedGraph();
//
//        WrapperInfrequentBehaviourSolver wrapperInfrequentBehaviourSolver = new WrapperInfrequentBehaviourSolver(automaton, automaton.getEdges(), automaton.getNodes());
//        Set<Edge<String>> infrequent = wrapperInfrequentBehaviourSolver.identifyRemovableEdges(new Gurobi_Solver());
//        System.out.println(infrequent);
//        infrequent = wrapperInfrequentBehaviourSolver.identifyRemovableEdges(new LPSolve_Solver());
//        System.out.println(infrequent);
    }

    private static XLog test(XLog log, boolean useGurobi, boolean useArcsFrequency, boolean debug_mode) {
        System.out.println(count(log));
        InfrequentBehaviourFilter filter = new InfrequentBehaviourFilter(new XEventNameClassifier(), useGurobi, useArcsFrequency, debug_mode);
        XLog filtered = filter.filterLog(log);
        System.out.println(count(filtered));
        return filtered;
    }

    private static int count(XLog log) {
        int count = 0;
        for(XTrace trace : log) {
            count += trace.size();
        }
        return count;
    }

}
