package com.raffaeleconforti.test;

import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.singletonlog.XFactorySingletonImpl;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import java.text.SimpleDateFormat;
import java.util.*;

public class VariousLogFiltering {

    private static String dir = "/Volumes/MobileData/Logs/Consultancies/Unipol/Analysis5/";
    private static String file_base = "APB";
    private static String file_ext = ".xes.gz";
    private static XFactory factory = new XFactoryNaiveImpl();
    private static XTimeExtension xte = XTimeExtension.instance();

    private static XLog log;
    private static String file;
    private static Iterator<XTrace> traceIterator;
    private static String canale = "canale_apertura_sx";
    private static String[] canale_values = new String[]{"AGENZIA", "CALL CENTER"};
    private static String regione = "REGIONE_ACCADIMENTO";
    private static String[] regione_values = new String[]{"04 LOM", "08 EMI", "12 LAZ"};
    private static String riapertura = "concept:name";
    private static String[] riapertura_values = new String[]{"Riapertura o cambio di esito"};
    private static String forfait = "ff_attivo";
    private static String[] forfait_values = new String[]{"FF_RECUPERATO_TUTTO"};
    private static int time = 30;

    public static void main(String[] args) throws Exception {
        String log_name = file_base;
        log = LogImporter.importFromFile(factory, dir + "/" + log_name + file_ext);

        log = removeDiscoDetails(log);
        LogImporter.exportToFile(dir + file_base + "/", "1 - " + file_base + file_ext, log);
        LogImporter.exportToFile(dir + file_base + "/", "3 - LE30" + file_ext, splitTimes(log, time)[0]);
        LogImporter.exportToFile(dir + file_base + "/", "3 - G30" + file_ext, splitTimes(log, time)[1]);
    }

    public static void main1(String[] args) throws Exception {
        String log_name = file_base;
        log = LogImporter.importFromFile(factory, dir + "/" + log_name + file_ext);

        log = removeDiscoDetails(log);
        XLog log1 = retainOrRemoveOnAttribute(log, canale, Arrays.asList(canale_values), true);
        LogImporter.exportToFile(dir + file_base + "/", "1 - " + file_base + file_ext, log1);

        XLog[] logs1 = splitOnAttribute(log1, regione, regione_values);
        for (int i = 0; i < logs1.length; i++) {
            LogImporter.exportToFile(dir + file_base + "/", "2 - " + regione_values[i] + file_ext, logs1[i]);
            XLog[] logs3 = splitTimes(logs1[i], time);
            LogImporter.exportToFile(dir + file_base + "/", "3 - " + regione_values[i] + " - LE30" + file_ext, logs3[0]);
            LogImporter.exportToFile(dir + file_base + "/", "3 - " + regione_values[i] + " - G30" + file_ext, logs3[1]);
        }

        XLog[] logs4 = splitOnAttribute(log1, canale, canale_values);
        for (int i = 0; i < logs4.length; i++) {
            LogImporter.exportToFile(dir + file_base + "/", "4 - " + canale_values[i] + file_ext, logs4[i]);
        }

        XLog[] logs5 = splitTimes(log1, time);
        LogImporter.exportToFile(dir + file_base + "/", "5 - LE30" + file_ext, logs5[0]);
        LogImporter.exportToFile(dir + file_base + "/", "5 - G30" + file_ext, logs5[1]);

        XLog log6a = retainOrRemoveOnAttribute(logs5[1], riapertura, Arrays.asList(riapertura_values), true);
        XLog log6b = retainOrRemoveOnAttribute(logs5[1], riapertura, Arrays.asList(riapertura_values), false);
        LogImporter.exportToFile(dir + file_base + "/", "6 - Riapertura" + file_ext, log6a);
        LogImporter.exportToFile(dir + file_base + "/", "6 - NoRiapertura" + file_ext, log6b);

        XLog log7a = retainOrRemoveOnAttribute(log1, forfait, Arrays.asList(forfait_values), true);
        XLog log7b = retainOrRemoveOnAttribute(log1, forfait, Arrays.asList(forfait_values), false);
        LogImporter.exportToFile(dir + file_base + "/", "7 - RecuperatoTutto" + file_ext, log7a);
        LogImporter.exportToFile(dir + file_base + "/", "7 - NoRecuperatoTutto" + file_ext, log7b);
    }

    private static XLog retainOrRemoveOnAttribute(XLog log, String attribute, Collection<String> attribute_values, boolean retain) throws Exception {
        XLog log1 = factory.createLog(log.getAttributes());
        for (XTrace trace : log) {
            boolean keep = false;
            for (XEvent event : trace) {
                if (event.getAttributes().get(attribute) != null && attribute_values.contains(event.getAttributes().get(attribute).toString())) {
                    keep = true;
                    break;
                }
            }
            if (retain && keep) log1.add(trace);
            if (!retain && !keep) log1.add(trace);
        }
        return log1;
    }

