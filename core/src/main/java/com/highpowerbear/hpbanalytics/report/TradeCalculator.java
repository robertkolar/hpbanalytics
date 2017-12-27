package com.highpowerbear.hpbanalytics.report;

import com.highpowerbear.hpbanalytics.common.CoreSettings;
import com.highpowerbear.hpbanalytics.common.CoreUtil;
import com.highpowerbear.hpbanalytics.dao.ReportDao;
import com.highpowerbear.hpbanalytics.entity.ExchangeRate;
import com.highpowerbear.hpbanalytics.entity.Execution;
import com.highpowerbear.hpbanalytics.entity.SplitExecution;
import com.highpowerbear.hpbanalytics.entity.Trade;
import com.highpowerbear.hpbanalytics.enums.Action;
import com.highpowerbear.hpbanalytics.enums.FuturePlMultiplier;
import com.highpowerbear.hpbanalytics.enums.TradeStatus;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by robertk on 12/25/2017.
 */
@Service
public class TradeCalculator {

    @Autowired ReportDao reportDao;

    private final Map<String, ExchangeRate> exchangeRateMap = new LinkedHashMap<>();

    public void calculateFields(Trade t) {
        MathContext mc = new MathContext(8);

        SplitExecution seFirst = t.getSplitExecutions().get(0);
        SplitExecution seLast = t.getSplitExecutions().get(t.getSplitExecutions().size() - 1);

        t.setReport(seFirst.getExecution().getReport());
        t.setType(seFirst.getCurrentPosition() > 0 ? TradeType.LONG : TradeType.SHORT);
        t.setSymbol(seFirst.getExecution().getSymbol());
        t.setUnderlying(seFirst.getExecution().getUnderlying());
        t.setCurrency(seFirst.getExecution().getCurrency());
        t.setSecType(seFirst.getExecution().getSecType());
        t.setOpenPosition(seLast.getCurrentPosition());

        BigDecimal cumulativeOpenPrice = BigDecimal.valueOf(0d);
        BigDecimal cumulativeClosePrice = BigDecimal.valueOf(0d);

        int cumulativeQuantity = 0;

        for (SplitExecution se : t.getSplitExecutions()) {
            Execution e = se.getExecution();

            if ((t.getType() == TradeType.LONG && e.getAction() == Action.BUY) || (t.getType() == TradeType.SHORT && e.getAction() == Action.SELL)) {
                cumulativeQuantity += se.getSplitQuantity();
                cumulativeOpenPrice = cumulativeOpenPrice.add(new BigDecimal(se.getSplitQuantity()).multiply(e.getFillPrice(), mc));
            }
            if (t.getStatus() == TradeStatus.CLOSED) {
                if ((t.getType() == TradeType.LONG && e.getAction() == Action.SELL) || (t.getType() == TradeType.SHORT && e.getAction() == Action.BUY)) {
                    cumulativeClosePrice = cumulativeClosePrice.add(new BigDecimal(se.getSplitQuantity()).multiply(e.getFillPrice(), mc));
                }
            }
        }

        t.setOpenDate(seFirst.getExecution().getFillDate());
        t.setAvgOpenPrice(cumulativeOpenPrice.divide(new BigDecimal(cumulativeQuantity), mc));

        if (t.getStatus() == TradeStatus.CLOSED) {
            t.setAvgClosePrice(cumulativeClosePrice.divide(new BigDecimal(cumulativeQuantity), mc));
            t.setCloseDate(seLast.getExecution().getFillDate());

            BigDecimal profitLoss = (TradeType.LONG.equals(t.getType()) ? cumulativeClosePrice.subtract(cumulativeOpenPrice, mc) : cumulativeOpenPrice.subtract(cumulativeClosePrice, mc));
            profitLoss = profitLoss.multiply(BigDecimal.valueOf(getMultiplier(t)), mc);
            t.setProfitLoss(profitLoss);
        }
    }

    public Double calculatePlPortfolioBase(Trade trade) {
        if (!TradeStatus.CLOSED.equals(trade.getStatus())) {
            return null;
        }

        if (exchangeRateMap.isEmpty()) {
            List<ExchangeRate> exchangeRates = reportDao.getAllExchangeRates();
            exchangeRates.forEach(exchangeRate -> exchangeRateMap.put(exchangeRate.getDate(), exchangeRate));
        }

        Double cumulativeOpenPrice = 0d;
        Double cumulativeClosePrice = 0d;

        // first step
        for (SplitExecution se : trade.getSplitExecutions()) {
            String date = CoreUtil.formatExchangeRateDate(se.getFillDate());
            ExchangeRate exchangeRate = exchangeRateMap.get(date);

            if (exchangeRate == null) {
                exchangeRate = reportDao.getExchangeRate(date);
                if (exchangeRate != null) {
                    exchangeRateMap.put(date, exchangeRate);
                } else {
                    String previousDate = CoreUtil.formatExchangeRateDate(CoreUtil.previousDay(se.getFillDate()));
                    exchangeRate = exchangeRateMap.get(previousDate);
                }
            }

            double fillPrice = se.getExecution().getFillPrice().doubleValue() / exchangeRate.getRate(CoreSettings.PORTFOLIO_BASE, trade.getCurrency());

            if ((TradeType.LONG. equals(trade.getType()) && Action.BUY. equals(se.getExecution().getAction())) ||
                    (TradeType.SHORT.equals(trade.getType()) && Action.SELL.equals(se.getExecution().getAction()))) {

                cumulativeOpenPrice += se.getSplitQuantity() * fillPrice;
            }

            if (TradeStatus.CLOSED.equals(trade.getStatus())) {
                if ((TradeType.LONG. equals(trade.getType()) && Action.SELL.equals(se.getExecution().getAction())) ||
                        (TradeType.SHORT.equals(trade.getType()) && Action.BUY. equals(se.getExecution().getAction()))) {

                    cumulativeClosePrice += se.getSplitQuantity() * fillPrice;
                }
            }
        }
        // second step
        Double profitLoss = (TradeType.LONG.equals(trade.getType()) ? cumulativeClosePrice - cumulativeOpenPrice : cumulativeOpenPrice - cumulativeClosePrice);
        profitLoss *= getMultiplier(trade);

        return profitLoss;
    }

    public int getMultiplier(Trade t) {
        switch (t.getSecType()) {
            case OPT: return 100;
            case FUT: return FuturePlMultiplier.getMultiplierByUnderlying(t.getUnderlying());
            default: return 1;
        }
    }
}
