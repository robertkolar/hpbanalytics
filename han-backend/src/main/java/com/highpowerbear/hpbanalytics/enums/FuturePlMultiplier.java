package com.highpowerbear.hpbanalytics.enums;

/**
 * Created by robertk on 11/18/2017.
 */
public enum FuturePlMultiplier {
    ES("ES", 50),
    NQ("NQ", 20),
    YM("YM", 5),
    GC("GC", 100),
    ZB("ZB", 1000);

    private String underlying;
    private Integer multiplier;

    FuturePlMultiplier(String underlying, Integer multiplier) {
        this.underlying = underlying;
        this.multiplier = multiplier;
    }

    public static Integer getMultiplierByUnderlying(String underlying) {
        if (ES.underlying.equalsIgnoreCase(underlying)) return ES.multiplier;
        if (NQ.underlying.equalsIgnoreCase(underlying)) return NQ.multiplier;
        if (YM.underlying.equalsIgnoreCase(underlying)) return YM.multiplier;
        if (GC.underlying.equalsIgnoreCase(underlying)) return GC.multiplier;
        if (ZB.underlying.equalsIgnoreCase(underlying)) return ZB.multiplier;
        return 1;
    }
}
