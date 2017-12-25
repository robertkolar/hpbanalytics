package com.highpowerbear.hpbanalytics.rest;

import java.util.List;

/**
 * Created by robertk on 1/14/2015.
 */
public class RestList<T> {
    private List<T> items;
    private Long total;

    public RestList(List<T> items, Long total) {
        this.items = items;
        this.total = total;
    }

    public Long getTotal() {
        return total;
    }

    public List<T> getItems() {
        return items;
    }
}
