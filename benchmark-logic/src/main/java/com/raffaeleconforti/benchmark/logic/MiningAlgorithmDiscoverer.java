package com.raffaeleconforti.benchmark.logic;

import com.raffaeleconforti.classloading.ClassFinder;
import com.raffaeleconforti.wrappers.MiningAlgorithm;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 17/10/2016.
 */
public class MiningAlgorithmDiscoverer {

    public static List<MiningAlgorithm> discoverAlgorithms() {
        Set<String> packages = new UnifiedSet<>();
        return discoverAlgorithms(packages);
    }

    public static List<MiningAlgorithm> discoverAlgorithms(Set<String> packages) {
        packages.add("com.raffaeleconforti");
        List<Class<? extends MiningAlgorithm>> list = ClassFinder.findAllMatchingTypes(packages, MiningAlgorithm.class);
        List<MiningAlgorithm> algorithms = new ArrayList<>(list.size());
        for(Class<? extends MiningAlgorithm> c : list) {
            try {
                algorithms.add(c.newInstance());
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return algorithms;
    }

    public static List<String> discoverAlgorithmsNames(List<MiningAlgorithm> algorithms) {
        List<String> algorithmsNames = new ArrayList<>(algorithms.size());
        for(MiningAlgorithm miningAlgorithm : algorithms) {
            algorithmsNames.add(miningAlgorithm.getAlgorithmName());
        }
        return algorithmsNames;
    }

}
