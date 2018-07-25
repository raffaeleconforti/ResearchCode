package com.raffaeleconforti.splitminer.log;

import org.deckfour.xes.model.XLog;

import java.util.Map;

/**
 * Created by Adriano on 27/10/2016.
 */
public class SimpleLog {
    private XLog xlog;
    private Map<String, Integer> traces;
    private Map<Integer, String> events;
    private Map<String, Integer> reverseMap;
    private int size;
    private long totalEvents;

    private long longestTrace;
    private long shortestTrace;

    private int startcode;
    private int endcode;

    public SimpleLog(Map<String, Integer> traces, Map<Integer, String> events, XLog xlog) {
        this.traces = traces;
        this.events = events;
        this.size = 0;

        totalEvents = -1;
        longestTrace = -1;
        shortestTrace = -1;

        for (int traceFrequency : traces.values()) this.size += traceFrequency;

        this.xlog = xlog;
    }

    public Map<String, Integer> getTraces() {
        return traces;
    }

    public Map<Integer, String> getEvents() {
        return events;
    }

    public int size() {
        return size;
    }

    public XLog getXLog() {
        return xlog;
    }

    public Map<String, Integer> getReverseMap() {
        return reverseMap;
    }

    public void setReverseMap(Map<String, Integer> reverseMap) {
        this.reverseMap = reverseMap;
    }

    public void setStartcode(int startcode) {
        this.startcode = startcode;
    }

    public int getStartcode() {
        return startcode;
    }

    public void setEndcode(int endcode) {
        this.endcode = endcode;
    }

    public int getEndcode() {
        return endcode;
    }

    public void setTotalEvents(long totalEvents) {
        this.totalEvents = totalEvents;
    }

    public long getTotalEvents() {
        return totalEvents;
    }

    public int getDistinctTraces() {
        return traces.size();
    }

    public int getDistinctEvents() {
        return (events.size() - 2);
    }

    public void setLongestTrace(long length) {
        longestTrace = length;
    }

    public long getLongestTrace() {
        return longestTrace;
    }

    public void setShortestTrace(long length) {
        shortestTrace = length;
    }

    public long getShortestTrace() {
        return shortestTrace;
    }

    public int getAvgTraceLength() {
        return (int) totalEvents / size;
    }
}
