package com.highpowerbear.hpbanalytics.report;

import com.highpowerbear.hpbanalytics.common.CoreSettings;
import com.highpowerbear.hpbanalytics.common.CoreUtil;
import com.highpowerbear.hpbanalytics.dao.ReportDao;
import com.highpowerbear.hpbanalytics.entity.ExchangeRate;
import com.highpowerbear.hpbanalytics.entity.Execution;
import com.highpowerbear.hpbanalytics.entity.SplitExecution;
import com.highpowerbear.hpbanalytics.entity.Trade;
import com.highpowerbear.hpbanalytics.enums.Action;
import com.highpowerbear.hpbanalytics.enums.ContractMultiplier;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.enums.TradeStatus;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by robertk on 12/25/2017.
 */
@Service
public class TradeCalculator {

    private final ReportDao reportDao;

    private final Map<String, ExchangeRate> exchangeRateMap = new LinkedHashMap<>();

    @Autowired
    public TradeCalculator(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

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

        t.setCumulativeQuantity(cumulativeQuantity);
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

    public Double calculatePLPortfolioBase(Trade t) {

        switch (CoreSettings.STATISTICS_PL_METHOD) {
            case PORTFOLIO_BASE_OPEN_CLOSE: return calculatePLPortfolioBaseOpenClose(t);
            case PORTFOLIO_BASE_CLOSE_ONLY: return calculatePLPortfolioBaseCloseOnly(t);
            case PORTFOLIO_BASE_CURRENT: return calculatePLPortfolioBaseCurrent(t);

            default: return null;
        }
    }

    public Double calculatePLPortfolioBaseOpenClose(Trade t) {
        validateClosed(t);

        double cumulativeOpenPrice = 0d;
        double cumulativeClosePrice = 0d;

        for (SplitExecution se : t.getSplitExecutions()) {
            Execution e = se.getExecution();

            double exchangeRate = getExchangeRate(se.getFillDate(), e.getCurrency());
            double fillPrice = se.getExecution().getFillPrice().doubleValue() / exchangeRate;

            if ((t.getType() == TradeType.LONG && e.getAction() == Action.BUY) || (t.getType() == TradeType.SHORT && e.getAction() == Action.SELL)) {
                cumulativeOpenPrice += se.getSplitQuantity() * fillPrice;

            } else if ((t.getType() == TradeType.LONG && e.getAction() == Action.SELL) || (t.getType() == TradeType.SHORT && e.getAction() == Action.BUY)) {
                cumulativeClosePrice += se.getSplitQuantity() * fillPrice;
            }
        }

        double profitLoss = (TradeType.LONG.equals(t.getType()) ? cumulativeClosePrice - cumulativeOpenPrice : cumulativeOpenPrice - cumulativeClosePrice);
        profitLoss *= getMultiplier(t);

        return profitLoss;
    }

    private Double calculatePLPortfolioBaseCloseOnly(Trade t) {
        return calculatePLPortfolioBaseSimple(t, false);
    }

    private Double calculatePLPortfolioBaseCurrent(Trade t) {
        return calculatePLPortfolioBaseSimple(t, true);
    }

    private Double calculatePLPortfolioBaseSimple(Trade t, boolean current) {
        validateClosed(t);
        Calendar plCalculationDate = current ? Calendar.getInstance() : t.getCloseDate();
        double exchangeRate = getExchangeRate(plCalculationDate, t.getCurrency());

        return t.getProfitLoss().doubleValue() / exchangeRate;
    }

    public int getMultiplier(Trade t) {
        switch (t.getSecType()) {
            case OPT: return ContractMultiplier.getByUnderlying(SecType.OPT, t.getUnderlying());
            case FUT: return ContractMultiplier.getByUnderlying(SecType.FUT, t.getUnderlying());
            default: return 1;
        }
    }

    private Double getExchangeRate(Calendar calendar, Currency currency) {
        if (exchangeRateMap.isEmpty()) {
            List<ExchangeRate> exchangeRates = reportDao.getAllExchangeRates();
            exchangeRates.forEach(exchangeRate -> exchangeRateMap.put(exchangeRate.getDate(), exchangeRate));
        }

        String date = CoreUtil.formatExchangeRateDate(calendar);
        ExchangeRate exchangeRate = exchangeRateMap.get(date);

        if (exchangeRate == null) {
            exchangeRate = reportDao.getExchangeRate(date);

            if (exchangeRate != null) {
                exchangeRateMap.put(date, exchangeRate);
            } else {
                String previousDate = CoreUtil.formatExchangeRateDate(CoreUtil.previousDay(calendar));
                exchangeRate = exchangeRateMap.get(previousDate);

                if (exchangeRate == null) {
                    exchangeRate = reportDao.getExchangeRate(previousDate);
                    exchangeRateMap.put(date, exchangeRate);
                }
            }
        }

        return exchangeRate.getRate(CoreSettings.PORTFOLIO_BASE, currency);
    }

    private void validateClosed(Trade t) {
        if (!TradeStatus.CLOSED.equals(t.getStatus())) {
            throw new IllegalArgumentException("cannot calculate pl, trade not closed");
        }
    }
}
