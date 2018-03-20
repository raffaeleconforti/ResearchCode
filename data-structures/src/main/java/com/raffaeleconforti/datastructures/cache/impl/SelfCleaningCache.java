package com.raffaeleconforti.datastructures.cache.impl;

import com.raffaeleconforti.datastructures.cache.Cache;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 2/08/2016.
 */
public class SelfCleaningCache<K, V> extends UnifiedMap<K, V> implements Cache<K, V> {

    private Map<K, Long> time = new ConcurrentHashMap();
    private Runnable cleaner = null;
    private boolean enabled;

    public SelfCleaningCache(boolean enabled) {
        super();
        this.enabled = enabled;
        if(enabled) {
            cleaner = new Cleaner();
            new Thread(cleaner).start();
        }
    }

    public SelfCleaningCache(boolean enabled, int initialCapacity) {
        super(initialCapacity);
        this.enabled = enabled;
        if(enabled) {
            cleaner = new Cleaner();
            new Thread(cleaner).start();
        }
    }

    public SelfCleaningCache(boolean enabled, int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        this.enabled = enabled;
        if(enabled) {
            cleaner = new Cleaner();
            new Thread(cleaner).start();
        }
    }

    public SelfCleaningCache(boolean enabled, Map<? extends K, ? extends V> map) {
        super(map);
        Long now = System.currentTimeMillis();
        for(K key : map.keySet()) {
            time.put(key, now);
        }
        this.enabled = enabled;
        if(enabled) {
            cleaner = new Cleaner();
            new Thread(cleaner).start();
        }
    }

    public V put(K key, V value) {
        time.put(key, System.currentTimeMillis());
        return super.put(key, value);
    }

    public V get(Object key) {
        if(time.containsKey(key)) {
            time.put((K) key, System.currentTimeMillis());
        }
        return super.get(key);
    }

    public void free() {
        Long now = System.currentTimeMillis();
        Iterator<K> iterator = time.keySet().iterator();
        while(iterator.hasNext()) {
            K key = iterator.next();
            if(time.get(key) != null && now - time.get(key) > 120000) {
                time.remove(key);
                iterator = time.keySet().iterator();
                remove(key);
            }
        }
    }

    class Cleaner implements Runnable{
        @Override
        public void run() {
            while(true) {
                free();
                try {
                    Thread.currentThread().sleep(1200000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
