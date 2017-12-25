package com.highpowerbear.hpbanalytics.report;

import com.highpowerbear.hpbanalytics.common.CoreUtil;
import com.highpowerbear.hpbanalytics.dao.ReportDao;
import com.highpowerbear.hpbanalytics.entity.Report;
import com.highpowerbear.hpbanalytics.entity.Trade;
import com.highpowerbear.hpbanalytics.enums.StatisticsInterval;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * Created by robertk on 4/26/2015.
 */
@Service
public class StatisticsCalculator {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(StatisticsCalculator.class);

    @Autowired private ReportDao reportDao;
    private Map<String, List<Statistics>> statisticsMap = new HashMap<>(); // caching statistics to prevent excessive recalculation

    public void clearCache(Report report) {
        Map<String, List<Statistics>> shanpshotMap = new HashMap<>(statisticsMap);
        shanpshotMap.keySet().stream().filter(key -> key.startsWith(report.getId() + "_")).forEach(statisticsMap::remove);
    }

    public List<Statistics> getStatistics(Report report, StatisticsInterval interval, String underlying, Integer maxPoints) {
        if(statisticsMap.get(report.getId() + "_" + interval.name() + "_" + underlying) == null) {
            calculateStatistics(report, interval, underlying);
        }

        List<Statistics> allStatistics = statisticsMap.get(report.getId() + "_" + interval.name() + "_" + underlying);
        Integer size = allStatistics.size();
        if (maxPoints == null || size < maxPoints) {
            maxPoints = size;
        }

        Integer firstIndex = size - maxPoints;
        // copy because reverse will be performed on it

        return new ArrayList<>(allStatistics.subList(firstIndex, size));
    }

    public List<Statistics> calculateStatistics(Report report, StatisticsInterval interval, String underlying) {
        log.info("START statistics calculation for " + report.getReportName() + ", undl=" + underlying + ", interval=" + interval);

        List<Trade> trades = reportDao.getTradesByUnderlying(report, underlying);
        List<Statistics> stats = doCalculate(trades, interval);
        statisticsMap.put(report.getId() + "_" + interval.name() + "_" + underlying, stats);
        log.info("END statistics calculation for " + report.getReportName() + ", interval=" + interval);

        return stats;
    }

    private List<Statistics> doCalculate(List<Trade> trades, StatisticsInterval interval) {
        List<Statistics> stats = new ArrayList<>();
        if (trades == null || trades.isEmpty()) {
            return stats;
        }

        Calendar firstPeriodDate = CoreUtil.toBeginOfPeriod(this.getFirstDate(trades), interval);
        Calendar lastPeriodDate = CoreUtil.toBeginOfPeriod(this.getLastDate(trades), interval);
        Calendar periodDate = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
        periodDate.setTimeInMillis(firstPeriodDate.getTimeInMillis());
        double cumulProfitLoss = 0.0;
        int statsCount = 1;

        while (periodDate.getTimeInMillis() <= lastPeriodDate.getTimeInMillis()) {
            Statistics s = new Statistics(statsCount++);
            Calendar periodDateCopy = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
            periodDateCopy.setTimeInMillis(periodDate.getTimeInMillis());
            s.setPeriodDate(periodDateCopy);
            s.setNumOpened(this.getNumTradesOpenedForPeriod(trades, periodDate, interval));
            List<Trade> tradesClosedForPeriod = this.getTradesClosedForPeriod(trades, periodDate, interval);
            s.setNumClosed(tradesClosedForPeriod.size());
            int numWinners = 0;
            int numLosers = 0;
            double winnersProfit = 0.0;
            double losersLoss = 0.0;
            double maxWinner = 0.0;
            double maxLoser = 0.0;
            double profitLoss;

            for (Trade t : tradesClosedForPeriod) {
                if (t.getProfitLoss().doubleValue() >= 0.0) {
                    numWinners++;
                    winnersProfit += t.getProfitLoss().doubleValue();
                    if (t.getProfitLoss().doubleValue() > maxWinner) {
                        maxWinner = t.getProfitLoss().doubleValue();
                    }
                } else {
                    numLosers++;
                    losersLoss -= t.getProfitLoss().doubleValue();
                    if (t.getProfitLoss().doubleValue() < maxLoser) {
                        maxLoser = t.getProfitLoss().doubleValue();
                    }
                }
            }

            profitLoss = winnersProfit - losersLoss;
            cumulProfitLoss += profitLoss;
            s.setNumWinners(numWinners);
            s.setNumLosers(numLosers);
            s.setWinnersProfit(CoreUtil.round2(winnersProfit));
            s.setLosersLoss(CoreUtil.round2(losersLoss));
            s.setMaxWinner(maxWinner);
            s.setMaxLoser(maxLoser == 0.0 ? maxLoser : -maxLoser);
            s.setProfitLoss(CoreUtil.round2(profitLoss));
            s.setCumulProfitLoss(CoreUtil.round2(cumulProfitLoss));
            stats.add(s);

            if (StatisticsInterval.DAY.equals(interval)) {
                periodDate.add(Calendar.DAY_OF_MONTH, +1);
            } else if (StatisticsInterval.MONTH.equals(interval)) {
                periodDate.add(Calendar.MONTH, +1);
            }
        }
        return stats;
    }

    private Calendar getFirstDate(List<Trade> trades) {
        Calendar firstDateOpened = trades.get(0).getOpenDate();
        for (Trade t: trades) {
            if (t.getOpenDate().before(firstDateOpened)) {
                firstDateOpened = t.getOpenDate();
            }
        }
        return firstDateOpened;
    }

    private Calendar getLastDate(List<Trade> trades) {
        Calendar lastDate;
        Calendar lastDateOpened = trades.get(0).getOpenDate();
        Calendar lastDateClosed = trades.get(0).getCloseDate();

        for (Trade t: trades) {
            if (t.getOpenDate().after(lastDateOpened)) {
                lastDateOpened = t.getOpenDate();
            }
        }
        for (Trade t: trades) {
            if (t.getCloseDate() != null && (lastDateClosed == null || t.getCloseDate().after(lastDateClosed))) {
                lastDateClosed = t.getCloseDate();
            }
        }
        lastDate = (lastDateClosed == null || lastDateOpened.after(lastDateClosed) ? lastDateOpened : lastDateClosed);
        return lastDate;
    }

    private int getNumTradesOpenedForPeriod(List<Trade> trades, Calendar periodDate, StatisticsInterval interval) {
        int count = 0;
        for (Trade t: trades) {
            if (CoreUtil.toBeginOfPeriod(t.getOpenDate(), interval).getTimeInMillis() == periodDate.getTimeInMillis()) {
                count++;
            }
        }
        return count;
    }

    private List<Trade> getTradesClosedForPeriod(List<Trade> trades, Calendar periodDate, StatisticsInterval interval) {
        return trades.stream().filter(t -> t.getCloseDate() != null && CoreUtil.toBeginOfPeriod(t.getCloseDate(), interval).getTimeInMillis() == periodDate.getTimeInMillis()).collect(Collectors.toList());
    }
}
