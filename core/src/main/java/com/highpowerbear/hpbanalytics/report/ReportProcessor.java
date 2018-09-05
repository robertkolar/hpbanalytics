package com.highpowerbear.hpbanalytics.report;

import com.highpowerbear.hpbanalytics.common.CoreUtil;
import com.highpowerbear.hpbanalytics.common.vo.OptionInfoVO;
import com.highpowerbear.hpbanalytics.dao.ReportDao;
import com.highpowerbear.hpbanalytics.entity.Execution;
import com.highpowerbear.hpbanalytics.entity.SplitExecution;
import com.highpowerbear.hpbanalytics.entity.Trade;
import com.highpowerbear.hpbanalytics.enums.Action;
import com.highpowerbear.hpbanalytics.enums.OptionType;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.enums.TradeStatus;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * Created by robertk on 4/26/2015.
 */
@Service
public class ReportProcessor {
    private static final Logger log = LoggerFactory.getLogger(ReportProcessor.class);

    private final ReportDao reportDao;
    private final TradeCalculator tradeCalculator;

    @Autowired
    public ReportProcessor(ReportDao reportDao, TradeCalculator tradeCalculator) {
        this.reportDao = reportDao;
        this.tradeCalculator = tradeCalculator;
    }

    @Transactional
    public void analyzeAll(int reportId) {
        log.info("BEGIN report processing for report " + reportId);

        reportDao.deleteAllTrades(reportId);
        List<Execution> executions = reportDao.getExecutions(reportId);

        if (executions.isEmpty()) {
            log.info("END report processing for report " + reportId + ", no executions, skipping");
            return;
        }

        List<Trade> trades = analyze(executions);
        reportDao.createTrades(trades);

        log.info("END report processing for report " + reportId);
    }

    @Transactional
    public void deleteReport(int reportId) {
        reportDao.deleteReport(reportId);
    }

    @Transactional
    public void deleteExecution(long executionId) {
        Execution execution = reportDao.findExecution(executionId);

        int reportId = execution.getReportId();
        String symbol = execution.getSymbol();

        List<Trade> tradesAffected = reportDao.getTradesAffectedByExecution(reportId, execution.getFillDate(), symbol);
        logTradesAffected(execution, tradesAffected);

        reportDao.deleteTrades(tradesAffected);
        reportDao.deleteExecution(executionId);

        SplitExecution firstSe = tradesAffected.get(0).getSplitExecutions().get(0);
        boolean isCleanCut = (firstSe.getSplitQuantity().equals(firstSe.getExecution().getQuantity()));
        boolean omitFirstSe = (isCleanCut && !reportDao.existsExecution(firstSe.getExecution().getId())); // cleanCut is redundant

        List<Execution> executionsToAnalyzeAgain = reportDao.getExecutionsAfterDate(reportId, firstSe.getExecution().getFillDate(), symbol);
        List<Trade> newTrades = analyzeSingleSymbol(executionsToAnalyzeAgain, (omitFirstSe ? null : firstSe));

        if (!newTrades.isEmpty()) {
            reportDao.createTrades(newTrades);
        }
    }

    @Transactional
    public void newExecution(Execution execution) {
        int reportId = execution.getReportId();
        String symbol = execution.getSymbol();

        List<Trade> tradesAffected = reportDao.getTradesAffectedByExecution(reportId, execution.getFillDate(), symbol);
        logTradesAffected(execution, tradesAffected);

        log.info("deleting " + tradesAffected.size() + " trades");
        reportDao.deleteTrades(tradesAffected);

        reportDao.createExecution(execution);

        // refresh from db
        execution = reportDao.findExecution(execution.getId());

        List<Execution> executionsToAnalyzeAgain = new ArrayList<>();
        List<Trade> trades;

        if (!tradesAffected.isEmpty()) {
            SplitExecution firstSe = tradesAffected.get(0).getSplitExecutions().get(0);
            log.info("firstSe=" + firstSe.print());

            boolean isNewAfterFirst = execution.getFillDate().after(firstSe.getExecution().getFillDate());
            log.info("isNewAfterFirst=" + isNewAfterFirst + ", " + execution.getFillDate().getTime() + ", " + firstSe.getExecution().getFillDate().getTime());

            if (isNewAfterFirst) {
                executionsToAnalyzeAgain = reportDao.getExecutionsAfterDate(reportId, firstSe.getExecution().getFillDate(), symbol);
            } else {
                executionsToAnalyzeAgain = reportDao.getExecutionsAfterDateInclusive(reportId, execution.getFillDate(), symbol);
            }
            trades = analyzeSingleSymbol(executionsToAnalyzeAgain, isNewAfterFirst ? firstSe : null);

        } else {
            executionsToAnalyzeAgain.add(execution);
            trades = analyzeSingleSymbol(executionsToAnalyzeAgain, null);
        }

        log.info("creating " + trades.size() + " trades");
        reportDao.createTrades(trades);
    }

