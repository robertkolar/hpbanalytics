package com.highpowerbear.hpbanalytics.service;

import com.highpowerbear.hpbanalytics.common.HanUtil;
import com.highpowerbear.hpbanalytics.config.WsTopic;
import com.highpowerbear.hpbanalytics.database.TradeRepository;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import com.highpowerbear.hpbanalytics.repository.ReportDao;
import com.highpowerbear.hpbanalytics.database.Execution;
import com.highpowerbear.hpbanalytics.database.SplitExecution;
import com.highpowerbear.hpbanalytics.database.Trade;
import com.highpowerbear.hpbanalytics.enums.StatisticsInterval;
import com.highpowerbear.hpbanalytics.model.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by robertk on 4/26/2015.
 */
@Service
public class StatisticsCalculatorService {
    private static final Logger log = LoggerFactory.getLogger(StatisticsCalculatorService.class);

    private final ReportDao reportDao;
    private final TradeRepository tradeRepository;
    private final MessageService messageService;
    private final TradeCalculatorService tradeCalculatorService;

    private final Map<String, List<Statistics>> statisticsMap = new HashMap<>(); // caching statistics to prevent excessive recalculation

    @Autowired
    public StatisticsCalculatorService(ReportDao reportDao,
                                       TradeRepository tradeRepository,
                                       MessageService messageService,
                                       TradeCalculatorService tradeCalculatorService) {
        this.reportDao = reportDao;
        this.tradeRepository = tradeRepository;
        this.messageService = messageService;
        this.tradeCalculatorService = tradeCalculatorService;
    }

    public List<Statistics> getStatistics(StatisticsInterval interval, int reportId, TradeType tradeType, SecType secType, Currency currency, String underlying, Integer maxPoints) {

        List<Statistics> statisticsList = statisticsMap.get(statisticsKey(interval, reportId, tradeType, secType, currency, underlying));
        if (statisticsList == null) {
            return new ArrayList<>();
        }

        int size = statisticsList.size();

        if (maxPoints == null || size < maxPoints) {
            maxPoints = size;
        }

        int firstIndex = size - maxPoints;
        // copy because reverse will be performed on it

        return new ArrayList<>(statisticsList.subList(firstIndex, size));
    }

    @Async("taskExecutor")
    public void calculateStatistics(StatisticsInterval interval, int reportId, TradeType tradeType, SecType secType, Currency currency, String underlying) {
        log.info("BEGIN statistics calculation for report " + reportId + ", interval=" + interval + ", tradeType=" + tradeType + ", secType=" + secType + ", currency=" + currency + ", undl=" + underlying);

        Example<Trade> filter = HanUtil.createTradeFilter(reportId, normalizeParam(tradeType), normalizeParam(secType), normalizeParam(currency), underlying);
        List<Trade> trades = tradeRepository.findAll(filter, Sort.by(Sort.Direction.ASC, "openDate"));

        List<Statistics> stats = doCalculate(trades, interval);
        statisticsMap.put(statisticsKey(interval, reportId, tradeType, secType, currency, underlying), stats);

        log.info("END statistics calculation for report " + reportId + ", interval=" + interval);

        messageService.sendWsMessage(WsTopic.STATISTICS, "statistics calculated for report " + reportId);
    }

    private String statisticsKey(StatisticsInterval interval, int reportId, TradeType tradeType, SecType secType, Currency currency, String underlying) {

        String reportIdKey = String.valueOf(reportId);
        String intervalKey = interval.name();
        String tradeTypeKey = tradeType == null ? "ALL" : tradeType.toString();
        String secTypeKey = secType == null ? "ALL" : secType.toString();
        String currencyKey = currency == null ? "ALL" : currency.toString();
        String underlyingKey = underlying == null ? "ALL" : underlying;

        return reportIdKey + "_" + intervalKey + "_" + tradeTypeKey + "_" + secTypeKey + "_" + currencyKey + "_" + underlyingKey;
    }

    private <T> T normalizeParam(T param) {
        return "ALL".equals(String.valueOf(param)) ? null : param;
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

        BigDecimal cumulProfitLoss = BigDecimal.ZERO;
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
            BigDecimal winnersProfit = BigDecimal.ZERO;
            BigDecimal losersLoss = BigDecimal.ZERO;
            BigDecimal bigWinner = BigDecimal.ZERO;
            BigDecimal bigLoser = BigDecimal.ZERO;
            BigDecimal profitLoss;

            for (Trade t : tradesClosedForPeriod) {
                BigDecimal pl = tradeCalculatorService.calculatePlPortfolioBase(t);

                if (pl.doubleValue() >= 0) {
                    numWinners++;
                    winnersProfit = winnersProfit.add(pl);

                    if (pl.compareTo(bigWinner) > 0) {
                        bigWinner = pl;
                    }
                } else {
                    numLosers++;
                    losersLoss = losersLoss.add(pl);

                    if (pl.compareTo(bigLoser) < 0) {
                        bigLoser = pl;
                    }
                }
            }
            pctWinners = numClosed != 0 ? ((double) numWinners / (double) numClosed) * 100.0 : 0.0;
            profitLoss = winnersProfit.add(losersLoss);
            cumulProfitLoss = cumulProfitLoss.add(profitLoss);

            Statistics s = new Statistics(
                    statsCount++,
                    periodDate,
                    numExecs,
                    numOpened,
                    numClosed,
                    numWinners,
                    numLosers,
                    HanUtil.round2(pctWinners),
                    bigWinner,
                    bigLoser,
                    winnersProfit,
                    losersLoss,
                    profitLoss,
                    cumulProfitLoss
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
