package com.highpowerbear.hpbanalytics.common.vo;

import com.highpowerbear.hpbanalytics.enums.OptionType;

import java.util.Date;

/**
 * Created by robertk on 10/9/2015.
 */
public class OptionInfoVO {

    private final String underlying;
    private final OptionType optionType;
    private final Date expirationDate;
    private final double strikePrice;

    public OptionInfoVO(String underlying, OptionType optionType, Date expirationDate, double strikePrice) {
        this.underlying = underlying;
        this.optionType = optionType;
        this.expirationDate = expirationDate;
        this.strikePrice = strikePrice;
    }

    public String getUnderlying() {
        return underlying;
    }

    public OptionType getOptionType() {
        return optionType;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public double getStrikePrice() {
        return strikePrice;
    }

    @Override
    public String toString() {
        return "OptionInfoVO{" +
                "underlying='" + underlying + '\'' +
                ", optionType=" + optionType +
                ", expirationDate=" + expirationDate +
                ", strikePrice=" + strikePrice +
                '}';
    }
}
