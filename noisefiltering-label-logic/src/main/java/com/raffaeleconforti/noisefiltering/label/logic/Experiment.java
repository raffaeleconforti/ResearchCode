package com.raffaeleconforti.noisefiltering.label.logic;

import au.edu.qut.petrinet.tools.SoundnessChecker;
import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.log.util.LogAnalyser;
import com.raffaeleconforti.log.util.LogCloner;
import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.marking.MarkingDiscoverer;
import com.raffaeleconforti.measurements.Measure;
import com.raffaeleconforti.measurements.MeasurementAlgorithm;
import com.raffaeleconforti.measurements.impl.AlignmentBasedFMeasure;
import com.raffaeleconforti.measurements.impl.ProjectedFMeasure;
import com.raffaeleconforti.soundnesschecker.CheckRelaxedSoundnessWithLola;
import com.raffaeleconforti.wrappers.MiningAlgorithm;
import com.raffaeleconforti.wrappers.PetrinetWithMarking;
import com.raffaeleconforti.wrappers.impl.SplitMinerWrapper;
import com.raffaeleconforti.wrappers.impl.heuristics.HeuristicsAlgorithmWrapper;
import com.raffaeleconforti.wrappers.impl.inductive.InductiveMinerIMfWrapper;
import com.raffaeleconforti.wrappers.settings.MiningSettings;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.model.XLog;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.json.JSONException;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;

import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 18/10/2016.
 */
public class Experiment {
    private String extLocation;
    private Map<String, XLog> logs;
    private Map<String, Object> logsInput;
    private XLog refLog;
    private String refLogName;
    private boolean relaxedSoundness = false;

    private Set<String> packages = new UnifiedSet<>();

    public static void main(String[] args) throws Exception {
        Experiment experiment = new Experiment();
        experiment.performBenchmark();
    }

    public Experiment() {
        this.extLocation = "/Volumes/Data/SharedFolder/Logs/Label";
    }

    public void performBenchmark() throws Exception {
        System.out.println("DEBUG - running benchmark ...");
        loadLogs();
        performBenchmarkFromLogInput(packages, logsInput);
    }

    private boolean isSelectedMeasurementAlgorithm(MeasurementAlgorithm measurementAlgorithm) {
        return measurementAlgorithm instanceof AlignmentBasedFMeasure;
//        if (measurementAlgorithm instanceof ProjectedFMeasure) return true;
    }

    private boolean isSelectedMiningAlgorithm(MiningAlgorithm miningAlgorithm) {
//        if(miningAlgorithm instanceof HeuristicsAlgorithmWrapper) return true;
        return miningAlgorithm instanceof InductiveMinerIMfWrapper;
//        if(miningAlgorithm instanceof SplitMinerWrapper) return true;
    }

    private boolean isSelectedLog(String logName) {
//        if(logName.contains("Sepsis")) return false;
//        if(logName.contains("Road")) return false;
//        if(logName.contains("BPI2017")) return false;
//        if(logName.contains("2015-5")) return false;
//        if(logName.contains("2015-4")) return false;
//        if(logName.contains("2015-3")) return false;
//        if(logName.contains("2015-2")) return false;
//        if(logName.contains("2015-1")) return false;
//        if(logName.contains("2014")) return false;
//        if(logName.contains("2014 (SimpleFilter 95")) return false;
//        if(logName.contains("2013_i")) return false;
//        if(logName.contains("2013_cp")) return false;
//        if(!logName.contains("2012")) return false;
        return logName.contains("(Python)");
    }

