package com.highpowerbear.hpbanalytics.web;

import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Created by robertk on 10/15/2015.
 */
public class CloseTradeDto {

    private Calendar closeDate;
    private BigDecimal closePrice;

    public Calendar getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Calendar closeDate) {
        this.closeDate = closeDate;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(BigDecimal closePrice) {
        this.closePrice = closePrice;
    }
}
