package com.raffaeleconforti.noisefiltering.event.commandline;

import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.noisefiltering.event.FrequentBehaviourFilter;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.plugins.bpmn.plugins.BpmnExportPlugin;

import java.io.File;
import java.util.Scanner;

/**
 * Created by conforti on 7/02/15.
 */

public class FrequentBehaviourFilterCommandLine {

    private final FrequentBehaviourFilter frequentBehaviourFilter = new FrequentBehaviourFilter();

    public static void main(String[] args) throws Exception {
        Scanner console = new Scanner(System.in);

        System.out.println("Do you want to analyse 1 log or compare 2? (1/2)");
        Integer algorithm = null;
        String token = null;
        while (algorithm == null || (algorithm != 1.0 && algorithm != 2.0)) {
            System.out.println("Do you want to analyse 1 log or compare 2? (1/2)");
            try {
                token = console.nextLine();
                algorithm = Integer.parseInt(token);
                if (algorithm != 1.0 && algorithm != 2.0) {
                    System.out.println("Select a 1 for true and 2 false");
                }
            } catch (NumberFormatException nfe) {
                if (token.isEmpty()) {
                    algorithm = 1;
                    System.out.println("Value selected: 1");
                } else {
                    System.out.println("Select a 1 for true and 2 false");
                }
            }
        }

        FrequentBehaviourFilterCommandLine fbfc = new FrequentBehaviourFilterCommandLine();

        XFactory factory = new XFactoryNaiveImpl();
        BPMNDiagram diagram = null;

        if(algorithm == 1) {
            System.out.println("Input file:");
            String name = console.nextLine();
            XLog log = LogImporter.importFromFile(factory, name);
            diagram = fbfc.generateDiagram(log);
        }else {
            System.out.println("Input file1:");
            String name1 = console.nextLine();
            XLog log1 = LogImporter.importFromFile(factory, name1);

            System.out.println("Input file2:");
            String name2 = console.nextLine();
            XLog log2 = LogImporter.importFromFile(factory, name2);

            diagram = fbfc.generateDiagramTwoLogs(log1, log2);
        }

        System.out.println("Output file: ");
        String path = console.next();

        BpmnExportPlugin bpmnExportPlugin = new BpmnExportPlugin();
        UIContext context = new UIContext();
        UIPluginContext uiPluginContext = context.getMainPluginContext();
        System.out.println("Output file:");
        bpmnExportPlugin.export(uiPluginContext, diagram, new File(path+".bpmn"));

    }

    public BPMNDiagram generateDiagram(XLog rawlog) {
        return frequentBehaviourFilter.generateDiagram(new FakePluginContext(), rawlog);
    }

    public BPMNDiagram generateDiagramTwoLogs(XLog rawlog1, XLog rawlog2) {
        return frequentBehaviourFilter.generateDiagramTwoLogs(new FakePluginContext(), rawlog1, rawlog2);
    }

}
