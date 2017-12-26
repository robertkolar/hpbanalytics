package com.highpowerbear.hpbanalytics.enums;

/**
 * Created by robertk on 11/18/2017.
 */
public enum FuturePlMultiplier {
    ES(50),
    NQ(20),
    YM(5),
    GC(100),
    ZB(1000);

    private Integer multiplier;

    FuturePlMultiplier(Integer multiplier) {
        this.multiplier = multiplier;
    }

    public static Integer getMultiplierByUnderlying(String underlying) {
        if (ES.name().equalsIgnoreCase(underlying)) return ES.multiplier;
        if (NQ.name().equalsIgnoreCase(underlying)) return NQ.multiplier;
        if (YM.name().equalsIgnoreCase(underlying)) return YM.multiplier;
        if (GC.name().equalsIgnoreCase(underlying)) return GC.multiplier;
        if (ZB.name().equalsIgnoreCase(underlying)) return ZB.multiplier;
        return 1;
    }
}
