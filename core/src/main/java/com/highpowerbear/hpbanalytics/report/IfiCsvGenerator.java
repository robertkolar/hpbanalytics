package com.highpowerbear.hpbanalytics.report;

import com.highpowerbear.hpbanalytics.config.HanSettings;
import com.highpowerbear.hpbanalytics.common.HanUtil;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

        ifiYears = IntStream.rangeClosed(HanSettings.IFI_START_YEAR, LocalDate.now().getYear()).boxed().collect(Collectors.toList());
    }

    @PostConstruct
    private void init() {
        secTypeMap.put(SecType.FUT, "01 - terminska pogodba");
        secTypeMap.put(SecType.CFD, "02 - pogodba na razliko");
        secTypeMap.put(SecType.OPT, "03 - opcija");

        tradeTypeMap.put(TradeType.LONG, "običajni");
        tradeTypeMap.put(TradeType.SHORT, "na kratko");

        nf.setMinimumFractionDigits(HanSettings.PL_SCALE);
        nf.setMaximumFractionDigits(HanSettings.PL_SCALE);
        nf.setGroupingUsed(false);
    }

    public String generate(int reportId, int year, int endMonth, TradeType tradeType) {
        log.info("BEGIN IfiCsvGenerator.generate, report=" + reportId + ", year=" + year +  ", endMonth=" + endMonth + ", tradeType=" + tradeType);

        LocalDateTime beginDate = LocalDate.ofYearDay(year, 1).atStartOfDay();
        LocalDateTime endDate = YearMonth.of(year, endMonth).atEndOfMonth().plusDays(1).atStartOfDay();
        List<Trade> trades = reportDao.getTradesBetweenDates(reportId, beginDate, endDate, tradeType);

        log.info("beginDate=" + beginDate + ", endDate=" + endDate + ", trades=" + trades.size());
        StringBuilder sb = new StringBuilder();

        if (TradeType.SHORT.equals(tradeType)) {
            writeCsvHeaderShort(sb);
        } else if (TradeType.LONG.equals(tradeType)) {
            writeCsvHeaderLong(sb);
        }
        int i = 0;
        BigDecimal sumPl = BigDecimal.ZERO;

        for (Trade trade : trades) {
            if (!trade.getSecType().isDerivative()) {
                continue;
            }

            BigDecimal tradePl = BigDecimal.ZERO;
            i++;
            writeTrade(sb, trade, i);
            List<SplitExecution> splitExecutions = trade.getSplitExecutions();
            int j = 0;
            for (SplitExecution se : splitExecutions) {
                j++;
                if (TradeType.SHORT.equals(tradeType) && Action.SELL.equals(se.getExecution().getAction())) {
                    writeTradeShortSplitExecutionSell(sb, se, i, j);

                } else if (TradeType.SHORT.equals(tradeType) && Action.BUY.equals(se.getExecution().getAction())) {
                    BigDecimal pl = writeTradeShortSplitExecutionBuy(sb, se, i, j);

                    if (pl != null) {
                        tradePl = pl;
                    }
                } else if (TradeType.LONG.equals(tradeType) && Action.BUY.equals(se.getExecution().getAction())) {
                    writeTradeLongSplitExecutionBuy(sb, se, i, j);

                } else if (TradeType.LONG.equals(tradeType) && Action.SELL.equals(se.getExecution().getAction())) {
                    BigDecimal pl = writeTradeLongSplitExecutionSell(sb, se, i, j);
                    if (pl != null) {
                        tradePl = pl;
                    }
                }
            }
            sumPl = sumPl.add(tradePl);
            sb.append(NL);
        }

        sb.append(NL).append("SKUPAJ");
        for (int k = 0; k < 14; k++) {
            sb.append(DL);
        }
        sb.append(nf.format(sumPl));

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
        sb.append(i).append("_").append(j).append(DL).append(DL).append(DL).append(DL);
        sb.append(se.getFillDate().format(dtf)).append(DL);
        sb.append(se.getSplitQuantity()).append(DL);

        sb.append(se.getExecution().getCurrency() == Currency.USD ? nf.format(fillValue(se)) : "").append(DL);
        sb.append(nf.format(fillValueBase(se))).append(DL);

        for (int k = 0; k < 5; k++) {
            sb.append(DL);
        }
        sb.append(NL);
    }

    private BigDecimal writeTradeShortSplitExecutionBuy(StringBuilder sb, SplitExecution se, int i, int j) {
        sb.append(i).append("_").append(j);
        for (int k = 0; k < 7; k++) {
            sb.append(DL);
        }

        sb.append(DL);
        sb.append(se.getFillDate().format(dtf)).append(DL);
        sb.append(acquireType).append(DL);
        sb.append(se.getSplitQuantity()).append(DL);

        sb.append(se.getExecution().getCurrency() == Currency.USD ? nf.format(fillValue(se)) : "").append(DL);
        sb.append(nf.format(fillValueBase(se))).append(DL);
        sb.append(se.getCurrentPosition()).append(DL);

        BigDecimal profitLoss = null;
        if (se.getCurrentPosition().equals(0)) {
            profitLoss = tradeCalculator.calculatePLPortfolioBaseOpenClose(se.getTrade());
            sb.append(nf.format(profitLoss.doubleValue()));
        }

        sb.append(NL);
        return profitLoss;
    }

    private void writeTradeLongSplitExecutionBuy(StringBuilder sb, SplitExecution se, int i, int j) {
        sb.append(i).append("_").append(j).append(DL).append(DL).append(DL).append(DL);
        sb.append(se.getFillDate().format(dtf)).append(DL);
        sb.append(acquireType).append(DL);
        sb.append(se.getSplitQuantity()).append(DL);

        sb.append(se.getExecution().getCurrency() == Currency.USD ? nf.format(fillValue(se)) : "").append(DL);
        sb.append(nf.format(fillValueBase(se))).append(DL);

        for (int k = 0; k < 4; k++) {
            sb.append(DL);
        }
        sb.append(NL);
    }

    private BigDecimal writeTradeLongSplitExecutionSell(StringBuilder sb, SplitExecution se, int i, int j) {
        sb.append(i).append("_").append(j);
        for (int k = 0; k < 8; k++) {
            sb.append(DL);
        }
        sb.append(DL);
        sb.append(se.getFillDate().format(dtf)).append(DL);
        sb.append(se.getSplitQuantity()).append(DL);

        sb.append(se.getExecution().getCurrency() == Currency.USD ? nf.format(fillValue(se)) : "").append(DL);
        sb.append(nf.format(fillValueBase(se))).append(DL);
        sb.append(se.getCurrentPosition()).append(DL);

        BigDecimal profitLoss = null;
        if (se.getCurrentPosition().equals(0)) {
            profitLoss = tradeCalculator.calculatePLPortfolioBaseOpenClose(se.getTrade());
            sb.append(nf.format(profitLoss.doubleValue()));
        }
        sb.append(NL);

        return profitLoss;
    }

    private double getExchangeRate(SplitExecution se) {
        String date = HanUtil.formatExchangeRateDate(se.getFillDate().toLocalDate());
        ExchangeRate exchangeRate = reportDao.getExchangeRate(date);

        if (exchangeRate == null) {
            String previousDate = HanUtil.formatExchangeRateDate(se.getFillDate().plusDays(-1).toLocalDate());
            exchangeRate = reportDao.getExchangeRate(previousDate);
        }
        Currency currency = se.getExecution().getCurrency();

        return exchangeRate.getRate(HanSettings.PORTFOLIO_BASE, currency);
    }

    private double fillValue(SplitExecution se) {
        BigDecimal contractFillPrice = se.getExecution().getFillPrice();
        BigDecimal multiplier = BigDecimal.valueOf(tradeCalculator.getMultiplier(se.getTrade()));

        return contractFillPrice.multiply(multiplier).doubleValue();
    }

    private double fillValueBase(SplitExecution se) {
        BigDecimal exchangeRate = BigDecimal.valueOf(getExchangeRate(se));
        BigDecimal contractFillPrice = se.getExecution().getFillPrice();
        BigDecimal multiplier = BigDecimal.valueOf(tradeCalculator.getMultiplier(se.getTrade()));

        return contractFillPrice.divide(exchangeRate, HanSettings.PL_SCALE, RoundingMode.HALF_UP).multiply(multiplier).doubleValue();
    }

    public List<Integer> getIfiYears() {
        return ifiYears;
    }
}
