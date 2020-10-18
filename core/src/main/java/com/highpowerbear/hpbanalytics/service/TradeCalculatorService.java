package com.highpowerbear.hpbanalytics.service;

import com.highpowerbear.hpbanalytics.common.HanUtil;
import com.highpowerbear.hpbanalytics.config.HanSettings;
import com.highpowerbear.hpbanalytics.database.ExchangeRate;
import com.highpowerbear.hpbanalytics.database.ExchangeRateRepository;
import com.highpowerbear.hpbanalytics.database.Execution;
import com.highpowerbear.hpbanalytics.database.Trade;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.TradeStatus;
import com.highpowerbear.hpbanalytics.enums.TradeType;
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
    private final Map<String, ExchangeRate> exchangeRateMap = new LinkedHashMap<>(); // TODO clear cache periodically, eventually move to hazelcast

    @Autowired
    public TradeCalculatorService(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public void calculateFields(Trade trade) {

        Execution firstExecution = trade.getExecutions().get(0);
        Execution lastExecution = trade.getExecutions().get(trade.getExecutions().size() - 1);

        trade
            .setType(firstExecution.getAction() == Types.Action.BUY ? TradeType.LONG : TradeType.SHORT)
            .setSymbol(firstExecution.getSymbol())
            .setUnderlying(firstExecution.getUnderlying())
            .setCurrency(firstExecution.getCurrency())
            .setSecType(firstExecution.getSecType())
            .setMultiplier(firstExecution.getMultiplier());

        TradeType tradeType = trade.getType();
        BigDecimal cumulativeOpenPrice = BigDecimal.ZERO;
        BigDecimal cumulativeClosePrice = BigDecimal.ZERO;
        int openPosition = 0;
        int cumulativeQuantity = 0;

        for (Execution execution : trade.getExecutions()) {
            Types.Action action = execution.getAction();
            int quantity = execution.getQuantity();
            BigDecimal fillPrice = execution.getFillPrice();

            openPosition += (action == Types.Action.BUY ? quantity : -quantity);

            if ((tradeType == TradeType.LONG && action == Types.Action.BUY) || (tradeType == TradeType.SHORT && action == Types.Action.SELL)) {
                cumulativeQuantity += quantity;
                cumulativeOpenPrice = cumulativeOpenPrice.add(BigDecimal.valueOf(quantity).multiply(fillPrice));

            } else if ((tradeType == TradeType.LONG && action == Types.Action.SELL) || (tradeType == TradeType.SHORT && action == Types.Action.BUY)) {
                cumulativeClosePrice = cumulativeClosePrice.add(BigDecimal.valueOf(quantity).multiply(fillPrice));
            }
        }

        BigDecimal avgOpenPrice = cumulativeOpenPrice.divide(BigDecimal.valueOf(cumulativeQuantity), RoundingMode.HALF_UP);

        trade
            .setOpenPosition(openPosition)
            .setStatus(openPosition != 0 ? TradeStatus.OPEN : TradeStatus.CLOSED)
            .setCumulativeQuantity(cumulativeQuantity)
            .setOpenDate(firstExecution.getFillDate())
            .setAvgOpenPrice(avgOpenPrice);

        if (trade.getStatus() == TradeStatus.CLOSED) {
            BigDecimal avgClosePrice = cumulativeClosePrice.divide(BigDecimal.valueOf(cumulativeQuantity), RoundingMode.HALF_UP);

            trade
                .setAvgClosePrice(avgClosePrice)
                .setCloseDate(lastExecution.getFillDate());

            BigDecimal cumulativePriceDiff = (TradeType.LONG.equals(tradeType) ? cumulativeClosePrice.subtract(cumulativeOpenPrice) : cumulativeOpenPrice.subtract(cumulativeClosePrice));
            BigDecimal profitLoss = cumulativePriceDiff.multiply(BigDecimal.valueOf(trade.getMultiplier()));
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

    public BigDecimal calculatePlPortfolioBaseOpenClose(Trade trade) {
        validateClosed(trade);

        TradeType tradeType = trade.getType();
        BigDecimal cumulativeOpenPrice = BigDecimal.ZERO;
        BigDecimal cumulativeClosePrice = BigDecimal.ZERO;

        for (Execution execution : trade.getExecutions()) {
            Types.Action action = execution.getAction();
            int quantity = execution.getQuantity();

            BigDecimal exchangeRate = BigDecimal.valueOf(getExchangeRate(execution.getFillDate().toLocalDate(), execution.getCurrency()));
            BigDecimal fillPrice = execution.getFillPrice().divide(exchangeRate, HanSettings.PL_SCALE, RoundingMode.HALF_UP);

            if ((tradeType == TradeType.LONG && action == Types.Action.BUY) || (tradeType == TradeType.SHORT && action == Types.Action.SELL)) {
                cumulativeOpenPrice = cumulativeOpenPrice.add(BigDecimal.valueOf(quantity).multiply(fillPrice));

            } else if ((tradeType == TradeType.LONG && action == Types.Action.SELL) || (tradeType == TradeType.SHORT && action == Types.Action.BUY)) {
                cumulativeClosePrice = cumulativeClosePrice.add(BigDecimal.valueOf(quantity).multiply(fillPrice));
            }
        }

        BigDecimal profitLoss = (TradeType.LONG.equals(tradeType) ? cumulativeClosePrice.subtract(cumulativeOpenPrice) : cumulativeOpenPrice.subtract(cumulativeClosePrice));
        profitLoss = profitLoss.multiply(BigDecimal.valueOf(trade.getMultiplier()));

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