    private void performBenchmarkFromLogInput(Set<String> packages, Map<String, Object> logsInput) throws Exception {
//        XEventClassifier xEventClassifier = new XEventAndClassifier(new XEventNameClassifier());
        XEventClassifier xEventClassifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
        FakePluginContext fakePluginContext = new FakePluginContext();

        /* retrieving all the mining algorithms */
        List<MiningAlgorithm> miningAlgorithms = new ArrayList<>();
        miningAlgorithms.add(new HeuristicsAlgorithmWrapper());
        miningAlgorithms.add(new InductiveMinerIMfWrapper());
        miningAlgorithms.add(new SplitMinerWrapper());

        /* retrieving all the measuring algorithms */
        List<MeasurementAlgorithm> measurementAlgorithms = new ArrayList<>();
        measurementAlgorithms.add(new AlignmentBasedFMeasure());
        measurementAlgorithms.add(new ProjectedFMeasure());

        /* populating measurements results */
        XLog log;
        LogCloner logCloner = new LogCloner(new XFactoryNaiveImpl());
        String[] lognames = logsInput.keySet().toArray(new String[logsInput.size()]);
        Arrays.sort(lognames);
        for(int i = lognames.length - 1; i >= 0; i--) {
            String logName = lognames[i];
            if(!isSelectedLog(logName)) continue;

            XLog rawlog = loadLog(logsInput.get(logName));

            String original = retrieveLogOriginalName(logName);
            refLog = retrieveRefLog(original);

            int labels = LogAnalyser.getUniqueActivities(rawlog, xEventClassifier).size();
            System.out.println("DEBUG - measuring on log: " + logName + " " + labels);

            for (MiningAlgorithm miningAlgorithm : miningAlgorithms) {
                if(isSelectedMiningAlgorithm(miningAlgorithm)) {

                    log = logCloner.cloneLog(rawlog);
                    String miningAlgorithmName = miningAlgorithm.getAlgorithmName();
                    String measurementAlgorithmName = "NULL";

                    try {
                        System.out.println("DEBUG - measuring on mining algorithm: " + miningAlgorithmName);
                        long sTime = System.currentTimeMillis();

                        MiningSettings miningSettings = new MiningSettings();
//                        miningSettings.setParam("noiseThresholdIMf", 0F);

                        ProcessTree processTree = getProcessTree(miningAlgorithm, fakePluginContext, log, miningSettings, xEventClassifier);
                        PetrinetWithMarking petrinetWithMarking = getPetrinetWithMarking(miningAlgorithm, processTree, fakePluginContext, log, miningSettings, xEventClassifier);
                        AcceptingPetriNet acceptingPetriNet = getAcceptingPetriNet(petrinetWithMarking);

//                        ExportAcceptingPetriNetPlugin exportAcceptingPetriNetPlugin = new ExportAcceptingPetriNetPlugin();
//                        exportAcceptingPetriNetPlugin.export(
//                                fakePluginContext,
//                                acceptingPetriNet,
//                                new File(extLocation + "/" + logName + "-" + miningAlgorithmName + ".pnml"));

//                        if(!isSound(acceptingPetriNet, relaxedSoundness)) continue;

                        for (MeasurementAlgorithm measurementAlgorithm : measurementAlgorithms) {
                            if (isSelectedMeasurementAlgorithm(measurementAlgorithm)) {
                                measurementAlgorithmName = measurementAlgorithm.getMeasurementName();
                                System.out.println("DEBUG - measuring: " + measurementAlgorithmName);

                                Measure measure;
                                if(measurementAlgorithm instanceof ProjectedFMeasure && miningAlgorithm.canMineProcessTree()) {
                                    measure = measurementAlgorithm.computeMeasurement(fakePluginContext, xEventClassifier,
                                            processTree, miningAlgorithm, refLog);
                                }else {
                                    measure = measurementAlgorithm.computeMeasurement(fakePluginContext, xEventClassifier,
                                            petrinetWithMarking, miningAlgorithm, refLog);
                                }

                                System.out.println("DEBUG - " + measurementAlgorithmName + measure);
                            }
                        }

                    } catch (Exception e) {
                        System.out.println("ERROR - [mining algorithm : measurement algorithm] > [" + miningAlgorithmName + " : " + measurementAlgorithmName + "]");
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private boolean isSound(AcceptingPetriNet acceptingPetriNet, boolean relaxedSoundness) throws JSONException, ConnectionCannotBeObtained {
        System.out.print("Checking Soundness...");
        SoundnessChecker checker = new SoundnessChecker(acceptingPetriNet.getNet());
        if(relaxedSoundness) {
            if (!CheckRelaxedSoundnessWithLola.isRelaxedSoundAndBounded(acceptingPetriNet)) {
                System.out.println("Unbounded or Non-RelaxedSound");
                System.out.println("DEBUG - Unbounded or Non-RelaxedSound");
                System.out.println("Projected Recall : N.A.");
                System.out.println("Projected Precision : N.A.");
                System.out.println("Projected f-Measure : N.A.");
                System.out.println();
                return false;
            }else {
                System.out.println("RelaxedSound and Bounded");
                return true;
            }
        }else {
            if (!checker.isSound()) {
                System.out.println("Unsound");
                System.out.println("DEBUG - Unsound");
                System.out.println("Projected Recall : N.A.");
                System.out.println("Projected Precision : N.A.");
                System.out.println("Projected f-Measure : N.A.");
                System.out.println();
                return false;
            }else {
                System.out.println("Sound");
                return true;
            }
        }
    }

    private AcceptingPetriNet getAcceptingPetriNet(PetrinetWithMarking petrinetWithMarking) {
        if(petrinetWithMarking.getFinalMarkings().size() > 1) return new AcceptingPetriNetImpl(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarkings());
        else return new AcceptingPetriNetImpl(petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking(), petrinetWithMarking.getFinalMarking());
    }

    private PetrinetWithMarking getPetrinetWithMarking(MiningAlgorithm miningAlgorithm, ProcessTree processTree, FakePluginContext fakePluginContext, XLog log, MiningSettings miningSettings, XEventClassifier xEventClassifier) {
        if(miningAlgorithm.canMineProcessTree()) {
            ProcessTree2Petrinet.PetrinetWithMarkings pn = null;

            try {
                pn = ProcessTree2Petrinet.convert(processTree);
            } catch (ProcessTree2Petrinet.NotYetImplementedException var6) {
                var6.printStackTrace();
            } catch (ProcessTree2Petrinet.InvalidProcessTreeException var7) {
                var7.printStackTrace();
            }

            MarkingDiscoverer.createInitialMarkingConnection(fakePluginContext, pn.petrinet, pn.initialMarking);
            MarkingDiscoverer.createFinalMarkingConnection(fakePluginContext, pn.petrinet, pn.finalMarking);
            return new PetrinetWithMarking(pn.petrinet, pn.initialMarking, pn.finalMarking);

        } else{
            return miningAlgorithm.minePetrinet(fakePluginContext, log, false, miningSettings, xEventClassifier);
        }
    }

    private ProcessTree getProcessTree(MiningAlgorithm miningAlgorithm, FakePluginContext fakePluginContext, XLog log, MiningSettings miningSettings, XEventClassifier xEventClassifier) {
        if(miningAlgorithm.canMineProcessTree()) return miningAlgorithm.mineProcessTree(fakePluginContext, log, false, miningSettings, xEventClassifier);
        return null;
    }

    private String retrieveLogOriginalName(String logName) {
        if(logName.contains(" ")) return logName.substring(0, logName.indexOf(" ")) + ".xes.gz";
        else return logName;
    }

    private XLog retrieveRefLog(String original) {
        String newRefLogName = extLocation + "/" + original;
        if(refLogName == null || !refLogName.equals(newRefLogName)) {
            this.refLog = LogImporter.importFromFile(new XFactoryNaiveImpl(), extLocation + "/" + original);
        }
        return refLog;
    }

    private void loadLogs() {
        logsInput = new UnifiedMap<>();
        String logName;
        try {
            /* checking if the user wants to upload also external logs */
            if( extLocation != null ) {
                System.out.println("DEBUG - importing external logs.");
                File folder = new File(extLocation);
                File[] listOfFiles = folder.listFiles();
                if( folder.isDirectory() ) {
                    for( File file : listOfFiles )
                        if( file.isFile() ) {
                            logName = file.getPath();
                            System.out.println("DEBUG - name: " + logName);
                            logsInput.put(file.getName(), logName);
                        }
                } else {
                    System.out.println("ERROR - external logs loading failed, input path is not a folder.");
                }
            }
        } catch( Exception e ) {
            System.out.println("ERROR - something went wrong reading the resource folder: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private XLog loadLog(Object o) {
        try {
            if(o instanceof String) {
                return LogImporter.importFromFile(new XFactoryNaiveImpl(), (String) o);
            }else if(o instanceof InputStream){
                return LogImporter.importFromInputStream((InputStream) o, new XesXmlGZIPParser(new XFactoryNaiveImpl()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
