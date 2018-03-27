package com.raffaeleconforti.dsm;

import com.raffaeleconforti.datastructures.multilevelmap.MultiLevelMap;
import com.raffaeleconforti.datastructures.multilevelmap.impl.MultiLevelHashMap;
import com.raffaeleconforti.dsm.factory.DirectFollowFactory;
import com.raffaeleconforti.foreignkeydiscovery.Couple;
import com.raffaeleconforti.kernelestimation.distribution.EventDistributionCalculator;
import com.raffaeleconforti.kernelestimation.distribution.impl.EventDistributionCalculatorImpl;
import com.raffaeleconforti.log.util.LogAnalyser;
import com.raffaeleconforti.log.util.LogImporter;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 20/03/2016.
 */
public class DesignStructureMatrix {

    private EventDistributionCalculator eventDistributionCalculator;
    private XEventClassifier eventClassifier = new XEventNameClassifier();
    private MultiLevelMap<String, Set<DesignStructureMatrixCell>> multiLevelMap;
    private Set<String> activities;
    private String[] activitiesOrder;

    public static void main(String[] args) throws Exception {
        XLog log = LogImporter.importFromFile(new XFactoryNaiveImpl(), "/Volumes/Data/SharedFolder/Logs/repairExample_complete_lifecycle_only.xes");
        DesignStructureMatrix dsm = new DesignStructureMatrix(log);
        MultiLevelHashMap<String, Boolean> directFollow = DirectFollowFactory.discoverDirectFollow(log, dsm.eventClassifier);
//        dsm = SequenceDSMFactory.discover(LogAnalyser.getUniqueActivities(log, dsm.eventClassifier), dsm.eventClassifier, directFollow, dsm);
//        dsm = ConflictDSMFactory.discover(LogAnalyser.getUniqueActivities(log, dsm.eventClassifier), dsm.eventClassifier, directFollow, dsm);
        System.out.println(dsm);
    }

    public DesignStructureMatrix(XLog log) {
        activities = LogAnalyser.getUniqueActivities(log, eventClassifier);
        activitiesOrder = new String[activities.size()];
        eventDistributionCalculator = new EventDistributionCalculatorImpl(log, eventClassifier);
        eventDistributionCalculator.analyseLog();
        multiLevelMap = new MultiLevelHashMap(2);
        generateDesignStructureMatrix(log);
    }

    private void generateDesignStructureMatrix(XLog log) {
        String firstActivity = identifyFirstElement(log);
        String lastActivity = identifyLastElement(log);

        activitiesOrder[0] = firstActivity;
        activitiesOrder[activitiesOrder.length - 1] = lastActivity;

        Set<String> visited = new UnifiedSet<>();
        visited.add(firstActivity);
        visited.add(lastActivity);

        int posForward = 0;
        int posBackward = activitiesOrder.length - 1;
        int processed = 2;

        while(processed < activitiesOrder.length) {
            Couple<String, Double> next = identifyNextActivityFrequency(visited, activitiesOrder[posForward]);
            Couple<String, Double> previous = identifyPreviousActivityFrequency(visited, activitiesOrder[posForward]);

            Couple<String, Double> c;
            int pos;
            if(next.getSecondElement() >= previous.getSecondElement()) {
                posForward++;
                pos = posForward;
                c = next;
            }else {
                posBackward--;
                pos = posBackward;
                c = previous;
            }

            activitiesOrder[pos] = c.getFirstElement();
            visited.add(c.getFirstElement());
            processed++;
        }
    }

    private Couple<String, Double> identifyNextActivityFrequency(Set<String> visited, String activity) {
        Map<String, Double> distribution = eventDistributionCalculator.computeLikelihoodNextEvent(activity);
        String next = null;
        Double max = 0.0;

        for(Map.Entry<String, Double> entry : distribution.entrySet()) {
            if(!visited.contains(entry.getKey()) && entry.getValue() > max) {
                max = entry.getValue();
                next = entry.getKey();
            }
        }

        if(next == null) {
            return identifyNextActivityFrequency(visited);
        }else {
            return new Couple<>(next, max);
        }
    }

