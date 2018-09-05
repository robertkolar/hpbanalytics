package com.highpowerbear.hpbanalytics.report;

import java.util.Calendar;

/**
 *
 * Created by robertk on 4/26/2015.
 */
public class StatisticsVO {

    private final int id;
    private final Calendar periodDate;
    private final int numOpened;
    private final int numClosed;
    private final int numWinners;
    private final int numLosers;
    private final double bigWinner;
    private final double bigLoser;
    private final double winnersProfit;
    private final double losersLoss;
    private final double profitLoss;
    private final double cumulProfitLoss;

    public StatisticsVO(
            int id, Calendar periodDate, int numOpened,int numClosed, int numWinners, int numLosers, double bigWinner,
            double bigLoser, double winnersProfit, double losersLoss, double profitLoss, double cumulProfitLoss) {

        this.id = id;
        this.periodDate = periodDate;
        this.numOpened = numOpened;
        this.numClosed = numClosed;
        this.numWinners = numWinners;
        this.numLosers = numLosers;
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

    public Calendar getPeriodDate() {
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
        return "StatisticsVO{" +
                "id=" + id +
                ", periodDate=" + periodDate +
                ", numOpened=" + numOpened +
                ", numClosed=" + numClosed +
                ", numWinners=" + numWinners +
                ", numLosers=" + numLosers +
                ", bigWinner=" + bigWinner +
                ", bigLoser=" + bigLoser +
                ", winnersProfit=" + winnersProfit +
                ", losersLoss=" + losersLoss +
                ", profitLoss=" + profitLoss +
                ", cumulProfitLoss=" + cumulProfitLoss +
                '}';
    }
}
