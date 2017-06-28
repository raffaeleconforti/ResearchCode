package com.raffaeleconforti.impl;

import com.raffaeleconforti.conversion.petrinet.PetriNetToBPMNConverter;
import com.raffaeleconforti.wrapper.MiningAlgorithm;
import com.raffaeleconforti.wrapper.settings.MiningSettings;
import com.raffaeleconforti.wrapper.PetrinetWithMarking;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.flexiblemodel.Flex;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.core.CNMining;
import org.processmining.plugins.core.Settings;
import org.processmining.plugins.flex.converter.PNFromFlex;

/**
 * Created by Adriano on 7/12/2016.
 */
public class CNMinerWrapper implements MiningAlgorithm {
    @Override
    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log, boolean structure, MiningSettings params) {
        PetrinetWithMarking petrinet = null;
        CNMining cnminer;

        cnminer = new CNMining();

        try {
            Settings s = new Settings();
            s.setConstraintsEnabled(false);
            s.setConstr_file_name(null);
            s.setSigmaLogNoise((double)5 / 100.0D);
            s.setFallFactor((double)90 / 100.0D);
            s.setRelativeToBest((double)75 / 100.0D);
            s.setLogName("logname");

            Flex cnet;
            Object result[];

            result = cnminer.startCNMining(context, log, s);
            System.out.println("ERROR - CN Miner: Got Result");

            if( result[0] instanceof Flex ) {
                cnet = (Flex) result[0];
                PNFromFlex converter = new PNFromFlex();
                result = converter.convertToPN(context, cnet);
                if( (result[0] instanceof Petrinet) && (result[1] instanceof Marking) ) petrinet = new PetrinetWithMarking((Petrinet)result[0], (Marking)result[1]);
            }

        } catch (Exception e) {
            System.out.println("ERROR - CN Miner failed");
            return petrinet;
        }

        return petrinet;
    }

    @Override
    public BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, boolean structure, MiningSettings params) {
        PetrinetWithMarking petrinetWithMarking = minePetrinet(context, log, structure, params);
        return PetriNetToBPMNConverter.convert(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking(), true);
    }

    @Override
    public String getAlgorithmName() {
        return "CN Miner";
    }

    @Override
    public String getAcronym() { return "CNM";}
}
