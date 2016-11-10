package com.raffaeleconforti.wrapper;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 9/11/16.
 */
public class InterruptingMiningAlgorithm {

    private MiningAlgorithm miningAlgorithm;
    private long timeout;

    public InterruptingMiningAlgorithm(MiningAlgorithm miningAlgorithm, long timeout) {
        this.miningAlgorithm = miningAlgorithm;
        this.timeout = timeout;
    }

    public PetrinetWithMarking minePetrinet(UIPluginContext context, XLog log, boolean structure) {
        final PetrinetWithMarking[] petrinetWithMarking = new PetrinetWithMarking[1];

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    petrinetWithMarking[0] = miningAlgorithm.minePetrinet(context, log, structure);
                } catch (Exception e) {

                }
            }
        };
        Thread t = new Thread(runnable);
        t.start();

        long time = 0;
        try {
            while(time < timeout && t.isAlive()) {
                Thread.currentThread().sleep(100);
                time += 100;
            }
            if (t.isAlive()) {
                t.interrupt();
                System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
                System.out.println(getAlgorithmName() + " - Timeout Reached!");
            }
        } catch (InterruptedException e) {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
            System.out.println(getAlgorithmName() + " - Timeout Reached!");
        }

        return petrinetWithMarking[0];
    }

    public BPMNDiagram mineBPMNDiagram(UIPluginContext context, XLog log, boolean structure) {
        return miningAlgorithm.mineBPMNDiagram(context, log, structure);
    }

    public String getAlgorithmName() {
        return miningAlgorithm.getAlgorithmName();
    }
}
