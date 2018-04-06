package com.raffaeleconforti.datastructures;

import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.Map;
import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 10/03/2016.
 */
public class Hierarchy<T> {

    private Map<T, Set<T>> hierarchy;

    public Hierarchy() {
        this(0);
    }

    public Hierarchy(int size) {
        hierarchy = new UnifiedMap<>(size);
    }

    public void add(T key, Map<T, Boolean> value) {
        Set<T> set;
        if ((set = hierarchy.get(key)) == null) {
            set = new UnifiedSet<T>();
            hierarchy.put(key, set);
        }
        for (T t : value.keySet()) {
            set.add(t);
        }
    }

    public void add(T key, Set<T> value) {
        Set<T> set;
        if ((set = hierarchy.get(key)) == null) {
            set = new UnifiedSet<T>();
            hierarchy.put(key, set);
        }
        set.addAll(value);
    }

    public Set<T> keySet() {
        return hierarchy.keySet();
    }

    public int size() {
        return hierarchy.size();
    }

    public Set<Map.Entry<T, Set<T>>> entrySet() {
        return hierarchy.entrySet();
    }

    public Set<T> get(T key) {
        return hierarchy.get(key);
    }
}
