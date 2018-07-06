package com.raffaeleconforti.deviancemining;

import com.raffaeleconforti.log.util.LogCloner;
import com.raffaeleconforti.log.util.NameExtractor;
import com.raffaeleconforti.log.util.TraceToString;
import com.raffaeleconforti.noisefiltering.event.InfrequentBehaviourFilter;
import ee.ut.eventstr.comparison.ApromoreCompareLL;
import ee.ut.utilities.Quadruplet;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.impl.multimap.set.UnifiedSetMultimap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.*;

public class DevianceMiner {

    private static NameExtractor nameExtractor = new NameExtractor(new XEventNameClassifier());
    private XLog normal_log;
    private XLog deviant_log;

    private boolean useGurobi = true;
    private boolean useArcsFrequency = true;
    private boolean debug_mode = false;

    private double threshold;

    private UnifiedSetMultimap<Deviance, Deviance> notReachable = new UnifiedSetMultimap();
    private UnifiedSet<Deviance> completed = new UnifiedSet<>();
//    private double relevance_threshold;

//    public DevianceMiner(double threshold, double relevance_threshold) {
//        this.threshold = threshold;
//        this.relevance_threshold = relevance_threshold;
//    }

    public List<Deviance> mineDeviances(XLog log) {
        int original_log_size = log.size();

        LogCloner logCloner = new LogCloner();
        int lowest = 0;
        int highest = 100;
        int best = 21;
        while (highest - lowest > 1) {
            XLog original = logCloner.cloneLog(log);
            int current = (lowest + highest) / 2;
            threshold = ((double) current / 100.0);
            XLog[] logs1 = splitLogInStandardAndDeviant(original);
            if (logs1[0].size() > logs1[1].size()) lowest = current;
            else highest = current;
        }
        best = (lowest + highest) / 2;
//        for(int i = 1; i < 101; i++) {
//            XLog original = logCloner.cloneLog(log);
//            threshold = ((double) i / 100.0);
//            XLog[] logs1 = splitLogInStandardAndDeviant(original);
//            if(logs1[0].size() > logs1[1].size()) bestI = i;
//        }


        threshold = ((double) best / 100.0);
        System.out.println(threshold);
        XLog[] logs1 = splitLogInStandardAndDeviant(log);
        XLog normalLog = logs1[0];
        XLog deviantLog = logs1[1];
        normal_log = new XFactoryNaiveImpl().createLog(normalLog.getAttributes());
        deviant_log = new XFactoryNaiveImpl().createLog(normalLog.getAttributes());
        normal_log.addAll(normalLog);
        deviant_log.addAll(deviantLog);
        System.out.println(normal_log.size());
        System.out.println(deviant_log.size());

//        LogImporter.exportToFile("/Users/raffaele/Downloads/Deviance Mining - SEPSIS Correct" + file_ext, normal_log);
//        LogImporter.exportToFile("/Users/raffaele/Downloads/Deviance Mining - SEPSIS Deviant" + file_ext, deviant_log);

        if (normalLog.size() > 0 && deviantLog.size() > 0) {
            Set<Deviance> deviances = generateStatementsFromTriples(normalLog, "normal behaviour", deviantLog, "deviant behaviour");
            List<Deviance> relevant_deviances = retainRelevantDeviances(deviances, original_log_size);
            determineCausalityBetweenDeviances(relevant_deviances);
            return relevant_deviances;
        }
        return new ArrayList<>();
    }

    public XLog getNormalLog() {
        return normal_log;
    }

    public XLog getDeviantLog() {
        return deviant_log;
    }

