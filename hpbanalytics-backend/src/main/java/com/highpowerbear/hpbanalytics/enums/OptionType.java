package com.highpowerbear.hpbanalytics.enums;

/**
 * Created by robertk on 11/18/2017.
 */
public enum OptionType {
    CALL("CALL", "C"),
    PUT("PUT", "P");

    private String name;
    private String shortName;

    OptionType(String name, String shortName) {
        this.name = name;
        this.shortName = shortName;
    }

    public String getName() {
        return name;
    }


    public String getShortName() {
        return shortName;
    }

    public static OptionType getFromShortName(String shortName) {
        return (shortName == null ? null : (shortName.equalsIgnoreCase(PUT.getShortName()) ? PUT : (shortName.equalsIgnoreCase(CALL.getShortName()) ? CALL : null)));
    }
}
