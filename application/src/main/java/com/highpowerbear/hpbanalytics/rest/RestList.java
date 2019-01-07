package com.highpowerbear.hpbanalytics.rest;

import java.util.Collection;

/**
 * Created by robertk on 1/14/2015.
 */
public class RestList<T> {
    private final Collection<T> items;
    private final int total;

    public RestList(Collection<T> items, int total) {
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