    public void determineCausalityBetweenDeviances(List<Deviance> deviances) {
        for (int i = 0; i < deviances.size(); i++) {
            for (int j = 0; j < deviances.size(); j++) {
                if (i != j) {
                    Deviance deviance1 = deviances.get(i);
                    String statement1 = deviance1.getStatement();
                    Set<XTrace> statement1NormalTraces = deviance1.getNormalTraces();
                    Set<XTrace> statement1DeviantTraces = deviance1.getDeviantTraces();

                    Deviance deviance2 = deviances.get(j);
                    String statement2 = deviance2.getStatement();
                    Set<XTrace> statement2NormalTraces = deviance2.getNormalTraces();
                    Set<XTrace> statement2DeviantTraces = deviance2.getDeviantTraces();

                    if (statement2DeviantTraces.size() > 0 &&
                            statement1NormalTraces.containsAll(statement2NormalTraces) &&
                            statement1DeviantTraces.containsAll(statement2DeviantTraces) &&
                            checkOrder(statement2DeviantTraces, statement1, statement2)) {
                        deviance1.addConsequentDeviation(deviance2);
                    }
                }
            }
        }

        boolean removed = true;
        while (removed) {
            removed = false;
            loop:
            for (Deviance deviance1 : deviances) {
                if (!completed.contains(deviance1)) {
                    for (Deviance deviance2 : deviance1.getConsequentDeviations()) {
                        boolean notReach = notReachable.get(deviance1).contains(deviance2);
                        if (!notReach) {
                            boolean reach = reachable(deviance1, deviance1, deviance2, new HashSet<>(), new boolean[]{false});
                            if (!reach) {
                                notReachable.get(deviance1).contains(deviance2);
                            } else {
                                deviance1.removeConsequentDeviation(deviance2);
                                removed = true;
                                break loop;
                            }
                        }
                    }
                    completed.add(deviance1);
                }
            }
        }
    }

    public List<Deviance> retainRelevantDeviances(Set<Deviance> deviances, double original_log_size) {
        List<Deviance> list_deviances = new ArrayList<>(deviances);
        Collections.sort(list_deviances);
        List<Deviance> relevant_deviances = new ArrayList<>();
        for (Deviance deviance : list_deviances) {
//            double perc1 = (double) deviance.getNormalTraces().size() / original_log_size;
//            double perc2 = (double) deviance.getDeviantTraces().size() / original_log_size;
//            if((perc1 > relevance_threshold || deviance.getNormalTraces().size() == 0) &&
//                    (perc2 > relevance_threshold || deviance.getDeviantTraces().size() == 0) &&
//                    (deviance.getNormalTraces().size() > 0 || deviance.getDeviantTraces().size() > 0)) {
            if (deviance.getRelevance() > 0.01 && deviance.getDeviantTraces().size() > 100) {
                relevant_deviances.add(deviance);
            }
        }

//        for (Deviance deviance : list_deviances) {
//            relevant_deviances.add(deviance);
//        }
        return relevant_deviances;
    }

    private boolean reachable(Deviance originalSource, Deviance source, Deviance target, Set<Deviance> visited, boolean[] found) {
        if (visited.contains(source)) return false;
        visited.add(source);
        for (Deviance steps : source.getConsequentDeviations()) {
            if (!found[0]) {
                if (originalSource.equals(source) && steps.equals(target)) {

                } else if (!originalSource.equals(source) && steps.equals(target)) {
                    found[0] = true;
                    return true;
                } else if (!steps.equals(target)) {
                    if (reachable(originalSource, steps, target, visited, found)) {
                        found[0] = true;
                        return true;
                    }
                }
            } else return true;
        }
        return false;
    }

    private boolean checkOrder(Set<XTrace> traces, String statement1, String statement2) {
        List<String> activities1 = getActivities(statement1);
        List<String> activities2 = getActivities(statement2);
        Collections.sort(activities1);
        Collections.sort(activities2);

        double position1 = checkPosition(traces, activities1);
        double position2 = checkPosition(traces, activities2);

        return position1 > 0 && position2 > 0 && position1 <= position2;
    }

