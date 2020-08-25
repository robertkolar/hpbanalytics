package com.highpowerbear.hpbanalytics.rest.model;

import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.enums.StatisticsInterval;
import com.highpowerbear.hpbanalytics.enums.TradeType;

import javax.validation.constraints.NotNull;

/**
 * Created by robertk on 8/25/2020.
 */
public class CalculateStatisticsRequest {

    private final StatisticsInterval interval;
    private final TradeType tradeType;
    private final SecType secType;
    private final Currency currency;
    private final String underlying;

    public CalculateStatisticsRequest(@NotNull StatisticsInterval interval, TradeType tradeType, SecType secType, Currency currency, String underlying) {
        this.interval = interval;
        this.tradeType = tradeType;
        this.secType = secType;
        this.currency = currency;
        this.underlying = underlying;
    }

    public StatisticsInterval getInterval() {
        return interval;
    }

    public TradeType getTradeType() {
        return tradeType;
    }

    public SecType getSecType() {
        return secType;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getUnderlying() {
        return underlying;
    }
}
