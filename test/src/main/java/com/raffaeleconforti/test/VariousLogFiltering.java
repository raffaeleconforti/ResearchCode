package com.raffaeleconforti.test;

import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.singletonlog.XFactorySingletonImpl;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

public class VariousLogFiltering {

    private static String dir = "/Volumes/MobileData/Logs/Consultancies/Unipol/Analysis3/";
    private static String file_base = "Unipol - 85";
    private static String file_ext = ".xes.gz";
    private static XTimeExtension xte = XTimeExtension.instance();

    private static XLog log;
    private static String file;
    private static Iterator<XTrace> traceIterator;
    private static String[] loc = new String[]{"04 LOM", "12 LAZ"};
    private static String[] liq = new String[]{"APB", "LIQ", "PD"};

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

    public static void main(String[] args) throws Exception {
        String[] files = new String[]{
                "1 - General Process (85%)"
        };

        for (String l : loc) {
            for (String file : files) {
                String log_name = dir + "Logs Top16/" + file;
                log = LogImporter.importFromFile(new XFactorySingletonImpl(), log_name + "" + file_ext);
                traceIterator = log.iterator();
                while (traceIterator.hasNext()) {
                    XTrace trace = traceIterator.next();
                    boolean keep = true;
                    for (XEvent event : trace) {
                        if (!event.getAttributes().get("REGIONE_ACCADIMENTO").toString().equals(l)) {
                            keep = false;
                            break;
                        }
                    }
                    if (!keep) traceIterator.remove();
                }
                LogImporter.exportToFile(dir + "Logs Top16/", l.substring(3) + file_ext, log);
            }
        }

        for (String file : files) {
            String log_name = dir + "Logs Top16/" + file;
            log = LogImporter.importFromFile(new XFactorySingletonImpl(), log_name + "" + file_ext);
            traceIterator = log.iterator();
            while (traceIterator.hasNext()) {
                XTrace trace = traceIterator.next();
                Calendar a = getCalendar(xte.extractTimestamp(trace.get(0)));
                Calendar b = getCalendar(xte.extractTimestamp(trace.get(trace.size() - 1)));
                if (!greater(a, b, 30)) traceIterator.remove();
            }
            LogImporter.exportToFile(dir + "Logs Top16/", "g30" + file_ext, log);
        }

        for (String file : files) {
            String log_name = dir + "Logs Top16/" + file;
            log = LogImporter.importFromFile(new XFactorySingletonImpl(), log_name + "" + file_ext);
            traceIterator = log.iterator();
            while (traceIterator.hasNext()) {
                XTrace trace = traceIterator.next();
                Calendar a = getCalendar(xte.extractTimestamp(trace.get(0)));
                Calendar b = getCalendar(xte.extractTimestamp(trace.get(trace.size() - 1)));
                if (greater(a, b, 30)) traceIterator.remove();
            }
            LogImporter.exportToFile(dir + "Logs Top16/", "le30" + file_ext, log);
        }

        for (String l : loc) {
            for (String file : files) {
                String log_name = dir + "Logs Top16/" + file;
                log = LogImporter.importFromFile(new XFactorySingletonImpl(), log_name + "" + file_ext);
                traceIterator = log.iterator();
                while (traceIterator.hasNext()) {
                    XTrace trace = traceIterator.next();
                    boolean keep = true;
                    for (XEvent event : trace) {
                        if (!event.getAttributes().get("REGIONE_ACCADIMENTO").toString().equals(l)) {
                            keep = false;
                            break;
                        }
                    }
                    if (!keep) traceIterator.remove();
                    else {
                        Calendar a = getCalendar(xte.extractTimestamp(trace.get(0)));
                        Calendar b = getCalendar(xte.extractTimestamp(trace.get(trace.size() - 1)));
                        if (!greater(a, b, 30)) traceIterator.remove();
                    }
                }
                LogImporter.exportToFile(dir + "Logs Top16/", l.substring(3) + " g30" + file_ext, log);
            }

            for (String file : files) {
                String log_name = dir + "Logs Top16/" + file;
                log = LogImporter.importFromFile(new XFactorySingletonImpl(), log_name + "" + file_ext);
                traceIterator = log.iterator();
                while (traceIterator.hasNext()) {
                    XTrace trace = traceIterator.next();
                    boolean keep = true;
                    for (XEvent event : trace) {
                        if (!event.getAttributes().get("REGIONE_ACCADIMENTO").toString().equals(l)) {
                            keep = false;
                            break;
                        }
                    }
                    if (!keep) traceIterator.remove();
                    else {
                        Calendar a = getCalendar(xte.extractTimestamp(trace.get(0)));
                        Calendar b = getCalendar(xte.extractTimestamp(trace.get(trace.size() - 1)));
                        if (greater(a, b, 30)) traceIterator.remove();
                    }
                }
                LogImporter.exportToFile(dir + "Logs Top16/", l.substring(3) + " le30" + file_ext, log);
            }
        }
    }