    private double checkPosition(Set<XTrace> traces, List<String> activities1) {
        List<int[]> positions = new ArrayList<>();
        for (XTrace trace : traces) {
            int[] position = new int[activities1.size()];
            for (int i = 0; i < position.length; i++) {
                position[i] = -1;
            }

            for (int i = 0; i < trace.size(); i++) {
                XEvent event = trace.get(i);
                String name = nameExtractor.getEventName(event);
                int pos = Collections.binarySearch(activities1, name);
                if (pos > -1 && position[pos] == -1) {
                    position[pos] = i;
                }
            }
            positions.add(position);
        }

        double average = -1;
        for (int[] position : positions) {
            double sum = 0;
            double count = 0;
            for (int i = 0; i < position.length; i++) {
                if (position[i] > -1) {
                    sum += position[i];
                    count++;
                }
            }
            if (count > 0) {
                if (average == -1) average = 0;
                average += (sum / count);
            }
        }
        return average / positions.size();
    }

    private List<String> getActivities(String statement) {
        Set<String> set = new HashSet<>();
        String statement1 = statement;

        while (statement1.contains("]")) {
            String activities = nextGroupActivities(statement1);

            StringTokenizer stringTokenizer = new StringTokenizer(activities, ",");
            while (stringTokenizer.hasMoreTokens()) {
                String activity = stringTokenizer.nextToken();
                if (activity.startsWith(" ")) activity = activity.substring(1);
                set.add(activity);
            }
            statement1 = statement1.substring(statement1.indexOf("]") + 1);
        }

        return new ArrayList<>(set);
    }

    private String nextGroupActivities(String statement) {
        int indexOpen = statement.indexOf("[");
        int indexClose = statement.indexOf("]");

        return statement.substring(indexOpen + 1, indexClose);
    }

    private Set<XLog> splitDeviantLog(XLog deviantLog) {
        Set<XLog> deviantLogs = new HashSet<>();

        XLog[] logs2 = splitLogInStandardAndDeviant(deviantLog);
        if (logs2[0].size() > 0 && logs2[1].size() > 0) {
            deviantLogs.addAll(splitDeviantLog(logs2[0]));
            deviantLogs.addAll(splitDeviantLog(logs2[1]));
        } else if (logs2[0].size() > 0) {
            deviantLogs.add(logs2[0]);
        } else {
            deviantLogs.add(logs2[1]);
        }

        return deviantLogs;
    }

