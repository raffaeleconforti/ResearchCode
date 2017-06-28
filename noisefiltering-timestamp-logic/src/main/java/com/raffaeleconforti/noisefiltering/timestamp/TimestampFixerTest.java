package com.raffaeleconforti.noisefiltering.timestamp;

import com.raffaeleconforti.log.util.LogCloner;
import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.memorylog.XFactoryMemoryImpl;
import com.raffaeleconforti.noisefiltering.event.InfrequentBehaviourFilter;
import com.raffaeleconforti.noisefiltering.timestamp.permutation.PermutationTechnique;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import java.util.StringTokenizer;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 20/4/17.
 */
public class TimestampFixerTest {

    public static void main(String[] args) throws Exception {
        XLog log = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/Dropbox/Consultancies/Cineca-UniParma/u-gov/logs/Log_CG_DG/Result/Arcs/Vendite/Vendite_50_Instances.xes.gz");

        for(XTrace trace : log) {
            for(XEvent event : trace) {
                String resource = XOrganizationalExtension.instance().extractResource(event);
                resource = resource.replace("@unipr.it", "");
                resource = resource.replace("@cineca.it", "");
                resource = resource.replace(".", " ");
                StringTokenizer st = new StringTokenizer(resource, " ");
                String new_resource = "<html><b><center><p style=\"color:navy\">";
                while (st.hasMoreTokens()) {
                    new_resource += capitalize(st.nextToken());
                    if(st.hasMoreTokens()) new_resource += "<br>";
                }
                new_resource += "</center></b></p></html>";
                XOrganizationalExtension.instance().assignResource(event, new_resource);
            }
        }

        LogImporter.exportToFile("/Volumes/Data/Dropbox/Consultancies/Cineca-UniParma/u-gov/logs/Log_CG_DG/Result/Arcs/Vendite/Vendite_50_Instances2.xes.gz", log);
    }

    public static String capitalize(String s) {
        if (s.length() == 0) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    public static void main2(String[] args) throws Exception {
        XLog log = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/SharedFolder/Logs/ArtificialLess.xes.gz");

        LogCloner logCloner = new LogCloner(new XFactoryMemoryImpl());

        TimeStampFixerSmartExecutor timeStampFixerSmartExecutor = new TimeStampFixerSmartExecutor(true, true);
        timeStampFixerSmartExecutor.filterLog(log, 11, PermutationTechnique.ILP_GUROBI_ARCS);

        XLog filtered1 = test(log, false, false);
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

    private static XLog test(XLog log, boolean useGurobi, boolean useArcsFrequency) {
        System.out.println(count(log));
        InfrequentBehaviourFilter filter = new InfrequentBehaviourFilter(new XEventNameClassifier(), useGurobi, useArcsFrequency);
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
