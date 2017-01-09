package com.raffaeleconforti.dsm.factory;

import com.raffaeleconforti.datastructures.multilevelmap.impl.MultiLevelHashMap;
import com.raffaeleconforti.dsm.DesignStructureMatrix;
import org.deckfour.xes.classification.XEventClassifier;

import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 8/04/2016.
 */
public interface DSMFactory {

    DesignStructureMatrix discover(Set<String> uniqueEvents, XEventClassifier eventClassifier, MultiLevelHashMap<String, Boolean> directFollowDepencencies, DesignStructureMatrix designStructureMatrix);

}
