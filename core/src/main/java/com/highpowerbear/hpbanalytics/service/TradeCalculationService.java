package com.highpowerbear.hpbanalytics.service;

import com.highpowerbear.hpbanalytics.config.HanSettings;
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

/**
 * Created by robertk on 12/25/2017.
 */
@Service
public class TradeCalculationService {

    private final ExchangeRateService exchangeRateService;

    @Autowired
    public TradeCalculationService(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
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

    public BigDecimal calculatePlPortfolioBaseOpenClose(Trade trade) {
        if (!TradeStatus.CLOSED.equals(trade.getStatus())) {
            throw new IllegalArgumentException("cannot calculate pl, trade not closed " + trade);
        }

        TradeType tradeType = trade.getType();
        BigDecimal cumulativeOpenPrice = BigDecimal.ZERO;
        BigDecimal cumulativeClosePrice = BigDecimal.ZERO;

        for (Execution execution : trade.getExecutions()) {
            Types.Action action = execution.getAction();
            int quantity = execution.getQuantity();

            LocalDate date = execution.getFillDate().toLocalDate();
            Currency currency = execution.getCurrency();

            BigDecimal exchangeRate = exchangeRateService.getExchangeRate(date, currency);
            BigDecimal fillPriceBase = execution.getFillPrice().divide(exchangeRate, HanSettings.DECIMAL_SCALE, RoundingMode.HALF_UP);

            if ((tradeType == TradeType.LONG && action == Types.Action.BUY) || (tradeType == TradeType.SHORT && action == Types.Action.SELL)) {
                cumulativeOpenPrice = cumulativeOpenPrice.add(BigDecimal.valueOf(quantity).multiply(fillPriceBase));

            } else if ((tradeType == TradeType.LONG && action == Types.Action.SELL) || (tradeType == TradeType.SHORT && action == Types.Action.BUY)) {
                cumulativeClosePrice = cumulativeClosePrice.add(BigDecimal.valueOf(quantity).multiply(fillPriceBase));
            }
        }

        BigDecimal cumulativeDiff;
        if (TradeType.LONG.equals(tradeType)) {
            cumulativeDiff = cumulativeClosePrice.subtract(cumulativeOpenPrice);
        } else {
            cumulativeDiff = cumulativeOpenPrice.subtract(cumulativeClosePrice);
        }

        return cumulativeDiff.multiply(BigDecimal.valueOf(trade.getMultiplier()));
    }

    public BigDecimal calculatePlPortfolioBaseCloseOnly(Trade trade) {
        if (!TradeStatus.CLOSED.equals(trade.getStatus())) {
            throw new IllegalArgumentException("cannot calculate pl, trade not closed " + trade);
        }

        LocalDate date = trade.getCloseDate().toLocalDate();
        Currency currency = trade.getCurrency();
        BigDecimal exchangeRate = exchangeRateService.getExchangeRate(date, currency);

        return trade.getProfitLoss().divide(exchangeRate, HanSettings.DECIMAL_SCALE, RoundingMode.HALF_UP);
    }
}
