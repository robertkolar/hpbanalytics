package com.highpowerbear.hpbanalytics.service;

import com.highpowerbear.hpbanalytics.common.HanUtil;
import com.highpowerbear.hpbanalytics.config.HanSettings;
import com.highpowerbear.hpbanalytics.database.*;
import com.highpowerbear.hpbanalytics.enums.*;
import com.ib.client.Types;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
@Service
public class TradeCalculatorService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final Map<String, ExchangeRate> exchangeRateMap = new LinkedHashMap<>();

    @Autowired
    public TradeCalculatorService(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public void calculateFields(Trade trade) {
        SplitExecution seFirst = trade.getSplitExecutions().get(0);
        SplitExecution seLast = trade.getSplitExecutions().get(trade.getSplitExecutions().size() - 1);

        trade
            .setType(seFirst.getCurrentPosition() > 0 ? TradeType.LONG : TradeType.SHORT)
            .setSymbol(seFirst.getExecution().getSymbol())
            .setUnderlying(seFirst.getExecution().getUnderlying())
            .setCurrency(seFirst.getExecution().getCurrency())
            .setSecType(seFirst.getExecution().getSecType())
            .setMultiplier(seFirst.getExecution().getMultiplier())
            .setOpenPosition(seLast.getCurrentPosition());

        BigDecimal cumulativeOpenPrice = BigDecimal.ZERO;
        BigDecimal cumulativeClosePrice = BigDecimal.ZERO;

        int cumulativeQuantity = 0;

        for (SplitExecution se : trade.getSplitExecutions()) {
            Execution e = se.getExecution();

            if ((trade.getType() == TradeType.LONG && e.getAction() == Types.Action.BUY) || (trade.getType() == TradeType.SHORT && e.getAction() == Types.Action.SELL)) {
                cumulativeQuantity += se.getSplitQuantity();
                cumulativeOpenPrice = cumulativeOpenPrice.add(BigDecimal.valueOf(se.getSplitQuantity()).multiply(e.getFillPrice()));
            }
            if (trade.getStatus() == TradeStatus.CLOSED) {
                if ((trade.getType() == TradeType.LONG && e.getAction() == Types.Action.SELL) || (trade.getType() == TradeType.SHORT && e.getAction() == Types.Action.BUY)) {
                    cumulativeClosePrice = cumulativeClosePrice.add(BigDecimal.valueOf(se.getSplitQuantity()).multiply(e.getFillPrice()));
                }
            }
        }

        trade
            .setCumulativeQuantity(cumulativeQuantity)
            .setOpenDate(seFirst.getExecution().getFillDate())
            .setAvgOpenPrice(cumulativeOpenPrice.divide(BigDecimal.valueOf(cumulativeQuantity), RoundingMode.HALF_UP));

        if (trade.getStatus() == TradeStatus.CLOSED) {
            trade
                .setAvgClosePrice(cumulativeClosePrice.divide(BigDecimal.valueOf(cumulativeQuantity), RoundingMode.HALF_UP))
                .setCloseDate(seLast.getExecution().getFillDate());

            BigDecimal profitLoss = (TradeType.LONG.equals(trade.getType()) ? cumulativeClosePrice.subtract(cumulativeOpenPrice) : cumulativeOpenPrice.subtract(cumulativeClosePrice));
            profitLoss = profitLoss.multiply(BigDecimal.valueOf(trade.getMultiplier()));
            trade.setProfitLoss(profitLoss);
        }
    }

    public BigDecimal calculatePlPortfolioBase(Trade t) {

        switch (HanSettings.STATISTICS_PL_METHOD) {
            case PORTFOLIO_BASE_OPEN_CLOSE: return calculatePlPortfolioBaseOpenClose(t);
            case PORTFOLIO_BASE_CLOSE_ONLY: return calculatePlPortfolioBaseCloseOnly(t);
            case PORTFOLIO_BASE_CURRENT: return calculatePlPortfolioBaseCurrent(t);

            default: throw new IllegalStateException();
        }
    }

    public BigDecimal calculatePlPortfolioBaseOpenClose(Trade t) {
        validateClosed(t);

        BigDecimal cumulativeOpenPrice = BigDecimal.ZERO;
        BigDecimal cumulativeClosePrice = BigDecimal.ZERO;

        for (SplitExecution se : t.getSplitExecutions()) {
            Execution e = se.getExecution();

            BigDecimal exchangeRate = BigDecimal.valueOf(getExchangeRate(se.getFillDate().toLocalDate(), e.getCurrency()));
            BigDecimal fillPrice = se.getExecution().getFillPrice().divide(exchangeRate, HanSettings.PL_SCALE, RoundingMode.HALF_UP);

            if ((t.getType() == TradeType.LONG && e.getAction() == Types.Action.BUY) || (t.getType() == TradeType.SHORT && e.getAction() == Types.Action.SELL)) {
                cumulativeOpenPrice = cumulativeOpenPrice.add(BigDecimal.valueOf(se.getSplitQuantity()).multiply(fillPrice));

            } else if ((t.getType() == TradeType.LONG && e.getAction() == Types.Action.SELL) || (t.getType() == TradeType.SHORT && e.getAction() == Types.Action.BUY)) {
                cumulativeClosePrice = cumulativeClosePrice.add(BigDecimal.valueOf(se.getSplitQuantity()).multiply(fillPrice));
            }
        }

        BigDecimal profitLoss = (TradeType.LONG.equals(t.getType()) ? cumulativeClosePrice.subtract(cumulativeOpenPrice) : cumulativeOpenPrice.subtract(cumulativeClosePrice));
        profitLoss = profitLoss.multiply(BigDecimal.valueOf(t.getMultiplier()));

        return profitLoss;
    }

    private BigDecimal calculatePlPortfolioBaseCloseOnly(Trade t) {
        return calculatePLPortfolioBaseSimple(t, false);
    }

    private BigDecimal calculatePlPortfolioBaseCurrent(Trade t) {
        return calculatePLPortfolioBaseSimple(t, true);
    }

    private BigDecimal calculatePLPortfolioBaseSimple(Trade t, boolean current) {
        validateClosed(t);
        LocalDateTime plCalculationDate = current ? LocalDateTime.now() : t.getCloseDate();
        BigDecimal exchangeRate = BigDecimal.valueOf(getExchangeRate(plCalculationDate.toLocalDate(), t.getCurrency()));

        return t.getProfitLoss().divide(exchangeRate, HanSettings.PL_SCALE, RoundingMode.HALF_UP);
    }

    private double getExchangeRate(LocalDate localDate, Currency currency) {
        if (exchangeRateMap.isEmpty()) {
            List<ExchangeRate> exchangeRates = exchangeRateRepository.findAll();
            exchangeRates.forEach(exchangeRate -> exchangeRateMap.put(exchangeRate.getDate(), exchangeRate));
        }

        String date = HanUtil.formatExchangeRateDate(localDate);
        ExchangeRate exchangeRate = exchangeRateMap.get(date);

        if (exchangeRate == null) {
            exchangeRate = exchangeRateRepository.findById(date).orElse(null);

            if (exchangeRate != null) {
                exchangeRateMap.put(date, exchangeRate);
            } else {
                String previousDate = HanUtil.formatExchangeRateDate(localDate.plusDays(-1));
                exchangeRate = exchangeRateMap.get(previousDate);

                if (exchangeRate == null) {
                    exchangeRate = exchangeRateRepository.findById(previousDate).orElse(null);
                    if (exchangeRate != null) {
                        exchangeRateMap.put(date, exchangeRate);
                    } else {
                        throw new IllegalStateException("exchange rate not available for " + date + " or " + previousDate);
                    }
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
