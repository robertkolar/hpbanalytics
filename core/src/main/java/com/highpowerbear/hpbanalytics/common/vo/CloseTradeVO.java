package com.highpowerbear.hpbanalytics.common.vo;

import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Created by robertk on 4/9/2018.
 */
public class CloseTradeVO {
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
        return "CloseTradeVO{" +
                "closeDate=" + closeDate +
                ", closePrice=" + closePrice +
                '}';
    }
}