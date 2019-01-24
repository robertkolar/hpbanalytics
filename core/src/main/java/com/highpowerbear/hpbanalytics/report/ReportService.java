package com.highpowerbear.hpbanalytics.report;

import com.highpowerbear.hpbanalytics.common.HanSettings;
import com.highpowerbear.hpbanalytics.common.HanUtil;
import com.highpowerbear.hpbanalytics.common.MessageService;
import com.highpowerbear.hpbanalytics.dao.ReportDao;
import com.highpowerbear.hpbanalytics.entity.*;
import com.highpowerbear.hpbanalytics.enums.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * Created by robertk on 4/26/2015.
 */
@Service
public class ReportService {
    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final ReportDao reportDao;
    private final TradeCalculator tradeCalculator;
    private final MessageService messageService;

    @Autowired
    public ReportService(ReportDao reportDao, TradeCalculator tradeCalculator, MessageService messageService) {
        this.reportDao = reportDao;
        this.tradeCalculator = tradeCalculator;
        this.messageService = messageService;
    }

    @JmsListener(destination = HanSettings.JMS_DEST_ORDER_FILLED)
    public void orderFilled(String ibOrderId) {
        log.info("handling execution for order " + ibOrderId);

        IbOrder ibOrder = reportDao.findIbOrder(Long.valueOf(ibOrderId));

        if (ibOrder.getStatus() != OrderStatus.FILLED) {
            log.error("cannot create execution, ibOrder " + ibOrderId + " not filled");
            return;
        }
        Execution execution = new Execution();

        execution.setOrigin("IB:" + ibOrder.getAccountId());
        execution.setReferenceId(String.valueOf(ibOrder.getPermId()));
        execution.setAction(Action.valueOf(ibOrder.getAction()));
        execution.setQuantity(ibOrder.getQuantity());
        execution.setUnderlying(ibOrder.getUnderlying());
        execution.setCurrency(Currency.valueOf(ibOrder.getCurrency()));
        execution.setSymbol(ibOrder.getSymbol());
        execution.setSecType(SecType.valueOf(ibOrder.getSecType()));
        execution.setFillDate(ibOrder.getStatusDate());
        execution.setFillPrice(BigDecimal.valueOf(ibOrder.getFillPrice()));

        processExecution(execution);
    }

    @JmsListener(destination = HanSettings.JMS_DEST_EXECUTION_RECEIVED)
    public void executionReceived (Execution execution) {
        processExecution(execution);
    }

    private void processExecution(Execution execution) {
        execution.setReceivedDate(LocalDateTime.now());

        if (execution.getFillDate() == null) {
            execution.setFillDate(execution.getReceivedDate());
        }
        Report report = reportDao.getReportByOriginAndSecType(execution.getOrigin(), execution.getSecType());

        if (report == null) {
            log.warn("no report for origin=" + execution.getOrigin() + " and secType=" + execution.getSecType() + ", skipping");
            return;
        }
        execution.setReport(report);

        newExecution(execution);
        messageService.sendWsMessage(HanSettings.WS_TOPIC_REPORT,  "new execution processed");
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

            boolean isNewAfterFirst = execution.getFillDate().isAfter(firstSe.getExecution().getFillDate());
            log.info("isNewAfterFirst=" + isNewAfterFirst + ", " + HanUtil.formatLogDate(execution.getFillDate()) + ", " + HanUtil.formatLogDate(firstSe.getExecution().getFillDate()));

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

    public void closeTrade(Trade trade, LocalDateTime closeDate, BigDecimal closePrice) {
        Execution execution = new Execution();

        execution.setReceivedDate(LocalDateTime.now());
        execution.setReport(trade.getReport());
        execution.setComment(trade.getSecType() == SecType.OPT && closePrice.compareTo(BigDecimal.ZERO) == 0 ? "EXPIRE" : "CLOSE");
        execution.setOrigin("INTERNAL");
        execution.setReferenceId("N/A");
        execution.setAction(trade.getType() == TradeType.LONG ? Action.SELL : Action.BUY);
        execution.setQuantity(Math.abs(trade.getOpenPosition()));
        execution.setSymbol(trade.getSymbol());
        execution.setUnderlying(trade.getUnderlying());
        execution.setCurrency(trade.getCurrency());
        execution.setSecType(trade.getSecType());
        execution.setFillDate(closeDate);
        execution.setFillPrice(closePrice);

        newExecution(execution);
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
