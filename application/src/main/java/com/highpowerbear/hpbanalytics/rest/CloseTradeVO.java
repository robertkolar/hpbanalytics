package com.highpowerbear.hpbanalytics.rest;

import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Created by robertk on 10/15/2015.
 */
public class CloseTradeVO {

    private Calendar closeDate;
    private BigDecimal closePrice;

    public CloseTradeVO(Calendar closeDate, BigDecimal closePrice) {
        this.closeDate = closeDate;
        this.closePrice = closePrice;
    }

    public Calendar getCloseDate() {
        return closeDate;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    @Override
    public String toString() {
        return "CloseTradeVO{" +
                "closeDate=" + closeDate +
                ", closePrice=" + closePrice +
                '}';
    }
}
