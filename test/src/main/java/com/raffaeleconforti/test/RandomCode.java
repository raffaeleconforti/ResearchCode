package com.raffaeleconforti.test;

import com.opencsv.CSVReader;
import com.raffaeleconforti.log.util.LogImporter;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;

import java.io.FileReader;
import java.io.IOException;


/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 3/7/17.
 */
public class RandomCode {

    public static XConceptExtension xce = XConceptExtension.instance();
    public static XTimeExtension xte = XTimeExtension.instance();
    public static XOrganizationalExtension xoe = XOrganizationalExtension.instance();

    //Graph
    public static void main(String[] args) throws Exception {

        String path = "/Volumes/Data/Dropbox/Consultancies/Cineca-UniParma/esse3/results/Log_16-17/";
        String name = "Esse3_log_16-17_Final.xes.gz";
        XLog log = LogImporter.importFromFile(new XFactoryNaiveImpl(), path + name);

        Set<String> set = new HashSet<>();

//        for (XTrace trace : log) {
//            for (int i = 0; i < trace.size() - 1; i++) {
//                XEvent e1 = trace.get(i);
//                XEvent e2 = trace.get(i + 1);
//
//                String n1 = xce.extractName(e1);
//                String n2 = xce.extractName(e2);
//
//                Date t1 = xte.extractTimestamp(e1);
//                Date t2 = xte.extractTimestamp(e2);
//
//                if (t1.equals(t2)) {
//                    if(n2.equals("ATTI_AMM_STU_CREATI") && n1.equals("ATTI_AMM_STU_ASS")) {
//                        trace.set(i, e2);
//                        trace.set(i + 1, e1);
//                    }
//                }
//            }
//        }

        for (XTrace trace : log) {
            for (XEvent e : trace) {
                XAttribute a = e.getAttributes().get("OPERATORE_GRUPPO_NOME");
                if(a != null) {
                    String role = ((XAttributeLiteralImpl) a).getValue();
                    xoe.assignResource(e, role);
                }else {
                    e.getAttributes().remove("org:resource");
                }
            }
        }

        LogImporter.exportToFile(path, "Esse3_log_16-17_SocialNetwork.xes.gz", log);

        System.out.println(set);
    }

//    //Graph
//    public static void main(String[] args) throws Exception {
//
//        String[] acquisti = new String[] {"ORDINE\\\\Avvia",
//                "ORDINE\\\\Pubblica",
//                "ORDINE\\\\Crea Scrittura Coan Anticipata per Dg",
//                "ORDINE\\\\Crea Scrittura Coan Anticipata Dett Dg",
//                "ORDINE\\\\(Vai a contabilizzato)",
//                "ORDINE\\\\Cancella Associazione Scrittura Coan",
//                "ORDINE\\\\(Vai a contabilizzato)",
//                "FATTURA_ACQUISTO\\\\Avvia",
//                "FATTURA_ACQUISTO\\\\Crea Scrittura Coge Prima",
//                "FATTURA_ACQUISTO\\\\Crea Scrittura Coan Normale",
//                "FATTURA_ACQUISTO\\\\(Vai a contabilizzato)",
//                "FATTURA_ACQUISTO\\\\Autorizza Pagamento",
//                "FATTURA_ACQUISTO\\Crea buono carico confermato",
//                "ORDINATIVO_PAGAMENTO_INCASSO\\\\Avvia",
//                "FATTURA_ACQUISTO\\\\Crea Ordinativo"
//        };
//
//        String[] missioni = new String[] {"MISSIONE\\\\Elabora e Calcola",
//                "MISSIONE\\\\Ripristina da elaborare e calcolare",
//                "MISSIONE\\\\Necessita Rielaborazione e Ricalcolo",
//                "MISSIONE\\\\Crea Scrittura Coge Prima",
//                "MISSIONE\\\\Crea Scrittura Coan Normale",
//                "MISSIONE\\\\(Vai a contabilizzato)",
//                "MISSIONE\\\\Autorizza Pagamento",
//                "MISSIONE\\\\Annulla Autorizzazione",
//                "ANTICIPO_MISSIONE\\\\Avvia",
//                "ANTICIPO_MISSIONE\\\\Transizione Vai ad Aperto",
//                "ANTICIPO_MISSIONE\\\\Transizione Vai a Valuta Cedolino",
//                "ANTICIPO_MISSIONE\\\\Crea Scrittura Coge Prima",
//                "ANTICIPO_MISSIONE\\\\Autorizza Pagamento",
//                "ANTICIPO_MISSIONE\\\\Crea Ordinativo",
//                "ORDINATIVO_PAGAMENTO_INCASSO\\\\Avvia",
//                "MISSIONE\\\\Crea Ordinativo",
//                "MISSIONE\\\\Pubblica",
//                "ANTICIPO_MISSIONE\\\\Valuta importo da pagato",
//                "ANTICIPO_MISSIONE\\\\Vai in chiuso"
//        };
//
//        String[] tasse = new String[] {"RIMBORSO_TASSE\\\\Esporta Stipendio",
//                "RIMBORSO_TASSE\\\\Rendi Contabilizzabile Coge",
//                "RIMBORSO_TASSE\\\\Rendi Contabilizzabile Coan",
//                "RIMBORSO_TASSE\\\\Crea Scrittura Coge Prima",
//                "RIMBORSO_TASSE\\\\Crea Scrittura Coan Normale",
//                "RIMBORSO_TASSE\\\\(Vai a contabilizzato)",
//                "ORDINATIVO_PAGAMENTO_INCASSO\\\\Avvia",
//                "RIMBORSO_TASSE\\\\Crea Ordinativo"
//        };
//
//        String[] vendite = new String[] {"FATTURA_PROFORMA\\\\Avvia",
//                "FATTURA_PROFORMA\\\\Crea Scrittura Coge Anticipata",
//                "FATTURA_PROFORMA\\\\Crea Scrittura Coan Normale",
//                "FATTURA_PROFORMA\\\\(Vai a contabilizzato)",
//                "FATTURA_VENDITA\\\\Avvia",
//                "FATTURA_VENDITA\\\\Crea Scrittura Iva per Fattura Vendita",
//                "FATTURA_VENDITA\\\\Cancella Associazione Scrittura Iva",
//                "FATTURA_VENDITA\\\\Crea Scrittura Coge Prima",
//                "FATTURA_VENDITA\\\\Crea Scrittura Coan Normale",
//                "FATTURA_VENDITA\\\\(Vai a contabilizzato)",
//                "GENERICO_ENTRATA\\\\Avvia",
//                "GENERICO_ENTRATA\\\\Crea Scrittura Coge Prima",
//                "GENERICO_ENTRATA\\\\Crea Scrittura Coan Normale",
//                "GENERICO_ENTRATA\\\\(Vai a contabilizzato)",
//                "ORDINATIVO_PAGAMENTO_INCASSO\\\\Avvia",
//                "FATTURA_VENDITA\\\\Crea Ordinativo",
//                "GENERICO_ENTRATA\\\\Crea Ordinativo"
//        };
//
//        String[] names = new String[] {"Acquisti", "Missioni", "Tasse", "Vendite"};
//        String[][] orders = new String[][] {acquisti, missioni, tasse, vendite};
//        Map<String, String> roles = convertRoles();
//
//        for(int i = 0; i < names.length; i++) {
//            String name = names[i];
//            String[] order = orders[i];
//
//            String path = "/Volumes/Data/Dropbox/Consultancies/Cineca-UniParma/u-gov/logs/Log_CG_DG/Result/Arcs/" + name + "/";
//            XLog log = LogImporter.importFromFile(new XFactoryNaiveImpl(), path + name + "_50_Instances.xes.gz");
//
//            Set<String> set = new HashSet<>();
//
//            Comparator<XEvent> comparator = new Comparator<XEvent>() {
//                @Override
//                public int compare(XEvent o1, XEvent o2) {
//                    Date t1 = xte.extractTimestamp(o1);
//                    Date t2 = xte.extractTimestamp(o2);
//
//                    if (t1.equals(t2)) {
//                        String n1 = xce.extractName(o1);
//                        String n2 = xce.extractName(o2);
//
//                        int i1 = find(order, n1);
//                        int i2 = find(order, n2);
//
//                        if (i1 > 0 && i2 > 0) {
//                            return Integer.compare(i1, i2);
//                        }
//                        return 0;
//                    } else {
//                        return t1.compareTo(t2);
//                    }
//                }
//
//                @Override
//                public boolean equals(Object obj) {
//                    return false;
//                }
//            };
//
//            log.getAttributes().remove("creator");
//            log.getAttributes().remove("library");
//            log.getClassifiers().get(0).getDefiningAttributeKeys()[0] = "NOME_TIPO_DG";
//            log.getClassifiers().get(0).getDefiningAttributeKeys()[1] = "EDGE_TEXT";
//
//            //        log.getClassifiers().remove("Resource");
//            log.getGlobalTraceAttributes().remove(2);
//            log.getGlobalTraceAttributes().remove(1);
////        log.getGlobalEventAttributes().remove(15);
//            log.getGlobalEventAttributes().remove(6);
//            log.getGlobalEventAttributes().remove(5);
//            log.getGlobalEventAttributes().remove(4);
//
//            for (XTrace trace : log) {
//                trace.getAttributes().remove("variant");
//                trace.getAttributes().remove("variant-index");
//                trace.getAttributes().remove("creator");
//
//                Collections.sort(trace, comparator);
//                for (XEvent event : trace) {
//                    event.getAttributes().remove("(case)_creator");
//                    event.getAttributes().remove("(case)_variant");
//                    event.getAttributes().remove("(case)_variant-index");
//
//                    XAttribute a = event.getAttributes().get("ID_UO_ORIGINE");
//                    String role = roles.get(((XAttributeLiteralImpl) a).getValue());
//                    xoe.assignResource(event, role);
//
//                    set.add(xce.extractName(event));
//                }
//            }
//
//            path = "/Volumes/Data/Dropbox/Consultancies/Cineca-UniParma/u-gov/logs/Log_CG_DG/Result/Social Network/";
//            LogImporter.exportToFile(path, name + "_50_Instances3.xes.gz", log);
//
//            System.out.println(set);
//        }
//    }

