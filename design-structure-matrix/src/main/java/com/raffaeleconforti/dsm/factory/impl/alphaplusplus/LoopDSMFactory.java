package com.raffaeleconforti.dsm.factory.impl.alphaplusplus;

import com.raffaeleconforti.datastructures.multilevelmap.impl.MultiLevelHashMap;
import com.raffaeleconforti.dsm.DesignStructureMatrix;
import com.raffaeleconforti.dsm.DesignStructureMatrixCell;
import com.raffaeleconforti.dsm.dsmcells.alphaplus.Loop;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 8/04/2016.
 */
public class LoopDSMFactory {

    public static DesignStructureMatrix discover(Set<String> uniqueEvents, MultiLevelHashMap<String, Boolean> shortLoopDependencies, DesignStructureMatrix designStructureMatrix) {
        Set<DesignStructureMatrixCell> set;

        for(String nameCurrent : uniqueEvents) {
            for(String nameNext : uniqueEvents) {
                if((set = designStructureMatrix.getCell(nameCurrent, nameNext)) == null) {
                    set = new UnifiedSet<>();
                }

                if(shortLoopDependencies.containsKeys(nameCurrent, nameNext) && shortLoopDependencies.containsKeys(nameNext, nameCurrent)) {
                    set.add(new Loop());
                    designStructureMatrix.setCell(set, nameCurrent, nameNext);
                }

            }
        }

        return designStructureMatrix;
    }

}