    public Set<Deviance> generateStatementsFromTriples(XLog log1, String nameLog1, XLog log2, String nameLog2) {
        int original_log_size = (log1.size() + log2.size());
        ApromoreCompareLL apromoreCompareLL = new ApromoreCompareLL();
        Set<Quadruplet<String, Set<XTrace>, Set<XTrace>, Integer>> differences = apromoreCompareLL.getDifferencesQuadrupletsAStar(log1, nameLog1, log2, nameLog2);

        List<String> lhses = new ArrayList<>();
        List<String> rhses = new ArrayList<>();

        Map<String, Integer> toBeRepeatedStatementsMin = new HashMap<>();
        Map<String, Integer> toBeRepeatedStatementsMax = new HashMap<>();
        Map<String, String> toBeRepeatedFullStatements = new HashMap<>();
        Map<String, Set<XTrace>[]> toBeRepeatedTraces = new HashMap<>();
        Map<String, Integer> toBeRepeatedCost = new HashMap<>();
        Set<Deviance> deviances = new HashSet<>();

        for (Quadruplet<String, Set<XTrace>, Set<XTrace>, Integer> differenceQuadruplet : differences) {
            String difference = getDifferenceInNormalForm(differenceQuadruplet.getA(), nameLog1, nameLog2);
            boolean startWithCorrect = difference.startsWith("The " + nameLog1);

            String[] hses = startWithCorrect ? splitLHSandRHS(difference, nameLog1, nameLog2) : splitLHSandRHS(difference, nameLog2, nameLog1);
            String lhs = hses[0];
            String rhs = hses[1];

            String[] lhs_split = splitHS(lhs);
            String[] rhs_split = splitHS(rhs);

            if ((lhs_split.length > 0 && lhs_split[1].equals("to be repeated")) || (rhs_split.length > 0 && rhs_split[1].equals("to be repeated"))) {
                String[] hs_split = lhs_split[1].equals("to be repeated") ? lhs_split : rhs_split;
                String repeated = hs_split[0];
                String first;
                if (repeated.indexOf(",") == -1) first = repeated;
                else first = repeated.substring(0, repeated.indexOf(","));

                String word = first.substring(1);

                int occurrences = countOccurrencesSubstring(repeated, word);
                String remaining = repeated.substring(1 + word.length());
                if (remaining.contains(word))
                    repeated = "[" + word + remaining.substring(0, remaining.indexOf(", " + word)) + "]";

                lhs = repeated + " " + hs_split[1] + " " + hs_split[2] + " " + hs_split[3];

                Integer min_number = toBeRepeatedStatementsMin.get(lhs);
                Integer max_number = toBeRepeatedStatementsMax.get(lhs);

                if (min_number == null || min_number > occurrences) {
                    toBeRepeatedStatementsMin.put(lhs, occurrences);
                    min_number = occurrences;
                }
                if (max_number == null || max_number < occurrences) {
                    toBeRepeatedStatementsMax.put(lhs, occurrences);
                    max_number = occurrences;
                }

                if (startWithCorrect) {
                    difference = "The " + nameLog1 + " allows " + lhs + ", while the " + nameLog2 + " " + rhs;
                } else {
                    difference = "The " + nameLog2 + " allows " + lhs + ", while the " + nameLog1 + " " + rhs;
                }

                if (min_number == 1 && max_number == 1) difference = difference;
                else if (min_number == max_number)
                    difference = difference.replaceAll("to be repeated", "to be repeated " + min_number + " extra time" + ((min_number > 1) ? "s" : ""));
                else
                    difference = difference.replaceAll("to be repeated", "to be repeated between " + min_number + " to " + max_number + " extra times");

                toBeRepeatedFullStatements.put(lhs, difference);
                Set[] traces = toBeRepeatedTraces.get(lhs);
                if (traces == null) {
                    toBeRepeatedTraces.put(lhs, new Set[]{differenceQuadruplet.getB(), differenceQuadruplet.getC()});
                    toBeRepeatedCost.put(lhs, differenceQuadruplet.getD());
                } else {
                    traces[0].addAll(differenceQuadruplet.getB());
                    traces[1].addAll(differenceQuadruplet.getC());
                }
            }

            int possible_problem_rhs = lhses.indexOf(lhs);
            int possible_problem_lhs = rhses.indexOf(rhs);

            if (!(possible_problem_lhs > -1 && checkDuplicate(lhs, lhses.get(possible_problem_lhs))) &&
                    !(possible_problem_rhs > -1 && checkDuplicate(rhs, rhses.get(possible_problem_rhs)))) {
                lhses.add(lhs);
                rhses.add(rhs);

                if (!lhs.equals(rhs) && !difference.contains("to be repeated")) {
                    Set<XTrace> normal_traces = differenceQuadruplet.getB();
                    if (normal_traces == null || normal_traces.isEmpty()) normal_traces = new HashSet<>(log1);
                    Set<XTrace> deviant_traces = differenceQuadruplet.getC();
                    if (deviant_traces == null || deviant_traces.isEmpty()) deviant_traces = new HashSet<>(log2);

                    int order;
                    if (!isConcurrently(difference) || (order = checkConcurrence(difference, nameLog1, normal_traces, nameLog2, deviant_traces)) == 0) {
                        deviances.add(new Deviance(difference, nameLog1, normal_traces, nameLog2, deviant_traces, original_log_size, differenceQuadruplet.getD()));
                    } else if (order > 0) {
                        String correct_difference = fixDifference(difference, nameLog1, nameLog2, order);
                        hses = startWithCorrect ? splitLHSandRHS(correct_difference, nameLog1, nameLog2) : splitLHSandRHS(correct_difference, nameLog2, nameLog1);
                        lhs = hses[0];
                        rhs = hses[1];
                        if (!lhs.equals(rhs)) {
                            deviances.add(new Deviance(difference, nameLog1, normal_traces, nameLog2, deviant_traces, original_log_size, differenceQuadruplet.getD()));
                        }
                    }
                }
            }
        }

        for (String sentence : toBeRepeatedStatementsMin.keySet()) {
            Set<XTrace> normal_traces = toBeRepeatedTraces.get(sentence)[0];
            if (normal_traces == null || normal_traces.isEmpty()) normal_traces = new HashSet<>(log1);
            Set<XTrace> deviant_traces = toBeRepeatedTraces.get(sentence)[1];
            if (deviant_traces == null || deviant_traces.isEmpty()) deviant_traces = new HashSet<>(log2);

            deviances.add(new Deviance(toBeRepeatedFullStatements.get(sentence), nameLog1, normal_traces, nameLog2, deviant_traces, original_log_size, toBeRepeatedCost.get(sentence)));
        }

        boolean cycle = true;
        while (cycle) {
            cycle = false;
            loop:
            for (Deviance deviance1 : deviances) {
                String lhs1 = deviance1.getNormal_statement();
                String rhs1 = deviance1.getDeviant_statement();
                for (Deviance deviance2 : deviances) {
                    if (!deviance1.equals(deviance2)) {
                        String lhs2 = deviance2.getNormal_statement();
                        String rhs2 = deviance2.getDeviant_statement();
                        if (checkInclusion(lhs1, lhs2, rhs1, rhs2)) {
                            deviances.remove(deviance2);
                            cycle = true;
                            break loop;
                        }
                    }
                }
            }
        }

        return deviances;
    }

