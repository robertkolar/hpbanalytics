package com.highpowerbear.hpbanalytics.enums;

/**
 * Created by robertk on 11/18/2017.
 */
public enum SecType {
    STK(false, "SMART"),
    OPT(true, "SMART"),
    FUT(true, "GLOBEX"),
    CASH(false, "IDEALPRO"),
    CFD(true, "SMART"),
    CMDTY(false, "SMART"),
    BAG(false, "SMART");

    private boolean derivative;
    private String defaultExchange;

    SecType(boolean derivative, String defaultExchange) {
        this.derivative = derivative;
        this.defaultExchange = defaultExchange;
    }

    public boolean isDerivative() {
        return derivative;
    }

    public String getDefaultExchange() {
        return defaultExchange;
    }
}
