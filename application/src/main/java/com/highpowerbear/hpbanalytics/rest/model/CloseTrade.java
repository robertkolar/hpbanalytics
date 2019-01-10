package com.highpowerbear.hpbanalytics.rest.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.highpowerbear.hpbanalytics.common.CoreSettings;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by robertk on 4/9/2018.
 */
public class CloseTrade {
    @JsonFormat(pattern = CoreSettings.JSON_DATE_FORMAT)
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
        return "CloseTrade{" +
                "closeDate=" + closeDate +
                ", closePrice=" + closePrice +
                '}';
    }
}