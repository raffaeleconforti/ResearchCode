package com.raffaeleconforti.bpmnminer.subprocessminer;

import com.raffaeleconforti.bpmn.util.BPMNCleaner;
import com.raffaeleconforti.bpmn.util.BPMNSimplifier;
import com.raffaeleconforti.bpmnminer.exception.ExecutionCancelledException;
import com.raffaeleconforti.bpmnminer.subprocessminer.selection.SelectMinerResult;
import com.raffaeleconforti.conversion.heuristicsnet.HNNetToBPMNConverter;
import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.log.util.LogReaderClassic;
import org.deckfour.uitopia.api.event.TaskListener;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.log.LogFile;
import org.processmining.mining.heuristicsmining.HeuristicsNetResult;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.heuristics.HeuristicsNet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.plugins.IM;
import org.processmining.plugins.InductiveMiner.plugins.IMPetriNet;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.plugins.heuristicsnet.miner.heuristics.converter.HeuristicsNetToPetriNetConverter;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.FlexibleHeuristicsMinerPlugin;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.LogUtility;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.gui.ParametersPanel;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.settings.HeuristicsMinerSettings;
import org.processmining.plugins.ilpminer.ILPMinerSettings;
import org.processmining.plugins.log.logabstraction.BasicLogRelations;
import com.raffaeleconforti.alphaminer.AlphaMiner;
import com.raffaeleconforti.heuristicsminer.HeuristicsMiner;
import com.raffaeleconforti.ilpminer.ILPMiner;

import java.util.concurrent.ExecutionException;


/**
 * Created by Raffaele Conforti on 20/02/14.
 */
public class BPMNMiner {

    public BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, String startEndEventPreName, int selectedAlgorithm, boolean clean, boolean commandline) throws ExecutionCancelledException {
        BPMNDiagram result = null;
        PetriNetToBPMNConverter converter = new PetriNetToBPMNConverter();

        Object[] miningResult = null;

        if (selectedAlgorithm == SelectMinerResult.ALPHAPOS) {
            try {
                // The following gathers information required by the Alpha miner
                XLogInfo logInfo = XLogInfoFactory.createLogInfo(log);
                BasicLogRelations basicLogRelations = new BasicLogRelations(log);

                // Call the miner
                miningResult = new AlphaMiner().doAlphaMiningPrivateWithRelations(context, logInfo, basicLogRelations);
                result = PetriNetToBPMNConverter.convert((Petrinet) miningResult[0], (Marking) miningResult[1], miningResult.length == 3?(Marking) miningResult[2]:null, clean);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else if (selectedAlgorithm == SelectMinerResult.ILPPOS) {
            if(!commandline) {
                try {
                    miningResult = new ILPMiner().doILPMining(context, log);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                try {
                    ILPMiner miner = new ILPMiner();
                    ILPMinerSettings settings = miner.generateSetting(context, log);
                    miningResult = miner.doILPMiningWithSettings(context, log, XLogInfoFactory.createLogInfo(log), settings);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            result = PetriNetToBPMNConverter.convert((Petrinet) miningResult[0], (Marking) miningResult[1], miningResult.length == 3?(Marking) miningResult[2]:null, clean);
        } else if (selectedAlgorithm == SelectMinerResult.IMPOS) {
            if(!commandline) {
                IM inductiveMiner = new IM();
                miningResult = inductiveMiner.mineGuiPetrinet(context, log);

                result = PetriNetToBPMNConverter.convert((Petrinet) miningResult[0], (Marking) miningResult[1], miningResult.length == 3?(Marking) miningResult[2]:null, clean);
            }else {
                IMMiningDialog miningDialog = new IMMiningDialog(log);
                TaskListener.InteractionResult guiResult = context.showWizard("Mine using Inductive Miner", true, true, miningDialog);
                if (guiResult == TaskListener.InteractionResult.CANCEL) {
                    throw new ExecutionCancelledException();
                }
                MiningParameters miningParameters = miningDialog.getMiningParameters();
                IMPetriNet miner = new IMPetriNet();
                miningResult = miner.minePetriNetParameters(context, log, miningParameters);

                result = PetriNetToBPMNConverter.convert((Petrinet) miningResult[0], (Marking) miningResult[1], miningResult.length == 3?(Marking) miningResult[2]:null, clean);
            }
        } else if (selectedAlgorithm == SelectMinerResult.HMWOPOS5) {
            HeuristicsMiner miner = new HeuristicsMiner();

            try {
                LogImporter.exportToFile("", "tmpLog.mxml.gz", log);
            } catch (Exception e) {
                e.printStackTrace();
            }

            LogFile lf = LogFile.getInstance("tmpLog.mxml.gz");

            HeuristicsNetResult hNet = null;
            try {
                hNet = (HeuristicsNetResult) miner.mine(LogReaderClassic.createInstance(null, lf), false);
            } catch (Exception e) {
                e.printStackTrace();
            }

            result = HNNetToBPMNConverter.convert(hNet.getHeuriticsNet());

        } else if (selectedAlgorithm == SelectMinerResult.HMWPOS5) {
            HeuristicsMiner miner = new HeuristicsMiner();

            try {
                LogImporter.exportToFile("", "tmpLog.mxml.gz", log);
            } catch (Exception e) {
                e.printStackTrace();
            }

            LogFile lf = LogFile.getInstance("tmpLog.mxml.gz");

            HeuristicsNetResult hNet = null;
            try {
                hNet = (HeuristicsNetResult) miner.mine(LogReaderClassic.createInstance(null, lf), true);
            } catch (Exception e) {
                e.printStackTrace();
            }

            result = HNNetToBPMNConverter.convert(hNet.getHeuriticsNet());

        } else if (selectedAlgorithm == SelectMinerResult.HMPOS6) {
            if(!commandline) {
                ParametersPanel parameters = new ParametersPanel(LogUtility.getEventClassifiers(log));
                parameters.removeAndThreshold();

                context.showConfiguration("Heuristics Miner Parameters", parameters);
                HeuristicsMinerSettings settings = parameters.getSettings();


                HeuristicsNet heuristicsNet = FlexibleHeuristicsMinerPlugin.run(context, log, settings);
                Object[] petriNetResult = HeuristicsNetToPetriNetConverter.converter(context, heuristicsNet);

                result = PetriNetToBPMNConverter.convert((Petrinet) petriNetResult[0], (Marking) petriNetResult[1], petriNetResult.length == 3?(Marking) petriNetResult[2]:null, clean);
            }else {
                ParametersPanel parameters = new ParametersPanel(LogUtility.getEventClassifiers(log));
                parameters.removeAndThreshold();

                context.showConfiguration("Heuristics Miner Parameters", parameters);
                HeuristicsMinerSettings settings = parameters.getSettings();

                HeuristicsNet heuristicsNet = FlexibleHeuristicsMinerPlugin.run(context, log, settings);
                Object[] petriNetResult = HeuristicsNetToPetriNetConverter.converter(context, heuristicsNet);

                result = PetriNetToBPMNConverter.convert((Petrinet) petriNetResult[0], (Marking) petriNetResult[1], petriNetResult.length == 3?(Marking) petriNetResult[2]:null, clean);
            }
        }
        result = BPMNCleaner.clean(result);
        result = BPMNSimplifier.renameStartAndEndEvents(result, startEndEventPreName);
        result = BPMNSimplifier.renameGateways(result, startEndEventPreName);
        result = BPMNSimplifier.simplify(result);

        return result;
    }
}
