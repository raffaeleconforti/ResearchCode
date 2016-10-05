package com.raffaeleconforti.log.util;

import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XTrace;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by conforti on 5/02/2016.
 */
public class TraceToString {

    private static final XTimeExtension xte = XTimeExtension.instance();

    public static String convertXTraceToString(XTrace trace, NameExtractor nameExtractor) {
        List<String> labels = new ArrayList<>(trace.size());
        for (int i = 0; i < trace.size(); i++) {
            labels.add(nameExtractor.getEventName(trace.get(i)));
        }
        return listToString(labels);
    }

    public static String[] convertXTraceToListOfString(XTrace trace, NameExtractor nameExtractor) {
        String[] labels = new String[trace.size()];
        for (int i = 0; i < trace.size(); i++) {
            labels[i] = nameExtractor.getEventName(trace.get(i));
        }
        return labels;
    }

    public static String convertXTraceToString(XTrace trace, Map<String, String> nameMap, NameExtractor nameExtractor) {
        List<String> labels = new ArrayList<>(trace.size());
        for (int i = 0; i < trace.size(); i++) {
            labels.add(nameMap.get(nameExtractor.getEventName(trace.get(i))));
        }
        return listToString(labels);
    }

    public static String[] convertXTraceToListOfString(XTrace trace, Map<String, String> nameMap, NameExtractor nameExtractor) {
        String[] labels = new String[trace.size()];
        for (int i = 0; i < trace.size(); i++) {
            labels[i] = nameMap.get(nameExtractor.getEventName(trace.get(i)));
        }
        return labels;
    }

    public static String convertXTraceToStringWithTimestamp(XTrace trace, NameExtractor nameExtractor, SimpleDateFormat simpleDateFormat) {
        List<String> labels = new ArrayList<>(trace.size());
        for (int i = 0; i < trace.size(); i++) {
            labels.add(nameExtractor.getEventName(trace.get(i)) + simpleDateFormat.format(xte.extractTimestamp(trace.get(i))));
        }
        return  listToString(labels);
    }

    public static String listToString(List<String> list) {
        StringBuffer sb = new StringBuffer();
        for(String event : list) {
            sb.append(event).append(", ");
        }
        return sb.toString();
    }
}
