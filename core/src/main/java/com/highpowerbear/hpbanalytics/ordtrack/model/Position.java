package com.highpowerbear.hpbanalytics.ordtrack.model;

import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.SecType;
/**
 * Created by robertk on 12/28/2017.
 */
public class Position {

    private final String accountId;
    private final int conid;
    private final SecType secType;
    private final String underlyingSymbol;
    private final String symbol;
    private final Currency currency;
    private final String exchange;
    private double size;

    public Position(String accountId, int conid, SecType secType, String underlyingSymbol, String symbol, Currency currency, String exchange, double size) {
        this.accountId = accountId;
        this.conid = conid;
        this.secType = secType;
        this.underlyingSymbol = underlyingSymbol;
        this.symbol = symbol;
        this.currency = currency;
        this.exchange = exchange;
        this.size = size;
    }

    public boolean isShort() {
        return size < 0d;
    }

    public String getAccountId() {
        return accountId;
    }

    public int getConid() {
        return conid;
    }

    public SecType getSecType() {
        return secType;
    }

    public String getUnderlyingSymbol() {
        return underlyingSymbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getExchange() {
        return exchange;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "Position{" +
                "accountId='" + accountId + '\'' +
                ", conid=" + conid +
                ", secType=" + secType +
                ", underlyingSymbol='" + underlyingSymbol + '\'' +
                ", symbol='" + symbol + '\'' +
                ", currency=" + currency +
                ", exchange='" + exchange + '\'' +
                ", size=" + size +
                '}';
    }
}
