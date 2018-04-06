package com.raffaeleconforti.outliers.statistics;

import com.raffaeleconforti.outliers.Outlier;
import com.raffaeleconforti.outliers.OutlierIdentifier;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.Map;
import java.util.Set;

/**
 * Created by conforti on 12/02/15.
 */
public class OutlierMap<T> {

    int totalNumber = 0;
    private final Map<Outlier<String>, Double> frequencyMap = new UnifiedMap<Outlier<String>, Double>();
    private final Map<OutlierIdentifier, Double> identifierFrequencyMap = new UnifiedMap<OutlierIdentifier, Double>();
    private final Map<OutlierIdentifier, Set<Outlier<T>>> map = new UnifiedMap<OutlierIdentifier, Set<Outlier<T>>>();

    public void addOutlier(Outlier<T> outlier) {
        OutlierIdentifier identifier = outlier.getIdentifier();
        Set<Outlier<T>> set = null;
        if((set = map.get(identifier)) == null) {
            set = new UnifiedSet<Outlier<T>>();
            map.put(identifier, set);
        }
        if(set.add(outlier)) {
            totalNumber++;
        }

    }

    public void increaseIdentifierFrequency(OutlierIdentifier identifier) {
        Double frequency = identifierFrequencyMap.get(identifier);
        if(frequency == null) {
            frequency = 0.0;
        }
        frequency++;
        identifierFrequencyMap.put(identifier, frequency);
    }

    public double getIdentifierFrequency(OutlierIdentifier identifier) {
        return identifierFrequencyMap.get(identifier);
    }

    public void setIdentifierFrequency(OutlierIdentifier identifier, double frequency) {
        identifierFrequencyMap.put(identifier, frequency);
    }

    public void increaseFrequency(Outlier<String> outlier) {
        Double frequency = frequencyMap.get(outlier);
        if(frequency == null) {
            frequency = 0.0;
        }
        frequency++;
        frequencyMap.put(outlier, frequency);
    }

    public double getFrequency(Outlier<String> outlier) {
        return frequencyMap.get(outlier);
    }

    public void setFrequency(Outlier<String> outlier, double frequency) {
        frequencyMap.put(outlier, frequency);
    }

    public Set<Outlier<T>> getOutliers(OutlierIdentifier identifier) {
        return map.get(identifier);
    }

    public void clear() {
        frequencyMap.clear();
        map.clear();
    }

    public int getTotalNumber() {
        return totalNumber;
    }

    public int size() {
        return map.size();
    }

    public Set<Map.Entry<OutlierIdentifier, Set<Outlier<T>>>> entrySet() {
        return map.entrySet();
    }
 }
