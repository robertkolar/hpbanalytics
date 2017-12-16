package com.highpowerbear.hpbanalytics.enums;

/**
 * Created by robertk on 12/16/2017.
 */
public enum IbOrderStatus {
    PENDINGSUBMIT ("PendingSubmit"),
    PENDINGCANCEL ("PendingCancel"),
    PRESUBMITTED ("PreSubmitted"),
    SUBMITTED ("Submitted"),
    CANCELLED ("Cancelled"),
    FILLED ("Filled"),
    INACTIVE ("Inactive");

    private String value;

    IbOrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
