package com.raffaeleconforti.datastructures.exception;

import com.raffaeleconforti.foreignkeydiscovery.conceptualmodels.Entity;

import java.util.List;

/**
 * Created by conforti on 26/11/14.
 */
public class EmptyLogException extends Throwable {

    private List<Entity> entities;

    public EmptyLogException() {

    }

    public EmptyLogException(List<Entity> entities) {
        this.entities = entities;
    }

    public List<Entity> getEntities() {
        return entities;
    }
}
