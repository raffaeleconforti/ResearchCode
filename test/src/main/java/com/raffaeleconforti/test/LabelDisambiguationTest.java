package com.raffaeleconforti.test;

import com.raffaeleconforti.benchmark.logic.MiningAlgorithmDiscoverer;
import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.measurements.impl.CFCComplexity;
import com.raffaeleconforti.measurements.impl.SizeComplexity;
import com.raffaeleconforti.wrappers.MiningAlgorithm;
import com.raffaeleconforti.wrappers.PetrinetWithMarking;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.acceptingpetrinet.plugins.ExportAcceptingPetriNetPlugin;
import org.processmining.acceptingpetrinet.plugins.ImportAcceptingPetriNetPlugin;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.inductiveminer2.efficienttree.EfficientTreeReduceParametersDuplicates;
import org.processmining.projectedrecallandprecision.framework.CompareParameters;
import org.processmining.projectedrecallandprecision.plugins.CompareLog2PetriNetPlugin;
import org.processmining.projectedrecallandprecision.result.ProjectedRecallPrecisionResult;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 30/6/17.
 */
public class LabelDisambiguationTest {

    private static XEventClassifier xEventClassifier = new XEventNameClassifier();

    //Graph
    public static void main(String[] args) {
        String path = "/Users/conforti/Downloads/Andreas Logs/";

        String[] typeAlgorithms = new String[] {"" +
//                "Cortadella-Precomputed",
//                "DeSanPedro-Cortadella",
                "Fodina",
                "NoDeduplication",
//                "OurMethod-1-best",
//                "OurMethod-2-best",
                "OurMethod-10-best",
//                "OurMethod-42-best",
//                "OurMethod-complete",
                "Xixi",
                "Xixi-Adaptive"
        };

        String[] logNames = new String[] {
                "BPIC2012",
//                "BPIC2013_i",
                "BPIC2013_op",
                "BPIC2014 (NoiseFiltered)",
                "BPIC2015 Municipality1 (NoiseFiltered)",
                "BPIC2015 Municipality2 (NoiseFiltered)",
                "BPIC2015 Municipality3 (NoiseFiltered)",
                "BPIC2015 Municipality4 (NoiseFiltered)",
                "BPIC2015 Municipality5 (NoiseFiltered)",
                "BPIC2017 - Loan Application (NoiseFilter)",
                "RTFMP",
//                "Sepsis Cases"
        };

        Set<String> packages = new UnifiedSet<>();
        List<MiningAlgorithm> miningAlgorithms = MiningAlgorithmDiscoverer.discoverAlgorithms(packages);
        Collections.sort(miningAlgorithms, new Comparator<MiningAlgorithm>() {
            @Override
            public int compare(MiningAlgorithm o1, MiningAlgorithm o2) {
                return o2.getAlgorithmName().compareTo(o1.getAlgorithmName());
            }
        });
        Iterator<MiningAlgorithm> iterator = miningAlgorithms.iterator();
        while (iterator.hasNext()) {
            MiningAlgorithm miningAlgorithm = iterator.next();
            if(!miningAlgorithm.getAcronym().equals("HM6")) iterator.remove();
            else

            if(miningAlgorithm.getAcronym().equals("sHM6")) iterator.remove();
            else if(miningAlgorithm.getAcronym().equals("SM")) iterator.remove();
            else if(miningAlgorithm.getAcronym().equals("sHM")) iterator.remove();
            else if(miningAlgorithm.getAcronym().startsWith("HPO")) iterator.remove();
//            else if(miningAlgorithm.getAcronym().startsWith("IM") && !miningAlgorithm.getAcronym().equals("IMf")) iterator.remove();
            else if(miningAlgorithm.getAcronym().startsWith("IM")) iterator.remove();
            else if(miningAlgorithm.getAcronym().equals("ILP")) iterator.remove();
            else if(miningAlgorithm.getAcronym().equals("HILP")) iterator.remove();
            else if(miningAlgorithm.getAcronym().equals("HM")) iterator.remove();
            else if(miningAlgorithm.getAcronym().equals("HM$")) iterator.remove();
            else if(miningAlgorithm.getAcronym().equals("ETM")) iterator.remove();
            else if(miningAlgorithm.getAcronym().equals("BPMNMiner")) iterator.remove();
            else if(miningAlgorithm.getAcronym().equals("A#")) iterator.remove();
            else if(miningAlgorithm.getAcronym().equals("A+")) iterator.remove();
            else if(miningAlgorithm.getAcronym().equals("A++")) iterator.remove();
            else if(miningAlgorithm.getAcronym().equals("AM")) iterator.remove();
            else if(miningAlgorithm.getAcronym().equals("A$")) iterator.remove();
            else if(miningAlgorithm.getAcronym().equals("AA")) iterator.remove();
        }

        FakePluginContext fakePluginContext = new FakePluginContext();
        XEventClassifier classifier = new XEventNameClassifier();

        CompareParameters parameters = new CompareParameters(2);
        parameters.setTreeReduceParameters(new EfficientTreeReduceParametersDuplicates(false));
        parameters.setDebug(false);
        parameters.setClassifier(classifier);

        ExportAcceptingPetriNetPlugin exportAcceptingPetriNetPlugin = new ExportAcceptingPetriNetPlugin();
        ImportAcceptingPetriNetPlugin importAcceptingPetriNetPlugin = new ImportAcceptingPetriNetPlugin();
        SizeComplexity sizeComplexity = new SizeComplexity();
        CFCComplexity cfcComplexity = new CFCComplexity();

        long time = 1800000;
        for (MiningAlgorithm miningAlgorithm : miningAlgorithms) {
            for(String logName : logNames) {
                XLog originalLog = null;
                try {

                    String originalLogPath = getLogs(logName, path + "NoDeduplication/candidates/")[0].getAbsolutePath();
                    originalLog = LogImporter.importFromFile(new XFactoryNaiveImpl(), originalLogPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                for(String typeAlgorithm : typeAlgorithms) {
                    for(File file : getLogs(logName, path + typeAlgorithm + "/candidates/" )) {
                        double size = 0;
                        double cfc = 0;
                        long t3 = 0;
                        long t6 = 0;
                        try {
                            File f = new File("Models/" + typeAlgorithm + "-" + miningAlgorithm.getAlgorithmName() + "-" + file.getName() + ".pnml");
                            if (f.exists()) continue;

                            XLog log = LogImporter.importFromFile(new XFactoryNaiveImpl(), file.getAbsolutePath());

                            long t1 = System.currentTimeMillis();
                            PetrinetWithMarking petrinetWithMarking = miningAlgorithm.minePetrinet(fakePluginContext, log, false, null, xEventClassifier);
                            long t2 = System.currentTimeMillis();

                            AcceptingPetriNet acceptingPetriNet;
                            if(petrinetWithMarking.getFinalMarkings().size() > 1) {
                                acceptingPetriNet = new AcceptingPetriNetImpl(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarkings());
                            }else {
                                acceptingPetriNet = new AcceptingPetriNetImpl(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking());
                            }

                            for (Transition transition : acceptingPetriNet.getNet().getTransitions()) {
                                if (transition.getLabel().endsWith("_")) {
                                    transition.getAttributeMap().put(AttributeMap.LABEL, transition.getLabel().substring(0, transition.getLabel().length() - 1));
                                } else if (transition.getLabel().contains("_")) {
                                    transition.getAttributeMap().put(AttributeMap.LABEL, transition.getLabel().substring(0, transition.getLabel().lastIndexOf("_")));
                                }
                            }

                            exportAcceptingPetriNetPlugin.export(fakePluginContext, acceptingPetriNet, f);
                            acceptingPetriNet = (AcceptingPetriNet) importAcceptingPetriNetPlugin.importFile(fakePluginContext, f);

                            t3 = (t2 - t1);
                            long totalTime = time - t3;
                            if (totalTime <= 0) {
                                System.out.println(typeAlgorithm + "," + miningAlgorithm.getAlgorithmName() + "," + file.getName() + ",T.O,T.O,T.O,T.O,T.O" + t3 + ",N.A");
                                continue;
                            }

                            TimerCanceller canceller = new TimerCanceller(totalTime);

                            long t4 = System.currentTimeMillis();
                            canceller.start();
                            ProjectedRecallPrecisionResult result = CompareLog2PetriNetPlugin.measure(originalLog, acceptingPetriNet, parameters, canceller);
                            long t5 = System.currentTimeMillis();
                            t6 = (t5 - t4);
                            long t7 = t3 + t6;


                            PetrinetWithMarking net = new PetrinetWithMarking(acceptingPetriNet.getNet(), acceptingPetriNet.getInitialMarking(), acceptingPetriNet.getFinalMarkings().iterator().next());
                            size = Double.parseDouble(sizeComplexity.computeMeasurement(fakePluginContext, classifier, net, null, null).getMetricValue("size"));
                            cfc = Double.parseDouble(cfcComplexity.computeMeasurement(fakePluginContext, classifier, net, null, null).getMetricValue("cfc"));

                            if (t7 <= time) {
                                double recall = result.getRecall();
                                double precision = result.getPrecision();
                                double fscore = 2 * (recall * precision) / (recall + precision);
                                System.out.println(typeAlgorithm + "," + miningAlgorithm.getAlgorithmName() + "," + file.getName() + "," + recall + "," + precision + "," + fscore + "," + size + "," + cfc + "," + t3 + "," + t6);
                            } else {
                                System.out.println(typeAlgorithm + "," + miningAlgorithm.getAlgorithmName() + "," + file.getName() + ",T.O,T.O,T.O," + size + "," + cfc + ","  + t3 + "," + t6);
                            }
                        } catch (Exception e) {
                            System.out.println(typeAlgorithm + "," + miningAlgorithm.getAlgorithmName() + "," + file.getName() + ",N.A,N.A,N.A," + size + "," + cfc + ","  + t3 + "," + t6);
                        }
                    }
                }
            }
        }

//        for (MiningAlgorithm miningAlgorithm : miningAlgorithms) {
//            for(String logName : logNames) {
//                XLog originalLog = null;
//                try {
//
//                    String originalLogPath = getLogs(logName, path + "NoDeduplication/candidates/")[0].getAbsolutePath();
//                    originalLog = LogImporter.importFromFile(new XFactoryNaiveImpl(), originalLogPath);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//
//                for(String typeAlgorithm : typeAlgorithms) {
//                    for(File file : getLogs(logName, path + typeAlgorithm + "/candidates/" )) {
//                        try {
//                            File f = new File("Models/" + typeAlgorithm + "-" + miningAlgorithm.getAlgorithmName() + "-" + file.getName() + ".pnml");
//                            if (f.exists()) continue;
//
//                            XLog log = LogImporter.importFromFile(new XFactoryNaiveImpl(), file.getAbsolutePath());
//
//                            long t1 = System.currentTimeMillis();
//                            PetrinetWithMarking petrinetWithMarking = miningAlgorithm.minePetrinet(fakePluginContext, log, false, null);
//                            long t2 = System.currentTimeMillis();
//
//                            AcceptingPetriNet acceptingPetriNet = new AcceptingPetriNetImpl(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking());
//
//                            for (Transition transition : acceptingPetriNet.getNet().getTransitions()) {
//                                if (transition.getLabel().endsWith("_")) {
//                                    transition.getAttributeMap().put(AttributeMap.LABEL, transition.getLabel().substring(0, transition.getLabel().length() - 1));
//                                } else if (transition.getLabel().contains("_")) {
//                                    transition.getAttributeMap().put(AttributeMap.LABEL, transition.getLabel().substring(0, transition.getLabel().lastIndexOf("_")));
//                                }
//                            }
//
//                            exportAcceptingPetriNetPlugin.export(fakePluginContext, acceptingPetriNet, f);
//                            acceptingPetriNet = (AcceptingPetriNet) importAcceptingPetriNetPlugin.importFile(fakePluginContext, f);
//
//                            long t3 = (t2 - t1);
//                            long totalTime = time - t3;
//                            if (totalTime <= 0) {
//                                System.out.println(typeAlgorithm + "," + miningAlgorithm.getAlgorithmName() + "," + file.getName() + ",T.O,T.O,T.O,T.O,T.O" + t3 + ",N.A");
//                                continue;
//                            }
//
//                            PetrinetWithMarking net = new PetrinetWithMarking(acceptingPetriNet.getNet(), acceptingPetriNet.getInitialMarking(), acceptingPetriNet.getFinalMarkings().iterator().next());
//                            double size = Double.parseDouble(sizeComplexity.computeMeasurement(fakePluginContext, classifier, net, null, null).getMetricValue("size"));
//                            double cfc = Double.parseDouble(cfcComplexity.computeMeasurement(fakePluginContext, classifier, net, null, null).getMetricValue("cfc"));
//
//                            System.out.println(typeAlgorithm + "," + miningAlgorithm.getAlgorithmName() + "," + file.getName() + ",O.M,O.M,O.M," + size + "," + cfc + "," + t3 + ",O.M");
//                        } catch (Exception e) {
//                            System.out.println(typeAlgorithm + "," + miningAlgorithm.getAlgorithmName() + "," + file.getName() + ",N.A,N.A,N.A,N.A,N.A,N.A,N.A");
//                        }
//                    }
//                }
//            }
//        }
    }

    private static File[] getLogs(String filename, String pathname) {
        File dir = new File(pathname);
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(filename) && name.endsWith("xes.gz");
            }
        });

        return files;
    }

    private static class TimerCanceller extends Thread implements ProMCanceller {

        private long duration;
        private AtomicBoolean cancelled;

        public TimerCanceller(long duration) {
            this.duration = duration;
            this.cancelled = new AtomicBoolean(false);
        }

        @Override
        public boolean isCancelled() {
            return cancelled.get();
        }

        @Override
        public void run() {
            try {
                sleep(duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cancelled.set(true);
        }
    }

}
