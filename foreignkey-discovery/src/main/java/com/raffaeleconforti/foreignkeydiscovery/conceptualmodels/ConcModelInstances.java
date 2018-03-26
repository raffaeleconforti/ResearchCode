package com.raffaeleconforti.foreignkeydiscovery.conceptualmodels;

import org.eclipse.collections.impl.map.mutable.UnifiedMap;

import java.util.Map;

public class ConcModelInstances {//the instances of all entities 

    Map<Entity, EntityInstances> allInstances;

    public ConcModelInstances() {
        allInstances = new UnifiedMap<Entity, EntityInstances>();
    }

    public EntityInstances getEntInstances(Entity ent) {
        return allInstances.get(ent);
    }

    public void addEntity(Entity ent) {
        allInstances.put(ent, new EntityInstances());
    }

}
