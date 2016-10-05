package com.raffaeleconforti.datastructures.multilevelmap;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 20/03/2016.
 */
public interface MultiLevelMap<K, V> {

    void clear();

    boolean containsKeys(K... keys);
    boolean containsValues(V value);
    V get(K... keys);
    void put(V value, K... keys);
    V remove(K... keys);
    int size();

}
