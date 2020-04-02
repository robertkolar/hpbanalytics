package com.highpowerbear.hpbanalytics.rest.model;

import java.util.Collection;

/**
 * Created by robertk on 1/14/2015.
 */
public class GenericList<T> {

    private final Collection<T> items;
    private final int total;

    public GenericList(Collection<T> items, int total) {
        this.items = items;
        this.total = total;
    }

    public int getTotal() {
        return total;
    }

    public Collection<T> getItems() {
        return items;
    }
}