    public static void main1(String[] args) throws Exception {
//        file = file_base + file_ext;
//        log = LogImporter.importFromFile(new XFactorySingletonImpl(), dir + file);
//        traceIterator = log.iterator();
//        while (traceIterator.hasNext()) {
//            XTrace trace = traceIterator.next();
//            if(trace.size() == 0) {
//                traceIterator.remove();
//                continue;
//            }
//            Iterator<XEvent> eventIterator = trace.iterator();
//            while (eventIterator.hasNext()) {
//                XEvent event = eventIterator.next();
//                if (event.getAttributes().get("OK").toString().equals("NO") ||
//                        event.getAttributes().get("OK").toString().equals("A2") ||
//                        event.getAttributes().get("OK").toString().equals("A4") ||
//                        event.getAttributes().get("OK").toString().equals("A5")) {
//                    eventIterator.remove();
//                }
//            }
//        }
//        LogImporter.exportToFile(dir, file_base + "2" + file_ext, log);
//        /* */
//
//        file = file_base + "2" + file_ext;
//        log = LogImporter.importFromFile(new XFactorySingletonImpl(), dir + file);
//        for(XTrace trace : log) {
//            for(XEvent event : trace) {
//                Iterator<String> iterator = event.getAttributes().keySet().iterator();
//                while (iterator.hasNext()) {
//                    String attributeName = iterator.next();
//                    if(!(("concept:name").equals(attributeName) ||
//                            ("concept:instance").equals(attributeName) ||
//                            ("lifecycle:transition").equals(attributeName) ||
//                            ("time:timestamp").equals(attributeName) ||
//                            ("ANOMALIA_EVENT_DATE").equals(attributeName) ||
//                            ("EVENTO_PRECEDE_APERTURA").equals(attributeName) ||
//                            ("EVENTO_SEGUE_CHIUSURA").equals(attributeName) ||
//                            ("IMPORTO_SINISTRO").equals(attributeName) ||
//                            ("MODALITA_LIQUIDAZIONE").equals(attributeName) ||
//                            ("REGIONE_ACCADIMENTO").equals(attributeName) ||
//                            ("GEST").equals(attributeName) ||
//                            ("OK").equals(attributeName) ||
//                            ("canale_apertura_sx").equals(attributeName) ||
//                            ("fascia_importo_sx").equals(attributeName) ||
//                            ("data_sinistro").equals(attributeName) ||
//                            ("data_apertura").equals(attributeName) ||
//                            ("data_effettiva_den").equals(attributeName) ||
//                            ("gg_avvenim_denuncia").equals(attributeName) ||
//                            ("gg_denuncia_apertura").equals(attributeName) ||
//                            ("accessi_altri").equals(attributeName) ||
//                            ("ff_passivo").equals(attributeName) ||
//                            ("ff_attivo").equals(attributeName)
//                    )) {
//                        event.getAttributes().remove(attributeName);
//                    }
//                }
//            }
//        }
//        LogImporter.exportToFile(dir, file_base + "_reduced" + file_ext, log);
//        /* */
//
//        file = file_base + " - reduced" + file_ext;
//        for(String l : liq) {
//            log = LogImporter.importFromFile(new XFactorySingletonImpl(), dir + file);
//            traceIterator = log.iterator();
//            while (traceIterator.hasNext()) {
//                XTrace trace = traceIterator.next();
//                boolean keep = true;
//                for (XEvent event : trace) {
//                    if (!event.getAttributes().get("MODALITA_LIQUIDAZIONE").toString().equals(l)) {
//                        keep = false;
//                        break;
//                    }
//                }
//                if (!keep) traceIterator.remove();
//            }
//            LogImporter.exportToFile(dir, file_base + " - " + l + file_ext, log);
//        }
//        /* */
//
//        file = file_base + " - reduced" + file_ext;
//        for(String l : loc) {
//            log = LogImporter.importFromFile(new XFactorySingletonImpl(), dir + file);
//            traceIterator = log.iterator();
//            while (traceIterator.hasNext()) {
//                XTrace trace = traceIterator.next();
//                boolean keep = true;
//                for (XEvent event : trace) {
//                    if (!event.getAttributes().get("REGIONE_ACCADIMENTO").toString().equals(l)) {
//                        keep = false;
//                        break;
//                    }
//                }
//                if (!keep) traceIterator.remove();
//            }
//            LogImporter.exportToFile(dir, file_base + " - " + l.substring(3) + file_ext, log);
//        }
//        /* */

        file = file_base + " - reduced" + file_ext;
        XTimeExtension xte = XTimeExtension.instance();
        log = LogImporter.importFromFile(new XFactorySingletonImpl(), dir + file);
        traceIterator = log.iterator();
        while (traceIterator.hasNext()) {
            XTrace trace = traceIterator.next();
            Calendar a = getCalendar(xte.extractTimestamp(trace.get(0)));
            Calendar b = getCalendar(xte.extractTimestamp(trace.get(trace.size() - 1)));
            if (!greater(a, b, 30)) {
                traceIterator.remove();
            }
        }
        LogImporter.exportToFile(dir, file_base + " - >30" + file_ext, log);

        log = LogImporter.importFromFile(new XFactorySingletonImpl(), dir + file);
        traceIterator = log.iterator();
        while (traceIterator.hasNext()) {
            XTrace trace = traceIterator.next();
            Calendar a = getCalendar(xte.extractTimestamp(trace.get(0)));
            Calendar b = getCalendar(xte.extractTimestamp(trace.get(trace.size() - 1)));
            if (greater(a, b, 30)) {
                traceIterator.remove();
            }
        }
        LogImporter.exportToFile(dir, file_base + " - <=30" + file_ext, log);

        String[] logs = {" - <=30", " - >30"};
        for (String a : logs) {
            file = file_base + a + file_ext;
            for (String l : loc) {
                log = LogImporter.importFromFile(new XFactorySingletonImpl(), dir + file);
                traceIterator = log.iterator();
                while (traceIterator.hasNext()) {
                    XTrace trace = traceIterator.next();
                    boolean keep = true;
                    for (XEvent event : trace) {
                        if (!event.getAttributes().get("REGIONE_ACCADIMENTO").toString().equals(l)) {
                            keep = false;
                            break;
                        }
                    }
                    if (!keep) traceIterator.remove();
                }
                LogImporter.exportToFile(dir, file_base + a + " - " + l.substring(3) + file_ext, log);
            }
        }
        /* */
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
