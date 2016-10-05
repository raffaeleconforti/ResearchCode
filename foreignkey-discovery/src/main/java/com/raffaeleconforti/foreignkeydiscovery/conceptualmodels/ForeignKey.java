package com.raffaeleconforti.foreignkeydiscovery.conceptualmodels;

/**
 * A foreign key consisting of the attribute which is a foreign key and the
 * entity and attribute to which it points (key or part of it)
 *
 * @author Viara Popova
 */
public class ForeignKey implements Comparable<ForeignKey> {
    private Attribute fk;
    private Entity ent; //entity
    private Attribute key;//key attribute

    public ForeignKey(Attribute f, Entity e, Attribute k) {
        this.fk = f;
        this.ent = e;
        this.key = k;
    }

    public Attribute getFKey() {
        return fk;
    }

    public Entity getEntity() {
        return ent;
    }

    public Attribute getKey() {
        return key;
    }

    public int compareTo(ForeignKey o) {
        if (ent.equals(o.ent)) {
            if (fk.equals(o.fk)) {
                if (key.equals(o.fk)) {
                    return 0;
                }
                return key.compareTo(o.key);
            }
            return fk.compareTo(o.fk);
        }
        return ent.compareTo(o.ent);
    }
}
