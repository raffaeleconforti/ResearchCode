package com.raffaeleconforti.wrapper;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 17/10/2016.
 */
@Plugin(name = "Algorithms Discoverer", parameterLabels = {""},
        returnLabels = {"Algorithms"},
        returnTypes = {String.class})
public class AlgorithmDiscoverer {

    @UITopiaVariant(affiliation = UITopiaVariant.EHV,
            author = "Raffaele Conforti",
            email = "raffaele.conforti@qut.edu.au",
            pack = "Noise Filtering")
    @PluginVariant(variantLabel = "Algorithms Discoverer", requiredParameterLabels = {})
    public String minePetrinet(UIPluginContext context) {
        System.out.println(discoverAlgorithms());
        System.out.println(discoverAlgorithmsNames(discoverAlgorithms()));
        return discoverAlgorithmsNames(discoverAlgorithms()).toString();
    }

    public static void main(String[] args) {
        AlgorithmDiscoverer a = new AlgorithmDiscoverer();
        List l = a.discoverAlgorithms();
        System.out.println(l);
    }

    public List<MiningAlgorithm> discoverAlgorithms() {
//        JavaClassFinder javaClassFinder = new JavaClassFinder();
//        List<Class<? extends MiningAlgorithm>> list = javaClassFinder.findAllMatchingTypes(MiningAlgorithm.class);

        List<Class<? extends MiningAlgorithm>> list = ClassFinder.findAllMatchingTypes(MiningAlgorithm.class);
        List<MiningAlgorithm> algorithms = new ArrayList<>(list.size());
        for(Class<? extends MiningAlgorithm> c : list) {
            System.out.println("TEST " + c);
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

    public List<String> discoverAlgorithmsNames(List<MiningAlgorithm> algorithms) {
        List<String> algorithmsNames = new ArrayList<>(algorithms.size());
        for(MiningAlgorithm miningAlgorithm : algorithms) {
            algorithmsNames.add(miningAlgorithm.getAlgorithmName());
        }
        return algorithmsNames;
    }

}