    private Couple<String, Double> identifyNextActivityFrequency(Set<String> visited) {
        Map<String, Double> distribution = new UnifiedMap<>();

        for(String currentActivity : visited) {
            Map<String, Double> tmp = eventDistributionCalculator.computeLikelihoodNextEvent(currentActivity);
            for(String activity : visited) {
                tmp.remove(activity);
            }
            for(Map.Entry<String, Double> entry : tmp.entrySet()) {
                Double d;
                if((d = distribution.get(entry.getKey())) == null) {
                    d = 0.0;
                }
                if(entry.getValue() > d) {
                    distribution.put(entry.getKey(), entry.getValue());
                }
            }
        }

        String next = null;
        Double max = 0.0;

        for(Map.Entry<String, Double> entry : distribution.entrySet()) {
            if(!visited.contains(entry.getKey()) && entry.getValue() > max) {
                max = entry.getValue();
                next = entry.getKey();
            }
        }

        if(next == null) {
            return null;
        }
        return new Couple<String, Double>(next, max);
    }


    private String identifyNextActivity(Set<String> visited, String activity) {
        Map<String, Double> distribution = eventDistributionCalculator.computeLikelihoodNextEvent(activity);
        String next = null;
        Double max = 0.0;

        for(Map.Entry<String, Double> entry : distribution.entrySet()) {
            if(!visited.contains(entry.getKey()) && entry.getValue() > max) {
                max = entry.getValue();
                next = entry.getKey();
            }
        }
        if(next == null) {
            next = identifyNextActivity(visited);
        }

        return next;
    }

    private String identifyNextActivity(Set<String> visited) {
        Map<String, Double> distribution = new UnifiedMap<>();

        for(String currentActivity : visited) {
            Map<String, Double> tmp = eventDistributionCalculator.computeLikelihoodNextEvent(currentActivity);
            for(String activity : visited) {
                tmp.remove(activity);
            }
            for(Map.Entry<String, Double> entry : tmp.entrySet()) {
                Double d;
                if((d = distribution.get(entry.getKey())) == null) {
                    d = 0.0;
                }
                if(entry.getValue() > d) {
                    distribution.put(entry.getKey(), entry.getValue());
                }
            }
        }

        String next = null;
        Double max = 0.0;

        for(Map.Entry<String, Double> entry : distribution.entrySet()) {
            if(!visited.contains(entry.getKey()) && entry.getValue() > max) {
                max = entry.getValue();
                next = entry.getKey();
            }
        }

        return next;
    }

    private Couple<String, Double> identifyPreviousActivityFrequency(Set<String> visited, String activity) {
        Map<String, Double> distribution = eventDistributionCalculator.computeLikelihoodPreviousEvent(activity);
        String next = null;
        Double max = 0.0;

        for(Map.Entry<String, Double> entry : distribution.entrySet()) {
            if(!visited.contains(entry.getKey()) && entry.getValue() > max) {
                max = entry.getValue();
                next = entry.getKey();
            }
        }
        if(next == null) {
            return identifyPreviousActivityFrequency(visited);
        }else {
            return new Couple<String, Double>(next, max);
        }
    }

    private Couple<String, Double> identifyPreviousActivityFrequency(Set<String> visited) {
        Map<String, Double> distribution = new UnifiedMap<>();

        for(String currentActivity : visited) {
            Map<String, Double> tmp = eventDistributionCalculator.computeLikelihoodPreviousEvent(currentActivity);
            for(String activity : visited) {
                tmp.remove(activity);
            }
            for(Map.Entry<String, Double> entry : tmp.entrySet()) {
                Double d;
                if((d = distribution.get(entry.getKey())) == null) {
                    d = 0.0;
                }
                if(entry.getValue() > d) {
                    distribution.put(entry.getKey(), entry.getValue());
                }
            }
        }

        String next = null;
        Double max = 0.0;

        for(Map.Entry<String, Double> entry : distribution.entrySet()) {
            if(!visited.contains(entry.getKey()) && entry.getValue() > max) {
                max = entry.getValue();
                next = entry.getKey();
            }
        }

        if(next == null) {
            return null;
        }
        return new Couple<String, Double>(next, max);
    }

