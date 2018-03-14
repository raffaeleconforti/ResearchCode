package com.raffaeleconforti.dsm.factory;

import com.raffaeleconforti.datastructures.multilevelmap.impl.MultiLevelHashMap;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 1/04/2016.
 */
public class DirectFollowFactory {

    public static MultiLevelHashMap<String, Boolean> discoverDirectFollow(XLog log, XEventClassifier eventClassifier) {
        MultiLevelHashMap<String, Boolean> MultiLevelHashMap = new MultiLevelHashMap<>(2);

        for(XTrace trace : log) {
            for(int i = 0; i < trace.size() - 1; i++) {
                XEvent current = trace.get(i);
                XEvent next = trace.get(i + 1);
                String nameCurrent = eventClassifier.getClassIdentity(current);
                String nameNext = eventClassifier.getClassIdentity(next);

                if(!MultiLevelHashMap.containsKeys(nameCurrent, nameNext)) {
                    MultiLevelHashMap.put(true, nameCurrent, nameNext);
                }
            }
        }

        return MultiLevelHashMap;
    }
}
