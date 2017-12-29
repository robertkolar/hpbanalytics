package com.highpowerbear.hpbanalytics.iblogger;

import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.SecType;

/**
 * Created by robertk on 12/28/2017.
 */
public class PositionVO {

    private String accountId;
    private String symbol;
    private String underlying;
    private Currency currency;
    private SecType secType;
    private double position;
    private double avgCost;

    public PositionVO(String accountId, String symbol, String underlying, Currency currency, SecType secType, double position, double avgCost) {
        this.accountId = accountId;
        this.symbol = symbol;
        this.underlying = underlying;
        this.currency = currency;
        this.secType = secType;
        this.position = position;
        this.avgCost = avgCost;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getUnderlying() {
        return underlying;
    }

    public Currency getCurrency() {
        return currency;
    }

    public SecType getSecType() {
        return secType;
    }

    public double getPosition() {
        return position;
    }

    public double getAvgCost() {
        return avgCost;
    }

    @Override
    public String toString() {
        return "PositionVO{" +
                "accountId='" + accountId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", underlying='" + underlying + '\'' +
                ", currency=" + currency +
                ", secType=" + secType +
                ", position=" + position +
                ", avgCost=" + avgCost +
                '}';
    }
}
