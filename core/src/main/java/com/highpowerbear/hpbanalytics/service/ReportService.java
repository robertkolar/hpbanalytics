package com.highpowerbear.hpbanalytics.service;

import com.highpowerbear.hpbanalytics.common.HanUtil;
import com.highpowerbear.hpbanalytics.config.WsTopic;
import com.highpowerbear.hpbanalytics.database.*;
import com.highpowerbear.hpbanalytics.enums.Action;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.enums.TradeStatus;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * Created by robertk on 4/26/2015.
 */
@Service
public class ReportService {
    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final ExecutionRepository executionRepository;
    private final TradeRepository tradeRepository;
    private final TradeCalculatorService tradeCalculatorService;
    private final MessageService messageService;

    @Autowired
    public ReportService(ExecutionRepository executionRepository,
                         TradeRepository tradeRepository,
                         TradeCalculatorService tradeCalculatorService,
                         MessageService messageService) {

        this.executionRepository = executionRepository;
        this.tradeRepository = tradeRepository;
        this.tradeCalculatorService = tradeCalculatorService;
        this.messageService = messageService;
    }

    // TODO listen
    public void executionReceived(Execution execution) {
        execution.setReceivedDate(LocalDateTime.now());

        if (execution.getFillDate() == null) {
            execution.setFillDate(execution.getReceivedDate());
        }

        newExecution(execution);
        messageService.sendWsMessage(WsTopic.EXECUTION,  "new execution processed");
    }

    @Transactional
    public void analyzeAll() {
        log.info("BEGIN analysis");

        tradeRepository.deleteAll();
        List<Execution> executions = executionRepository.findAllByOrderByFillDateAsc();

        if (executions.isEmpty()) {
            log.info("END analysis , no executions, skipping");
            return;
        }
        List<Trade> trades = analyze(executions);
        tradeRepository.saveAll(trades);

        log.info("END analysis");
    }

    @Transactional
    public void deleteExecution(long executionId) {
        Execution execution = executionRepository.findById(executionId).orElse(null);
        if (execution == null) {
            log.warn("execution " + executionId + " does not exists, cannot delete");
            return;
        }

        String symbol = execution.getSymbol();

        List<Trade> tradesAffected = tradeRepository.findTradesAffectedByExecution(execution.getFillDate(), symbol);
        logTradesAffected(execution, tradesAffected);

        tradeRepository.deleteAll(tradesAffected); // TODO cascade delete splitExecutions, check if it is already handled
        executionRepository.deleteById(executionId);

        SplitExecution firstSe = tradesAffected.get(0).getSplitExecutions().get(0);
        boolean isCleanCut = (firstSe.getSplitQuantity().equals(firstSe.getExecution().getQuantity()));
        boolean omitFirstSe = (isCleanCut && !executionRepository.existsById(firstSe.getExecution().getId())); // cleanCut is redundant

        List<Execution> executionsToAnalyzeAgain = executionRepository.findBySymbolAndFillDateAfterOrderByFillDateAsc(symbol, firstSe.getExecution().getFillDate());
        List<Trade> newTrades = analyzeSingleSymbol(executionsToAnalyzeAgain, (omitFirstSe ? null : firstSe));

        if (!newTrades.isEmpty()) {
            tradeRepository.saveAll(newTrades);
        }
    }