    public void closeTrade(Trade trade, Calendar closeDate, BigDecimal closePrice) {
        Execution e = new Execution();

        e.setReceivedDate(Calendar.getInstance());
        e.setReport(trade.getReport());
        e.setComment("CLOSE");
        e.setOrigin("INTERNAL");
        e.setReferenceId("N/A");
        e.setAction(trade.getType() == TradeType.LONG ? Action.SELL : Action.BUY);
        e.setQuantity(Math.abs(trade.getOpenPosition()));
        e.setSymbol(trade.getSymbol());
        e.setUnderlying(trade.getUnderlying());
        e.setCurrency(trade.getCurrency());
        e.setSecType(trade.getSecType());
        e.setFillDate(closeDate);
        e.setFillPrice(closePrice);

        newExecution(e);
    }

    public void expireTrade(Trade trade) {
        OptionInfoVO optionInfo = CoreUtil.parseOptionSymbol(trade.getSymbol());
        if (optionInfo == null) {
            return;
        }

        Execution e = new Execution();

        e.setReceivedDate(Calendar.getInstance());
        e.setReport(trade.getReport());
        e.setComment("EXPIRE");
        e.setOrigin("INTERNAL");
        e.setReferenceId("N/A");
        e.setAction(trade.getType() == TradeType.LONG ? Action.SELL : Action.BUY);
        e.setQuantity(Math.abs(trade.getOpenPosition()));
        e.setSymbol(trade.getSymbol());
        e.setUnderlying(trade.getUnderlying());
        e.setCurrency(trade.getCurrency());
        e.setSecType(trade.getSecType());
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(optionInfo.getExpirationDate().getTime());
        e.setFillDate(cal);
        e.setFillPrice(new BigDecimal(0.0));

        newExecution(e);
    }

    public void assignTrade(Trade trade) {
        OptionInfoVO optionInfo = CoreUtil.parseOptionSymbol(trade.getSymbol());
        if (optionInfo == null) {
            return;
        }

        Execution e = new Execution();

        e.setReceivedDate(Calendar.getInstance());
        e.setReport(trade.getReport());
        e.setComment("ASSIGN");
        e.setOrigin("INTERNAL");
        e.setReferenceId("N/A");
        e.setAction(trade.getType() == TradeType.LONG ? Action.SELL : Action.BUY);
        e.setQuantity(Math.abs(trade.getOpenPosition()));
        e.setSymbol(trade.getSymbol());
        e.setUnderlying(trade.getUnderlying());
        e.setCurrency(trade.getCurrency());
        e.setSecType(trade.getSecType());
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(optionInfo.getExpirationDate().getTime());
        e.setFillDate(cal);
        e.setFillPrice(new BigDecimal(0.0));

        newExecution(e);

        Execution ce = new Execution();
        ce.setReceivedDate(e.getReceivedDate());
        ce.setReport(trade.getReport());
        ce.setComment("ASSIGN");
        ce.setOrigin("INTERNAL");
        ce.setReferenceId("N/A");
        ce.setAction(OptionType.PUT.equals(optionInfo.getOptionType()) ? Action.BUY : Action.SELL);
        ce.setQuantity(e.getQuantity() * 100);
        ce.setSymbol(e.getUnderlying());
        ce.setUnderlying(e.getUnderlying());
        ce.setCurrency(e.getCurrency());
        ce.setSecType(SecType.STK);
        // introduce random offset for stocks that were purchased/sold as a result of assignment so in case of same symbol they don't get exactly the same date
        // this is required constraint for all executions for the same symbol and execution source, see Execution entity
        Random r = new Random();
        long randomLong = r.nextInt(59000);
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(cal.getTimeInMillis() + randomLong);
        ce.setFillDate(cal1);
        ce.setFillPrice(new BigDecimal(optionInfo.getStrikePrice()));

        newExecution(ce);
    }

    private List<Trade> analyze(List<Execution> executions) {
        return createTrades(createSplitExecutions(executions));
    }
    
