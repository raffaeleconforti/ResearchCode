package com.raffaeleconforti.foreignkeydiscovery.mapping;

import java.lang.ref.WeakReference;

public class MappedObject<M, B> {

    private WeakReference<M> MappedElement; //Attribute for conceptual model attributes, ProcletTransition for Proclet transitions
    private WeakReference<B> Belongs2Object;

    public MappedObject(M m, B b) {
        MappedElement = new WeakReference<M>(m);
        Belongs2Object = new WeakReference<B>(b);
    }

    public M getElement() {
        return MappedElement.get();
    }

    public B getBelongs2() {
        return Belongs2Object.get();
    }

}
