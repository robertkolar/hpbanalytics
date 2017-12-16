package com.highpowerbear.hpbanalytics.enums;

/**
 * Created by robertk on 11/18/2017.
 */
public enum StatisticsInterval {
    DAY("Daily"),
    MONTH("Monthly"),
    YEAR("Yearly");

    private String name;

    StatisticsInterval(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static StatisticsInterval getByName(String name) {
        return (DAY.getName().equals(name) ? DAY : MONTH);
    }
}
