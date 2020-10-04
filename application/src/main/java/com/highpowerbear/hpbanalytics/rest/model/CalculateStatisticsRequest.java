package com.highpowerbear.hpbanalytics.rest.model;

import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.StatisticsInterval;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import com.ib.client.Types;

import javax.validation.constraints.NotNull;

/**
 * Created by robertk on 8/25/2020.
 */
public class CalculateStatisticsRequest {

    private final StatisticsInterval interval;
    private final TradeType tradeType;
    private final Types.SecType secType;
    private final Currency currency;
    private final String underlying;

    public CalculateStatisticsRequest(@NotNull StatisticsInterval interval, TradeType tradeType, Types.SecType secType, Currency currency, String underlying) {
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

    public Types.SecType getSecType() {
        return secType;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getUnderlying() {
        return underlying;
    }
}
