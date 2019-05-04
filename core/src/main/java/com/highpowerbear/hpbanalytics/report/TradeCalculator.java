package com.highpowerbear.hpbanalytics.report;

import com.highpowerbear.hpbanalytics.common.HanSettings;
import com.highpowerbear.hpbanalytics.common.HanUtil;
import com.highpowerbear.hpbanalytics.dao.ReportDao;
import com.highpowerbear.hpbanalytics.entity.ExchangeRate;
import com.highpowerbear.hpbanalytics.entity.Execution;
import com.highpowerbear.hpbanalytics.entity.SplitExecution;
import com.highpowerbear.hpbanalytics.entity.Trade;
import com.highpowerbear.hpbanalytics.enums.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by robertk on 12/25/2017.
 */
@Component
public class TradeCalculator {

    private final ReportDao reportDao;

    private final Map<String, ExchangeRate> exchangeRateMap = new LinkedHashMap<>();

    @Autowired
    public TradeCalculator(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    public void calculateFields(Trade t) {
        SplitExecution seFirst = t.getSplitExecutions().get(0);
        SplitExecution seLast = t.getSplitExecutions().get(t.getSplitExecutions().size() - 1);

        t.setReport(seFirst.getExecution().getReport());
        t.setType(seFirst.getCurrentPosition() > 0 ? TradeType.LONG : TradeType.SHORT);
        t.setSymbol(seFirst.getExecution().getSymbol());
        t.setUnderlying(seFirst.getExecution().getUnderlying());
        t.setCurrency(seFirst.getExecution().getCurrency());
        t.setSecType(seFirst.getExecution().getSecType());
        t.setOpenPosition(seLast.getCurrentPosition());

        BigDecimal cumulativeOpenPrice = BigDecimal.ZERO;
        BigDecimal cumulativeClosePrice = BigDecimal.ZERO;

        int cumulativeQuantity = 0;

        for (SplitExecution se : t.getSplitExecutions()) {
            Execution e = se.getExecution();

            if ((t.getType() == TradeType.LONG && e.getAction() == Action.BUY) || (t.getType() == TradeType.SHORT && e.getAction() == Action.SELL)) {
                cumulativeQuantity += se.getSplitQuantity();
                cumulativeOpenPrice = cumulativeOpenPrice.add(BigDecimal.valueOf(se.getSplitQuantity()).multiply(e.getFillPrice()));
            }
            if (t.getStatus() == TradeStatus.CLOSED) {
                if ((t.getType() == TradeType.LONG && e.getAction() == Action.SELL) || (t.getType() == TradeType.SHORT && e.getAction() == Action.BUY)) {
                    cumulativeClosePrice = cumulativeClosePrice.add(BigDecimal.valueOf(se.getSplitQuantity()).multiply(e.getFillPrice()));
                }
            }
        }

        t.setCumulativeQuantity(cumulativeQuantity);
        t.setOpenDate(seFirst.getExecution().getFillDate());
        t.setAvgOpenPrice(cumulativeOpenPrice.divide(BigDecimal.valueOf(cumulativeQuantity), RoundingMode.HALF_UP));

        if (t.getStatus() == TradeStatus.CLOSED) {
            t.setAvgClosePrice(cumulativeClosePrice.divide(BigDecimal.valueOf(cumulativeQuantity), RoundingMode.HALF_UP));
            t.setCloseDate(seLast.getExecution().getFillDate());

            BigDecimal profitLoss = (TradeType.LONG.equals(t.getType()) ? cumulativeClosePrice.subtract(cumulativeOpenPrice) : cumulativeOpenPrice.subtract(cumulativeClosePrice));
            profitLoss = profitLoss.multiply(BigDecimal.valueOf(getMultiplier(t)));
            t.setProfitLoss(profitLoss);
        }
    }

    public BigDecimal calculatePLPortfolioBase(Trade t) {

        switch (HanSettings.STATISTICS_PL_METHOD) {
            case PORTFOLIO_BASE_OPEN_CLOSE: return calculatePLPortfolioBaseOpenClose(t);
            case PORTFOLIO_BASE_CLOSE_ONLY: return calculatePLPortfolioBaseCloseOnly(t);
            case PORTFOLIO_BASE_CURRENT: return calculatePLPortfolioBaseCurrent(t);

            default: throw new IllegalStateException();
        }
    }

    public BigDecimal calculatePLPortfolioBaseOpenClose(Trade t) {
        validateClosed(t);

        BigDecimal cumulativeOpenPrice = BigDecimal.ZERO;
        BigDecimal cumulativeClosePrice = BigDecimal.ZERO;

        for (SplitExecution se : t.getSplitExecutions()) {
            Execution e = se.getExecution();

            BigDecimal exchangeRate = BigDecimal.valueOf(getExchangeRate(se.getFillDate().toLocalDate(), e.getCurrency()));
            BigDecimal fillPrice = se.getExecution().getFillPrice().divide(exchangeRate, HanSettings.PL_SCALE, RoundingMode.HALF_UP);

            if ((t.getType() == TradeType.LONG && e.getAction() == Action.BUY) || (t.getType() == TradeType.SHORT && e.getAction() == Action.SELL)) {
                cumulativeOpenPrice = cumulativeOpenPrice.add(BigDecimal.valueOf(se.getSplitQuantity()).multiply(fillPrice));

            } else if ((t.getType() == TradeType.LONG && e.getAction() == Action.SELL) || (t.getType() == TradeType.SHORT && e.getAction() == Action.BUY)) {
                cumulativeClosePrice = cumulativeClosePrice.add(BigDecimal.valueOf(se.getSplitQuantity()).multiply(fillPrice));
            }
        }

        BigDecimal profitLoss = (TradeType.LONG.equals(t.getType()) ? cumulativeClosePrice.subtract(cumulativeOpenPrice) : cumulativeOpenPrice.subtract(cumulativeClosePrice));
        profitLoss = profitLoss.multiply(BigDecimal.valueOf(getMultiplier(t)));

        return profitLoss;
    }

    private BigDecimal calculatePLPortfolioBaseCloseOnly(Trade t) {
        return calculatePLPortfolioBaseSimple(t, false);
    }

    private BigDecimal calculatePLPortfolioBaseCurrent(Trade t) {
        return calculatePLPortfolioBaseSimple(t, true);
    }

    private BigDecimal calculatePLPortfolioBaseSimple(Trade t, boolean current) {
        validateClosed(t);
        LocalDateTime plCalculationDate = current ? LocalDateTime.now() : t.getCloseDate();
        BigDecimal exchangeRate = BigDecimal.valueOf(getExchangeRate(plCalculationDate.toLocalDate(), t.getCurrency()));

        return t.getProfitLoss().divide(exchangeRate, HanSettings.PL_SCALE, RoundingMode.HALF_UP);
    }

    public int getMultiplier(Trade t) {
        switch (t.getSecType()) {
            case OPT: return ContractMultiplier.getByUnderlying(SecType.OPT, t.getUnderlying());
            case FUT: return ContractMultiplier.getByUnderlying(SecType.FUT, t.getUnderlying());
            default: return 1;
        }
    }

    private double getExchangeRate(LocalDate localDate, Currency currency) {
        if (exchangeRateMap.isEmpty()) {
            List<ExchangeRate> exchangeRates = reportDao.getAllExchangeRates();
            exchangeRates.forEach(exchangeRate -> exchangeRateMap.put(exchangeRate.getDate(), exchangeRate));
        }

        String date = HanUtil.formatExchangeRateDate(localDate);
        ExchangeRate exchangeRate = exchangeRateMap.get(date);

        if (exchangeRate == null) {
            exchangeRate = reportDao.getExchangeRate(date);

            if (exchangeRate != null) {
                exchangeRateMap.put(date, exchangeRate);
            } else {
                String previousDate = HanUtil.formatExchangeRateDate(localDate.plusDays(-1));
                exchangeRate = exchangeRateMap.get(previousDate);

                if (exchangeRate == null) {
                    exchangeRate = reportDao.getExchangeRate(previousDate);
                    exchangeRateMap.put(date, exchangeRate);
                }
            }
        }

        return exchangeRate.getRate(HanSettings.PORTFOLIO_BASE, currency);
    }

    private void validateClosed(Trade t) {
        if (!TradeStatus.CLOSED.equals(t.getStatus())) {
            throw new IllegalArgumentException("cannot calculate pl, trade not closed");
        }
    }
}
