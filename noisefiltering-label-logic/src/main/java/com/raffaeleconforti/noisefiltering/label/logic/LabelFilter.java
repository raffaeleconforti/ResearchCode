package com.raffaeleconforti.noisefiltering.label.logic;

import com.raffaeleconforti.noisefiltering.label.logic.clustering.AbstractClustering;
import com.raffaeleconforti.noisefiltering.label.logic.clustering.centroid.KMedoidsEMFiltering;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

import java.util.*;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 14/11/16.
 */
public class LabelFilter {
    private static final XConceptExtension xce = XConceptExtension.instance();
    private final XEventClassifier xEventClassifier;

    private int label_removed;

    private final ObjectIntHashMap<String> stringToIntMap = new ObjectIntHashMap<>();
    private final IntObjectHashMap<String> intToStringMap = new IntObjectHashMap<>();

    private int events = 1;
    public static String testName = "(Python)";

    private static String logName;

    public static void main(String[] args) {
        XFactory factory = new XFactoryNaiveImpl();
//        String[] logNames = new String[] {"BPI2015-4"};
        String[] logNames = new String[] {"BPI2011", "BPI2012", "BPI2013_cp", "BPI2013_i", "BPI2014", "BPI2015-1", "BPI2015-2", "BPI2015-3", "BPI2015-4", "BPI2015-5", "BPI2017", "Road", "Sepsis"};
//        String[] logNames = new String[] {"BPI2012", "BPI2013_cp", "BPI2013_i", "BPI2014", "BPI2015-1", "BPI2015-2", "BPI2015-3", "BPI2015-4", "BPI2015-5", "BPI2017", "Road", "Sepsis"};

        for(int i = logNames.length - 1; i >= 0; i--) {
            logName = logNames[i];
            XLog log = LogImporter.importFromFile(factory, "/Volumes/Data/SharedFolder/Logs/Label/" + logName + ".xes.gz");
//            LogModifier logModifier = new LogModifier(factory, XConceptExtension.instance(), XTimeExtension.instance(), new LogOptimizer());
//            logModifier.insertArtificialStartAndEndEvent(log);

            LabelFilter labelFilter = new LabelFilter(
//                    new XEventAndClassifier(new XEventNameClassifier()));
                    new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier()));

            log = labelFilter.filterLog(log);

            xce.assignName(log, xce.extractName(log) + " (Label)");

//            if (labelFilter.label_removed > 0) {
//                log = logModifier.removeArtificialStartAndEndEvent(log);
                LogImporter.exportToFile("/Volumes/Data/SharedFolder/Logs/Label/" + logName + " " + testName + ".xes.gz", log);
//            }
        }
    }

    private List<IntArrayList> codeLog(XLog log) {
        List<IntArrayList> codedLog = new ArrayList<>(log.size());
        for(XTrace trace : log) {
            IntArrayList list = new IntArrayList(trace.size());
            for(XEvent event : trace) {
                String name = xEventClassifier.getClassIdentity(event);
                Integer value;
                if((value = stringToIntMap.get(name)) > 0) {
                    list.add(value);
                }else {
                    stringToIntMap.put(name, events);
                    intToStringMap.put(events, name);
                    list.add(events);
                    events++;
                }
            }
            codedLog.add(list);
        }
        return codedLog;
    }

    public LabelFilter(XEventClassifier xEventClassifier) {
        this.xEventClassifier = xEventClassifier;
    }

    public XLog filterLog(XLog log) {
        Map<String, Double> occurrence_in_log = new HashMap<>();
        Map<String, Double> unique_occurrence_in_traces = new HashMap<>();
        Map<String, Map<String, Double>> connections_in = new HashMap<>();
        Map<String, Map<String, Double>> connections_out = new HashMap<>();
        Map<String, Double> number_connections_in = new HashMap<>();
        Map<String, Double> number_connections_out = new HashMap<>();

        for(XTrace trace : log) {
            Set<String> set = new HashSet<>();
            for(int i = 0; i < trace.size(); i++) {
                String name = xEventClassifier.getClassIdentity(trace.get(i));
                set.add(name);
                Double count;
                if((count = occurrence_in_log.get(name)) == null) {
                    count = 0.0;
                }
                count++;
                occurrence_in_log.put(name, count);

                Map<String, Double> in_connected;
                Map<String, Double> out_connected;
                if((in_connected = connections_in.get(name)) == null) {
                    in_connected = new HashMap<>();
                }
                if((out_connected = connections_out.get(name)) == null) {
                    out_connected = new HashMap<>();
                }

                if(i > 0) {
                    Double val;
                    if((val = in_connected.get(xEventClassifier.getClassIdentity(trace.get(i - 1)))) == null) {
                        val = 0.0;
                    }
                    in_connected.put(xEventClassifier.getClassIdentity(trace.get(i - 1)), val + 1);
                }
                if(i < trace.size() - 1) {
                    Double val;
                    if((val = out_connected.get(xEventClassifier.getClassIdentity(trace.get(i + 1)))) == null) {
                        val = 0.0;
                    }
                    out_connected.put(xEventClassifier.getClassIdentity(trace.get(i + 1)), val + 1);
                }
                connections_in.put(name, in_connected);
                connections_out.put(name, out_connected);
            }
            for(String name : set) {
                Double count;
                if((count = unique_occurrence_in_traces.get(name)) == null) {
                    count = 0.0;
                }
                count++;
                unique_occurrence_in_traces.put(name, count);
            }
        }

        for(String name : connections_in.keySet()) {
            number_connections_in.put(name, (double) connections_in.get(name).size());
        }
        for(String name : connections_out.keySet()) {
            number_connections_out.put(name, (double) connections_out.get(name).size());
        }

        Set<String> toremove = new HashSet<>();

//        List<String> labels = filter(
//                normalize(occurrence_in_log)
////                ,
////                normalize(unique_occurrence_in_traces)
////                ,
////                inverse_normalize(number_connections_in)
////                ,
////                inverse_normalize(number_connections_out)
//        );

        String[] key_orig = connections_in.keySet().toArray(new String[connections_in.size()]);
//        double[][] data = new double[key.length][2 * key.length + 1];
////        double[][] data = new double[key.length][2 * key.length + 2];
//        for (int i = 0; i < data.length; i++) {
////            data[i][0] = occurrence_in_log.get(key[i]);
//            data[i][0] = unique_occurrence_in_traces.get(key[i]);
////            data[i][1] = unique_occurrence_in_traces.get(key[i]);
//            for (int j = 1; j < data.length; j++) {
////            for (int j = 2; j < data.length; j++) {
//                Double d = connections_in.get(key[i]).get(key[j]);
//                data[i][j] = d != null ? d : 0.0;
//
//                d = connections_out.get(key[i]).get(key[j]);
//                data[i][j + data.length] = d != null ? d : 0.0;
//            }
//        }
        int retained = 0;
        for (int i = 0; i < key_orig.length; i++) {
            if(occurrence_in_log.get(key_orig[i]) == 1 || unique_occurrence_in_traces.get(key_orig[i]) == 1) {
                toremove.add(key_orig[i]);
            }else if(number_connections_in.get(key_orig[i]) > 1 && number_connections_out.get(key_orig[i]) > 1) {
                retained++;
            }
        }

        String[] key = new String[retained];
        double[][] data = new double[retained][4];
        int i = 0;
        for (int j = 0; j < key_orig.length; j++) {
            if(i == key.length) break;
            key[i] = key_orig[j];

            data[i][0] = normalize(occurrence_in_log).get(key[i]);
            data[i][1] = normalize(unique_occurrence_in_traces).get(key[i]);

            Double d = number_connections_in.get(key[i]);
//            Double d = normalize(number_connections_in).get(key[i]);
            data[i][2] = d != null ? d : 0.0;

            d = number_connections_out.get(key[i]);
//            d = normalize(number_connections_out).get(key[i]);
            data[i][3] = d != null ? d : 0.0;

            if(occurrence_in_log.get(key[i]) > 1 && unique_occurrence_in_traces.get(key_orig[i]) > 1 && number_connections_in.get(key[i]) > 1 && number_connections_out.get(key[i]) > 1) {
                i++;
            }
        }

        List<String> labels = filter(
                key, data
        );
        toremove.addAll(labels);
        System.out.println(toremove);

        this.label_removed = toremove.size();
        XFactory factory = new XFactoryNaiveImpl();
        XLog filteredLog = factory.createLog(log.getAttributes());
        for(XTrace trace : log) {
            XTrace filteredTrace = factory.createTrace(trace.getAttributes());
            for(XEvent event : trace) {
                if (!toremove.contains(xEventClassifier.getClassIdentity(event))) {
                    filteredTrace.add(event);
                }
            }
            filteredLog.add(filteredTrace);
        }
        return filteredLog;
    }

    private List<String> filter(Map<String, Double>... maps) {
        String[] key = maps[0].keySet().toArray(new String[maps[0].size()]);
        double[][] data = new double[key.length][maps.length];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                data[i][j] = maps[j].get(key[i]);
            }
        }
        return filter(key, data);
    }

    private List<String> filter(String[] key, double[][] data) {

//        /* Connectivity-based clustering (connectivity clustering) */
//        List<FilteringResult> agnesOutliers = getOutliers(new AGNESFiltering(maps));
//        List<FilteringResult> clinkOutliers = getOutliers(new CLINKFiltering(maps));
//        List<FilteringResult> slinkOutliers = getOutliers(new SLINKFiltering(maps));
//
//        /* Centroid-based clustering (k-means clustering) */
//        List<FilteringResult> kMeansCompareOutliers = getOutliers(new KMeansCompareFiltering(maps));
//        List<FilteringResult> kMeansElkanOutliers = getOutliers(new KMeansElkanFiltering(maps));
//        List<FilteringResult> kMeansHamerlyOutliers = getOutliers(new KMeansHamerlyFiltering(maps));
//        List<FilteringResult> kMeansLloydOutliers = getOutliers(new KMeansLloydFiltering(maps));
//        List<FilteringResult> kMeansMacQueenOutliers = getOutliers(new KMeansMacQueenFiltering(maps));
//        List<FilteringResult> kMeansSortOutliers = getOutliers(new KMeansSortFiltering(maps));
//        List<FilteringResult> kMediansLloydOutliers = getOutliers(new KMediansLloydFiltering(maps));
//        List<FilteringResult> kMedoidsEMOutliers = getOutliers(new KMedoidsEMFiltering(maps));
//
//        /* Distribution-based clustering (EM clustering) */
//        List<FilteringResult> emDiagonalGaussianModelOutliers = getOutliers(new EMDiagonalGaussianModelFiltering(maps));
//        List<FilteringResult> emMultivariateGaussianModelOutliers = getOutliers(new EMMultivariateGaussianModelFiltering(maps));
//        List<FilteringResult> emSphericalGaussianModelOutliers = getOutliers(new EMSphericalGaussianModelFiltering(maps));
//
//        /* Density-based clustering (DBSCAN clustering) */
//        List<FilteringResult> fastOPTICSOutliers = getOutliers(new FastOPTICSFiltering(maps));
//        List<FilteringResult> opticsHeapOutlifers = getOutliers(new OPTICSHeapFiltering(maps));

        List<FilteringResult<String>> results = aggregateRsults(
//                getOutliers(new AGNESFiltering(key, data))
//                ,
//                getOutliers(new CLINKFiltering(key, data))
//                ,
//                getOutliers(new SLINKFiltering(key, data))
//                ,
//                getOutliers(new KMeansCompareFiltering(key, data))
//                ,
//                getOutliers(new KMeansElkanFiltering(key, data))
//                ,
//                getOutliers(new KMeansHamerlyFiltering(key, data))
//                ,
//                getOutliers(new KMeansLloydFiltering(key, data))
//                ,
//                getOutliers(new KMeansMacQueenFiltering(key, data))
//                ,
//                getOutliers(new KMeansSortFiltering(key, data))
//                ,
//                getOutliers(new KMediansLloydFiltering(key, data))
//                ,
                getOutliers(new KMedoidsEMFiltering(key, data, 2)),
                getOutliers(new KMedoidsEMFiltering(key, data, 3)),
                getOutliers(new KMedoidsEMFiltering(key, data, 4))
//                ,
//                getOutliers(new EMDiagonalGaussianModelFiltering(key, data))
//                ,
//                getOutliers(new EMMultivariateGaussianModelFiltering(key, data))
//                ,
//                getOutliers(new EMSphericalGaussianModelFiltering(key, data))
//                ,
//                getOutliers(new FastOPTICSFiltering(key, data))
//                ,
//                getOutliers(new OPTICSHeapFiltering(key, data))
        );
        List<String> outliers = getMajorityVote(results);
        System.out.println(outliers);
        return outliers;
    }

    private Map<String, Double> normalize(Map<String, Double> occurrence) {
        double occurrence_max = 0;
        double occurrence_min = Double.MAX_VALUE;
        for(String s : occurrence.keySet()) {
            occurrence_max = Math.max(occurrence.get(s), occurrence_max);
            occurrence_min = Math.min(occurrence.get(s), occurrence_min);
        }

        Map<String, Double> tmp_occurrence = new HashMap<>();
        double denominator = (occurrence_max - occurrence_min);
        for(String s : occurrence.keySet()) {
            double val = (occurrence.get(s) - occurrence_min) / denominator;
            tmp_occurrence.put(s, val);
        }
        return tmp_occurrence;
    }

    private Map<String, Double> inverse_normalize(Map<String, Double> occurrence) {
        double occurrence_max = 0;
        double occurrence_min = Double.MAX_VALUE;
        for(String s : occurrence.keySet()) {
            occurrence_max = Math.max(occurrence.get(s), occurrence_max);
            occurrence_min = Math.min(occurrence.get(s), occurrence_min);
        }

        Map<String, Double> tmp_occurrence = new HashMap<>();
        double denominator = (occurrence_max - occurrence_min);
        for(String s : occurrence.keySet()) {
            double val = (occurrence.get(s) - occurrence_min) / denominator;
            tmp_occurrence.put(s, 1 - val);
        }
        return tmp_occurrence;
    }

    private List<FilteringResult<String>> getOutliers(AbstractClustering<String> abstractClustering) {
        List<FilteringResult<String>> results = abstractClustering.getOutliers();
        if(!sanityCheck(results)) {
            System.out.println("Failed " + results.get(0).getTechnique());
        }
        return results;
    }

    private boolean sanityCheck(List<FilteringResult<String>>... outliers) {
        List<FilteringResult<String>> results = aggregateRsults(outliers);
        if(results.size() == 1) return true;
        
        List<String> o = getMajorityVoteSilent(results);
        for(List<FilteringResult<String>> filteringResults : outliers) {
            for(FilteringResult filteringResult : filteringResults) {
                if(!o.equals(filteringResult.getOutliers())) return true;
            }
        }
        return false;
    }

    private List<FilteringResult<String>> aggregateRsults(List<FilteringResult<String>>... outliers) {
        List<FilteringResult<String>> results = new ArrayList<>();

        for (int i = 0; i < outliers.length; i++) {
            results.addAll(outliers[i]);
        }

        return results;
    }

    private List<String> getMajorityVoteSilent(List<FilteringResult<String>> outliers) {
        List<String> selectedOutliers = new ArrayList<>();
        Map<String, Integer> candidatesOutliers = new HashMap<>();
        Map<String, List<String>> candidatesVotes = new HashMap<>();

        for(FilteringResult<String> filteringResult : outliers) {
            for(String candidateOutlier : filteringResult.getOutliers()) {
                Integer votes;
                List<String> names = new ArrayList<>();
                if((votes = candidatesOutliers.get(candidateOutlier)) == null) {
                    votes = 0;
                }else {
                    names = candidatesVotes.get(candidateOutlier);
                }
                votes++;
                names.add(filteringResult.getTechnique());
                candidatesOutliers.put(candidateOutlier, votes);
                candidatesVotes.put(candidateOutlier, names);
            }
        }

        for(String outlier : candidatesOutliers.keySet()) {
            if(candidatesOutliers.get(outlier) >= Math.ceil(2.0 * outliers.size() / 3.0)) selectedOutliers.add(outlier);
        }
        return selectedOutliers;
    }

    private List<String> getMajorityVote(List<FilteringResult<String>> outliers) {
        List<String> selectedOutliers = new ArrayList<>();
        Map<String, Integer> candidatesOutliers = new HashMap<>();
        Map<String, List<String>> candidatesVotes = new HashMap<>();

        for(FilteringResult<String> filteringResult : outliers) {
            for(String candidateOutlier : filteringResult.getOutliers()) {
                Integer votes;
                List<String> names = new ArrayList<>();
                if((votes = candidatesOutliers.get(candidateOutlier)) == null) {
                    votes = 0;
                }else {
                    names = candidatesVotes.get(candidateOutlier);
                }
                votes++;
                names.add(filteringResult.getTechnique());
                candidatesOutliers.put(candidateOutlier, votes);
                candidatesVotes.put(candidateOutlier, names);
            }
        }

        for(String outlier : candidatesOutliers.keySet()) {
            System.out.println(outlier + " " + candidatesOutliers.get(outlier) + "/" + outliers.size() + " required " + Math.ceil(2.0 * outliers.size() / 3.0));
            System.out.println("\t" + candidatesVotes.get(outlier));
            if(candidatesOutliers.get(outlier) >= Math.ceil(2.0 * outliers.size() / 3.0)) selectedOutliers.add(outlier);
        }
        return selectedOutliers;
    }
}
