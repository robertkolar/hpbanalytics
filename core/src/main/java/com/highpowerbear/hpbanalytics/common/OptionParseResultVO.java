package com.highpowerbear.hpbanalytics.common;

import com.highpowerbear.hpbanalytics.enums.OptionType;

import java.util.Date;

/**
 * Created by robertk on 10/9/2015.
 */
public class OptionParseResultVO {

    private String underlying;
    private OptionType optType;
    private Date expDate;
    private double strikePrice;

    public OptionParseResultVO(String underlying, OptionType optType, Date expDate, double strikePrice) {
        this.underlying = underlying;
        this.optType = optType;
        this.expDate = expDate;
        this.strikePrice = strikePrice;
    }

    public String getUnderlying() {
        return underlying;
    }

    public OptionType getOptType() {
        return optType;
    }

    public Date getExpDate() {
        return expDate;
    }

    public double getStrikePrice() {
        return strikePrice;
    }

    @Override
    public String toString() {
        return "OptionParseResultVO{" +
                "underlying='" + underlying + '\'' +
                ", optType=" + optType +
                ", expDate=" + expDate +
                ", strikePrice=" + strikePrice +
                '}';
    }
}
