package com.highpowerbear.hpbanalytics.enums;

/**
 * Created by robertk on 11/18/2017.
 */
public enum OrderStatus {

    PRESUBMITTED(com.ib.client.OrderStatus.PreSubmitted),
    SUBMITTED(com.ib.client.OrderStatus.Submitted),
    UPDATED(null),
    CANCELLED(com.ib.client.OrderStatus.Cancelled),
    FILLED(com.ib.client.OrderStatus.Filled),
    UNKNOWN(null);

    private com.ib.client.OrderStatus ibStatus;

    OrderStatus(com.ib.client.OrderStatus ibStatus) {
        this.ibStatus = ibStatus;
    }

    public String getIbStatus() {
        return ibStatus != null ? ibStatus.name() : "";
    }
}