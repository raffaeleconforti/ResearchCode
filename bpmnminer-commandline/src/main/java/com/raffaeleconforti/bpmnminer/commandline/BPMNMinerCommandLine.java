package com.raffaeleconforti.bpmnminer.commandline;

import au.edu.qut.bpmn.structuring.StructuringService;
import com.raffaeleconforti.bpmnminer.commandline.preprocessing.functionaldependencies.ui.DiscoverERModel_UI;
import com.raffaeleconforti.bpmnminer.commandline.subprocessminer.ui.EntityDiscoverer_UI;
import com.raffaeleconforti.bpmnminer.commandline.subprocessminer.ui.SelectMinerUI;
import com.raffaeleconforti.bpmnminer.exception.ExecutionCancelledException;
import com.raffaeleconforti.bpmnminer.subprocessminer.BPMNSubProcessMiner;
import com.raffaeleconforti.bpmnminer.subprocessminer.EntityDiscoverer;
import com.raffaeleconforti.bpmnminer.subprocessminer.selection.SelectMinerResult;
import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.foreignkeydiscovery.conceptualmodels.ConceptualModel;
import com.raffaeleconforti.foreignkeydiscovery.conceptualmodels.Entity;
import com.raffaeleconforti.foreignkeydiscovery.functionaldependencies.NoEntityException;
import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.log.util.LogOptimizer;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.plugins.bpmn.BpmnDefinitions;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by conforti on 30/03/15.
 */
public class BPMNMinerCommandLine {

    public BPMNDiagram mineBPMNModel(final UIPluginContext context, XLog rawlog) throws ExecutionCancelledException {
        BPMNSubProcessMiner subProcessMiner = new BPMNSubProcessMiner();

        LogOptimizer logOptimizer = new LogOptimizer();
        XLog optimizedLog = logOptimizer.optimizeLog(rawlog);
        rawlog = optimizedLog;

        UIPluginContext pluginContext = context;
        pluginContext.getProgress().setIndeterminate(true);
        pluginContext.getProgress().setCaption("BPMN Miner");

        pluginContext.getProgress().setIndeterminate(false);
        pluginContext.getProgress().setMinimum(0);
        pluginContext.getProgress().setMaximum(20);//concModel.getEntities().size() + 2);
        pluginContext.getProgress().setValue(1);

        EntityDiscoverer_UI entityDiscoverer_ui = new EntityDiscoverer_UI();
        EntityDiscoverer entityDiscoverer = entityDiscoverer_ui.getEntityDiscoverer();

        //Select Miner
        SelectMinerUI selectMinerUI = new SelectMinerUI();
        SelectMinerResult guiResult = selectMinerUI.showGUI();

        Integer algorithm = 1;

        //discover the ER model
        DiscoverERModel_UI ERmodel = new DiscoverERModel_UI();
        ConceptualModel concModel = null;

        //ui choose artifacts from entities
        //------------------------------------------
        List<Entity> groupEntities = new ArrayList<Entity>();
        List<Entity> candidatesEntities = new ArrayList<Entity>();
        List<Entity> selectedEntities = new ArrayList<Entity>();

        try {
            concModel = ERmodel.showGUI(rawlog, algorithm);

            if(concModel != null) {
                System.out.println("Discovering groupEntities...");
                groupEntities = entityDiscoverer_ui.discoverGroupEntities(concModel, false);
                System.out.println("groupEntities discovered");

                System.out.println("Discovering candidatesEntities...");
                candidatesEntities = entityDiscoverer_ui.discoverCandidatesEntities(concModel, groupEntities);
                System.out.println("candidatesEntities discovered");

                System.out.println("Discovering selectedEntities...");
                selectedEntities = entityDiscoverer_ui.selectEntities(groupEntities, candidatesEntities, false);
                System.out.println("selectedEntities discovered");
            }
        } catch (NoEntityException nee) {}

        StringBuilder sb = new StringBuilder();
        if (groupEntities != null) {
            sb.append("groupEntities:\n");
            for (Entity e : groupEntities) {
                sb.append(e.toString()).append("\n");
            }
        }
        if (candidatesEntities != null) {
            sb.append("candidatesEntities:\n");
            for (Entity e : candidatesEntities) {
                sb.append(e.toString()).append("\n");
            }
        }
        if(selectedEntities != null) {
            sb.append("selectedEntities:\n");
            for (Entity e : selectedEntities) {
                sb.append(e.toString()).append("\n");
            }
        }
        System.out.println(sb.toString());

        BPMNDiagram model = subProcessMiner.mineBPMNModel(context, rawlog, false, guiResult, algorithm, entityDiscoverer, concModel, groupEntities, candidatesEntities, selectedEntities, true);

        return model;

    }

