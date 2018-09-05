package com.highpowerbear.hpbanalytics.ordtrack;

import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.ib.client.Contract;

/**
 * Created by robertk on 12/28/2017.
 */
public class Position {

    private final String accountId;

    private final String symbol;
    private final String underlying;
    private final Currency currency;
    private final String exchange;
    private final SecType secType;

    private final double position;
    private final double avgCost;

    public Position(String accountId, Contract contract, double position, double avgCost) {
        this.accountId = accountId;

        symbol = contract.localSymbol();
        underlying = contract.symbol();
        currency = Currency.valueOf(contract.currency());
        secType = SecType.valueOf(contract.getSecType());
        exchange = contract.exchange() != null ? contract.exchange() : secType.getDefaultExchange();

        this.position = position;
        this.avgCost = avgCost;
    }

    public boolean isShort() {
        return position < 0d;
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

    public String getExchange() {
        return exchange;
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
        return "Position{" +
                "accountId='" + accountId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", underlying='" + underlying + '\'' +
                ", currency=" + currency +
                ", exchange='" + exchange + '\'' +
                ", secType=" + secType +
                ", position=" + position +
                ", avgCost=" + avgCost +
                '}';
    }
}