    private String fixDifference(String difference, String logName1, String logName2, int order) {
        String[] s = splitLHSandRHS(difference, logName1, logName2);
        if (isConcurrently(s[0])) {
            if (difference.contains("The " + logName1)) {
                String newDifference = "The " + logName1 + fixSubDifference(splitHS(s[0]), order, difference.contains("The " + logName1 + " allows"));
                if (!s[1].isEmpty()) newDifference += ", while the " + logName2 + " " + s[1];
                return newDifference;
            } else {
                String newDifference = "The " + logName2 + fixSubDifference(splitHS(s[0]), order, difference.contains("The " + logName2 + " allows"));
                if (!s[1].isEmpty()) newDifference += ", while the " + logName1 + " " + s[1];
                return newDifference;
            }
        } else if (isConcurrently(s[1])) {
            if (difference.contains("The " + logName1)) {
                String newDifference = "The " + logName1 + " " + s[0] + ", while the " + logName2 + fixSubDifference(splitHS(s[1]), order, difference.contains(", while the " + logName2 + " allows"));
                return newDifference;
            } else {
                String newDifference = "The " + logName2 + " " + s[0] + ", while the " + logName1 + fixSubDifference(splitHS(s[1]), order, difference.contains(", while the " + logName1 + " allows"));
                return newDifference;
            }
        }
        return null;
    }

    private String getDifferenceInNormalForm(String difference, String logName1, String logName2) {
        String[] s = splitLHSandRHS(difference, logName1, logName2);
        if (isBefore(s[0])) {
            if (difference.contains("The " + logName1)) {
                String newDifference = "The " + logName1 + fixSubDifference(splitHS(s[0]), 1, difference.contains("The " + logName1 + " allows"));
                if (!s[1].isEmpty()) newDifference += ", while the " + logName2 + " " + s[1];
                return newDifference;
            } else {
                String newDifference = "The " + logName2 + fixSubDifference(splitHS(s[0]), 1, difference.contains("The " + logName2 + " allows"));
                if (!s[1].isEmpty()) newDifference += ", while the " + logName1 + " " + s[1];
                return newDifference;
            }
        } else if (isBefore(s[1])) {
            if (difference.contains("The " + logName1)) {
                String newDifference = "The " + logName1 + " " + s[0] + ", while the " + logName2 + fixSubDifference(splitHS(s[1]), 1, difference.contains(", while the " + logName2 + " allows"));
                return newDifference;
            } else {
                String newDifference = "The " + logName2 + " " + s[0] + ", while the " + logName1 + fixSubDifference(splitHS(s[1]), 1, difference.contains(", while the " + logName2 + " allows"));
                return newDifference;
            }
        }
        return difference;
    }

