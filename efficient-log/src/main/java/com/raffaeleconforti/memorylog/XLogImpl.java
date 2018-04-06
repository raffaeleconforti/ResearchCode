package com.raffaeleconforti.memorylog;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.*;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/08/2016.
 */
public class XLogImpl extends ArrayList<XTrace> implements XLog {

    private int[] counter;
    private IntObjectHashMap<XEvent> intToEvent;
    private ObjectIntHashMap<XEvent> eventToInt;

    /**
     * serial version UID.
     */
    private static final long serialVersionUID = -9192919845877466525L;

    /**
     * Map of attributes for this log.
     */
    private XAttributeMap attributes;
    /**
     * Extensions.
     */
    private Set<XExtension> extensions;
    /**
     * Classifiers.
     */
    private List<XEventClassifier> classifiers;
    /**
     * Global trace attributes.
     */
    private List<XAttribute> globalTraceAttributes;
    /**
     * Global event attributes.
     */
    private List<XAttribute> globalEventAttributes;

    /**
     * Single-item cache. Only the last info is cached.
     * Typically, only one classifier will be used for a log.
     */
    private XEventClassifier cachedClassifier;
    private XLogInfo cachedInfo;

    /**
     * Creates a new log.
     *
     * @param attributeMap The attribute map used to store this
     * 	log's attributes.
     */
    public XLogImpl(XAttributeMap attributeMap, int[] counter, IntObjectHashMap<XEvent> intToEvent, ObjectIntHashMap<XEvent> eventToInt) {
        this.counter = counter;
        this.intToEvent = intToEvent;
        this.eventToInt = eventToInt;

        this.attributes = attributeMap;
        this.extensions = new UnifiedSet<XExtension>();
        this.classifiers = new ArrayList<XEventClassifier>();
        this.globalTraceAttributes = new ArrayList<XAttribute>();
        this.globalEventAttributes = new ArrayList<XAttribute>();
        this.cachedClassifier = null;
        this.cachedInfo = null;
    }

    /* (non-Javadoc)
     * @see org.deckfour.xes.model.XAttributable#getAttributes()
     */
    public XAttributeMap getAttributes() {
        if(attributes == null) attributes = new XAttributeMapLazyImpl<XAttributeMapImpl>(XAttributeMapImpl.class);
        return attributes;
    }

    /* (non-Javadoc)
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

    /* (non-Javadoc)
     * @see org.deckfour.xes.model.XAttributable#getExtensions()
     */
    public Set<XExtension> getExtensions() {
        return extensions;
    }

    /* (non-Javadoc)
     * @see org.deckfour.xes.model.XLog#getClassifiers()
     */
    public List<XEventClassifier> getClassifiers() {
        return classifiers;
    }

    /* (non-Javadoc)
     * @see java.util.ArrayList#clone()
     */
    public Object clone() {
        XLogImpl clone = new XLogImpl((XAttributeMap) attributes.clone(), counter, intToEvent, eventToInt);
        clone.counter = counter;
        clone.intToEvent = new IntObjectHashMap<XEvent>(intToEvent);
        clone.eventToInt = new ObjectIntHashMap<XEvent>(eventToInt);
        clone.extensions = new UnifiedSet<XExtension>(extensions);
        clone.classifiers = new ArrayList<XEventClassifier>(classifiers);
        clone.globalTraceAttributes = new ArrayList<XAttribute>(globalTraceAttributes);
        clone.globalEventAttributes = new ArrayList<XAttribute>(globalEventAttributes);
        clone.cachedClassifier = null;
        clone.cachedInfo = null;
        clone.clear();
        for(XTrace trace : this) {
            clone.add((XTrace)trace.clone());
        }
        return clone;
    }

    /* (non-Javadoc)
     * @see org.deckfour.xes.model.XLog#getGlobalEventAttributes()
     */
    public List<XAttribute> getGlobalEventAttributes() {
        return globalEventAttributes;
    }

    /* (non-Javadoc)
     * @see org.deckfour.xes.model.XLog#getGlobalTraceAttributes()
     */
    public List<XAttribute> getGlobalTraceAttributes() {
        return globalTraceAttributes;
    }

    /*
     * Runs the given visitor on this log.
     *
     * (non-Javadoc)
     * @see org.deckfour.xes.model.XLog#accept(org.deckfour.xes.model.XVisitor)
     */
    public boolean accept(XVisitor visitor) {
		/*
		 * Check whether the visitor may run.
		 */
        if (visitor.precondition()) {
			/*
			 * Yes, it may. Now initialize.
			 */
            visitor.init(this);
			/*
			 * First call.
			 */
            visitor.visitLogPre(this);
			/*
			 * Visit the extensions.
			 */
            for (XExtension extension: extensions) {
                extension.accept(visitor, this);
            }
			/*
			 * Visit the classifiers.
			 */
            for (XEventClassifier classifier: classifiers) {
                classifier.accept(visitor, this);
            }
			/*
			 * Visit the attributes.
			 */
            for (XAttribute attribute: attributes.values()) {
                attribute.accept(visitor, this);
            }
			/*
			 * Visit the traces.
			 */
            for (XTrace trace: this) {
                trace.accept(visitor, this);
            }
			/*
			 * Last call.
			 */
            visitor.visitLogPost(this);
            return true;
        }
        return false;
    }

