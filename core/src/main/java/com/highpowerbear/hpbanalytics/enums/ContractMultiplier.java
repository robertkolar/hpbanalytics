package com.highpowerbear.hpbanalytics.enums;

/**
 * Created by robertk on 11/18/2017.
 */
public enum ContractMultiplier {
    ES(50),
    NQ(20),
    YM(5),
    GC(100),
    ZB(1000),
    M6E(12500),
    M6A(10000),
    M6B(6250),
    ESTX50(10),
    DAX(5),
    SMI(10),
    N225(1000),
    N225M(100),
    K200(250000),
    K200M(50000);

    private final Integer multiplier;

    ContractMultiplier(Integer multiplier) {
        this.multiplier = multiplier;
    }

    public static Integer getByUnderlying(SecType secType, String underlying) {
        int multiplier = secType == SecType.OPT ? 100 : 1;

        for (ContractMultiplier cm : ContractMultiplier.values()) {
            if (cm.name().equals(underlying)) {
                multiplier = cm.multiplier;
                break;
            }
        }
        return multiplier;
    }
}
