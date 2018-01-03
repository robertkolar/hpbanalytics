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
    M6B(6250);

    private Integer multiplier;

    ContractMultiplier(Integer multiplier) {
        this.multiplier = multiplier;
    }

    public Integer getMultiplier() {
        return multiplier;
    }

    public static Integer getByUnderlying(String underlying) {
        int multiplier = 1;
        for (ContractMultiplier cm : ContractMultiplier.values()) {
            if (cm.name().equals(underlying)) {
                multiplier = cm.multiplier;
                break;
            }
        }
        return multiplier;
    }
}