    /**
     * Returns the cached info if the given classifier is the cached classifier.
     * Returns null otherwise.
     */
    public XLogInfo getInfo(XEventClassifier classifier) {
        return classifier.equals(cachedClassifier) ? cachedInfo : null;
    }

    /**
     * Sets the cached classifier and info to the given objects.
     */
    public void setInfo(XEventClassifier classifier, XLogInfo info) {
        cachedClassifier = classifier;
        cachedInfo = info;
    }

//    private void addEvents(XTrace trace) {
//        for(XEvent event : trace) {
//            if(eventToInt.get(event) == null) {
//                eventToInt.put(event, counter[0]);
//                intToEvent.put(counter[0], event);
//                counter[0]++;
//            }
//        }
//    }
//
//    @Override
//    public boolean add(XTrace xEvents) {
//        addEvents(xEvents);
//        return traces.add(new XTraceOptimizedImpl(xEvents, eventToInt));
//    }
//
//    @Override
//    public boolean addAll(Collection<? extends XTrace> c) {
//        for(XTrace trace : c) {
//            add(trace);
//        }
//        return true;
//    }
//
//    @Override
//    public boolean addAll(int index, Collection<? extends XTrace> c) {
//        return false;
//    }
//
//    @Override
//    public void add(int index, XTrace element) {
//
//    }
//
//    @Override
//    public XTrace remove(int index) {
//        return traces.remove(index).getXTrace(new XFactoryMemoryImpl(), intToEvent);
//    }
//
//    @Override
//    public int indexOf(Object o) {
//        return 0;
//    }
//
//    @Override
//    public int lastIndexOf(Object o) {
//        return 0;
//    }
//
//    @Override
//    public ListIterator<XTrace> listIterator() {
//        return new ListIterator<XTrace>() {
//            ListIterator<XTraceOptimizedImpl> iterator = traces.listIterator();
//            @Override
//            public boolean hasNext() {
//                return iterator.hasNext();
//            }
//
//            @Override
//            public XTrace next() {
//                return iterator.next().getXTrace(new XFactoryMemoryImpl(), intToEvent);
//            }
//
//            @Override
//            public boolean hasPrevious() {
//                return iterator.hasPrevious();
//            }
//
//            @Override
//            public XTrace previous() {
//                return iterator.previous().getXTrace(new XFactoryMemoryImpl(), intToEvent);
//            }
//
//            @Override
//            public int nextIndex() {
//                return iterator.nextIndex();
//            }
//
//            @Override
//            public int previousIndex() {
//                return iterator.previousIndex();
//            }
//
//            @Override
//            public void remove() {
//                iterator.remove();
//            }
//
//            @Override
//            public void set(XTrace trace) {
//                addEvents(trace);
//                iterator.set(new XTraceOptimizedImpl(trace, eventToInt));
//            }
//
//            @Override
//            public void add(XTrace trace) {
//                addEvents(trace);
//                iterator.add(new XTraceOptimizedImpl(trace, eventToInt));
//            }
//        };
//    }
//
//    @Override
//    public ListIterator<XTrace> listIterator(int index) {
//        return new ListIterator<XTrace>() {
//            ListIterator<XTraceOptimizedImpl> iterator = traces.listIterator(index);
//            @Override
//            public boolean hasNext() {
//                return iterator.hasNext();
//            }
//
//            @Override
//            public XTrace next() {
//                return iterator.next().getXTrace(new XFactoryMemoryImpl(), intToEvent);
//            }
//
//            @Override
//            public boolean hasPrevious() {
//                return iterator.hasPrevious();
//            }
//
//            @Override
//            public XTrace previous() {
//                return iterator.previous().getXTrace(new XFactoryMemoryImpl(), intToEvent);
//            }
//
//            @Override
//            public int nextIndex() {
//                return iterator.nextIndex();
//            }
//
//            @Override
//            public int previousIndex() {
//                return iterator.previousIndex();
//            }
//
//            @Override
//            public void remove() {
//                iterator.remove();
//            }
//
//            @Override
//            public void set(XTrace trace) {
//                addEvents(trace);
//                iterator.set(new XTraceOptimizedImpl(trace, eventToInt));
//            }
//
//            @Override
//            public void add(XTrace trace) {
//                addEvents(trace);
//                iterator.add(new XTraceOptimizedImpl(trace, eventToInt));
//            }
//        };
//    }

}
