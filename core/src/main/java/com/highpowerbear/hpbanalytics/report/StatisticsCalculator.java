package com.highpowerbear.hpbanalytics.report;

import com.highpowerbear.hpbanalytics.common.CoreUtil;
import com.highpowerbear.hpbanalytics.common.MessageSender;
import com.highpowerbear.hpbanalytics.dao.ReportDao;
import com.highpowerbear.hpbanalytics.entity.Report;
import com.highpowerbear.hpbanalytics.entity.Trade;
import com.highpowerbear.hpbanalytics.enums.StatisticsInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static com.highpowerbear.hpbanalytics.common.CoreSettings.WS_TOPIC_REPORT;

/**
 * Created by robertk on 4/26/2015.
 */
@Service
public class StatisticsCalculator {
    private static final Logger log = LoggerFactory.getLogger(StatisticsCalculator.class);

    @Autowired private ReportDao reportDao;
    @Autowired private MessageSender messageSender;
    @Autowired private TradeCalculator tradeCalculator;

    private final Map<String, List<StatisticsVO>> statisticsMap = new HashMap<>(); // caching statistics to prevent excessive recalculation

    public List<StatisticsVO> getStatistics(Report report, StatisticsInterval interval, String underlying, Integer maxPoints) {

        List<StatisticsVO> allStatistics = statisticsMap.get(report.getId() + "_" + interval.name() + "_" + underlyingKey(underlying));
        if (allStatistics == null) {
            return new ArrayList<>();
        }

        Integer size = allStatistics.size();

        if (maxPoints == null || size < maxPoints) {
            maxPoints = size;
        }

        Integer firstIndex = size - maxPoints;
        // copy because reverse will be performed on it

        return new ArrayList<>(allStatistics.subList(firstIndex, size));
    }

    @Async
    public void calculateStatistics(int reportId, StatisticsInterval interval, String underlying) {
        log.info("BEGIN statistics calculation for report " + reportId + ", undl=" + underlying + ", interval=" + interval);

        List<Trade> trades = reportDao.getTradesByUnderlying(reportId, normalizeUnderlying(underlying));

        List<StatisticsVO> stats = doCalculate(trades, interval);
        statisticsMap.put(reportId + "_" + interval.name() + "_" + underlyingKey(underlying), stats);

        log.info("END statistics calculation for report " + reportId + ", interval=" + interval);

        messageSender.sendWsMessage(WS_TOPIC_REPORT, "statistics calculated for report " + reportId);
    }

    private String underlyingKey(String underlying) {
        return underlying == null ? "ALLUNDLS" : underlying;
    }

    private String normalizeUnderlying(String underlying) {
        return "ALLUNDLS".equals(underlying) ? null : underlying;
    }

    private List<StatisticsVO> doCalculate(List<Trade> trades, StatisticsInterval interval) {
        List<StatisticsVO> stats = new ArrayList<>();

        if (trades == null || trades.isEmpty()) {
            return stats;
        }

        Calendar firstDate = getFirstDate(trades);
        Calendar lastDate = getLastDate(trades);

        Calendar firstPeriodDate = CoreUtil.toBeginOfPeriod(firstDate, interval);
        Calendar lastPeriodDate = CoreUtil.toBeginOfPeriod(lastDate, interval);
        Calendar periodDate = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
        periodDate.setTimeInMillis(firstPeriodDate.getTimeInMillis());

        double cumulProfitLoss = 0.0;
        int statsCount = 1;

        while (periodDate.getTimeInMillis() <= lastPeriodDate.getTimeInMillis()) {

            Calendar periodDateCopy = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
            periodDateCopy.setTimeInMillis(periodDate.getTimeInMillis());
            List<Trade> tradesClosedForPeriod = this.getTradesClosedForPeriod(trades, periodDate, interval);

            int numWinners = 0;
            int numLosers = 0;
            double winnersProfit = 0.0;
            double losersLoss = 0.0;
            double bigWinner = 0.0;
            double bigLoser = 0.0;
            double profitLoss;

            for (Trade t : tradesClosedForPeriod) {
                double pl = tradeCalculator.calculatePLPortfolioBase(t);

                if (pl >= 0) {
                    numWinners++;
                    winnersProfit += pl;

                    if (pl > bigWinner) {
                        bigWinner = pl;
                    }
                } else {
                    numLosers++;
                    losersLoss += pl;

                    if (pl < bigLoser) {
                        bigLoser = pl;
                    }
                }
            }
            profitLoss = winnersProfit + losersLoss;
            cumulProfitLoss += profitLoss;

            StatisticsVO s = new StatisticsVO(
                    statsCount++,
                    periodDateCopy,
                    getNumTradesOpenedForPeriod(trades, periodDate, interval),
                    tradesClosedForPeriod.size(),
                    numWinners,
                    numLosers,
                    CoreUtil.round2(winnersProfit),
                    CoreUtil.round2(losersLoss),
                    CoreUtil.round2(bigWinner),
                    CoreUtil.round2(bigLoser),
                    CoreUtil.round2(profitLoss),
                    CoreUtil.round2(cumulProfitLoss)
            );
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
        return trades.stream()
                .filter(t -> t.getCloseDate() != null)
                .filter(t -> CoreUtil.toBeginOfPeriod(t.getCloseDate(), interval).getTimeInMillis() == periodDate.getTimeInMillis())
                .collect(Collectors.toList());
    }
}
