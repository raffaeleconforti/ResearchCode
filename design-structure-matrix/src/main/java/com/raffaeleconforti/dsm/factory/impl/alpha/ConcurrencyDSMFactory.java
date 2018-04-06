package com.raffaeleconforti.dsm.factory.impl.alpha;

import com.raffaeleconforti.datastructures.multilevelmap.impl.MultiLevelHashMap;
import com.raffaeleconforti.dsm.DesignStructureMatrix;
import com.raffaeleconforti.dsm.DesignStructureMatrixCell;
import com.raffaeleconforti.dsm.dsmcells.alpha.Concurrency;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 8/04/2016.
 */
public class ConcurrencyDSMFactory {

    public static DesignStructureMatrix discover(Set<String> uniqueEvents, MultiLevelHashMap<String, Boolean> directFollowDepencencies, DesignStructureMatrix designStructureMatrix) {
        Set<DesignStructureMatrixCell> set;

        for(String nameCurrent : uniqueEvents) {
            for(String nameNext : uniqueEvents) {
                if((set = designStructureMatrix.getCell(nameCurrent, nameNext)) == null) {
                    set = new UnifiedSet<>();
                }

                if(directFollowDepencencies.containsKeys(nameCurrent, nameNext) && directFollowDepencencies.containsKeys(nameNext, nameCurrent)) {
                    set.add(new Concurrency());
                    designStructureMatrix.setCell(set, nameCurrent, nameNext);
                }

            }
        }

        return designStructureMatrix;
    }

}
