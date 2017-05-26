package com.raffaeleconforti.memorylog;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.*;
import org.deckfour.xes.util.XAttributeUtils;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.util.*;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/08/2016.
 */
public class XTraceImpl implements XTrace {

    private int size = 0;
    private int[] counter;
    private IntObjectHashMap<XEvent> intToEvent;
    private ObjectIntHashMap<XEvent> eventToInt;
    private int[] array = new int[0];
    private byte[] compressedArray;

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 843122019760036963L;

    /**
     * Map of attributes for this trace.
     */
    private XAttributeMap attributes;

    /**
     * Creates a new trace.
     *
     * @param attributeMap
     *            Attribute map used to store this trace's attributes.
     */
    public XTraceImpl(XAttributeMap attributeMap, int[] counter, IntObjectHashMap<XEvent> intToEvent, ObjectIntHashMap<XEvent> eventToInt) {
        this.counter = counter;
        this.intToEvent = intToEvent;
        this.eventToInt = eventToInt;
        this.attributes = attributeMap;
        try {
            this.compressedArray = Snappy.compress(array);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deckfour.xes.model.XAttributable#getAttributes()
     */
    public XAttributeMap getAttributes() {
        if(attributes == null) attributes = new XAttributeMapLazyImpl<XAttributeMapImpl>(XAttributeMapImpl.class);
        return attributes;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deckfour.xes.model.XAttributable#getExtensions()
     */
    public Set<XExtension> getExtensions() {
        return XAttributeUtils.extractExtensions(attributes);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deckfour.xes.model.XAttributable#setAttributes(java.util.Map)
     */
    public void setAttributes(XAttributeMap attributes) {
        this.attributes = attributes;
    }

    /* (non-Javadoc)
     * @see org.deckfour.xes.model.XAttributable#hasAttributes()
     */
    @Override
    public boolean hasAttributes() {
        return attributes != null && !attributes.isEmpty();
    }

    /**
     * Creates a clone, i.e. deep copy, of this trace.
     */
    public Object clone() {
        XTraceImpl clone = new XTraceImpl((XAttributeMap) attributes.clone(), counter, intToEvent, eventToInt);
        clone.clear();
        for (XEvent event : this) {
            clone.add((XEvent) event.clone());
        }
        return clone;
    }

    public synchronized int insertOrdered(XEvent event) {
        if (this.size() == 0) {
            // append if list is empty
            add(event);
            return 0;
        }
        XAttribute insTsAttr = event.getAttributes().get(
                XTimeExtension.KEY_TIMESTAMP);
        if (insTsAttr == null) {
            // append if event has no timestamp
            add(event);
            return (size() - 1);
        }
        Date insTs = ((XAttributeTimestamp) insTsAttr).getValue();
        for (int i = (size() - 1); i >= 0; i--) {
            XAttribute refTsAttr = get(i).getAttributes().get(
                    XTimeExtension.KEY_TIMESTAMP);
            if (refTsAttr == null) {
                // trace contains events w/o timestamps, append.
                add(event);
                return (size() - 1);
            }
            Date refTs = ((XAttributeTimestamp) refTsAttr).getValue();
            if (insTs.before(refTs) == false) {
                // insert position reached
                add(i + 1, event);
                return (i + 1);
            }
        }
        // beginning reached, insert at head
        add(0, event);
        return 0;
    }

    /*
     * Runs the given visitor for the given log on this trace.
     *
     * (non-Javadoc)
     * @see org.deckfour.xes.model.XTrace#accept(org.deckfour.xes.model.XVisitor, org.deckfour.xes.model.XLog)
     */
    public void accept(XVisitor visitor, XLog log) {
		/*
		 * First call.
		 */
        visitor.visitTracePre(this, log);
		/*
		 * Visit the attributes.
		 */
        for (XAttribute attribute: attributes.values()) {
            attribute.accept(visitor, this);
        }
		/*
		 * Visit the events.
		 */
        for (XEvent event: this) {
            event.accept(visitor, this);
        }
		/*
		 * Last call.
		 */
        visitor.visitTracePost(this, log);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return deCompress().length == 0;
    }

    @Override
    public boolean contains(Object o) {
        if(o instanceof XEvent) {
            XEvent event = (XEvent) o;
            int i = eventToInt.get(event);
            if(i != 0) {
                for(int j : deCompress()) {
                    if(i == j) return true;
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public Iterator<XEvent> iterator() {
        return listIterator();
    }

    @Override
    public Object[] toArray() {
        int[] array = deCompress();
        Object[] objects = new Object[array.length];
        for(int i = 0; i < array.length; i++) {
            objects[i] = intToEvent.get(array[i]);
        }
        return objects;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        int[] array = deCompress();
        XEvent[] objects = new XEvent[array.length];
        for(int i = 0; i < array.length; i++) {
            objects[i] = intToEvent.get(array[i]);
        }
        return (T[]) objects;
    }

    @Override
    public boolean add(XEvent xEvent) {
        int val;
        if((val = eventToInt.get(xEvent)) == 0) {
            val = counter[0];
            eventToInt.put(xEvent, counter[0]);
            intToEvent.put(counter[0], xEvent);
            counter[0]++;
        }
        int[] array = deCompress();
        array = Arrays.copyOf(array, array.length + 1);
        array[array.length - 1] = val;
        compress(array);
        size++;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        int val;
        if(o instanceof XEvent) {
            XEvent event = (XEvent) o;
            if ((val = eventToInt.get(event)) != 0 && contains(event)) {
                int[] array = deCompress();
                int[] newArray = new int[array.length - 1];
                int pos = 0;
                boolean done = false;
                for(int i = 0; i < array.length; i++) {
                    if(!done && array[i] == val) {
                        done = true;
                    }else {
                        newArray[pos] = array[i];
                        pos++;
                    }
                }
                compress(newArray);
                size--;
                return true;
            }else return false;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        boolean contain = true;
        for(Object o : c) {
            contain &= contains(o);
        }
        return contain;
    }

    @Override
    public boolean addAll(Collection<? extends XEvent> c) {
        for(XEvent event : c) {
            add(event);
        }
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends XEvent> c) {
        for(XEvent event : c) {
            add(index, event);
            index++;
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        for(Object o : c) {
            remove(o);
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {
        compress(new int[0]);
    }

    @Override
    public XEvent get(int index) {
        return intToEvent.get(deCompress()[index]);
    }

    @Override
    public XEvent set(int index, XEvent element) {
        int val;
        if((val = eventToInt.get(element)) == 0) {
            val = counter[0];
            eventToInt.put(element, counter[0]);
            intToEvent.put(counter[0], element);
            counter[0]++;
        }
        int[] array = deCompress();
        XEvent event = intToEvent.get(array[index]);
        array[index] = val;
        compress(array);
        return event;
    }

    @Override
    public void add(int index, XEvent element) {
        int val;
        if((val = eventToInt.get(element)) == 0) {
            val = counter[0];
            eventToInt.put(element, counter[0]);
            intToEvent.put(counter[0], element);
            counter[0]++;
        }

        int[] array = deCompress();
        int[] newArray = new int[array.length + 1];
        int pos = 0;
        for(int i = 0; i < newArray.length; i++) {
            if(i == index) {
                newArray[i] = val;
            }else {
                newArray[i] = array[pos];
                pos++;
            }
        }
        compress(newArray);
        size++;
    }

    @Override
    public XEvent remove(int index) {
        XEvent event = null;
        int[] array = deCompress();
        int[] newArray = new int[array.length - 1];
        int pos = 0;
        for(int i = 0; i < array.length; i++) {
            if(i == index) {
                event = intToEvent.get(array[i]);
            }else {
                newArray[pos] = array[i];
                pos++;
            }
        }
        compress(newArray);
        size--;
        return event;
    }

    @Override
    public int indexOf(Object o) {
        if(o instanceof XEvent) {
            XEvent event = (XEvent) o;
            int i = eventToInt.get(event);
            if(i != 0) {
                int[] array = deCompress();
                for(int j = 0; j < array.length; j++) {
                    if(i == array[j]) return j;
                }
            }
            return -1;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if(o instanceof XEvent) {
            XEvent event = (XEvent) o;
            int i = eventToInt.get(event);
            if(i != 0) {
                int[] array = deCompress();
                for(int j = array.length - 1; j >= 0; j--) {
                    if(i == array[j]) return j;
                }
            }
            return -1;
        }
        return -1;
    }

    @Override
    public ListIterator<XEvent> listIterator() {
        return new ListIterator<XEvent>() {

            ListIterator<Integer> iterator = new ArrayList<Integer>() {{ for (int i : deCompress()) add(i); }}.listIterator();
            int lastIndex = -1;

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public XEvent next() {
                lastIndex = iterator.nextIndex();
                return intToEvent.get(iterator.next());
            }

            @Override
            public boolean hasPrevious() {
                return iterator.hasPrevious();
            }

            @Override
            public XEvent previous() {
                lastIndex = iterator.previousIndex();
                return intToEvent.get(iterator.previous());
            }

            @Override
            public int nextIndex() {
                return iterator.nextIndex();
            }

            @Override
            public int previousIndex() {
                return iterator.previousIndex();
            }

            @Override
            public void remove() {
                XTraceImpl.this.remove(lastIndex);
                iterator.remove();
            }

            @Override
            public void set(XEvent xEvent) {
                XTraceImpl.this.set(lastIndex, xEvent);
                iterator.set(eventToInt.get(xEvent));
            }

            @Override
            public void add(XEvent xEvent) {
                XTraceImpl.this.add(lastIndex, xEvent);
            }
        };
    }

    @Override
    public ListIterator<XEvent> listIterator(int index) {
        return new ListIterator<XEvent>() {

            ListIterator<Integer> iterator = new ArrayList<Integer>() {{ for (int i : deCompress()) add(i); }}.listIterator(index);
            int lastIndex = index;

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public XEvent next() {
                lastIndex = iterator.nextIndex();
                return intToEvent.get(iterator.next());
            }

            @Override
            public boolean hasPrevious() {
                return iterator.hasPrevious();
            }

            @Override
            public XEvent previous() {
                lastIndex = iterator.previousIndex();
                return intToEvent.get(iterator.previous());
            }

            @Override
            public int nextIndex() {
                return iterator.nextIndex();
            }

            @Override
            public int previousIndex() {
                return iterator.previousIndex();
            }

            @Override
            public void remove() {
                XTraceImpl.this.remove(lastIndex);
                iterator.remove();
            }

            @Override
            public void set(XEvent xEvent) {
                XTraceImpl.this.set(lastIndex, xEvent);
                iterator.set(eventToInt.get(xEvent));
            }

            @Override
            public void add(XEvent xEvent) {
                XTraceImpl.this.add(lastIndex, xEvent);
            }
        };
    }

    @Override
    public List<XEvent> subList(int fromIndex, int toIndex) {
        List<XEvent> list = new ArrayList<>();
        for(int i = fromIndex; i < toIndex; i++) {
            list.add(intToEvent.get(i));
        }
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof XTrace) {
            XTrace trace = (XTrace) o;
            if(trace.size() != deCompress().length) return false;
            if(!trace.getAttributes().equals(attributes)) return false;

            int pos = 0;
            int[] array = deCompress();
            for(XEvent event : trace) {
                if(!event.equals(intToEvent.get(array[pos]))) return false;
                pos++;
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        if(array != null) {
            int[] tmp = array;
            array = null;
            compress(tmp);
            return Arrays.hashCode(tmp);
        }
        return Arrays.hashCode(deCompress());
    }

    private void compress(int[] array) {
        try {
            if(this.array != null) this.array = array;
            else compressedArray = Snappy.compress(array);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int[] deCompress() {
        try {
            if(array != null) return array;
            return Snappy.uncompressIntArray(compressedArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
