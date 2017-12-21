package com.highpowerbear.hpbanalytics.common;

import com.highpowerbear.hpbanalytics.enums.OptionType;

import java.util.Date;

/**
 * Created by robertk on 10/9/2015.
 */
public class OptionParseResult {
    private String underlying;
    private OptionType optType;
    private Date expDate;
    private Double strikePrice;

    public String getUnderlying() {
        return underlying;
    }

    public void setUnderlying(String underlying) {
        this.underlying = underlying;
    }

    public OptionType getOptType() {
        return optType;
    }

    public void setOptType(OptionType optType) {
        this.optType = optType;
    }

    public Date getExpDate() {
        return expDate;
    }

    public void setExpDate(Date expDate) {
        this.expDate = expDate;
    }

    public Double getStrikePrice() {
        return strikePrice;
    }

    public void setStrikePrice(Double strikePrice) {
        this.strikePrice = strikePrice;
    }
}
