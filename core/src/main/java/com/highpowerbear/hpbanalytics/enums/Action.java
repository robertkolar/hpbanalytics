package com.highpowerbear.hpbanalytics.enums;

/**
 * Created by robertk on 11/18/2017.
 */
public enum Action {
    BUY,
    SELL;

    public static Action getByExecSide(String execAction) {
        switch(execAction) {
            case "BOT": return BUY;
            case "SLD": return SELL;
            default: return null;
        }
    }
}
