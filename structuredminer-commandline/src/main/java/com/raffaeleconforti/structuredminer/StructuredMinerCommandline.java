package com.raffaeleconforti.structuredminer;

import au.edu.qut.bpmn.structuring.core.StructuringCore;
import au.edu.qut.bpmn.structuring.ui.iBPStructUIResult;
import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.structuredminer.miner.StructuredMiner;
import com.raffaeleconforti.structuredminer.ui.SettingsStructuredMiner;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
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

    public static void main(String[] args) {

        System.out.println("This is the stand alone version of the structured miner proposed in:");
        System.out.println();
        System.out.println("A. Augusto, R. Conforti, M. Dumas, M. La Rosa, and G. Bruno.");
        System.out.println("Automated Discovery of Structured Process Models: Discover Structured vs. Discover and Structure.");
        System.out.println("In proceedings of the 35th International Conference on Conceptual Modeling (ER 2016), 2016.");
        System.out.println();

        File logfile;
        int miningAlgorithm;
        String name;
        int icmd = 0;

        iBPStructUIResult settingStructuring = new iBPStructUIResult();
        settingStructuring.setForceStructuring(false);
        settingStructuring.setKeepBisimulation(true);
        settingStructuring.setTimeBounded(true);
        settingStructuring.setMaxMinutes(2);
        settingStructuring.setMaxChildren(10);
        settingStructuring.setMaxDepth(100);
        settingStructuring.setMaxStates(100);
        settingStructuring.setMaxSol(500);
        settingStructuring.setPolicy(StructuringCore.Policy.ASTAR);

        int maxMinutes;

        try {
            /* first parameter
             */
            if( args[icmd].equalsIgnoreCase("-help") ) {
                showHelp();
                return;
            }
            else if( args[icmd].equalsIgnoreCase("HM52") ) miningAlgorithm = SettingsStructuredMiner.HMPOS52;
            else if( args[icmd].equalsIgnoreCase("HM") ) miningAlgorithm = SettingsStructuredMiner.HMPOS;
            else if( args[icmd].equalsIgnoreCase("FO") ) miningAlgorithm = SettingsStructuredMiner.FODINAPOS;
            else throw new Exception("ERROR - specified mining algorithm not found.");
            icmd++;

            if( args[icmd].equalsIgnoreCase("p") ) {
                settingStructuring.setKeepBisimulation(false);
                icmd++;
            }

            if( args[icmd].equalsIgnoreCase("f") ) {
                settingStructuring.setForceStructuring(true);
                icmd++;
            }

            try {
                maxMinutes = Integer.valueOf(args[icmd]);
                if( maxMinutes == 0 ) settingStructuring.setTimeBounded(false);
                settingStructuring.setMaxMinutes(maxMinutes);
                icmd++;
            } catch ( NumberFormatException nfe){}

            logfile = new File(args[icmd]);
            if( !logfile.exists() ) throw new Exception("ERROR - input log file not found.");
            XLog log = LogImporter.importFromFile(new XFactoryNaiveImpl(), args[icmd]);
            icmd++;
            name = args[icmd];

            SettingsStructuredMiner settingsStructuredMiner = new SettingsStructuredMiner(miningAlgorithm);
            StructuredMiner structuredMiner = new StructuredMiner(new FakePluginContext(), log, settingsStructuredMiner, settingStructuring);
            BPMNDiagram diagram = structuredMiner.mine();

            BpmnExportPlugin bpmnExportPlugin = new BpmnExportPlugin();
            UIContext context = new UIContext();
            UIPluginContext uiPluginContext = context.getMainPluginContext();
            bpmnExportPlugin.export(uiPluginContext, diagram, new File(name+".bpmn"));

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ERROR: " + e.getMessage());
            System.out.println("RUN: java -jar StructuredMiner [hm|hm52|fo] [p] [f] [minutes] logFileName.[mxml|xes] bpmnFileName");
            System.out.println("HELP: java -jar StructuredMiner -help");
        }
    }

    private static void showHelp(){
        System.out.println("COMMAND: java -jar StructuredMiner");
        System.out.println("PARAM1 (mandatory) - mining algorithn: hm|hm52|fo");
        System.out.println("\t- HM stands for Heuristics Miner ProM 6.5");
        System.out.println("\t- HM52 stands for Heuristics Miner ProM 5.2");
        System.out.println("\t- FO stands for Fodina Miner");
        System.out.println("PARAM2 (optional) - pull-up rule flag: p");
        System.out.println("\t- when present enable the pull-up rule and the output model may not be weakly bisimilar");
        System.out.println("PARAM3 (optional) - force structuring flag: f");
        System.out.println("\t- when present it forces the structuring, meaning the structured model may lose or gain behaviour");
        System.out.println("PARAM4 (optional) - minutes for time-out of the TBA*: integer > 0");
        System.out.println("\t- if not set, the time-out is by default 2 minutes");
        System.out.println("PARAM5 (mandatory) - input log file name: logFileName.[mxml|xes]");
        System.out.println("\t- accepted log files are in .mxml or .xes format");
        System.out.println("PARAM6 (mandatory) - output model file name: bpmnFileName");
        System.out.println("\t- this will be the name of the output .bpmn file containing the structured model");
    }

}