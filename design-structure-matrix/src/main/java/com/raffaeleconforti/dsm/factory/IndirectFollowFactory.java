package com.raffaeleconforti.dsm.factory;

import com.raffaeleconforti.datastructures.multilevelmap.impl.MultiLevelHashMap;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 1/04/2016.
 */
public class IndirectFollowFactory {

    public static MultiLevelHashMap<String, Boolean> discoverIndirectFollow(XLog log, Set<String> uniqueEvents, XEventClassifier eventClassifier) {
        MultiLevelHashMap<String, Boolean> directFollow = DirectFollowFactory.discoverDirectFollow(log, eventClassifier);
        MultiLevelHashMap<String, Boolean> MultiLevelHashMap = new MultiLevelHashMap<>(2);

        for(XTrace trace : log) {
            for (int i = 0; i < trace.size() - 1; i++) {
                for (int j = i + 1; j < trace.size(); j++) {
                    XEvent current = trace.get(i);
                    XEvent next = trace.get(j);
                    String nameCurrent = eventClassifier.getClassIdentity(current);
                    String nameNext = eventClassifier.getClassIdentity(next);

                    if (!directFollow.containsKeys(nameCurrent, nameNext)) {
                        for (int k = i + 1; k < j; k++) {
                            XEvent eventK = trace.get(k);
                            String nameK = eventClassifier.getClassIdentity(eventK);
                            if (!nameK.equals(nameCurrent) && !nameK.equals(nameNext)
                                    && !(isXORSPlit(uniqueEvents, nameK, nameCurrent, directFollow)
                                    || !isXORJoin(uniqueEvents, nameK, nameCurrent, directFollow)
                                    )
                            ) {
                                MultiLevelHashMap.put(true, nameCurrent, nameNext);
                            }
                        }
                    }
                }
            }
        }

        return MultiLevelHashMap;
    }

    private static boolean isXORSPlit(Set<String> uniqueEvents, String nameCurrent, String nameNext, MultiLevelHashMap<String, Boolean> directFollowDepencencies) {
        if(!directFollowDepencencies.containsKeys(nameCurrent, nameNext) && !directFollowDepencencies.containsKeys(nameNext, nameCurrent)) {
            for(String split : uniqueEvents) {
                if(directFollowDepencencies.containsKeys(split, nameCurrent) && directFollowDepencencies.containsKeys(split, nameNext)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isXORJoin(Set<String> uniqueEvents, String nameCurrent, String nameNext, MultiLevelHashMap<String, Boolean> directFollowDepencencies) {
        if(!directFollowDepencencies.containsKeys(nameCurrent, nameNext) && !directFollowDepencencies.containsKeys(nameNext, nameCurrent)) {
            for(String join : uniqueEvents) {
                if(directFollowDepencencies.containsKeys(nameCurrent, join) && directFollowDepencencies.containsKeys(nameNext, join)) {
                    return true;
                }
            }
        }
        return false;
    }
}