    private static int find(String[] names, String name) {
        for(int i = 0; i < names.length; i++) {
            if(names[i].equals(name)) return i;
        }
        return -1;
    }

    private static Map<String, String> convertRoles() throws IOException {
        CSVReader reader = new CSVReader(new FileReader("/Volumes/Data/Dropbox/Consultancies/Cineca-UniParma/u-gov/logs/unipr_uo.csv"),',');


        Map<String, String> roles = new HashMap<String, String>();
        String [] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            List<String> tmpAttributes = new ArrayList<String>(Arrays.asList(nextLine));
            roles.put(removeQuotes(tmpAttributes.get(0)), introduceNewLine(removeQuotes(tmpAttributes.get(1))));
        }

        return roles;
    }

    private static String removeQuotes(String value) {
        if(value != null) value = value.replace("\"", "");
        return value;
    }

    private static String introduceNewLine(String value) {
        String s = "<html><b><center><p>";// style="color:navy"
        StringTokenizer st = new StringTokenizer(value, " ");
        String tmp = "";
        while (st.hasMoreTokens()) {
            if(tmp.length() > 15) {
                s += tmp + "<br>";
                tmp = "";
            }else {
                tmp += " ";
            }
            tmp += st.nextToken();
        }
        s += tmp + "</center></b></p></html>";
        return s;
    }
}
