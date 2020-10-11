package com.highpowerbear.hpbanalytics.rest.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by robertk on 4/9/2018.
 */
public class CloseTradeRequest {

    private String executionReference;
    private LocalDateTime closeDate;
    private BigDecimal closePrice;

    public String getExecutionReference() {
        return executionReference;
    }

    public LocalDateTime getCloseDate() {
        return closeDate;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }
}
