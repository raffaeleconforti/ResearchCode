/*
 *  Copyright (C) 2018 Raffaele Conforti (www.raffaeleconforti.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.raffaeleconforti.benchmark.logic;

import com.raffaeleconforti.classloading.ClassFinder;
import com.raffaeleconforti.measurements.MeasurementAlgorithm;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 17/10/2016.
 */
public class MeasurementAlgorithmDiscoverer {

    public static List<MeasurementAlgorithm> discoverAlgorithms() {
        Set<String> packages = new UnifiedSet<>();
        return discoverAlgorithms(packages);
    }

    public static List<MeasurementAlgorithm> discoverAlgorithms(Set<String> packages) {
        packages.add("com.raffaeleconforti");
        List<Class<? extends MeasurementAlgorithm>> list = ClassFinder.findAllMatchingTypes(packages, MeasurementAlgorithm.class);
        List<MeasurementAlgorithm> algorithms = new ArrayList<>(list.size());
        for(Class<? extends MeasurementAlgorithm> c : list) {
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

    public static List<String> discoverAlgorithmsNames(List<MeasurementAlgorithm> algorithms) {
        List<String> algorithmsNames = new ArrayList<>(algorithms.size());
        for(MeasurementAlgorithm measurementAlgorithm : algorithms) {
            algorithmsNames.add(measurementAlgorithm.getMeasurementName());
        }
        return algorithmsNames;
    }

}
