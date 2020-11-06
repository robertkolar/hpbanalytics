package com.highpowerbear.hpbanalytics.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * Created by robertk on 4/26/2015.
 */
public class Statistics implements Serializable {
    private static final long serialVersionUID = 8464224239795026258L;

    private final int id;
    private final LocalDateTime periodDate;
    private final int numExecs;
    private final int numOpened;
    private final int numClosed;
    private final int numWinners;
    private final int numLosers;
    private final double pctWinners;
    private final BigDecimal bigWinner;
    private final BigDecimal bigLoser;
    private final BigDecimal winnersProfit;
    private final BigDecimal losersLoss;
    private final BigDecimal profitLoss;
    private final BigDecimal cumulProfitLoss;

    public Statistics(int id, LocalDateTime periodDate, int numExecs, int numOpened,int numClosed, int numWinners, int numLosers, double pctWinners,
                      BigDecimal bigWinner, BigDecimal bigLoser, BigDecimal winnersProfit, BigDecimal losersLoss, BigDecimal profitLoss, BigDecimal cumulProfitLoss) {

        this.id = id;
        this.periodDate = periodDate;
        this.numExecs = numExecs;
        this.numOpened = numOpened;
        this.numClosed = numClosed;
        this.numWinners = numWinners;
        this.numLosers = numLosers;
        this.pctWinners = pctWinners;
        this.bigWinner = bigWinner;
        this.bigLoser = bigLoser;
        this.winnersProfit = winnersProfit;
        this.losersLoss = losersLoss;
        this.profitLoss = profitLoss;
        this.cumulProfitLoss = cumulProfitLoss;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getPeriodDate() {
        return periodDate;
    }

    public int getNumExecs() {
        return numExecs;
    }

    public int getNumOpened() {
        return numOpened;
    }

    public int getNumClosed() {
        return numClosed;
    }

    public int getNumWinners() {
        return numWinners;
    }

    public int getNumLosers() {
        return numLosers;
    }

    public double getPctWinners() {
        return pctWinners;
    }

    public BigDecimal getBigWinner() {
        return bigWinner;
    }

    public BigDecimal getBigLoser() {
        return bigLoser;
    }

    public BigDecimal getWinnersProfit() {
        return winnersProfit;
    }

    public BigDecimal getLosersLoss() {
        return losersLoss;
    }

    public BigDecimal getProfitLoss() {
        return profitLoss;
    }

    public BigDecimal getCumulProfitLoss() {
        return cumulProfitLoss;
    }

    @Override
    public String toString() {
        return "Statistics{" +
                "id=" + id +
                ", periodDate=" + periodDate +
                ", numExecs=" + numExecs +
                ", numOpened=" + numOpened +
                ", numClosed=" + numClosed +
                ", numWinners=" + numWinners +
                ", numLosers=" + numLosers +
                ", pctWinners=" + pctWinners +
                ", bigWinner=" + bigWinner +
                ", bigLoser=" + bigLoser +
                ", winnersProfit=" + winnersProfit +
                ", losersLoss=" + losersLoss +
                ", profitLoss=" + profitLoss +
                ", cumulProfitLoss=" + cumulProfitLoss +
                '}';
    }
}