    @Transactional
    public void newExecution(Execution execution) {
        String symbol = execution.getSymbol();

        List<Trade> tradesAffected = tradeRepository.findTradesAffectedByExecution(execution.getFillDate(), symbol);
        logTradesAffected(execution, tradesAffected);

        log.info("deleting " + tradesAffected.size() + " trades");
        tradeRepository.deleteAll(tradesAffected); // TODO cascade delete splitExecutions, check if it is already handled

        execution = executionRepository.save(execution);

        List<Execution> executionsToAnalyzeAgain = new ArrayList<>();
        List<Trade> trades;

        if (!tradesAffected.isEmpty()) {
            SplitExecution firstSe = tradesAffected.get(0).getSplitExecutions().get(0);
            log.info("firstSe=" + firstSe.print());

            boolean isNewAfterFirst = execution.getFillDate().isAfter(firstSe.getExecution().getFillDate());
            log.info("isNewAfterFirst=" + isNewAfterFirst + ", " + HanUtil.formatLogDate(execution.getFillDate()) + ", " + HanUtil.formatLogDate(firstSe.getExecution().getFillDate()));

            if (isNewAfterFirst) {
                executionsToAnalyzeAgain = executionRepository.findBySymbolAndFillDateAfterOrderByFillDateAsc(symbol, firstSe.getExecution().getFillDate());
            } else {
                executionsToAnalyzeAgain = executionRepository.findBySymbolAndFillDateGreaterThanEqualOrderByFillDateAsc(symbol, execution.getFillDate());
            }
            trades = analyzeSingleSymbol(executionsToAnalyzeAgain, isNewAfterFirst ? firstSe : null);

        } else {
            executionsToAnalyzeAgain.add(execution);
            trades = analyzeSingleSymbol(executionsToAnalyzeAgain, null);
        }

        log.info("creating " + trades.size() + " trades");
        tradeRepository.saveAll(trades);
    }

    public void closeTrade(Trade trade, LocalDateTime closeDate, BigDecimal closePrice) {
        newExecution(new Execution()
                .setReceivedDate(LocalDateTime.now())
                .setComment(trade.getSecType() == SecType.OPT && closePrice.compareTo(BigDecimal.ZERO) == 0 ? "EXPIRE" : "CLOSE")
                .setOrigin("INTERNAL")
                .setReferenceId("N/A")
                .setAction(trade.getType() == TradeType.LONG ? Action.SELL : Action.BUY)
                .setQuantity(Math.abs(trade.getOpenPosition()))
                .setSymbol(trade.getSymbol())
                .setUnderlying(trade.getUnderlying())
                .setCurrency(trade.getCurrency())
                .setSecType(trade.getSecType())
                .setFillDate(closeDate)
                .setFillPrice(closePrice));
    }

    private List<Trade> analyze(List<Execution> executions) {
        return createTrades(createSplitExecutions(executions));
    }
    
    private List<Trade> analyzeSingleSymbol(List<Execution> executions, SplitExecution firstSplitExecution) {
        if (firstSplitExecution != null) {
            firstSplitExecution
                    .setId(null)
                    .setTrade(null);
        }
        return createTradesSingleSymbol(createSesSingleSymbol(executions, firstSplitExecution));
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

            if (currentPos < 0 && newPos > 0) {
                // split
                sesSingleSymbol.add(new SplitExecution() // first
                        .setExecution(e)
                        .setFillDate(e.getFillDate())
                        .setSplitQuantity(-currentPos)
                        .setCurrentPosition(0));

                sesSingleSymbol.add( new SplitExecution() // second
                        .setExecution(e)
                        .setFillDate(e.getFillDate())
                        .setSplitQuantity(newPos)
                        .setCurrentPosition(newPos));

            } else if (currentPos > 0 && newPos < 0) {
                // split
                sesSingleSymbol.add(new SplitExecution() // first
                        .setExecution(e)
                        .setFillDate(e.getFillDate())
                        .setSplitQuantity(currentPos)
                        .setCurrentPosition(0));

                sesSingleSymbol.add(new SplitExecution() // second
                        .setExecution(e)
                        .setFillDate(e.getFillDate())
                        .setSplitQuantity(-newPos)
                        .setCurrentPosition(newPos));
            } else {
                // normal
                sesSingleSymbol.add(new SplitExecution()
                        .setExecution(e)
                        .setFillDate(e.getFillDate())
                        .setSplitQuantity(e.getQuantity())
                        .setCurrentPosition(newPos));
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
            Trade trade = new Trade()
                    .setStatus(TradeStatus.OPEN);

            for (SplitExecution se : singleSymbolSet) {
                singleTradeSet.add(se);
                if (se.getCurrentPosition() == 0) {
                    trade.setStatus(TradeStatus.CLOSED);
                    break;
                }
            }
            trade.setSplitExecutions(new ArrayList<>(singleTradeSet));
            tradeCalculatorService.calculateFields(trade);
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
