package com.highpowerbear.hpbanalytics.report;

import com.highpowerbear.hpbanalytics.common.CoreSettings;
import com.highpowerbear.hpbanalytics.common.CoreUtil;
import com.highpowerbear.hpbanalytics.dao.ReportDao;
import com.highpowerbear.hpbanalytics.entity.ExchangeRate;
import com.highpowerbear.hpbanalytics.entity.SplitExecution;
import com.highpowerbear.hpbanalytics.entity.Trade;
import com.highpowerbear.hpbanalytics.enums.Action;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by robertk on 10/10/2016.
 */
@Component
public class IfiCsvGenerator {
    private static final Logger log = LoggerFactory.getLogger(IfiCsvGenerator.class);

    private final ReportDao reportDao;
    private final TradeCalculator tradeCalculator;

    private final String NL = "\n";
    private final String DL = ",";
    private final String acquireType = "A - nakup";

    private final Map<SecType, String> secTypeMap = new HashMap<>();
    private final Map<TradeType, String> tradeTypeMap = new HashMap<>();

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private final NumberFormat nf = NumberFormat.getInstance(Locale.US);

    private final List<Integer> ifiYears;

    @Autowired
    public IfiCsvGenerator(ReportDao reportDao, TradeCalculator tradeCalculator) {
        this.reportDao = reportDao;
        this.tradeCalculator = tradeCalculator;

        ifiYears = IntStream.rangeClosed(CoreSettings.IFI_START_YEAR, LocalDate.now().getYear()).boxed().collect(Collectors.toList());
    }

    @PostConstruct
    private void init() {
        secTypeMap.put(SecType.FUT, "01 - terminska pogodba");
        secTypeMap.put(SecType.CFD, "02 - pogodba na razliko");
        secTypeMap.put(SecType.OPT, "03 - opcija");

        tradeTypeMap.put(TradeType.LONG, "običajni");
        tradeTypeMap.put(TradeType.SHORT, "na kratko");

        nf.setMinimumFractionDigits(4);
        nf.setMaximumFractionDigits(4);
        nf.setGroupingUsed(false);
    }

    public String generate(int reportId, Integer year, TradeType tradeType) {
        log.info("BEGIN IfiCsvGenerator.generate, report=" + reportId + ", year=" + year + ", tradeType=" + tradeType);

        LocalDateTime beginDate = LocalDate.ofYearDay(year, 1).atStartOfDay();
        LocalDateTime endDate = beginDate.plusYears(1);
        List<Trade> trades = reportDao.getTradesBetweenDates(reportId, beginDate, endDate, tradeType);

        log.info("beginDate=" + beginDate + ", endDate=" + endDate + ", trades=" + trades.size());
        StringBuilder sb = new StringBuilder();

        if (TradeType.SHORT.equals(tradeType)) {
            writeCsvHeaderShort(sb);
        } else if (TradeType.LONG.equals(tradeType)) {
            writeCsvHeaderLong(sb);
        }
        int i = 0;
        Double sumPlEur = 0D;

        for (Trade trade : trades) {
            if (!trade.getSecType().isDerivative()) {
                continue;
            }

            Double tradePlEur = 0D;
            i++;
            writeTrade(sb, trade, i);
            List<SplitExecution> splitExecutions = trade.getSplitExecutions();
            int j = 0;
            for (SplitExecution se : splitExecutions) {
                j++;
                if (TradeType.SHORT.equals(tradeType) && Action.SELL.equals(se.getExecution().getAction())) {
                    writeTradeShortSplitExecutionSell(sb, se, i, j);

                } else if (TradeType.SHORT.equals(tradeType) && Action.BUY.equals(se.getExecution().getAction())) {
                    Double plEur = writeTradeShortSplitExecutionBuy(sb, se, i, j);

                    if (plEur != null) {
                        tradePlEur = plEur;
                    }
                } else if (TradeType.LONG.equals(tradeType) && Action.BUY.equals(se.getExecution().getAction())) {
                    writeTradeLongSplitExecutionBuy(sb, se, i, j);

                } else if (TradeType.LONG.equals(tradeType) && Action.SELL.equals(se.getExecution().getAction())) {
                    Double plEur = writeTradeLongSplitExecutionSell(sb, se, i, j);
                    if (plEur != null) {
                        tradePlEur = plEur;
                    }
                }
            }
            sumPlEur += tradePlEur;
            sb.append(NL);
        }

        sb.append(NL).append("SKUPAJ");
        for (int k = 0; k < 14; k++) {
            sb.append(DL);
        }
        sb.append(nf.format(sumPlEur));

        log.info("END IfiCsvGenerator.generate, report=" + reportId + ", year=" + year + ", tradeType=" + tradeType);
        return sb.toString();
    }

