package com.highpowerbear.hpbanalytics.report;

import java.util.Calendar;

/**
 * Created by robertk on 12/28/2017.
 */
public class ReportInfoVO {

    private long numExecutions;
    private long numTrades;
    private long numOpenTrades;
    private long numUnderlyings;
    private long numOpenUnderlyings;
    private Calendar firstExecutionDate;
    private Calendar lastExecutionDate;

    public ReportInfoVO(long numExecutions, long numTrades, long numOpenTrades, long numUnderlyings, long numOpenUnderlyings, Calendar firstExecutionDate, Calendar lastExecutionDate) {
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
        return "ReportInfoVO{" +
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
