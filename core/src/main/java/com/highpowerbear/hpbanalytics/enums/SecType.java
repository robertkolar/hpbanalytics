package com.highpowerbear.hpbanalytics.enums;

/**
 * Created by robertk on 11/18/2017.
 */
public enum SecType {
    STK(false, "SMART", "TRADES"),
    OPT(true, "SMART", "TRADES"),
    FUT(true, "GLOBEX", "TRADES"),
    CASH(false, "IDEALPRO", "MIDPOINT"),
    CFD(true, "SMART", "MIDPOINT"),
    CMDTY(false, "SMART", "TRADES");

    private boolean derivative;
    private String defaultExchange;
    private String ibWhatToShow;

    SecType(boolean derivative, String defaultExchange, String ibWhatToShow) {
        this.derivative = derivative;
        this.defaultExchange = defaultExchange;
        this.ibWhatToShow = ibWhatToShow;
    }

    public boolean isDerivative() {
        return derivative;
    }

    public String getDefaultExchange() {
        return defaultExchange;
    }

    public String getIbWhatToShow() {
        return ibWhatToShow;
    }
}
