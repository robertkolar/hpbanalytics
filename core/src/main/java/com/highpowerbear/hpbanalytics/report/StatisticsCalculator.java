package com.highpowerbear.hpbanalytics.report;

import com.highpowerbear.hpbanalytics.common.CoreUtil;
import com.highpowerbear.hpbanalytics.common.MessageSender;
import com.highpowerbear.hpbanalytics.dao.ReportDao;
import com.highpowerbear.hpbanalytics.entity.Report;
import com.highpowerbear.hpbanalytics.entity.Trade;
import com.highpowerbear.hpbanalytics.enums.StatisticsInterval;
import com.highpowerbear.hpbanalytics.report.model.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.highpowerbear.hpbanalytics.common.CoreSettings.WS_TOPIC_REPORT;

/**
 * Created by robertk on 4/26/2015.
 */
@Service
public class StatisticsCalculator {
    private static final Logger log = LoggerFactory.getLogger(StatisticsCalculator.class);

    private final ReportDao reportDao;
    private final MessageSender messageSender;
    private final TradeCalculator tradeCalculator;

    private final Map<String, List<Statistics>> statisticsMap = new ConcurrentHashMap<>(); // caching statistics to prevent excessive recalculation

    @Autowired
    public StatisticsCalculator(ReportDao reportDao, MessageSender messageSender, TradeCalculator tradeCalculator) {
        this.reportDao = reportDao;
        this.messageSender = messageSender;
        this.tradeCalculator = tradeCalculator;
    }

    public List<Statistics> getStatistics(Report report, StatisticsInterval interval, String tradeType, String secType, String currency, String underlying, Integer maxPoints) {

        List<Statistics> statisticsList = statisticsMap.get(statisticsKey(report.getId(), interval, tradeType, secType, currency, underlying));
        if (statisticsList == null) {
            return new ArrayList<>();
        }

        Integer size = statisticsList.size();

        if (maxPoints == null || size < maxPoints) {
            maxPoints = size;
        }

        int firstIndex = size - maxPoints;
        // copy because reverse will be performed on it

        return new ArrayList<>(statisticsList.subList(firstIndex, size));
    }

    @Async("taskExecutor")
    public void calculateStatistics(int reportId, StatisticsInterval interval, String tradeType, String secType, String currency, String underlying) {
        log.info("BEGIN statistics calculation for report " + reportId + ", interval=" + interval + ", tradeType=" + tradeType + ", secType=" + secType + ", currency=" + currency + ", undl=" + underlying);

        List<Trade> trades = reportDao.getTrades(reportId, normalizeParam(tradeType), normalizeParam(secType), normalizeParam(currency), normalizeParam(underlying));

        List<Statistics> stats = doCalculate(trades, interval);
        statisticsMap.put(statisticsKey(reportId, interval, tradeType, secType, currency, underlying), stats);

        log.info("END statistics calculation for report " + reportId + ", interval=" + interval);

        messageSender.sendWsMessage(WS_TOPIC_REPORT, "statistics calculated for report " + reportId);
    }

    private String statisticsKey(int reportId, StatisticsInterval interval, String tradeType, String secType, String currency, String underlying) {

        String reportIdKey = String.valueOf(reportId);
        String intervalKey = interval.name();
        String tradeTypeKey = tradeType == null ? "ALL" : tradeType;
        String secTypeKey = secType == null ? "ALL" : secType;
        String currencyKey = currency == null ? "ALL" : currency;
        String underlyingKey = underlying == null ? "ALL" : underlying;

        return reportIdKey + "_" + intervalKey + "_" + tradeTypeKey + "_" + secTypeKey + "_" + currencyKey + "_" + underlyingKey;
    }

    private String normalizeParam(String param) {
        return "ALL".equals(param) ? null : param;
    }

    private List<Statistics> doCalculate(List<Trade> trades, StatisticsInterval interval) {
        List<Statistics> stats = new ArrayList<>();

        if (trades == null || trades.isEmpty()) {
            return stats;
        }

        Calendar firstDate = getFirstDate(trades);
        Calendar lastDate = getLastDate(trades);

        Calendar firstPeriodDate = CoreUtil.toBeginOfPeriod(firstDate, interval);
        Calendar lastPeriodDate = CoreUtil.toBeginOfPeriod(lastDate, interval);
        Calendar periodDate = CoreUtil.calNow();
        periodDate.setTimeInMillis(firstPeriodDate.getTimeInMillis());

        double cumulProfitLoss = 0.0;
        int statsCount = 1;

        while (periodDate.getTimeInMillis() <= lastPeriodDate.getTimeInMillis()) {

            Calendar periodDateCopy = CoreUtil.calNow();
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

            Statistics s = new Statistics(
                    statsCount++,
                    periodDateCopy,
                    getNumTradesOpenedForPeriod(trades, periodDate, interval),
                    tradesClosedForPeriod.size(),
                    numWinners,
                    numLosers,
                    CoreUtil.round2(bigWinner),
                    CoreUtil.round2(bigLoser),
                    CoreUtil.round2(winnersProfit),
                    CoreUtil.round2(losersLoss),
                    CoreUtil.round2(profitLoss),
                    CoreUtil.round2(cumulProfitLoss)
            );
            stats.add(s);

            if (StatisticsInterval.DAY.equals(interval)) {
                periodDate.add(Calendar.DAY_OF_MONTH, 1);

            } else if (StatisticsInterval.MONTH.equals(interval)) {
                periodDate.add(Calendar.MONTH, 1);

            } else if (StatisticsInterval.YEAR.equals(interval)) {
                periodDate.add(Calendar.YEAR, 1);
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
