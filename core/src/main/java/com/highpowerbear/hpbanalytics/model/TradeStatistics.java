package com.highpowerbear.hpbanalytics.model;

/**
 * Created by robertk on 10/25/2020.
 */
public class TradeStatistics {

    private long numAllTrades;
    private long numAllUnderlyings;
    private long numOpenTrades;
    private long numOpenUnderlyings;

    public long getNumAllTrades() {
        return numAllTrades;
    }

    public TradeStatistics setNumAllTrades(long numAllTrades) {
        this.numAllTrades = numAllTrades;
        return this;
    }

    public long getNumAllUnderlyings() {
        return numAllUnderlyings;
    }

    public TradeStatistics setNumAllUnderlyings(long numAllUnderlyings) {
        this.numAllUnderlyings = numAllUnderlyings;
        return this;
    }

    public long getNumOpenTrades() {
        return numOpenTrades;
    }

    public TradeStatistics setNumOpenTrades(long numOpenTrades) {
        this.numOpenTrades = numOpenTrades;
        return this;
    }

    public long getNumOpenUnderlyings() {
        return numOpenUnderlyings;
    }

    public TradeStatistics setNumOpenUnderlyings(long numOpenUnderlyings) {
        this.numOpenUnderlyings = numOpenUnderlyings;
        return this;
    }
}
