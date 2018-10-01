package com.highpowerbear.hpbanalytics.report.model;

import java.util.Calendar;

/**
 * Created by robertk on 12/28/2017.
 */
public class ReportInfo {

    private final long numExecutions;
    private final long numTrades;
    private final long numOpenTrades;
    private final long numUnderlyings;
    private final long numOpenUnderlyings;
    private final Calendar firstExecutionDate;
    private final Calendar lastExecutionDate;

    public ReportInfo(long numExecutions, long numTrades, long numOpenTrades, long numUnderlyings, long numOpenUnderlyings, Calendar firstExecutionDate, Calendar lastExecutionDate) {
        this.numExecutions = numExecutions;
        this.numTrades = numTrades;
        this.numOpenTrades = numOpenTrades;
        this.numUnderlyings = numUnderlyings;
        this.numOpenUnderlyings = numOpenUnderlyings;
        this.firstExecutionDate = firstExecutionDate;
        this.lastExecutionDate = lastExecutionDate;
    }

    public long getNumExecutions() {
        return numExecutions;
    }

    public long getNumTrades() {
        return numTrades;
    }

    public long getNumOpenTrades() {
        return numOpenTrades;
    }

    public long getNumUnderlyings() {
        return numUnderlyings;
    }

    public long getNumOpenUnderlyings() {
        return numOpenUnderlyings;
    }

    public Calendar getFirstExecutionDate() {
        return firstExecutionDate;
    }

    public Calendar getLastExecutionDate() {
        return lastExecutionDate;
    }

    @Override
    public String toString() {
        return "ReportInfo{" +
                "numExecutions=" + numExecutions +
                ", numTrades=" + numTrades +
                ", numOpenTrades=" + numOpenTrades +
                ", numUnderlyings=" + numUnderlyings +
                ", numOpenUnderlyings=" + numOpenUnderlyings +
                ", firstExecutionDate=" + firstExecutionDate +
                ", lastExecutionDate=" + lastExecutionDate +
                '}';
    }
}
