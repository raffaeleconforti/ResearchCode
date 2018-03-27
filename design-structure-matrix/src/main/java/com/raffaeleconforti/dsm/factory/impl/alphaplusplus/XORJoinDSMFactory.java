package com.raffaeleconforti.dsm.factory.impl.alphaplusplus;

import com.raffaeleconforti.datastructures.multilevelmap.impl.MultiLevelHashMap;
import com.raffaeleconforti.dsm.DesignStructureMatrix;
import com.raffaeleconforti.dsm.DesignStructureMatrixCell;
import com.raffaeleconforti.dsm.dsmcells.alphaplusplus.XORJoin;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 8/04/2016.
 */
public class XORJoinDSMFactory {

    public static DesignStructureMatrix discover(Set<String> uniqueEvents, MultiLevelHashMap<String, Boolean> directFollowDepencencies, MultiLevelHashMap<String, Boolean> shortLoopDependencies, DesignStructureMatrix designStructureMatrix) {
        Set<DesignStructureMatrixCell> set;

        for(String nameCurrent : uniqueEvents) {
            for(String nameNext : uniqueEvents) {
                if((set = designStructureMatrix.getCell(nameCurrent, nameNext)) == null) {
                    set = new UnifiedSet<>();
                }

                if(!directFollowDepencencies.containsKeys(nameCurrent, nameNext) && !directFollowDepencencies.containsKeys(nameNext, nameCurrent)) {
                    for(String split : uniqueEvents) {
                        if(directFollowDepencencies.containsKeys(nameCurrent, split) && directFollowDepencencies.containsKeys(nameNext, split)) {
                            set.add(new XORJoin());
                            designStructureMatrix.setCell(set, nameCurrent, nameNext);
                            break;
                        }
                    }
                }

            }
        }

        return designStructureMatrix;
    }

}