    public static void main(String[] args) throws Exception {

        System.out.println("This is the stand alone version of the BPMNMiner algorithm proposed in:");
        System.out.println();
        System.out.println("R. Conforti, M. Dumas, L. García-Bañuelos, and M. La Rosa.");
        System.out.println("BPMN Miner: Automated discovery of BPMN process models with connectivity structure.");
        System.out.println("Information Systems, 56, pp 284-303, 2016.");
        System.out.println();
        System.out.println("For more info contact me at raffaele.conforti@unimelb.edu.au");
        System.out.println("or visit my website www.raffaeleconforti.com");

        Scanner console = new Scanner(System.in);
        System.out.println("Input file:");
        String name = console.nextLine();

        while(name.endsWith(" ")) {
            name = name.substring(0, name.length() - 1);
        }

        XFactory factory = new XFactoryNaiveImpl();
        XLog log = LogImporter.importFromFile(factory, name);

        UIPluginContext fakeContext = new FakePluginContext();
        BPMNMinerCommandLine plugin = new BPMNMinerCommandLine();

        BPMNDiagram diagram = plugin.mineBPMNModel(fakeContext, log);

        System.out.println("Do you want to structure the diagram? (y/n)");
        String token = null;
        boolean structure = true;
        while (token == null) {
            token = console.nextLine();
            if(token.equalsIgnoreCase("y")) {
                structure = true;
            }else if(token.equalsIgnoreCase("n")) {
                structure = false;
            }else {
                token = null;
                System.out.println("Accepted input Y or N");
                System.out.println("Do you want to structure the diagram? (y/n)");
            }
        }

        for(Activity activity : diagram.getActivities()) {
            if(activity.getLabel().endsWith("+complete")) {
                activity.getAttributeMap().put("ProM_Vis_attr_label", activity.getLabel().substring(0, activity.getLabel().indexOf("+complete")));
            }
        }

        if(structure) {
            StructuringService ss = new StructuringService();
            diagram = ss.structureDiagram(diagram);
        }

        if(name.endsWith(".xes")) name = name.substring(0, name.length() - 4);
        else if(name.endsWith(".xes.gz")) name = name.substring(0, name.length() - 7);
        else if(name.endsWith(".mxml")) name = name.substring(0, name.length() - 5);
        else if(name.endsWith(".mxml.gz")) name = name.substring(0, name.length() - 8);

        System.out.println("Output file: " + name + ".bpmn");
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        UIContext context = new UIContext();
        UIPluginContext uiPluginContext = context.getMainPluginContext();
        BpmnDefinitions.BpmnDefinitionsBuilder definitionsBuilder = new BpmnDefinitions.BpmnDefinitionsBuilder(uiPluginContext, diagram);
        BpmnDefinitions definitions = new BpmnDefinitions("definitions", definitionsBuilder);

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"\n " +
                "xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\"\n " +
                "xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\"\n " +
                "xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\"\n " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n " +
                "targetNamespace=\"http://www.omg.org/bpmn20\"\n " +
                "xsi:schemaLocation=\"http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd\">");

        sb.append(definitions.exportElements());
        sb.append("</definitions>");
        FileWriter fileWriter = new FileWriter(new File(name + ".bpmn"));
        fileWriter.write(sb.toString());
        fileWriter.flush();
        fileWriter.close();
    }

}
