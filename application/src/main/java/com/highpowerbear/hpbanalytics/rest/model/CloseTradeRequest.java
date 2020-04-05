package com.highpowerbear.hpbanalytics.rest.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.highpowerbear.hpbanalytics.config.HanSettings;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by robertk on 4/9/2018.
 */
public class CloseTradeRequest {

    @JsonFormat(pattern = HanSettings.JSON_DATE_FORMAT)
    private LocalDateTime closeDate;
    private BigDecimal closePrice;

    public LocalDateTime getCloseDate() {
        return closeDate;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    @Override
    public String toString() {
        return "CloseTradeRequest{" +
                "closeDate=" + closeDate +
                ", closePrice=" + closePrice +
                '}';
    }
}
