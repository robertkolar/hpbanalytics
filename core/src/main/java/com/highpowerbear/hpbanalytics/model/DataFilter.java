package com.highpowerbear.hpbanalytics.model;

import java.util.List;

/**
 * Created by robertk on 4/19/2020.
 */
public class DataFilter {

    private List<FilterItem> items;

    public List<FilterItem> getItems() {
        return items;
    }

    public static class FilterItem {
        private String value;
        private String property;

        public String getValue() {
            return value;
        }

        public String getProperty() {
            return property;
        }
    }
}
