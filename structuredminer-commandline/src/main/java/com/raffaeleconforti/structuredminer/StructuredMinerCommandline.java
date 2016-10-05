package com.raffaeleconforti.structuredminer;

import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.structuredminer.miner.StructuredMiner;
import com.raffaeleconforti.structuredminer.ui.SettingsStructuredMiner;
import com.raffaeleconforti.memorylog.XFactoryMemoryImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.plugins.bpmn.plugins.BpmnExportPlugin;

import java.io.File;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 15/04/2016.
 */
public class StructuredMinerCommandline  {

    public static void main(String[] args) throws Exception {
        File logfile;
        int miningAlgorithm = 0;
        String name = "";

        try {
            logfile = new File(args[0]);
            if( !logfile.exists() ) throw new Exception("file not found.");
            if( args[1].equals("HM") ) miningAlgorithm = SettingsStructuredMiner.HMPOS52;
            else if( args[1].equals("FO") ) miningAlgorithm = SettingsStructuredMiner.FODINAPOS;
            else throw new Exception("wrong usage.");
            name = args[2];
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            System.out.println("TYPE: java -jar StructuredMiner logFileName.[mxml|xes] [HM|FO] bpmnFileName");
        }

        XLog log = LogImporter.importFromFile(new XFactoryMemoryImpl(), args[0]);
        SettingsStructuredMiner settingsStructuredMiner = new SettingsStructuredMiner(miningAlgorithm);
        StructuredMiner structuredMiner = new StructuredMiner(new FakePluginContext(), log, settingsStructuredMiner);
        BPMNDiagram diagram = structuredMiner.mine();

        BpmnExportPlugin bpmnExportPlugin = new BpmnExportPlugin();
        UIContext context = new UIContext();
        UIPluginContext uiPluginContext = context.getMainPluginContext();
        bpmnExportPlugin.export(uiPluginContext, diagram, new File(name+".bpmn"));

    }
}