    private String fixSubDifference(String[] activities, int order, boolean allows) {
        if (order == 1) {
            if (allows) return " allows " + activities[3] + " to occur after " + activities[0];
            else return " does not allow " + activities[3] + " to occur after " + activities[0];
        } else {
            if (allows) return " allows " + activities[0] + " to occur after " + activities[3];
            else return " does not allow " + activities[0] + " to occur after " + activities[3];
        }
    }

    private int checkConcurrence(String difference, String logName1, Set<XTrace> normal_traces, String logName2, Set<XTrace> deviant_traces) {
        String[] s = splitLHSandRHS(difference, logName1, logName2);
        if (isConcurrently(s[0])) {
            Set<XTrace> traces = difference.contains("The " + logName1) ? normal_traces : deviant_traces;
            return checkConcurrence(splitHS(s[0]), traces);
        } else if (isConcurrently(s[1])) {
            Set<XTrace> traces = difference.contains("The " + logName1) ? deviant_traces : normal_traces;
            return checkConcurrence(splitHS(s[1]), traces);
        }
        return -1;
    }

    private int checkConcurrence(String[] activities, Set<XTrace> traces) {
        String activity1 = activities[0].substring(1, activities[0].length() - 1);
        String activity2 = activities[3].substring(1, activities[3].length() - 1);

        boolean ab = false;
        boolean ba = false;

        for (XTrace trace : traces) {
            for (int i = 0; i < trace.size() - 1; i++) {
                String a = nameExtractor.getEventName(trace.get(i));
                String b = nameExtractor.getEventName(trace.get(i + 1));
                if (a.equals(activity1) && b.equals(activity2)) {
                    ab = true;
                } else if (a.equals(activity2) && b.equals(activity1)) {
                    ba = true;
                }

                if (ab && ba) return 0;
            }
        }

        if (ab) return 1;
        if (ba) return 2;
        return -1;
    }

    private boolean isConcurrently(String difference) {
        return difference.contains("concurrently to");
    }

    private boolean isBefore(String difference) {
        return !difference.contains("repeated") && !difference.contains("substituted") && difference.contains("before");
    }

    private String[] splitLHSandRHS(String difference, String logName1, String logName2) {
        String lhs = difference.contains(", while") ? difference.substring(logName1.length() + 5, difference.indexOf(", while")) : difference.substring(logName1.length() + 5);
        String rhs = difference.contains(", while") ? difference.substring(difference.indexOf(", while ") + logName2.length() + 13) : "";

        return new String[]{lhs, rhs};
    }

