package com.highpowerbear.hpbanalytics.iblogger;

import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.ib.client.Contract;

/**
 * Created by robertk on 12/28/2017.
 */
public class Position {

    private String accountId;

    private String symbol;
    private String underlying;
    private Currency currency;
    private String exchange;
    private SecType secType;

    private double position;
    private double avgCost;
    private double lastPrice;

    public Position(String accountId, Contract contract, double position, double avgCost) {
        this.accountId = accountId;

        symbol = contract.localSymbol();
        underlying = contract.symbol();
        currency = Currency.valueOf(contract.currency());
        exchange = contract.exchange();
        secType = SecType.valueOf(contract.getSecType());

        if (exchange == null) {
            switch (secType) {
                case CASH: exchange = "IDEALPRO"; break;
                case FUT: exchange = "GLOBEX"; break;
                default: exchange = "SMART";
            }
        }

        this.position = position;
        this.avgCost = avgCost;
    }

    public Contract createHistDataContract() {
        Contract contract = new Contract();

        contract.symbol(underlying);
        contract.localSymbol(symbol);
        contract.currency(currency.name());
        contract.exchange(exchange);
        contract.secType(secType.name());

        if (secType == SecType.CFD) {
            if (symbol.endsWith("n")) {
                contract.localSymbol(symbol.substring(0, symbol.length() - 1));
            }
            if (currency == Currency.USD) {
                contract.secType(SecType.STK.name());
            }
        }
        return contract;
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

    public double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
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
                ", lastPrice=" + lastPrice +
                '}';
    }
}
