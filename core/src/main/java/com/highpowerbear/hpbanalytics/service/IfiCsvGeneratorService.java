package com.highpowerbear.hpbanalytics.service;

import com.highpowerbear.hpbanalytics.common.HanUtil;
import com.highpowerbear.hpbanalytics.config.HanSettings;
import com.highpowerbear.hpbanalytics.database.*;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import com.ib.client.Types;
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

    private final Map<Types.SecType, String> secTypeMap = new HashMap<>();
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
        secTypeMap.put(Types.SecType.FUT, "01 - terminska pogodba");
        secTypeMap.put(Types.SecType.CFD, "02 - pogodba na razliko");
        secTypeMap.put(Types.SecType.OPT, "03 - opcija");
        secTypeMap.put(Types.SecType.FOP, "03 - opcija");

        tradeTypeMap.put(TradeType.LONG, "običajni");
        tradeTypeMap.put(TradeType.SHORT, "na kratko");

        nf.setMinimumFractionDigits(HanSettings.PL_SCALE);
        nf.setMaximumFractionDigits(HanSettings.PL_SCALE);
        nf.setGroupingUsed(false);
    }

    public String generate(int year, int endMonth, TradeType tradeType) {
        log.info("BEGIN IfiCsvGenerator.generate year=" + year +  ", endMonth=" + endMonth + ", tradeType=" + tradeType);

        LocalDateTime beginDate = LocalDate.ofYearDay(year, 1).atStartOfDay();
        LocalDateTime endDate = YearMonth.of(year, endMonth).atEndOfMonth().plusDays(1).atStartOfDay();
        List<Trade> trades = tradeRepository.findByTypeAndCloseDateBetweenOrderByOpenDateAsc(tradeType, beginDate, endDate);

        log.info("beginDate=" + beginDate + ", endDate=" + endDate + ", trades=" + trades.size());
        StringBuilder sb = new StringBuilder();

        if (TradeType.SHORT.equals(tradeType)) {
            writeCsvHeaderShort(sb);
        } else if (TradeType.LONG.equals(tradeType)) {
            writeCsvHeaderLong(sb);
        }
        int tCount = 0;
        BigDecimal sumPl = BigDecimal.ZERO;

        for (Trade trade : trades) {
            if (!isDerivative(trade.getSecType())) {
                continue;
            }

            BigDecimal tradePl = BigDecimal.ZERO;
            tCount++;
            writeTrade(sb, trade, tCount);
            int eCount = 0;
            int currentPos = 0;
            for (Execution execution : trade.getExecutions()) {
                Types.Action action = execution.getAction();
                currentPos += (action == Types.Action.BUY ? execution.getQuantity() : -execution.getQuantity());
                eCount++;
                if (TradeType.SHORT.equals(tradeType) && Types.Action.SELL.equals(action)) {
                    writeTradeShortExecutionSell(sb, execution, tCount, eCount);

                } else if (TradeType.SHORT.equals(tradeType) && Types.Action.BUY.equals(action)) {
                    BigDecimal pl = writeTradeShortExecutionBuy(sb, trade, execution, currentPos, tCount, eCount);

                    if (pl != null) {
                        tradePl = pl;
                    }
                } else if (TradeType.LONG.equals(tradeType) && Types.Action.BUY.equals(action)) {
                    writeTradeLongExecutionBuy(sb, execution, tCount, eCount);

                } else if (TradeType.LONG.equals(tradeType) && Types.Action.SELL.equals(action)) {
                    BigDecimal pl = writeTradeLongExecutionSell(sb, trade, execution, currentPos, tCount, eCount);
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

        log.info("END IfiCsvGenerator.generate year=" + year + ", tradeType=" + tradeType);
        return sb.toString();
    }

    private void writeCsvHeaderShort(StringBuilder sb) {
        sb  .append("Zap. št.").append(DL)
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

    private void writeCsvHeaderLong(StringBuilder sb) {
        sb  .append("Zap. št.").append(DL)
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

    private void writeTrade(StringBuilder sb, Trade trade, int tCount) {
        sb  .append(tCount).append(DL)
            .append(secTypeMap.get(trade.getSecType())).append(DL)
            .append(tradeTypeMap.get(trade.getType())).append(DL)
            .append(trade.getSymbol()).append(DL);

        for (int k = 0; k < 10; k++) {
            sb.append(DL);
        }
        sb.append(NL);
    }

    private void writeTradeShortExecutionSell(StringBuilder sb, Execution execution, int tCount, int eCount) {
        sb  .append(tCount).append("_").append(eCount).append(DL).append(DL).append(DL).append(DL)
            .append(execution.getFillDate().format(dtf)).append(DL)
            .append(execution.getQuantity()).append(DL)
            .append(execution.getCurrency() == Currency.USD ? nf.format(fillValue(execution)) : "").append(DL)
            .append(nf.format(fillValueBase(execution))).append(DL);

        for (int k = 0; k < 5; k++) {
            sb.append(DL);
        }
        sb.append(NL);
    }

    private BigDecimal writeTradeShortExecutionBuy(StringBuilder sb, Trade trade, Execution execution, int currentPos, int tCount, int eCount) {
        sb.append(tCount).append("_").append(eCount);
        for (int k = 0; k < 7; k++) {
            sb.append(DL);
        }

        sb  .append(DL)
            .append(execution.getFillDate().format(dtf)).append(DL)
            .append(acquireType).append(DL)
            .append(execution.getQuantity()).append(DL)
            .append(execution.getCurrency() == Currency.USD ? nf.format(fillValue(execution)) : "").append(DL)
            .append(nf.format(fillValueBase(execution))).append(DL)
            .append(currentPos).append(DL);

        BigDecimal profitLoss = null;
        if (currentPos == 0) {
            profitLoss = tradeCalculatorService.calculatePlPortfolioBaseOpenClose(trade);
            sb.append(nf.format(profitLoss.doubleValue()));
        }

        sb.append(NL);
        return profitLoss;
    }

    private void writeTradeLongExecutionBuy(StringBuilder sb, Execution execution, int tCount, int eCount) {
        sb  .append(tCount).append("_").append(eCount).append(DL).append(DL).append(DL).append(DL)
            .append(execution.getFillDate().format(dtf)).append(DL)
            .append(acquireType).append(DL)
            .append(execution.getQuantity()).append(DL)
            .append(execution.getCurrency() == Currency.USD ? nf.format(fillValue(execution)) : "").append(DL)
            .append(nf.format(fillValueBase(execution))).append(DL);

        for (int k = 0; k < 4; k++) {
            sb.append(DL);
        }
        sb.append(NL);
    }

    private BigDecimal writeTradeLongExecutionSell(StringBuilder sb, Trade trade, Execution execution, int currentPos, int tCount, int eCount) {
        sb.append(tCount).append("_").append(eCount);
        for (int k = 0; k < 8; k++) {
            sb.append(DL);
        }
        sb  .append(DL)
            .append(execution.getFillDate().format(dtf)).append(DL)
            .append(execution.getQuantity()).append(DL)
            .append(execution.getCurrency() == Currency.USD ? nf.format(fillValue(execution)) : "").append(DL)
            .append(nf.format(fillValueBase(execution))).append(DL)
            .append(currentPos).append(DL);

        BigDecimal profitLoss = null;
        if (currentPos == 0) {
            profitLoss = tradeCalculatorService.calculatePlPortfolioBaseOpenClose(trade);
            sb.append(nf.format(profitLoss.doubleValue()));
        }
        sb.append(NL);

        return profitLoss;
    }

    private double getExchangeRate(Execution execution) {
        String date = HanUtil.formatExchangeRateDate(execution.getFillDate().toLocalDate());
        ExchangeRate exchangeRate = exchangeRateRepository.findById(date).orElse(null);

        if (exchangeRate == null) {
            String previousDate = HanUtil.formatExchangeRateDate(execution.getFillDate().plusDays(-1).toLocalDate());
            exchangeRate = exchangeRateRepository.findById(previousDate).orElse(null);

            if (exchangeRate == null) {
                throw new IllegalStateException("exchange rate not available for " + date + " or " + previousDate);
            }
        }
        Currency currency = execution.getCurrency();

        return exchangeRate.getRate(HanSettings.PORTFOLIO_BASE, currency);
    }

    private double fillValue(Execution execution) {
        BigDecimal contractFillPrice = execution.getFillPrice();
        BigDecimal multiplier = BigDecimal.valueOf(execution.getMultiplier());

        return contractFillPrice.multiply(multiplier).doubleValue();
    }

    private double fillValueBase(Execution execution) {
        BigDecimal exchangeRate = BigDecimal.valueOf(getExchangeRate(execution));
        BigDecimal contractFillPrice = execution.getFillPrice();
        BigDecimal multiplier = BigDecimal.valueOf(execution.getMultiplier());

        return contractFillPrice.divide(exchangeRate, HanSettings.PL_SCALE, RoundingMode.HALF_UP).multiply(multiplier).doubleValue();
    }

    private boolean isDerivative(Types.SecType secType) {
        switch(secType) {
            case FUT:
            case OPT:
            case FOP:
            case CFD:
                return true;
            default:
                return false;
        }
    }

    public List<Integer> getIfiYears() {
        return ifiYears;
    }
}