    private List<Trade> analyzeSingleSymbol(List<Execution> executions, SplitExecution firstSe) {
        if (firstSe != null) {
            firstSe.setId(null);
            firstSe.setTrade(null);
        }
        return createTradesSingleSymbol(createSesSingleSymbol(executions, firstSe));
    }
    
    
    private List<SplitExecution> createSplitExecutions(List<Execution> executions) {
        List<SplitExecution> splitExecutions = new ArrayList<>();
        Set<String> symbols = executions.stream().map(Execution::getSymbol).collect(Collectors.toSet());
        Map<String, List<Execution>> mapExecutions = new HashMap<>();
        for (String s : symbols) {
            mapExecutions.put(s, new ArrayList<>());
        }
        for (Execution e : executions) {
            mapExecutions.get(e.getSymbol()).add(e);
        }
        for (String s : symbols) {
            splitExecutions.addAll(createSesSingleSymbol(mapExecutions.get(s), null));
        }
        return splitExecutions;
    }
   
    private List<SplitExecution> createSesSingleSymbol(List<Execution> executions, SplitExecution firstSe) {
        List<SplitExecution> sesSingleSymbol = new ArrayList<>();
        int currentPos = (firstSe != null ? firstSe.getCurrentPosition() : 0);
        if (firstSe != null) {
            sesSingleSymbol.add(firstSe);
        }
        for (Execution e : executions) {
            int ePos = (e.getAction() == Action.BUY ? e.getQuantity() : -e.getQuantity());
            int newPos = currentPos + ePos;
            SplitExecution se;

            if (currentPos < 0 && newPos > 0) {
                // split
                se = new SplitExecution(); // first
                se.setExecution(e);
                se.setFillDate(e.getFillDate());
                se.setSplitQuantity(-currentPos);
                se.setCurrentPosition(0);
                sesSingleSymbol.add(se); 
                se = new SplitExecution(); //second
                se.setExecution(e);
                se.setFillDate(e.getFillDate());
                se.setSplitQuantity(newPos);
                se.setCurrentPosition(newPos);
                sesSingleSymbol.add(se);

            } else if (currentPos > 0 && newPos < 0) {
                // split
                se = new SplitExecution(); // first
                se.setExecution(e);
                se.setFillDate(e.getFillDate());
                se.setSplitQuantity(currentPos);
                se.setCurrentPosition(0);
                sesSingleSymbol.add(se);
                se = new SplitExecution(); //second
                se.setExecution(e);
                se.setFillDate(e.getFillDate());
                se.setSplitQuantity(-newPos);
                se.setCurrentPosition(newPos);
                sesSingleSymbol.add(se);

            } else {
                // normal
                se = new SplitExecution();
                se.setExecution(e);
                se.setFillDate(e.getFillDate());
                se.setSplitQuantity(e.getQuantity());
                se.setCurrentPosition(newPos);
                sesSingleSymbol.add(se);
            }
            currentPos = newPos;
        }
        return sesSingleSymbol;
    }
    
    private List<Trade> createTrades(List<SplitExecution> splitExecutions) {
        List<Trade> trades = new ArrayList<>();
        Set<String> symbols = splitExecutions.stream().map(se -> se.getExecution().getSymbol()).collect(Collectors.toSet());
        Map<String, List<SplitExecution>> mapSe = new HashMap<>();

        symbols.forEach(s -> mapSe.put(s, new ArrayList<>()));
        splitExecutions.forEach(se -> mapSe.get(se.getExecution().getSymbol()).add(se));
        symbols.forEach(s -> trades.addAll(createTradesSingleSymbol(mapSe.get(s))));

        return trades;
    }
    
    private List<Trade> createTradesSingleSymbol(List<SplitExecution> splitExecutions) {
        List<Trade> trades = new ArrayList<>();
        Set<SplitExecution> singleSymbolSet = new LinkedHashSet<>(splitExecutions);

        while (!singleSymbolSet.isEmpty()) {
            Set<SplitExecution> singleTradeSet = new LinkedHashSet<>();
            Trade trade = new Trade();
            trade.setStatus(TradeStatus.OPEN);

            for (SplitExecution se : singleSymbolSet) {
                singleTradeSet.add(se);
                if (se.getCurrentPosition() == 0) {
                    trade.setStatus(TradeStatus.CLOSED);
                    break;
                }
            }
            trade.setSplitExecutions(new ArrayList<>(singleTradeSet));
            tradeCalculator.calculateFields(trade);
            trades.add(trade);
            singleSymbolSet.removeAll(singleTradeSet);
        }
        return trades;
    }

    private void logTradesAffected(Execution execution, List<Trade> tradesAffected) {
        StringBuilder sb = new StringBuilder();
        sb.append("trades affected by execution: ").append(execution.print()).append("\n");

        tradesAffected.forEach(t -> {
            sb.append("trade: ").append(t.print()).append("\n");
            t.getSplitExecutions().forEach(se -> sb.append(se.print()).append("\n"));
        });
        log.info(sb.toString());
    }
}
