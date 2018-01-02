package com.highpowerbear.hpbanalytics.enums;

/**
 * Created by robertk on 11/18/2017.
 */
public enum SecType {
    STK (false, "TRADES"),
    OPT (true, "TRADES"),
    FUT (true, "TRADES"),
    CASH(false, "MIDPOINT"),
    CFD (true, "MIDPOINT");

    private boolean derivative;
    private String ibWhatToShow;

    SecType(boolean derivative, String ibWhatToShow) {
        this.derivative = derivative;
        this.ibWhatToShow = ibWhatToShow;
    }

    public boolean isDerivative() {
        return derivative;
    }

    public String getIbWhatToShow() {
        return ibWhatToShow;
    }
}
