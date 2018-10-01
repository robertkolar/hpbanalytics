package com.highpowerbear.hpbanalytics.common.model;

import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Created by robertk on 4/9/2018.
 */
public class CloseTrade {
    private Calendar closeDate;
    private BigDecimal closePrice;

    public Calendar getCloseDate() {
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