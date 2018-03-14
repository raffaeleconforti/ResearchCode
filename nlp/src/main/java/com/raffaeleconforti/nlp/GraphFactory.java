package com.raffaeleconforti.nlp;

import com.raffaeleconforti.nlp.graph.Graph;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import java.util.ArrayList;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 18/1/17.
 */
public class GraphFactory {

    private int window_size;
    private XEventClassifier xEventClassifier;

//    public static void main(String[] args) {
//        LogImporter
//        XLog log =
//    }

    public GraphFactory(XEventClassifier xEventClassifier, int window_size) {
        this.xEventClassifier = xEventClassifier;
        this.window_size = window_size;
    }

    public Graph createGraph(XLog log, String event_label) {
        Graph graph = new Graph();

        ArrayList<XEvent> pre_window = new ArrayList<>(window_size);
        ArrayList<XEvent> post_window = new ArrayList<>(window_size);
        XEvent center_element = null;

        for(XTrace trace : log) {
            pre_window.clear();
            post_window.clear();
            center_element = null;
            for(int i = 0; i < trace.size(); i++) {
                center_element = shiftWindows(pre_window, post_window, center_element, trace.get(i));
                if(center_element != null) {
                    System.out.println(toString(pre_window) + " " + toString(center_element) + " " + toString(post_window));
                }
            }
        }

        return graph;
    }

    private String toString(XEvent event) {
        return xEventClassifier.getClassIdentity(event);
    }

    private String toString(ArrayList<XEvent> window) {
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < window.size(); i++) {
            stringBuilder.append(window.get(i));
            if(i < window.size() - 1) {
                stringBuilder.append(", ");
            }
        }
        return stringBuilder.toString();
    }

    private XEvent shiftWindows(ArrayList<XEvent> pre_window, ArrayList<XEvent> post_window, XEvent center_element, XEvent new_element) {
        post_window.add(new_element);

        XEvent new_center_element;
        if(post_window.size() > window_size) {
            new_center_element = post_window.remove(0);
            pre_window.add(center_element);
            if (pre_window.size() > window_size) {
                pre_window.remove(0);
            }
        }else {
            new_center_element = center_element;
        }

        return new_center_element;
    }

}