    private static XLog removeDiscoDetails(XLog log) {
        for (XTrace trace : log) {
            for (String attributeName : trace.getAttributes().keySet().toArray(new String[12])) {
                if ((("creator").equals(attributeName) ||
                        ("variant").equals(attributeName) ||
                        ("variant-index").equals(attributeName))) {
                    trace.getAttributes().remove(attributeName);
                }
            }
            Iterator<XEvent> iterator = trace.iterator();
            while (iterator.hasNext()) {
                XEvent event = iterator.next();
                for (String attributeName : event.getAttributes().keySet().toArray(new String[12])) {
                    if ((("Activity").equals(attributeName) ||
                            ("(case) creator").equals(attributeName) ||
                            ("(case) variant").equals(attributeName) ||
                            ("(case) variant-index").equals(attributeName))) {
                        event.getAttributes().remove(attributeName);
                    }
                }
                if (XConceptExtension.instance().extractName(event).equals("Start")) {
                    iterator.remove();
                }
                if (XConceptExtension.instance().extractName(event).equals("End")) {
                    iterator.remove();
                }
            }
        }
        return log;
    }

    private static XLog[] splitOnAttribute(XLog log, String attribute, String[] attribute_values) throws Exception {
        XLog[] logs = new XLog[attribute_values.length];
        for (int i = 0; i < attribute_values.length; i++) {
            String l = attribute_values[i];
            XLog log1 = factory.createLog(log.getAttributes());
            for (XTrace trace : log) {
                boolean keep = true;
                for (XEvent event : trace) {
                    if (!event.getAttributes().get(attribute).toString().equals(l)) {
                        keep = false;
                        break;
                    }
                }
                if (keep) log1.add(trace);
            }
            logs[i] = log1;
        }
        return logs;
    }

    private static XLog[] splitTimes(XLog log, int time) throws Exception {
        XLog[] logs = new XLog[2];
        XLog log1 = factory.createLog(log.getAttributes());
        XLog log2 = factory.createLog(log.getAttributes());
        for (XTrace trace : log) {
            Calendar a = getCalendar(xte.extractTimestamp(trace.get(0)));
            Calendar b = getCalendar(xte.extractTimestamp(trace.get(trace.size() - 1)));
            if (!greater(a, b, time)) {
                log1.add(trace);
            } else {
                log2.add(trace);
            }
        }
        logs[0] = log1;
        logs[1] = log2;
        return logs;
    }

    public static void main5(String[] args) throws Exception {
        String[] files = new String[]{
                "3 - General Process (85%)",
                "g30",
                "LAZ g30",
                "LAZ le30",
                "LAZ",
                "le30",
                "LOM g30",
                "LOM le30",
                "LOM"
        };

        for (String file : files) {
            String log_name = dir + "Logs Top15/" + file;
            log = LogImporter.importFromFile(new XFactorySingletonImpl(), log_name + "" + file_ext);
            for (XTrace trace : log) {
                for (XEvent event : trace) {
                    for (String attributeName : event.getAttributes().keySet().toArray(new String[12])) {
                        if (!(("concept:name").equals(attributeName) ||
                                ("lifecycle:transition").equals(attributeName) ||
                                ("time:timestamp").equals(attributeName))) {
                            event.getAttributes().remove(attributeName);
                        }
                    }
                }
            }
            LogImporter.exportToFile(dir + "Essential/", file + file_ext, log);
        }
    }

    public static void main4(String[] args) throws Exception {
        String[] files = new String[]{
                "1_Unipol_Modalita_APB",
                "1_Unipol_Modalita_LIQ",
                "1_Unipol_Modalita_PD",
                "1_Unipol_Regione_LAZ",
                "1_Unipol_Regione_LOM",
                "1_Unipol_tracking_reduced"
        };

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        Date start = dateFormat.parse("02/02/2018 00:00:00");
        for (String file : files) {
            String log_name = dir + file;
            log = LogImporter.importFromFile(new XFactorySingletonImpl(), log_name + "" + file_ext);
            Iterator<XTrace> traceIterator = log.iterator();
            while (traceIterator.hasNext()) {
                XTrace trace = traceIterator.next();
                boolean keep = true;
                for (XEvent event : trace) {
                    XAttributeTimestamp timestamp = (XAttributeTimestamp) event.getAttributes().get("time:timestamp");
                    if (timestamp.getValue().before(start)) {
                        keep = false;
                        break;
                    }
                }
                if (!keep) traceIterator.remove();
            }
            LogImporter.exportToFile(dir + "timestamp/", file + "_timestamp" + file_ext, log);
        }
    }

    private static boolean greater(Calendar first, Calendar second, int i) {
        int a_year = first.get(Calendar.YEAR);
        int b_year = second.get(Calendar.YEAR);
        int a_day = first.get(Calendar.DAY_OF_YEAR);
        int b_day = second.get(Calendar.DAY_OF_YEAR);
        if (b_year - a_year > 0 && ((365 - a_day) + b_day > i)) return true;
        else if (b_day - a_day > i) return true;
        else if (b_day - a_day == i) {
            int a_hour = first.get(Calendar.HOUR_OF_DAY);
            int b_hour = second.get(Calendar.HOUR_OF_DAY);
            if (b_hour > a_hour) return true;
            else if (b_hour == a_hour) {
                int a_min = first.get(Calendar.MINUTE);
                int b_min = second.get(Calendar.MINUTE);
                if (b_min > a_min) return true;
                else if (b_min == a_min) {
                    int a_sec = first.get(Calendar.SECOND);
                    int b_sec = second.get(Calendar.SECOND);
                    if (b_sec > a_sec) return true;
                    else if (b_sec == a_sec) {
                        int a_mil = first.get(Calendar.MILLISECOND);
                        int b_mil = second.get(Calendar.MILLISECOND);
                        if (b_mil > a_mil) return true;
                    }
                }
            }
        }
        return false;
    }

    public static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

}