    private void writeCsvHeaderShort(StringBuilder sb) {
        sb.append("Zap. št.").append(DL);
        sb.append("Vrsta IFI").append(DL);
        sb.append("Vrsta posla").append(DL);
        sb.append("Trgovalna koda").append(DL);
        sb.append("Datum odsvojitve").append(DL);
        sb.append("Količina odsvojenega IFI").append(DL);
        sb.append("Vrednost ob odsvojitvi (na enoto) USD").append(DL);
        sb.append("Vrednost ob odsvojitvi (na enoto) EUR").append(DL);
        sb.append("Datum pridobitve").append(DL);
        sb.append("Način pridobitve").append(DL);
        sb.append("Količina").append(DL);
        sb.append("Vrednost ob pridobitvi na enoto) USD").append(DL);
        sb.append("Vrednost ob pridobitvi (na enoto) EUR").append(DL);
        sb.append("Zaloga IFI").append(DL);
        sb.append("Dobiček Izguba EUR").append(NL);
    }

    private void writeCsvHeaderLong(StringBuilder sb) {
        sb.append("Zap. št.").append(DL);
        sb.append("Vrsta IFI").append(DL);
        sb.append("Vrsta posla").append(DL);
        sb.append("Trgovalna koda").append(DL);
        sb.append("Datum pridobitve").append(DL);
        sb.append("Način pridobitve").append(DL);
        sb.append("Količina").append(DL);
        sb.append("Nabavna vrednost ob pridobitvi (na enoto) USD").append(DL);
        sb.append("Nabavna vrednost ob pridobitvi (na enoto) EUR").append(DL);
        sb.append("Datum odsvojitve").append(DL);
        sb.append("Količina odsvojenega IFI").append(DL);
        sb.append("Vrednost ob odsvojitvi (na enoto) USD").append(DL);
        sb.append("Vrednost ob odsvojitvi (na enoto) EUR").append(DL);
        sb.append("Zaloga IFI").append(DL);
        sb.append("Dobiček Izguba EUR").append(NL);
    }

    private void writeTrade(StringBuilder sb, Trade trade, int i) {
        sb.append(i).append(DL);
        sb.append(secTypeMap.get(trade.getSecType())).append(DL);
        sb.append(tradeTypeMap.get(trade.getType())).append(DL);
        sb.append(trade.getSymbol()).append(DL);

        for (int k = 0; k < 10; k++) {
            sb.append(DL);
        }
        sb.append(NL);
    }

    private void writeTradeShortSplitExecutionSell(StringBuilder sb, SplitExecution se, int i, int j) {
        double exchangeRate = getExchangeRate(se);

        sb.append(i).append("_").append(j).append(DL).append(DL).append(DL).append(DL);
        sb.append(se.getFillDate().format(dtf)).append(DL);
        sb.append(se.getSplitQuantity()).append(DL);

        double fillPrice = se.getExecution().getFillPrice().doubleValue();
        fillPrice *= tradeCalculator.getMultiplier(se.getTrade());

        Currency currency = se.getExecution().getCurrency();

        sb.append(currency == Currency.USD ? nf.format(fillPrice) : "").append(DL);
        sb.append(nf.format(fillPrice / exchangeRate)).append(DL);

        for (int k = 0; k < 5; k++) {
            sb.append(DL);
        }
        sb.append(NL);
    }

