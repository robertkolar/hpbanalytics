package com.highpowerbear.hpbanalytics.enums;

/**
 * Created by robertk on 11/18/2017.
 */
public enum OptionType {
    CALL,
    PUT;

    public static OptionType getFromShortName(String shortName) {
        switch (shortName) {
            case "C": return CALL;
            case "P": return PUT;
            default: return null;
        }
    }
}
