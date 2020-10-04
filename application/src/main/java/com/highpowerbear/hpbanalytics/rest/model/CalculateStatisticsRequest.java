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

    @NotNull
    private StatisticsInterval interval;
    private TradeType tradeType;
    private Types.SecType secType;
    private Currency currency;
    private String underlying;

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
