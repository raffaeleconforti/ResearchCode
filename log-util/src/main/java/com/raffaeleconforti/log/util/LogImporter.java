package com.raffaeleconforti.log.util;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.in.*;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collection;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 16/02/15.
 */

public class LogImporter {

    public static XLog importFromFile(XFactory factory, String location) throws Exception {
        if(location.endsWith("mxml.gz")) {
            return importFromInputStream(new FileInputStream(location), new XMxmlGZIPParser(factory));
        }else if(location.endsWith("mxml")) {
            return importFromInputStream(new FileInputStream(location), new XMxmlParser(factory));
        }else if(location.endsWith("xes.gz")) {
            return importFromInputStream(new FileInputStream(location), new XesXmlGZIPParser(factory));
        }else if(location.endsWith("xes")) {
            return importFromInputStream(new FileInputStream(location), new XesXmlParser(factory));
        }
        return null;
    }

    public static void exportToFile(String name, XLog log) {
        if(name.endsWith("mxml.gz")) {
            exportToInputStream(log, name, new XMxmlGZIPSerializer());
        }else if(name.endsWith("mxml")) {
            exportToInputStream(log, name, new XMxmlSerializer());
        }else if(name.endsWith("xes.gz")) {
            exportToInputStream(log, name, new XesXmlGZIPSerializer());
        }else if(name.endsWith("xes")) {
            exportToInputStream(log, name, new XesXmlSerializer());
        }else {
            exportToInputStream(log, name, new XesXmlGZIPSerializer());
        }
    }

    public static void exportToFile(String path, String name, XLog log) {
        if(name.endsWith("mxml.gz")) {
            exportToInputStream(log, path + name, new XMxmlGZIPSerializer());
        }else if(name.endsWith("mxml")) {
            exportToInputStream(log, path + name, new XMxmlSerializer());
        }else if(name.endsWith("xes.gz")) {
            exportToInputStream(log, path + name, new XesXmlGZIPSerializer());
        }else if(name.endsWith("xes")) {
            exportToInputStream(log, path + name, new XesXmlSerializer());
        }else {
            exportToInputStream(log, path + name, new XesXmlGZIPSerializer());
        }
    }

    public static XLog importFromInputStream(InputStream inputStream, XParser parser) throws Exception {
        Collection<XLog> logs;
        try {
            logs = parser.parse(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            logs = null;
        }
        if (logs == null) {
            // try any other parser
            for (XParser p : XParserRegistry.instance().getAvailable()) {
                if (p == parser) {
                    continue;
                }
                try {
                    logs = p.parse(inputStream);
                    if (logs.size() > 0) {
                        break;
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                    logs = null;
                }
            }
        }

        // log sanity checks;
        // notify user if the log is awkward / does miss crucial information
        if (logs == null || logs.size() == 0) {
            throw new Exception("No processes contained in log!");
        }

        XLog log = logs.iterator().next();
        if (XConceptExtension.instance().extractName(log) == null) {
            XConceptExtension.instance().assignName(log, "Anonymous log imported from ");
        }

        if (log.isEmpty()) {
            throw new Exception("No process instances contained in log!");
        }

        return log;
    }

    public static void exportToInputStream(XLog log, String name, XSerializer serializer) {
        FileOutputStream outputStream;
        try {
            File f = new File(name);
            if(!f.exists()) f.createNewFile();
            outputStream = new FileOutputStream(f);
            serializer.serialize(log, outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error");
        }
    }

}