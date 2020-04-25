package com.highpowerbear.hpbanalytics.service;

import com.highpowerbear.hpbanalytics.common.HanUtil;
import com.highpowerbear.hpbanalytics.config.HanSettings;
import com.highpowerbear.hpbanalytics.database.*;
import com.highpowerbear.hpbanalytics.enums.Action;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
@Service
public class IfiCsvGeneratorService {
    private static final Logger log = LoggerFactory.getLogger(IfiCsvGeneratorService.class);

    private final ExchangeRateRepository exchangeRateRepository;
    private final TradeRepository tradeRepository;
    private final TradeCalculatorService tradeCalculatorService;

    private final String NL = "\n";
    private final String DL = ",";
    private final String acquireType = "A - nakup";

    private final Map<SecType, String> secTypeMap = new HashMap<>();
    private final Map<TradeType, String> tradeTypeMap = new HashMap<>();

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private final NumberFormat nf = NumberFormat.getInstance(Locale.US);

    private final List<Integer> ifiYears;

    @Autowired
    public IfiCsvGeneratorService(ExchangeRateRepository exchangeRateRepository,
                                  TradeRepository tradeRepository,
                                  TradeCalculatorService tradeCalculatorService) {

        this.exchangeRateRepository = exchangeRateRepository;
        this.tradeRepository = tradeRepository;
        this.tradeCalculatorService = tradeCalculatorService;

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
        List<Trade> trades = tradeRepository.getByReportIdAndTypeAndCloseDateBetweenOrderByOpenDateAsc(reportId, tradeType, beginDate, endDate);

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

    private void writeCsvHeaderShort(StringBuilder stringBuilder) {
        stringBuilder
                .append("Zap. št.").append(DL)
                .append("Vrsta IFI").append(DL)
                .append("Vrsta posla").append(DL)
                .append("Trgovalna koda").append(DL)
                .append("Datum odsvojitve").append(DL)
                .append("Količina odsvojenega IFI").append(DL)
                .append("Vrednost ob odsvojitvi (na enoto) USD").append(DL)
                .append("Vrednost ob odsvojitvi (na enoto) EUR").append(DL)
                .append("Datum pridobitve").append(DL)
                .append("Način pridobitve").append(DL)
                .append("Količina").append(DL)
                .append("Vrednost ob pridobitvi na enoto) USD").append(DL)
                .append("Vrednost ob pridobitvi (na enoto) EUR").append(DL)
                .append("Zaloga IFI").append(DL)
                .append("Dobiček Izguba EUR").append(NL);
    }

    private void writeCsvHeaderLong(StringBuilder stringBuilder) {
        stringBuilder
                .append("Zap. št.").append(DL)
                .append("Vrsta IFI").append(DL)
                .append("Vrsta posla").append(DL)
                .append("Trgovalna koda").append(DL)
                .append("Datum pridobitve").append(DL)
                .append("Način pridobitve").append(DL)
                .append("Količina").append(DL)
                .append("Nabavna vrednost ob pridobitvi (na enoto) USD").append(DL)
                .append("Nabavna vrednost ob pridobitvi (na enoto) EUR").append(DL)
                .append("Datum odsvojitve").append(DL)
                .append("Količina odsvojenega IFI").append(DL)
                .append("Vrednost ob odsvojitvi (na enoto) USD").append(DL)
                .append("Vrednost ob odsvojitvi (na enoto) EUR").append(DL)
                .append("Zaloga IFI").append(DL)
                .append("Dobiček Izguba EUR").append(NL);
    }

    private void writeTrade(StringBuilder stringBuilder, Trade trade, int i) {
        stringBuilder
                .append(i).append(DL)
                .append(secTypeMap.get(trade.getSecType())).append(DL)
                .append(tradeTypeMap.get(trade.getType())).append(DL)
                .append(trade.getSymbol()).append(DL);

        for (int k = 0; k < 10; k++) {
            stringBuilder.append(DL);
        }
        stringBuilder.append(NL);
    }

    private void writeTradeShortSplitExecutionSell(StringBuilder stringBuilder, SplitExecution se, int i, int j) {
        stringBuilder
                .append(i).append("_").append(j).append(DL).append(DL).append(DL).append(DL)
                .append(se.getFillDate().format(dtf)).append(DL)
                .append(se.getSplitQuantity()).append(DL)
                .append(se.getExecution().getCurrency() == Currency.USD ? nf.format(fillValue(se)) : "").append(DL)
                .append(nf.format(fillValueBase(se))).append(DL);

        for (int k = 0; k < 5; k++) {
            stringBuilder.append(DL);
        }
        stringBuilder.append(NL);
    }

    private BigDecimal writeTradeShortSplitExecutionBuy(StringBuilder stringBuilder, SplitExecution se, int i, int j) {
        stringBuilder.append(i).append("_").append(j);
        for (int k = 0; k < 7; k++) {
            stringBuilder.append(DL);
        }

        stringBuilder
                .append(DL)
                .append(se.getFillDate().format(dtf)).append(DL)
                .append(acquireType).append(DL)
                .append(se.getSplitQuantity()).append(DL)
                .append(se.getExecution().getCurrency() == Currency.USD ? nf.format(fillValue(se)) : "").append(DL)
                .append(nf.format(fillValueBase(se))).append(DL)
                .append(se.getCurrentPosition()).append(DL);

        BigDecimal profitLoss = null;
        if (se.getCurrentPosition().equals(0)) {
            profitLoss = tradeCalculatorService.calculatePlPortfolioBaseOpenClose(se.getTrade());
            stringBuilder.append(nf.format(profitLoss.doubleValue()));
        }

        stringBuilder.append(NL);
        return profitLoss;
    }

    private void writeTradeLongSplitExecutionBuy(StringBuilder stringBuilder, SplitExecution se, int i, int j) {
        stringBuilder
                .append(i).append("_").append(j).append(DL).append(DL).append(DL).append(DL)
                .append(se.getFillDate().format(dtf)).append(DL)
                .append(acquireType).append(DL)
                .append(se.getSplitQuantity()).append(DL)
                .append(se.getExecution().getCurrency() == Currency.USD ? nf.format(fillValue(se)) : "").append(DL)
                .append(nf.format(fillValueBase(se))).append(DL);

        for (int k = 0; k < 4; k++) {
            stringBuilder.append(DL);
        }
        stringBuilder.append(NL);
    }

    private BigDecimal writeTradeLongSplitExecutionSell(StringBuilder stringBuilder, SplitExecution se, int i, int j) {
        stringBuilder.append(i).append("_").append(j);
        for (int k = 0; k < 8; k++) {
            stringBuilder.append(DL);
        }
        stringBuilder
                .append(DL)
                .append(se.getFillDate().format(dtf)).append(DL)
                .append(se.getSplitQuantity()).append(DL)
                .append(se.getExecution().getCurrency() == Currency.USD ? nf.format(fillValue(se)) : "").append(DL)
                .append(nf.format(fillValueBase(se))).append(DL)
                .append(se.getCurrentPosition()).append(DL);

        BigDecimal profitLoss = null;
        if (se.getCurrentPosition().equals(0)) {
            profitLoss = tradeCalculatorService.calculatePlPortfolioBaseOpenClose(se.getTrade());
            stringBuilder.append(nf.format(profitLoss.doubleValue()));
        }
        stringBuilder.append(NL);

        return profitLoss;
    }

    private double getExchangeRate(SplitExecution se) {
        String date = HanUtil.formatExchangeRateDate(se.getFillDate().toLocalDate());
        ExchangeRate exchangeRate = exchangeRateRepository.findById(date).orElse(null);

        if (exchangeRate == null) {
            String previousDate = HanUtil.formatExchangeRateDate(se.getFillDate().plusDays(-1).toLocalDate());
            exchangeRate = exchangeRateRepository.findById(previousDate).orElse(null);

            if (exchangeRate == null) {
                throw new IllegalStateException("exchange rate not available for " + date + " or " + previousDate);
            }
        }
        Currency currency = se.getExecution().getCurrency();

        return exchangeRate.getRate(HanSettings.PORTFOLIO_BASE, currency);
    }

    private double fillValue(SplitExecution se) {
        BigDecimal contractFillPrice = se.getExecution().getFillPrice();
        BigDecimal multiplier = BigDecimal.valueOf(tradeCalculatorService.getMultiplier(se.getTrade()));

        return contractFillPrice.multiply(multiplier).doubleValue();
    }

    private double fillValueBase(SplitExecution se) {
        BigDecimal exchangeRate = BigDecimal.valueOf(getExchangeRate(se));
        BigDecimal contractFillPrice = se.getExecution().getFillPrice();
        BigDecimal multiplier = BigDecimal.valueOf(tradeCalculatorService.getMultiplier(se.getTrade()));

        return contractFillPrice.divide(exchangeRate, HanSettings.PL_SCALE, RoundingMode.HALF_UP).multiply(multiplier).doubleValue();
    }

    public List<Integer> getIfiYears() {
        return ifiYears;
    }
}
