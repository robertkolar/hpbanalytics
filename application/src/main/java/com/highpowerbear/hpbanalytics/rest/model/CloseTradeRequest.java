package com.highpowerbear.hpbanalytics.rest.model;

import com.highpowerbear.hpbanalytics.enums.ManualCloseReason;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by robertk on 4/9/2018.
 */
public class CloseTradeRequest {

    private LocalDateTime closeDate;
    private BigDecimal closePrice;
    private ManualCloseReason reason;

    public LocalDateTime getCloseDate() {
        return closeDate;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    public ManualCloseReason getReason() {
        return reason;
    }
}
