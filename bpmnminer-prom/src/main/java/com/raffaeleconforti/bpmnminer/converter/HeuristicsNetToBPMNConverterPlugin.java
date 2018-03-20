package com.raffaeleconforti.bpmnminer.converter;

/**
 * Created by Raffaele Conforti on 28/02/14.
 */

import com.raffaeleconforti.conversion.heuristicsnet.HNNetToBPMNConverter;
import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.log.util.LogReaderClassic;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.log.LogFile;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.mining.heuristicsmining.HeuristicsMiner;
import org.processmining.mining.heuristicsmining.HeuristicsNetResult;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

@Plugin(name = "Mine BPMN Using HeuristicsMiner of Prom5.2", parameterLabels = {"Log"},
        returnLabels = {"BPMNDiagram"},
        returnTypes = {BPMNDiagram.class})
public class HeuristicsNetToBPMNConverterPlugin {


    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@unimelb.edu.au",
            pack = "bpmnminer")
    @PluginVariant(variantLabel = "Mine BPMN Using HeuristicsMiner of Prom5.2", requiredParameterLabels = {0})
    public BPMNDiagram convert(final UIPluginContext context, XLog log) {
        HeuristicsMiner miner = new HeuristicsMiner();

        System.out.println("Exporting MXML...");
        try {
            LogImporter.exportToFile("", "tmpLog.mxml.gz", log);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("MXML Exported");

        System.out.println("Importing MXML...");
        LogFile lf = LogFile.getInstance("tmpLog.mxml.gz");
        System.out.println("MXML Imported");

        HeuristicsNetResult hNet = null;
        try {
            hNet = (HeuristicsNetResult) miner.mine(LogReaderClassic.createInstance(null, lf));
        } catch (Exception e) {
            e.printStackTrace();
        }
        HeuristicsNet net = hNet.getHeuriticsNet();

        return HNNetToBPMNConverter.convert(net);
    }
}
