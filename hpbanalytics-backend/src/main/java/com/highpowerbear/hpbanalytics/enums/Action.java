package com.highpowerbear.hpbanalytics.enums;

/**
 * Created by robertk on 11/18/2017.
 */
public enum Action {
    BUY ("BUY"),
    SELL ("SELL");

    private String name;

    Action(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
