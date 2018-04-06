package com.raffaeleconforti.foreignkeydiscovery.util;

import com.raffaeleconforti.foreignkeydiscovery.conceptualmodels.Attribute;
import com.raffaeleconforti.foreignkeydiscovery.conceptualmodels.Entity;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 9/03/2016.
 */
public class EntityNameExtractor {

    public static Set<String> getEntityName(Entity entity) {

        String name = entity.getName();

        name = name.replace(" ", "");
        name = name.substring(1, name.length() - 1);

        Set<String> result = new UnifiedSet<String>();

        StringTokenizer st = new StringTokenizer(name, ",;");
        if (name.contains(",") || name.contains(";")) {
            while (st.hasMoreTokens()) {
                result.add(st.nextToken());
            }
        } else {
            result.add(name);
        }

        return result;

    }

    public static Set<String> evTypeNames(Entity e) {
        Set<String> names = new UnifiedSet<String>();
        List<Attribute> TS = e.getTimestamps();
        for (Attribute ts : TS) {
            names.add(ts.getName());
        }
        return names;
    }
}
