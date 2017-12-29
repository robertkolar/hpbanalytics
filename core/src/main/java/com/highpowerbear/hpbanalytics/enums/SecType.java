package com.highpowerbear.hpbanalytics.enums;

/**
 * Created by robertk on 11/18/2017.
 */
public enum SecType {
    STK (false),
    OPT (true),
    FUT (true),
    CASH(false),
    CFD (true);

    private boolean derivative;

    SecType(boolean derivative) {
        this.derivative = derivative;
    }

    public boolean isDerivative() {
        return derivative;
    }
}
