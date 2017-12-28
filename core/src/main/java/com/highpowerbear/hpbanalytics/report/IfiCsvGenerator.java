package com.highpowerbear.hpbanalytics.report;

import com.highpowerbear.hpbanalytics.common.CoreSettings;
import com.highpowerbear.hpbanalytics.common.CoreUtil;
import com.highpowerbear.hpbanalytics.dao.ReportDao;
import com.highpowerbear.hpbanalytics.entity.ExchangeRate;
import com.highpowerbear.hpbanalytics.entity.Report;
import com.highpowerbear.hpbanalytics.entity.SplitExecution;
import com.highpowerbear.hpbanalytics.entity.Trade;
import com.highpowerbear.hpbanalytics.enums.Action;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.enums.StatisticsInterval;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by robertk on 10/10/2016.
 */
@Service
public class IfiCsvGenerator {
    private static final Logger log = LoggerFactory.getLogger(IfiCsvGenerator.class);

    @Autowired private ReportDao reportDao;
    @Autowired private TradeCalculator tradeCalculator;

    private final String NL = "\n";
    private final String DL = ",";
    private final String acquireType = "A - nakup";

    private final Map<SecType, String> secTypeMap = new HashMap<>();
    private final Map<TradeType, String> tradeTypeMap = new HashMap<>();

    private final DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
    private final DateFormat dfLog = new SimpleDateFormat("MM/dd/yyyy");
    private final NumberFormat nf = NumberFormat.getInstance(Locale.US);

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

    public String generate(Report report, Integer year, TradeType tradeType) {
        log.info("BEGIN IfiCsvGenerator.generate, report=" + report.getId() + ", year=" + year + ", tradeType=" + tradeType);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        Calendar beginDate = CoreUtil.toBeginOfPeriod(cal, StatisticsInterval.YEAR);
        cal.set(Calendar.YEAR, year + 1);
        Calendar endDate = CoreUtil.toBeginOfPeriod(cal, StatisticsInterval.YEAR);
        List<Trade> trades = reportDao.getTradesBetweenDates(report, beginDate, endDate, tradeType);

        log.info("Begin date=" + dfLog.format(beginDate.getTime()) + ", endDate=" + dfLog.format(endDate.getTime()) + ", trades=" + trades.size());
        StringBuilder sb = new StringBuilder();
        if (TradeType.SHORT.equals(tradeType)) {
            writeCsvHeaderShort(sb);
        } else if (TradeType.LONG.equals(tradeType)) {
            writeCsvHeaderLong(sb);
        }
        int i = 0;
        Double sumPlEur = 0D;
        for (Trade trade : trades) {
            Double tradePlEur = 0D;
            if (!(SecType.FUT.equals(trade.getSecType()) || SecType.OPT.equals(trade.getSecType()) || SecType.CFD.equals(trade.getSecType()))) {
                continue;
            }

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
            sb.append(DL).append("");
        }
        sb.append(nf.format(sumPlEur));

        log.info("END IfiCsvGenerator.generate, report=" + report.getId() + ", year=" + year + ", tradeType=" + tradeType);
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
        sb.append(df.format(se.getFillDate().getTime())).append(DL);
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
            sb.append(DL).append("");
        }

        sb.append(DL);
        sb.append(df.format(se.getFillDate().getTime())).append(DL);
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
        sb.append(df.format(se.getFillDate().getTime())).append(DL);
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
            sb.append(DL).append("");
        }
        sb.append(DL);
        sb.append(df.format(se.getFillDate().getTime())).append(DL);
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
            String previousDate = CoreUtil.formatExchangeRateDate(CoreUtil.previousDay(se.getFillDate()));
            exchangeRate = reportDao.getExchangeRate(previousDate);
        }
        Currency currency = se.getExecution().getCurrency();

        return exchangeRate.getRate(CoreSettings.PORTFOLIO_BASE, currency);
    }
}
