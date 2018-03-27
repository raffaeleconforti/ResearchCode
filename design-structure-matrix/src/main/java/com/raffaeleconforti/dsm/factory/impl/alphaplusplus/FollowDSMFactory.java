package com.raffaeleconforti.dsm.factory.impl.alphaplusplus;

import com.raffaeleconforti.datastructures.multilevelmap.impl.MultiLevelHashMap;
import com.raffaeleconforti.dsm.DesignStructureMatrix;
import com.raffaeleconforti.dsm.DesignStructureMatrixCell;
import com.raffaeleconforti.dsm.dsmcells.alphaplusplus.Follow;
import com.raffaeleconforti.dsm.factory.DirectFollowFactory;
import com.raffaeleconforti.dsm.factory.IndirectFollowFactory;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 8/04/2016.
 */
public class FollowDSMFactory {

    public static DesignStructureMatrix discover(XLog log, Set<String> uniqueEvents, XEventClassifier eventClassifier, DesignStructureMatrix designStructureMatrix) {
        MultiLevelHashMap<String, Boolean> directFollow = DirectFollowFactory.discoverDirectFollow(log, eventClassifier);
        MultiLevelHashMap<String, Boolean> indirecltyFollow = IndirectFollowFactory.discoverIndirectFollow(log, uniqueEvents, eventClassifier);
        Set<DesignStructureMatrixCell> set;

        for (String nameCurrent : uniqueEvents) {
            for (String nameNext : uniqueEvents) {
                if ((set = designStructureMatrix.getCell(nameCurrent, nameNext)) == null) {
                    set = new UnifiedSet<>();
                }

                if (directFollow.containsKeys(nameCurrent, nameNext) || indirecltyFollow.containsKeys(nameCurrent, nameNext)) {
                    set.add(new Follow());
                    designStructureMatrix.setCell(set, nameCurrent, nameNext);
                }

            }
        }

        return designStructureMatrix;
    }

}
