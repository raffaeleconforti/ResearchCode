package com.raffaeleconforti.efficientlog;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.*;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/08/2016.
 */
public class XAttributeMapLazyImpl<T extends XAttributeMap> implements XAttributeMap {

    /**
     * Default empty entry set used for lazy operation.
     */
    private static final Set<Entry<String, XAttribute>> EMPTY_ENTRYSET
            = Collections.unmodifiableSet(new UnifiedSet<Entry<String, XAttribute>>(0));

    /**
     * Default empty key set used for lazy operation.
     */
    private static final Set<String> EMPTY_KEYSET =
            Collections.unmodifiableSet(new UnifiedSet<String>(0));

    /**
     * Default empty entries used for lazy operation.
     */
    private static final Collection<XAttribute> EMPTY_ENTRIES =
            Collections.unmodifiableCollection(new ArrayList<XAttribute>(0));

    /**
     * Class implementing the backing store; this is needed for initialization
     * of generic classes, as of the Java language.
     */
    private Class<T> backingStoreClass;


    /**
     * Backing store, initialized lazily, i.e. on the fly.
     */
    private T backingStore = null;

    /**
     * Creates a new lazy attribute map instance.
     *
     * @param implementingClass Class which should be used for
     * 		instantiating the backing storage.
     */
    public XAttributeMapLazyImpl(Class<T> implementingClass) {
        backingStoreClass = implementingClass;
        backingStore = null;
    }

    /**
     * Returns the class used for implementing the
     * backing store.
     *
     * @return The class used for implementing the
     * backing store.
     */
    public Class<T> getBackingStoreClass() {
        return backingStoreClass;
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    public synchronized void clear() {
        backingStore = null;
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public synchronized boolean containsKey(Object key) {
        if(backingStore != null) {
            return backingStore.containsKey(key);
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public synchronized boolean containsValue(Object value) {
        if(backingStore != null) {
            return backingStore.containsValue(value);
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    public synchronized Set<java.util.Map.Entry<String, XAttribute>> entrySet() {
        if(backingStore != null) {
            return backingStore.entrySet();
        } else {
            return EMPTY_ENTRYSET;
        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    public synchronized XAttribute get(Object key) {
        if(backingStore != null) {
            return backingStore.get(key);
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    public synchronized boolean isEmpty() {
        if(backingStore != null) {
            return backingStore.isEmpty();
        } else {
            return true;
        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    public synchronized Set<String> keySet() {
        if(backingStore != null) {
            return backingStore.keySet();
        } else {
            return EMPTY_KEYSET;
        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public synchronized XAttribute put(String key, XAttribute value) {
        if(backingStore == null) {
            try {
                backingStore = backingStoreClass.newInstance();
            } catch (Exception e) {
                // Fuckup
                e.printStackTrace();
            }
        }
        return backingStore.put(key, value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    public synchronized void putAll(Map<? extends String, ? extends XAttribute> t) {
        if(t.size() > 0) {
            if(backingStore == null) {
                try {
                    backingStore = backingStoreClass.newInstance();
                } catch (Exception e) {
                    // Fuckup
                    e.printStackTrace();
                }
            }
            backingStore.putAll(t);
        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    public synchronized XAttribute remove(Object key) {
        if(backingStore != null) {
            return backingStore.remove(key);
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    public synchronized int size() {
        if(backingStore != null) {
            return backingStore.size();
        } else {
            return 0;
        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
    public synchronized Collection<XAttribute> values() {
        if(backingStore != null) {
            return backingStore.values();
        } else {
            return EMPTY_ENTRIES;
        }
    }

    /**
     * Creates a clone, i.e. deep copy, of this lazy attribute map.
     */
    @SuppressWarnings("unchecked")
    public Object clone() {
        XAttributeMapLazyImpl<T> clone;
        try {
            clone = (XAttributeMapLazyImpl<T>)super.clone();
            if(backingStore != null) {
                clone.backingStore = (T)backingStore.clone();
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            // Fuckup!
            e.printStackTrace();
            return null;
        }
    }

}