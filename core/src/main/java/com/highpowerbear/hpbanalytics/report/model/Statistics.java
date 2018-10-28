package com.highpowerbear.hpbanalytics.report.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.highpowerbear.hpbanalytics.common.CoreSettings;

import java.time.LocalDateTime;

/**
 *
 * Created by robertk on 4/26/2015.
 */
public class Statistics {

    private final int id;
    @JsonFormat(pattern = CoreSettings.JSON_DATE_FORMAT)
    private final LocalDateTime periodDate;
    private final int numOpened;
    private final int numClosed;
    private final int numWinners;
    private final int numLosers;
    private final double pctWinners;
    private final double bigWinner;
    private final double bigLoser;
    private final double winnersProfit;
    private final double losersLoss;
    private final double profitLoss;
    private final double cumulProfitLoss;

    public Statistics(int id, LocalDateTime periodDate, int numOpened,int numClosed, int numWinners, int numLosers, double pctWinners,
                      double bigWinner, double bigLoser, double winnersProfit, double losersLoss, double profitLoss, double cumulProfitLoss) {

        this.id = id;
        this.periodDate = periodDate;
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

    public double getBigWinner() {
        return bigWinner;
    }

    public double getBigLoser() {
        return bigLoser;
    }

    public double getWinnersProfit() {
        return winnersProfit;
    }

    public double getLosersLoss() {
        return losersLoss;
    }

    public double getProfitLoss() {
        return profitLoss;
    }

    public double getCumulProfitLoss() {
        return cumulProfitLoss;
    }

    @Override
    public String toString() {
        return "Statistics{" +
                "id=" + id +
                ", periodDate=" + periodDate +
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