    private Double writeTradeShortSplitExecutionBuy(StringBuilder sb, SplitExecution se, int i, int j) {
        double exchangeRate = getExchangeRate(se);

        sb.append(i).append("_").append(j);
        for (int k = 0; k < 7; k++) {
            sb.append(DL);
        }

        sb.append(DL);
        sb.append(se.getFillDate().format(dtf)).append(DL);
        sb.append(acquireType).append(DL);
        sb.append(se.getSplitQuantity()).append(DL);

        double fillPrice = se.getExecution().getFillPrice().doubleValue();
        fillPrice *= tradeCalculator.getMultiplier(se.getTrade());

        Currency currency = se.getExecution().getCurrency();

        sb.append(currency == Currency.USD ? nf.format(fillPrice) : "").append(DL);
        sb.append(nf.format(fillPrice / exchangeRate)).append(DL);
        sb.append(se.getCurrentPosition()).append(DL);

        Double profitLoss = null;
        if (se.getCurrentPosition().equals(0)) {
            profitLoss = tradeCalculator.calculatePLPortfolioBaseOpenClose(se.getTrade());
            sb.append(nf.format(profitLoss));
        }

        sb.append(NL);
        return profitLoss;
    }

    private void writeTradeLongSplitExecutionBuy(StringBuilder sb, SplitExecution se, int i, int j) {
        double exchangeRate = getExchangeRate(se);

        sb.append(i).append("_").append(j).append(DL).append(DL).append(DL).append(DL);
        sb.append(se.getFillDate().format(dtf)).append(DL);
        sb.append(acquireType).append(DL);
        sb.append(se.getSplitQuantity()).append(DL);

        double fillPrice = se.getExecution().getFillPrice().doubleValue();
        fillPrice *= tradeCalculator.getMultiplier(se.getTrade());

        Currency currency = se.getExecution().getCurrency();

        sb.append(currency == Currency.USD ? nf.format(fillPrice) : "").append(DL);
        sb.append(nf.format(fillPrice / exchangeRate)).append(DL);

        for (int k = 0; k < 4; k++) {
            sb.append(DL);
        }
        sb.append(NL);
    }

    private Double writeTradeLongSplitExecutionSell(StringBuilder sb, SplitExecution se, int i, int j) {
        double exchangeRate = getExchangeRate(se);

        sb.append(i).append("_").append(j);
        for (int k = 0; k < 8; k++) {
            sb.append(DL);
        }
        sb.append(DL);
        sb.append(se.getFillDate().format(dtf)).append(DL);
        sb.append(se.getSplitQuantity()).append(DL);

        double fillPrice = se.getExecution().getFillPrice().doubleValue();
        fillPrice *= tradeCalculator.getMultiplier(se.getTrade());

        Currency currency = se.getExecution().getCurrency();

        sb.append(currency == Currency.USD ? nf.format(fillPrice) : "").append(DL);
        sb.append(nf.format(fillPrice / exchangeRate)).append(DL);
        sb.append(se.getCurrentPosition()).append(DL);

        Double profitLoss = null;
        if (se.getCurrentPosition().equals(0)) {
            profitLoss = tradeCalculator.calculatePLPortfolioBaseOpenClose(se.getTrade());
            sb.append(nf.format(profitLoss));
        }
        sb.append(NL);

        return profitLoss;
    }

    private double getExchangeRate(SplitExecution se) {
        String date = CoreUtil.formatExchangeRateDate(se.getFillDate());
        ExchangeRate exchangeRate = reportDao.getExchangeRate(date);

        if (exchangeRate == null) {
            String previousDate = CoreUtil.formatExchangeRateDate(se.getFillDate().plusDays(-1));
            exchangeRate = reportDao.getExchangeRate(previousDate);
        }
        Currency currency = se.getExecution().getCurrency();

        return exchangeRate.getRate(CoreSettings.PORTFOLIO_BASE, currency);
    }

    public List<Integer> getIfiYears() {
        return ifiYears;
    }
}
