package com.highpowerbear.hpbanalytics.report;

import com.highpowerbear.hpbanalytics.common.CoreUtil;
import com.highpowerbear.hpbanalytics.common.MessageSender;
import com.highpowerbear.hpbanalytics.dao.ReportDao;
import com.highpowerbear.hpbanalytics.entity.Execution;
import com.highpowerbear.hpbanalytics.entity.Report;
import com.highpowerbear.hpbanalytics.entity.SplitExecution;
import com.highpowerbear.hpbanalytics.entity.Trade;
import com.highpowerbear.hpbanalytics.enums.StatisticsInterval;
import com.highpowerbear.hpbanalytics.report.model.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

        LocalDateTime firstDate = getFirstDate(trades);
        LocalDateTime lastDate = getLastDate(trades);

        LocalDateTime firstPeriodDate = toBeginOfPeriod(firstDate, interval);
        LocalDateTime lastPeriodDate = toBeginOfPeriod(lastDate, interval);
        LocalDateTime periodDate = firstPeriodDate;

        double cumulProfitLoss = 0.0;
        int statsCount = 1;

        while (!periodDate.isAfter(lastPeriodDate)) {
            List<Trade> tradesOpenedForPeriod = getTradesOpenedForPeriod(trades, periodDate, interval);
            List<Trade> tradesClosedForPeriod = getTradesClosedForPeriod(trades, periodDate, interval);

            int numExecs = getNumberExecutionsForPeriod(trades, periodDate, interval);
            int numOpened = tradesOpenedForPeriod.size();
            int numClosed = tradesClosedForPeriod.size();
            int numWinners = 0;
            int numLosers = 0;
            double pctWinners;
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
            pctWinners = numClosed != 0 ? ((double) numWinners / (double) numClosed) * 100.0 : 0.0;
            profitLoss = winnersProfit + losersLoss;
            cumulProfitLoss += profitLoss;

            Statistics s = new Statistics(
                    statsCount++,
                    periodDate,
                    numExecs,
                    numOpened,
                    numClosed,
                    numWinners,
                    numLosers,
                    CoreUtil.round2(pctWinners),
                    CoreUtil.round2(bigWinner),
                    CoreUtil.round2(bigLoser),
                    CoreUtil.round2(winnersProfit),
                    CoreUtil.round2(losersLoss),
                    CoreUtil.round2(profitLoss),
                    CoreUtil.round2(cumulProfitLoss)
            );
            stats.add(s);

            if (StatisticsInterval.DAY.equals(interval)) {
                periodDate = periodDate.plusDays(1);

            } else if (StatisticsInterval.MONTH.equals(interval)) {
                periodDate = periodDate.plusMonths(1);

            } else if (StatisticsInterval.YEAR.equals(interval)) {
                periodDate = periodDate.plusYears(1);
            }
        }
        return stats;
    }

    private LocalDateTime getFirstDate(List<Trade> trades) {
        LocalDateTime firstDateOpened = trades.get(0).getOpenDate();
        for (Trade t: trades) {
            if (t.getOpenDate().isBefore(firstDateOpened)) {
                firstDateOpened = t.getOpenDate();
            }
        }
        return firstDateOpened;
    }

    private LocalDateTime getLastDate(List<Trade> trades) {
        LocalDateTime lastDate;
        LocalDateTime lastDateOpened = trades.get(0).getOpenDate();
        LocalDateTime lastDateClosed = trades.get(0).getCloseDate();

        for (Trade t: trades) {
            if (t.getOpenDate().isAfter(lastDateOpened)) {
                lastDateOpened = t.getOpenDate();
            }
        }
        for (Trade t: trades) {
            if (t.getCloseDate() != null && (lastDateClosed == null || t.getCloseDate().isAfter(lastDateClosed))) {
                lastDateClosed = t.getCloseDate();
            }
        }
        lastDate = (lastDateClosed == null || lastDateOpened.isAfter(lastDateClosed) ? lastDateOpened : lastDateClosed);
        return lastDate;
    }

    private List<Trade> getTradesOpenedForPeriod(List<Trade> trades, LocalDateTime periodDate, StatisticsInterval interval) {
        return trades.stream()
                .filter(t -> toBeginOfPeriod(t.getOpenDate(), interval).isEqual(periodDate))
                .collect(Collectors.toList());
    }

    private List<Trade> getTradesClosedForPeriod(List<Trade> trades, LocalDateTime periodDate, StatisticsInterval interval) {
        return trades.stream()
                .filter(t -> t.getCloseDate() != null)
                .filter(t -> toBeginOfPeriod(t.getCloseDate(), interval).isEqual(periodDate))
                .collect(Collectors.toList());
    }

    private int getNumberExecutionsForPeriod(List<Trade> trades, LocalDateTime periodDate, StatisticsInterval interval) {
        return (int) trades.stream()
                .flatMap(t -> t.getSplitExecutions().stream())
                .map(SplitExecution::getExecution)
                .filter(e -> toBeginOfPeriod(e.getFillDate(), interval).isEqual(periodDate))
                .map(Execution::getId)
                .distinct()
                .count();
    }

    private LocalDateTime toBeginOfPeriod(LocalDateTime localDateTime, StatisticsInterval interval) {
        LocalDate localDate = localDateTime.toLocalDate();

        if (StatisticsInterval.YEAR.equals(interval)) {
            localDate = localDate.withDayOfYear(1);

        } else if (StatisticsInterval.MONTH.equals(interval)) {
            localDate = localDate.withDayOfMonth(1);
        }

        return localDate.atStartOfDay();
    }
}
