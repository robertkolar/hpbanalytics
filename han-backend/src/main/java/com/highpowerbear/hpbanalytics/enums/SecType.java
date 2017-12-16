package com.highpowerbear.hpbanalytics.enums;

/**
 * Created by robertk on 11/18/2017.
 */
public enum SecType {
    STK ("STK"),
    OPT ("OPT"),
    FUT ("FUT"),
    CASH ("CASH"),
    CFD("CFD");

    private String name;

    SecType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
