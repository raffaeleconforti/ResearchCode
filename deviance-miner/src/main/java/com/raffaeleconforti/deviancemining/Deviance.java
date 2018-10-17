/*
 *  Copyright (C) 2018 Raffaele Conforti (www.raffaeleconforti.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.raffaeleconforti.deviancemining;

import com.raffaeleconforti.log.util.LogAnalyser;
import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.statistics.StatisticsSelector;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.stat.inference.TTest;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class Deviance implements Comparable<Deviance> {

    private final String file_ext = ".xes.gz";

    public enum Type {POSITIVE, NEUTRAL, NEGATIVE}

    private StatisticsSelector statisticsSelector = new StatisticsSelector();
    private int scale = -1;

    private boolean changed = true;

    private String statement;
    private Set<Deviance> consequent;
    private Set<Deviance> caused;

    private String normal_behaviour_name;
    private String deviant_behaviour_name;

    private String normal_statement;
    private String deviant_statement;

    private Set<XTrace> normal_traces;
    private Set<XTrace> deviant_traces;

    private int original_log_size;
    private Type type;
    private String assessment;
    private int cost;

    public Deviance(String statement, String normal_behaviour_name, Set<XTrace> normal_traces, String deviant_behaviour_name, Set<XTrace> deviant_traces, int original_log_size, int cost) {
        this.original_log_size = original_log_size;
        this.statement = statement;

        this.normal_behaviour_name = normal_behaviour_name;
        this.normal_traces = normal_traces;

        this.deviant_behaviour_name = deviant_behaviour_name;
        this.deviant_traces = deviant_traces;

        this.cost = cost;

        consequent = new HashSet<>();
        caused = new HashSet<>();

        String[] single_statements = splitLHSandRHS(statement);
        normal_statement = single_statements[0].contains(normal_behaviour_name) ?
                single_statements[0].substring(normal_behaviour_name.length() + 1) :
                !single_statements[1].isEmpty() ?
                        single_statements[1].substring(normal_behaviour_name.length() + 1) :
                        "";
        deviant_statement = single_statements[0].contains(normal_behaviour_name) ?
                single_statements[1].substring(deviant_behaviour_name.length() + 1) :
                single_statements[0].substring(deviant_behaviour_name.length() + 1);

        if (!normal_statement.equals("") && !normal_statement.equals("does not"))
            clean(false, normal_statement, normal_traces);
        else clean(true, deviant_statement, normal_traces);

        if (!deviant_statement.equals("") && !deviant_statement.equals("does not"))
            clean(false, deviant_statement, deviant_traces);
        clean(true, normal_statement, deviant_traces);
    }

    public String getNormal_statement() {
        return normal_statement;
    }

    public String getDeviant_statement() {
        return deviant_statement;
    }

    public void setOriginalLogSize(int original_log_size) {
        this.original_log_size = original_log_size;
    }

    public double getRelevance() {
//        double perc1 = (double) normal_traces.size() / original_log_size;
        double avg1 = 0;
        double avg2 = 0;
        for (XTrace trace : normal_traces) {
            avg1 += trace.size();
        }
        for (XTrace trace : deviant_traces) {
            avg2 += trace.size();
        }
        avg1 /= normal_traces.size();
        avg2 /= deviant_traces.size();
        if (cost >= avg1) {
            return 0;
        }
        if (cost >= avg2) {
            return 0;
        }
        double perc2 = (double) deviant_traces.size() / original_log_size;
//        return Math.min(perc1, perc2);
        return perc2;
    }

    public boolean isTopDeviance() {
        return caused.size() == 0;
    }

    public String getStatement() {
        return statement;
    }

    public Set<XTrace> getNormalTraces() {
        return normal_traces;
    }

    public Set<XTrace> getDeviantTraces() {
        return deviant_traces;
    }

    public void addConsequentDeviation(Deviance deviance) {
        consequent.add(deviance);
        deviance.caused.add(this);
        changed = true;
    }

    public void addAllConsequentDeviations(Collection<Deviance> deviances) {
        for (Deviance deviance : deviances) {
            addConsequentDeviation(deviance);
        }
    }

    public void removeConsequentDeviation(Deviance deviance) {
        consequent.remove(deviance);
        deviance.caused.remove(this);
        changed = true;
    }

    public void removeAllConsequentDeviations(Collection<Deviance> deviances) {
        for (Deviance deviance : deviances) {
            removeConsequentDeviation(deviance);
        }
    }

    public Set<Deviance> getConsequentDeviations() {
//        return new HashSet<Deviance>(consequent);
        return consequent;
    }

    public Set<Deviance> getCausingDeviations() {
//        return new HashSet<Deviance>(caused);
        return caused;
    }

    public String assessDifferenceInPerformance() {
        if (assessment == null || changed) {
            double[][] performance = compare(normal_traces, deviant_traces);

            double[] correctPerformance = performance[0];
            double[] deviantPerformance = performance[1];

            double correctMean = statisticsSelector.evaluate(StatisticsSelector.StatisticsMeasures.MEAN, null, correctPerformance);
            double correctSd = statisticsSelector.evaluate(StatisticsSelector.StatisticsMeasures.SD, correctMean, correctPerformance);

            double deviantMean = statisticsSelector.evaluate(StatisticsSelector.StatisticsMeasures.MEAN, null, deviantPerformance);
            double deviantSd = statisticsSelector.evaluate(StatisticsSelector.StatisticsMeasures.SD, deviantMean, deviantPerformance);

            TTest tTest = new TTest();
            Double significance = null;
            try {
                significance = tTest.tTest(correctPerformance, deviantPerformance);
            } catch (NumberIsTooSmallException nitse) {
            }

            assessment = "";
            if (correctMean > deviantMean) {
                type = Type.POSITIVE;
                assessment = "Reducing cycle time from " +
                        format(correctMean) + " \u00B1 " + format(correctSd) + " " + LogAnalyser.getScale(scale) + " to " +
                        format(deviantMean) + " \u00B1 " + format(deviantSd) + " " + LogAnalyser.getScale(scale);
                if (significance != null) {
                    assessment += " with a confidence interval of " + format(100 * (1 - significance)) + "%";
                }
            } else if (correctMean < deviantMean) {
                type = Type.NEGATIVE;
                assessment = "Increasing cycle time from " +
                        format(correctMean) + " \u00B1 " + format(correctSd) + " " + LogAnalyser.getScale(scale) + " to " +
                        format(deviantMean) + " \u00B1 " + format(deviantSd) + " " + LogAnalyser.getScale(scale);
                if (significance != null) {
                    assessment += " with a confidence interval of " + format(100 * (1 - significance)) + "%";
                }
            } else {
                type = Type.NEUTRAL;
            }
            changed = false;
        }
        return assessment;
    }

    private double[][] compare(Collection<XTrace> normal_traces, Collection<XTrace> deviant_traces) {
        double[] correctPerformance, deviantPerformance;

        double correctCases, correctMax, correctMin;
        double deviantCases, deviantMax, deviantMin;
        do {
            scale++;
            correctPerformance = LogAnalyser.measureTimePerformance(normal_traces, scale);
            deviantPerformance = LogAnalyser.measureTimePerformance(deviant_traces, scale);

            correctCases = correctPerformance.length;
            correctMax = statisticsSelector.evaluate(StatisticsSelector.StatisticsMeasures.MAX, null, correctPerformance);
            correctMin = statisticsSelector.evaluate(StatisticsSelector.StatisticsMeasures.MIN, null, correctPerformance);

            deviantCases = deviantPerformance.length;
            deviantMax = statisticsSelector.evaluate(StatisticsSelector.StatisticsMeasures.MAX, null, deviantPerformance);
            deviantMin = statisticsSelector.evaluate(StatisticsSelector.StatisticsMeasures.MIN, null, deviantPerformance);

        }
        while (((correctMax - correctMin) > correctCases || (deviantMax - deviantMin) > deviantCases) && scale < LogAnalyser.YEARS);

        return new double[][]{correctPerformance, deviantPerformance};
    }

    private String format(double number) {
        NumberFormat numberFormat = new DecimalFormat("0.00");
        return numberFormat.format(number);
    }

    private String getType() {
        if (type == Type.POSITIVE) {
            return "Positive deviance";
        } else if (type == Type.NEUTRAL) {
            return "Neutral deviance";
        } else {
            return "Negative deviance";
        }
    }

    private String[] brakeDownStatement(String statement) {
        if (statement.contains("after") || statement.contains("before") || statement.contains("concurrently to")) {
            String a = statement.contains("does not allow") ? "does not allow" : "allows";

            String b = "";
            String c = "";
            if (statement.contains("to occur")) {
                b = statement.substring(statement.indexOf("["), statement.indexOf("to occur") - 1);
                c = "to occur";
            } else if (statement.contains("to be repeated")) {
                b = statement.substring(statement.indexOf("["), statement.indexOf("to be repeated") - 1);
                c = "to be repeated";
            } else if (statement.contains("to be substituted")) {
                b = statement.substring(statement.indexOf("["), statement.indexOf("to be substituted") - 1);
                c = "to be substituted";
            }

            String d = "";
            if (statement.contains("after")) d = "after";
            else if (statement.contains("before")) d = "before";
            else if (statement.contains("concurrently to")) d = "concurrently to";
            else if (statement.contains("in the same run with")) d = "in the same run with";

            String e = statement.substring(statement.indexOf(d) + d.length() + 1);
            if (e.contains("the occurrence of ")) e = e.substring(e.indexOf("the occurrence of ") + 18);

            return new String[]{a, b, c, d, e};
        } else {
            return new String[]{};
        }
    }

    private String[] splitLHSandRHS(String statement) {
        String lhs = statement.contains(", while ") ? statement.substring(4, statement.indexOf(", while")) : statement.substring(4);
        String rhs = statement.contains(", while ") ? statement.substring(statement.indexOf(", while the ") + 12) : "";

        return new String[]{lhs, rhs};
    }

    public void serialize(String path) {
        XLog normal_log = new XFactoryNaiveImpl().createLog();
        normal_log.addAll(normal_traces);
        LogImporter.exportToFile(path + reformulateFromDeviantPointOfView() + " - Normal" + file_ext, normal_log);
        XLog deviant_log = new XFactoryNaiveImpl().createLog();
        deviant_log.addAll(deviant_traces);
        LogImporter.exportToFile(path + reformulateFromDeviantPointOfView() + " - Deviant" + file_ext, deviant_log);
    }

    private String reformulateFromDeviantPointOfView() {
        if (deviant_statement.endsWith("does not")) {
            String reformulated_statement = "The " + deviant_behaviour_name + " does not " + normal_statement.replaceAll("allows", "allow");
            return reformulated_statement;
        } else {
            String reformulated_statement = "The " + deviant_behaviour_name + " " + deviant_statement;
            if (!normal_statement.isEmpty() && !normal_statement.endsWith("does not"))
                reformulated_statement += ", while the " + normal_behaviour_name + " " + normal_statement.replaceAll("allows", "allow");
            return reformulated_statement;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Deviance) {
            Deviance d = (Deviance) o;
            return d.statement.equals(statement);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return statement.hashCode();
    }

    @Override
    public int compareTo(Deviance o) {
        int compare = -Double.compare(this.getRelevance(), o.getRelevance());
        if (compare != 0) return compare;
        return statement.compareTo(o.statement);
    }

    @Override
    public String toString() {
        return reformulateFromDeviantPointOfView();
    }

    public String toFullString(int level, double relevance_threshold) {
        return toString("", level, relevance_threshold);
    }

    private String toString(String indent, int level, double relevance_threshold) {
        StringBuilder stringBuilder = new StringBuilder();
        assessDifferenceInPerformance();
        stringBuilder.append(indent + getType() + " affecting " + format(getRelevance() * 100) + "% of the log");
        if (type != Type.NEUTRAL) stringBuilder.append("\n" + indent + assessDifferenceInPerformance());
        stringBuilder.append("\n" + indent + reformulateFromDeviantPointOfView() + " [" + deviant_traces.size() + " / " + normal_traces.size() + " (deviant traces / normal traces)]");
        if (consequent.size() > 0 && level > 1) {
            List<Deviance> list = new ArrayList<>(consequent);
            Collections.sort(list);
            StringBuilder stringBuilder2 = new StringBuilder();
            for (Deviance deviance : list) {
                if (deviance.getRelevance() >= relevance_threshold) {
                    stringBuilder2.append("\n" + deviance.toString(indent + "   ", level - 1, relevance_threshold));
                }
            }
            if (stringBuilder2.length() > 0) {
                stringBuilder.append("\n" + indent + "causes");
                stringBuilder.append(stringBuilder2.toString());
            }
        }
        return stringBuilder.toString();
    }

    private void clean(boolean reverse, String statement, Set<XTrace> traces) {
        boolean allows = statement.contains("allows");
        if (statement.contains("to be skipped")) {
            if (reverse) allows = !allows;
            String activity = statement.substring(statement.indexOf("[") + 1, statement.indexOf("]"));
            Iterator<XTrace> traceIterator = traces.iterator();
            while (traceIterator.hasNext()) {
                boolean found = false;
                for (XEvent event : traceIterator.next()) {
                    if (XConceptExtension.instance().extractName(event).equals(activity)) {
                        found = true;
                        break;
                    }
                }
                if (allows && found) traceIterator.remove();
                else if (!allows && !found) traceIterator.remove();
            }
        } else if (statement.contains("to occur after")) {
            if (reverse) allows = !allows;
            String activity1 = statement.substring(statement.indexOf("[") + 1, statement.indexOf("]"));
            String[] activities1 = activity1.split(", ");
            String s = statement.substring(statement.indexOf("]") + 1);
            String activity2 = s.substring(s.indexOf("[") + 1, s.indexOf("]"));
            String[] activities2 = activity2.split(", ");
            List<String> list = new ArrayList<>();
            Collections.addAll(list, activities2);
            Collections.addAll(list, activities1);
            removeTrace(allows, list, traces);
        } else if (statement.contains("to be substituted by")) {
            String activity1 = statement.substring(statement.indexOf("[") + 1, statement.indexOf("]"));
            String[] activities1 = activity1.split(", ");
            String s = statement.substring(statement.indexOf("]") + 1);
            String activity2 = s.substring(s.indexOf("[") + 1, s.indexOf("]"));
            String[] activities2 = activity2.split(", ");
            List<String> list1 = new ArrayList<>();
            List<String> list2 = new ArrayList<>();
            if (statement.contains("after the occurrence of")) {
                s = s.substring(s.indexOf("]") + 1);
                String activity3 = s.substring(s.indexOf("[") + 1, s.indexOf("]"));
                String[] activities3 = activity3.split(", ");
                Collections.addAll(list1, activities3);
                Collections.addAll(list2, activities3);
            }
            Collections.addAll(list1, activities1);
            Collections.addAll(list2, activities2);
            if (reverse) removeTrace(allows, list1, traces);
            else removeTrace(allows, list2, traces);
        } else if (statement.contains("to occur in the same run with")) {
            if (reverse) allows = !allows;
            String activity1 = statement.substring(statement.indexOf("[") + 1, statement.indexOf("]"));
            String[] activities1 = activity1.split(", ");
            String s = statement.substring(statement.indexOf("]") + 1);
            String activity2 = s.substring(s.indexOf("[") + 1, s.indexOf("]"));
            String[] activities2 = activity2.split(", ");
            Set<String> set = new HashSet<>();
            Collections.addAll(set, activities2);
            Collections.addAll(set, activities1);

            Iterator<XTrace> traceIterator = traces.iterator();
            while (traceIterator.hasNext()) {
                int i = 0;
                boolean found = false;
                XTrace trace = traceIterator.next();
                for (String a : set) {
                    for (XEvent event : trace) {
                        if (XConceptExtension.instance().extractName(event).equals(a)) {
                            i++;
                            if (i == set.size()) found = true;
                            break;
                        }
                    }
                }
                if (allows && !found) traceIterator.remove();
                else if (!allows && found) traceIterator.remove();
            }
        }
    }

    private void removeTrace(boolean allows, List<String> list, Set<XTrace> traces) {
        Iterator<XTrace> traceIterator = traces.iterator();
        while (traceIterator.hasNext()) {
            int i = 0;
            boolean found = false;
            for (XEvent event : traceIterator.next()) {
                if (XConceptExtension.instance().extractName(event).equals(list.get(i))) {
                    i++;
                    if (i == list.size()) {
                        found = true;
                        break;
                    }
                } else {
                    i = 0;
                }
            }
            if (allows && !found) traceIterator.remove();
            else if (!allows && found) traceIterator.remove();
        }
    }
}
