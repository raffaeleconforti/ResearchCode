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

package com.raffaeleconforti.test;

import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.memorylog.XAttributeLiteralImpl;
import com.raffaeleconforti.singletonlog.XFactorySingletonImpl;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class VariousLogFiltering {

    //    private static String dir = "/Volumes/MobileData/Logs/Consultancies/Unipol/Analysis5/";
    private static String dir = "/Volumes/MobileData/Logs/Consultancies/UoM/Version 5/";
    private static String file_base = "UoM";
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

    private static int[] means = {
            5,
            5,
            10 * 60,
            5,
            5 * 60,
            30 * 60,
            5 * 60,
            30 * 60,
            10 * 60,
            10 * 60,
            5 * 60,
            5 * 60,
            5 * 60,
            10 * 60,
            10 * 60,
            5 * 60,
            10 * 60,
            30 * 60,
            5 * 60,
    };

    private static String[] activities = {
            "Cancel Application", //0
            "Cancel Offer", //1
            "Check Documentation", //2
            "Close Application", //3
            "Contact Customer", //4
            "Create Offer", //5
            "Decline Application", //6
            "Disburse Loan", //7
            "Enquire Interest For New Offer", //8
            "Perform Preliminary Assessment", //9
            "Receive Application", //10
            "Receive Application Cancellation", //11
            "Receive Reviewed Offer", //12
            "Record Application", //13
            "Record Offer Acceptance", //14
            "Record Offer Decline", //15
            "Request Missing Files", //16
            "Send Offer by email", //17
            "Send Offer by post", //18
    };

    private static String[] loan_officers = {
            "Mary Smith",
            "Elizabeth Jones",
            "Sarah Williams",
            "Ann Taylor",
            "John Brown",
            "William Davies",
            "Thomas Evans",
    };

    private static String[] senior_loan_officers = {
            "Jane Thomas",
            "George Johnson",
    };

    private static String accounting_officer = "James Wilson";

    private static double getNext(Random r, double lambda) {
        return Math.log(1 - r.nextDouble()) / (-lambda);
    }

    public static void main(String[] args) throws Exception {
        XLog log1 = LogImporter.importFromFile(factory, "/Volumes/MobileData/Logs/Consultancies/UoM/Version 6/DID_Science Data v4.xes.gz");
        String[] ids = {"67726", "68036", "68939", "70550", "71420", "72958", "73180", "73761", "73914", "73990", "74297", "74556", "74686", "74835", "75461", "75462", "75486", "75648", "75740", "75817", "75934", "76053", "76063", "76103", "76135", "76184", "76245", "76483", "76598", "76631", "76665", "76711", "76744", "76974", "77013", "77047", "77069", "77125", "77227", "77281", "77300", "77491", "77530", "77531", "77535", "77541", "77602", "77608", "77632", "77657", "77733", "77779", "77782", "77798", "77802", "77926", "77927", "77928", "77953", "77974", "77985", "77997", "78015", "78040", "78082", "78112", "78113", "78125", "78136", "78140", "78142", "78143", "78170", "78223", "78284", "78285", "78297", "78307", "78313", "78320", "78327", "78351", "78374", "78383", "78397", "78400", "78483", "78505", "78512", "78526", "78560", "78584", "78594", "78620", "78621", "78632", "78667", "78668", "78674", "78705", "78711", "78787", "78797", "78807", "78809", "78959", "78960", "79110", "79229", "79235", "79254", "79273", "79318", "79357", "79358", "79366", "79371", "79378", "79379", "79386", "79391", "79408", "79418", "79433", "79434", "79445", "79482", "79497", "79524", "79551", "79561", "79597", "79606", "79625", "79634", "79635", "79644", "79675", "79678", "79700", "79703", "79724", "79728", "79745", "79791", "79809", "79813", "79817", "79830", "79835", "79850", "79856", "79857", "79874", "79876", "80000", "80006", "80020", "80023", "80030", "80042", "80045", "80046", "80056", "80081", "80098", "80104", "80118", "80129", "80144", "80206", "80210", "80288", "80307", "80316", "80327", "80336", "80347", "80366", "80373", "80378", "80400", "80417", "80425", "80454", "80469", "80479", "80507", "80536", "80547", "80550", "80593", "80636", "80639", "80647", "80688", "80696", "80713", "80737", "80784", "80820", "80823", "80887", "80894", "80901", "80908", "80920", "80928", "80932", "80943", "80959", "80989", "81010", "81019", "81025", "81027", "81039", "81076", "81093", "81101", "81112", "81130", "81154", "81157", "81158", "81166", "81167", "81182", "81188", "81206", "81224", "81228", "81243", "81251", "81254", "81257", "81260", "81266", "81275", "81277", "81295", "81300", "81315", "81326", "81352", "81356", "81366", "81373", "81405", "81406", "81408", "81415", "81419", "81426", "81432", "81441", "81450", "81452", "81456", "81459", "81466", "81468", "81475", "81476", "81488", "81490", "81495", "81500", "81501", "81511", "81512", "81518", "81521", "81525", "81531", "81556", "81562", "81566", "81587", "81595", "81621", "81629", "81652", "81656", "81661", "81670", "81687", "81688", "81700", "81712", "81811", "81820", "81825", "81837", "81885", "81892", "81915", "81932", "81972", "81980", "81987", "81989", "81990", "81993", "81994", "81999", "82026", "82035", "82071", "82073", "82109", "82115", "82135", "82136", "82137", "82154", "82160", "82164", "82169", "82171", "82172", "82202", "82222", "82234", "82258", "82281", "82302", "82311", "82327", "82328", "82331", "82345", "82404", "82426", "82469", "82473", "82477", "82482", "82490", "82513", "82538", "82541", "82552", "82559"};
        Iterator<XTrace> iterator = log1.iterator();
        while (iterator.hasNext()) {
            XTrace trace = iterator.next();
            String id = XConceptExtension.instance().extractName(trace);
            if (Arrays.binarySearch(ids, id) > -1) {
                iterator.remove();
            }
        }
        LogImporter.exportToFile("/Volumes/MobileData/Logs/Consultancies/UoM/Version 6/", "Log.xes.gz", log1);
    }

    public static void maina(String[] args) throws Exception {
        XLog log1 = LogImporter.importFromFile(factory, "/Users/raffaele/Downloads/simulation_logs.xes.gz");
        XLog log2 = LogImporter.importFromFile(factory, "/Users/raffaele/Downloads/simulation_logs (1).xes.gz");

        XLog log3 = factory.createLog(log1.getAttributes());
        XConceptExtension xce = XConceptExtension.instance();
        XOrganizationalExtension xoe = XOrganizationalExtension.instance();
        XTimeExtension xte = XTimeExtension.instance();
        XLifecycleExtension xle = XLifecycleExtension.instance();

        Random r = new Random(1000);
        int process_id = 1;
        int subprocess_id = 0;
        for (XTrace trace : log1) {
            XTrace t = factory.createTrace(trace.getAttributes());

            int loan_amount = ((int) (Math.abs(r.nextGaussian()) * 19)) * 5000 + 5000;
            String risk = (loan_amount < 50000) ? "Low" : (loan_amount < 250000) ? "Medium" : "High";
            int interest = r.nextInt(10) + 5;

            String loan_officer = loan_officers[r.nextInt(7)];
            String senior_loan_officer = senior_loan_officers[r.nextInt(2)];

            xce.assignName(trace, "" + process_id);
            process_id++;
            for (XEvent event : trace) {
                String event_name = xce.extractName(event);
                int pos = Arrays.binarySearch(activities, event_name);
                int mean = means[pos];
                double duration = getNext(r, (1.0 / mean));

                if (pos < 5 || pos == 8 || pos > 9) {
                    xoe.assignGroup(event, "Loan Officer");
                    xoe.assignResource(event, loan_officer);
                } else if (pos == 5 || pos == 6 || pos == 9) {
                    xoe.assignGroup(event, "Senior Loan Officer");
                    xoe.assignResource(event, senior_loan_officer);
                } else {
                    xoe.assignGroup(event, "Accounting Officer");
                    xoe.assignResource(event, accounting_officer);
                }
                XEvent prev = factory.createEvent((XAttributeMap) event.getAttributes().clone());
                xle.assignTransition(prev, "start");
                xte.assignTimestamp(prev, xte.extractTimestamp(event).getTime() - (long) (duration * 1000));

                XAttribute application = new XAttributeLiteralImpl("Application_ID", "" + process_id);
                event.getAttributes().put("Application_ID", application);
                XAttribute loan_amount_v = new XAttributeLiteralImpl("Loan Amount", "" + loan_amount);
                event.getAttributes().put("Loan Amount", loan_amount_v);
                XAttribute risk_v = new XAttributeLiteralImpl("Risk Level", "" + risk);
                event.getAttributes().put("Risk Level", risk_v);

                if (event_name.equals("Create Offer")) {
                    subprocess_id++;
                    interest--;
                }
                if (event_name.equals("Create Offer") ||
                        event_name.equals("Send Offer by email") ||
                        event_name.equals("Send Offer by post") ||
                        event_name.equals("Contact Customer")) {
                    XAttribute offer = new XAttributeLiteralImpl("Offer_ID", "" + subprocess_id);
                    event.getAttributes().put("Offer_ID", offer);
                    XAttribute interest_v = new XAttributeLiteralImpl("Interest", interest + "%");
                    event.getAttributes().put("Interest", interest_v);
                }
                t.add(prev);
                t.add(event);
            }
            log3.add(t);
        }
        for (XTrace trace : log2) {
            XTrace t = factory.createTrace(trace.getAttributes());

            int loan_amount = ((int) (Math.abs(r.nextGaussian()) * 19)) * 5000 + 5000;
            String risk = (loan_amount < 50000) ? "Low" : (loan_amount < 250000) ? "Medium" : "High";
            int interest = r.nextInt(10) + 5;

            String loan_officer = loan_officers[r.nextInt(7)];
            String senior_loan_officer = senior_loan_officers[r.nextInt(2)];

            xce.assignName(trace, "" + process_id);
            process_id++;
            for (XEvent event : trace) {
                String event_name = xce.extractName(event);
                int pos = Arrays.binarySearch(activities, event_name);
                int mean = means[pos];
                double duration = getNext(r, (1.0 / mean));

                if (pos < 5 || pos == 8 || pos > 9) {
                    xoe.assignGroup(event, "Loan Officer");
                    xoe.assignResource(event, loan_officer);
                } else if (pos == 5 || pos == 6 || pos == 9) {
                    xoe.assignGroup(event, "Senior Loan Officer");
                    xoe.assignResource(event, senior_loan_officer);
                } else {
                    xoe.assignGroup(event, "Accounting Officer");
                    xoe.assignResource(event, accounting_officer);
                }
                XEvent prev = factory.createEvent((XAttributeMap) event.getAttributes().clone());
                xle.assignTransition(prev, "start");
                xte.assignTimestamp(prev, xte.extractTimestamp(event).getTime() - (long) (duration * 1000));

                XAttribute application = new XAttributeLiteralImpl("Application_ID", "" + process_id);
                event.getAttributes().put("Application_ID", application);
                XAttribute loan_amount_v = new XAttributeLiteralImpl("Loan Amount", "" + loan_amount);
                event.getAttributes().put("Loan Amount", loan_amount_v);
                XAttribute risk_v = new XAttributeLiteralImpl("Risk Level", "" + risk);
                event.getAttributes().put("Risk Level", risk_v);

                if (event_name.equals("Create Offer")) {
                    subprocess_id++;
                    interest--;
                }
                if (event_name.equals("Create Offer") ||
                        event_name.equals("Send Offer by email") ||
                        event_name.equals("Send Offer by post") ||
                        event_name.equals("Contact Customer")) {
                    XAttribute offer = new XAttributeLiteralImpl("Offer_ID", "" + subprocess_id);
                    event.getAttributes().put("Offer_ID", offer);
                    XAttribute interest_v = new XAttributeLiteralImpl("Interest", interest + "%");
                    event.getAttributes().put("Interest", interest_v);
                }
                t.add(prev);
                t.add(event);
            }
            log3.add(t);
        }
        log3 = removeDiscoDetails(log3);
        LogImporter.exportToFile("/Users/raffaele/Downloads/", "tutorial.xes.gz", log3);
    }

    public static void maine(String[] args) throws Exception {
        String log_name = file_base;
        log = LogImporter.importFromFile(factory, dir + log_name + "2" + file_ext);

        for (XTrace trace : log) {
            Iterator<XEvent> iterator = trace.iterator();
            String name = null;
            Date time = null;
            while (iterator.hasNext()) {
                XEvent event = iterator.next();
                if (name == null) {
                    name = XConceptExtension.instance().extractName(event);
                    time = XTimeExtension.instance().extractTimestamp(event);
                } else {
                    String name1 = XConceptExtension.instance().extractName(event);
                    Date time1 = XTimeExtension.instance().extractTimestamp(event);
                    long diff = time1.getTime() - time.getTime();
                    if (name.equals(name1) && diff < 60000) {
                        iterator.remove();
                        time = time1;
                    } else {
                        name = name1;
                        time = time1;
                    }
                }
            }
        }
        LogImporter.exportToFile(dir, file_base + "3" + file_ext, log);
    }

    public static void main2(String[] args) throws Exception {
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
