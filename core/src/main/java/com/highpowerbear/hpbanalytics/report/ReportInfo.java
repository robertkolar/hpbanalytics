package com.highpowerbear.hpbanalytics.report;

import java.util.Calendar;

/**
 * Created by robertk on 12/28/2017.
 */
public class ReportInfo {

    private Long numExecutions;
    private Long numTrades;
    private Long numOpenTrades;
    private Long numUnderlyings;
    private Long numOpenUnderlyings;
    private Calendar firstExecutionDate;
    private Calendar lastExecutionDate;

    public ReportInfo(Long numExecutions, Long numTrades, Long numOpenTrades, Long numUnderlyings, Long numOpenUnderlyings, Calendar firstExecutionDate, Calendar lastExecutionDate) {
        this.numExecutions = numExecutions;
        this.numTrades = numTrades;
        this.numOpenTrades = numOpenTrades;
        this.numUnderlyings = numUnderlyings;
        this.numOpenUnderlyings = numOpenUnderlyings;
        this.firstExecutionDate = firstExecutionDate;
        this.lastExecutionDate = lastExecutionDate;
    }

    public Long getNumExecutions() {
        return numExecutions;
    }

    public Long getNumTrades() {
        return numTrades;
    }

    public Long getNumOpenTrades() {
        return numOpenTrades;
    }

    public Long getNumUnderlyings() {
        return numUnderlyings;
    }

    public Long getNumOpenUnderlyings() {
        return numOpenUnderlyings;
    }

    public Calendar getFirstExecutionDate() {
        return firstExecutionDate;
    }

    public Calendar getLastExecutionDate() {
        return lastExecutionDate;
    }
}