    private String identifyPreviousActivity(Set<String> visited, String activity) {
        Map<String, Double> distribution = eventDistributionCalculator.computeLikelihoodPreviousEvent(activity);
        String next = null;
        Double max = 0.0;

        for(Map.Entry<String, Double> entry : distribution.entrySet()) {
            if(!visited.contains(entry.getKey()) && entry.getValue() > max) {
                max = entry.getValue();
                next = entry.getKey();
            }
        }
        if(next == null) {
            next = identifyPreviousActivity(visited);
        }

        return next;
    }

    private String identifyPreviousActivity(Set<String> visited) {
        Map<String, Double> distribution = new UnifiedMap<>();

        for(String currentActivity : visited) {
            Map<String, Double> tmp = eventDistributionCalculator.computeLikelihoodPreviousEvent(currentActivity);
            for(String activity : visited) {
                tmp.remove(activity);
            }
            for(Map.Entry<String, Double> entry : tmp.entrySet()) {
                Double d;
                if((d = distribution.get(entry.getKey())) == null) {
                    d = 0.0;
                }
                if(entry.getValue() > d) {
                    distribution.put(entry.getKey(), entry.getValue());
                }
            }
        }

        String next = null;
        Double max = 0.0;

        for(Map.Entry<String, Double> entry : distribution.entrySet()) {
            if(!visited.contains(entry.getKey()) && entry.getValue() > max) {
                max = entry.getValue();
                next = entry.getKey();
            }
        }

        return next;
    }

    private String identifyFirstElement(XLog log) {
        Map<String, Integer> initialActivities = LogAnalyser.getInitialActivityFriquencies(log);
        String activity = null;
        Integer max = 0;
        for(Map.Entry<String, Integer> entry : initialActivities.entrySet()) {
            if(entry.getValue() > max) {
                max = entry.getValue();
                activity = entry.getKey();
            }
        }
        return activity;
    }

    private String identifyLastElement(XLog log) {
        Map<String, Integer> finalActivity = LogAnalyser.getFinalActivityFriquencies(log);
        String activity = null;
        Integer max = 0;
        for(Map.Entry<String, Integer> entry : finalActivity.entrySet()) {
            if(entry.getValue() > max) {
                max = entry.getValue();
                activity = entry.getKey();
            }
        }
        return activity;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        int columnWidths = 0;
        for (int index = 0; index < activitiesOrder.length; index++) {
            columnWidths = Math.max(columnWidths, activitiesOrder[index].length()+1);
        }

        for(int i = 0; i < activitiesOrder.length; i++) {
            for (int j = 0; j < activitiesOrder.length; j++) {
                columnWidths = Math.max(columnWidths, getName(activitiesOrder[i], activitiesOrder[j]).length());
            }
        }

        sb.append("").append(fill("", columnWidths, ' '));
        for(int i = 0; i < activitiesOrder.length; i++) {
            sb.append(activitiesOrder[i]).append(fill(activitiesOrder[i], columnWidths, ' '));
        }
        sb.append("\n");

        for(int i = 0; i < activitiesOrder.length; i++) {
            sb.append(activitiesOrder[i]).append(fill(activitiesOrder[i], columnWidths, ' '));
            for(int j = 0; j < activitiesOrder.length; j++) {
                sb.append(getName(activitiesOrder[i], activitiesOrder[j])).append(fill((getName(activitiesOrder[i], activitiesOrder[j])), columnWidths, ' '));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public static String fill(String sValue, int iMinLength, char with) {
        StringBuilder filled = new StringBuilder(iMinLength);

        while (filled.length() < iMinLength - sValue.length()) {
            filled.append(with);
        }

        return filled.toString();
    }

    public Set<DesignStructureMatrixCell> getCell(String... keys) {
        if(multiLevelMap.get(keys) == null) return null;

        Set<DesignStructureMatrixCell> set = new UnifiedSet<>();
        set.addAll(multiLevelMap.get(keys));
        return set;
    }

    public void setCell(Set<DesignStructureMatrixCell> cell, String... keys) {
        multiLevelMap.put(cell, keys);
    }

    private String getName(String... keys) {
        Set<DesignStructureMatrixCell> dsmc = multiLevelMap.get(keys);
        if (dsmc == null) return  "";
        return dsmc.toString();
    }
}