    public boolean checkInclusion(String lhs, String old_lhs, String rhs, String old_rhs) {
        String[] lsplit1 = splitHS(lhs);
        String[] lsplit2 = splitHS(old_lhs);
        String[] rsplit1 = splitHS(rhs);
        String[] rsplit2 = splitHS(old_rhs);

        if (lsplit1.length > 0 && lsplit2.length > 0 && rsplit1.length > 0 && rsplit2.length > 0) {
            if (lsplit1[0].equals(lsplit2[0]) && lsplit1[3].equals(lsplit2[3]) &&
                    rsplit1[0].equals(rsplit2[0]) && rsplit1[3].equals(rsplit2[3])) {
                if (lsplit1[2].equals(lsplit1[2]) &&
                        ((rsplit1[2].equals("concurrently to") && rsplit2[2].equals("after")) ||
                                rsplit1[2].equals("concurrently to") && rsplit2[2].equals("before")) ||
                        rsplit1[2].equals(rsplit2[2])) {
                    return true;
                }
                if (rsplit1[2].equals(rsplit2[2]) &&
                        ((lsplit1[2].equals("concurrently to") && lsplit2[2].equals("after")) ||
                                lsplit1[2].equals("concurrently to") && lsplit2[2].equals("before")) ||
                        lsplit1[2].equals(lsplit2[2])) {
                    return true;
                }
            }
        } else if (lsplit1.length > 0 && lsplit2.length > 0) {
            if (lsplit1[0].equals(lsplit2[0]) && lsplit1[3].equals(lsplit2[3])) {
                if (((lsplit1[2].equals("concurrently to") && lsplit2[2].equals("after")) ||
                        lsplit1[2].equals("concurrently to") && lsplit2[2].equals("before")) ||
                        lsplit1[2].equals(lsplit2[2])) {
                    return true;
                }
            }
        } else if (rsplit1.length > 0 && rsplit2.length > 0) {
            if (rsplit1[0].equals(rsplit2[0]) && rsplit1[3].equals(rsplit2[3])) {
                if (((rsplit1[2].equals("concurrently to") && rsplit2[2].equals("after")) ||
                        rsplit1[2].equals("concurrently to") && rsplit2[2].equals("before")) ||
                        rsplit1[2].equals(rsplit2[2])) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkDuplicate(String hs, String old_hs) {
        String[] split1 = splitHS(hs);
        String[] split2 = splitHS(old_hs);

        if (split1.length > 0 && split2.length > 0) {
            if (split1[0].equals(split2[3]) && split1[3].equals(split2[0]) &&
                    ((split1[2].equals("after") && split2[2].equals("before")) ||
                            (split2[2].equals("after") && split1[2].equals("before")) ||
                            (split2[2].equals("concurrently to") && split1[2].equals("concurrently to")))) {
                return true;
            }
            if (split1[0].equals(split2[0]) && split1[3].equals(split2[3]) &&
                    ((split1[2].equals("after") && split2[2].equals("after")) ||
                            (split1[2].equals("before") && split2[2].equals("before")) ||
                            (split1[2].equals("concurrently to") && split2[2].equals("concurrently to")))) {
                return true;
            }
        }
        return false;
    }

    private String[] splitHS(String statement) {
        if (statement.contains("after") || statement.contains("before") || statement.contains("concurrently to")) {
            String a = "";
            String b = "";
            if (statement.contains("to occur")) {
                a = statement.substring(statement.indexOf("["), statement.indexOf("to occur") - 1);
                b = "to occur";
            } else if (statement.contains("to be repeated")) {
                a = statement.substring(statement.indexOf("["), statement.indexOf("to be repeated") - 1);
                b = "to be repeated";
            } else if (statement.contains("to be substituted")) {
                a = statement.substring(statement.indexOf("["), statement.indexOf("to be substituted") - 1);
                b = "to be substituted";
            }

            String c = "";
            if (statement.contains("after")) c = "after";
            else if (statement.contains("before")) c = "before";
            else if (statement.contains("concurrently to")) c = "concurrently to";
            else if (statement.contains("in the same run with")) c = "in the same run with";

            String d = statement.substring(statement.indexOf(c) + c.length() + 1);

            return new String[]{a, b, c, d};
        } else {
            return new String[]{};
        }
    }

    private XLog[] splitLogInStandardAndDeviant(XLog log) {
        NameExtractor nameExtractor = new NameExtractor(new XEventNameClassifier());

        XLog correctLog = removeDeviantBehavior(log);
        List<String> stringCorrectLog = new ArrayList<String>();
        for (XTrace trace : correctLog) {
            stringCorrectLog.add(TraceToString.convertXTraceToString(trace, nameExtractor));
        }

        Iterator<XTrace> traceIterator = log.iterator();
        while (traceIterator.hasNext()) {
            XTrace trace = traceIterator.next();
            if (stringCorrectLog.contains(TraceToString.convertXTraceToString(trace, nameExtractor)))
                traceIterator.remove();
        }

        return new XLog[]{correctLog, log};
    }

    private XLog removeDeviantBehavior(XLog log) {
        InfrequentBehaviourFilter filter = new InfrequentBehaviourFilter(new XEventNameClassifier(), useGurobi, useArcsFrequency, debug_mode, true, true, 0.5, 0.5, true, threshold);
        return filter.filterDeviances(log);
    }

    private int countOccurrencesSubstring(String string, String substring) {
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {

            lastIndex = string.indexOf(substring, lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += substring.length();
            }
        }
        return count;
    }

}

