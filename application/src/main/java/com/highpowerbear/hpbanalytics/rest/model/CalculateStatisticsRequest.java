package com.highpowerbear.hpbanalytics.rest.model;

import javax.validation.constraints.NotNull;
import java.time.temporal.ChronoUnit;

/**
 * Created by robertk on 8/25/2020.
 */
public class CalculateStatisticsRequest {

    @NotNull
    private ChronoUnit interval;
    private String tradeType;
    private String secType;
    private String currency;
    private String underlying;

    public ChronoUnit getInterval() {
        return interval;
    }

    public String getTradeType() {
        return tradeType;
    }

    public String getSecType() {
        return secType;
    }

    public String getCurrency() {
        return currency;
    }

    public String getUnderlying() {
        return underlying;
    }
}
