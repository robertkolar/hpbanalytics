package com.highpowerbear.hpbanalytics.service;

import com.highpowerbear.hpbanalytics.config.WsTopic;
import com.highpowerbear.hpbanalytics.database.Execution;
import com.highpowerbear.hpbanalytics.database.ExecutionRepository;
import com.highpowerbear.hpbanalytics.database.Trade;
import com.highpowerbear.hpbanalytics.database.TradeRepository;
import com.highpowerbear.hpbanalytics.model.ExecutionContract;
import com.ib.client.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * Created by robertk on 4/26/2015.
 */
@Service
public class AnalyticsService {
    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final ExecutionRepository executionRepository;
    private final TradeRepository tradeRepository;
    private final TradeService tradeService;
    private final MessageService messageService;

    @Autowired
    public AnalyticsService(ExecutionRepository executionRepository,
                            TradeRepository tradeRepository,
                            TradeService tradeService,
                            MessageService messageService) {

        this.executionRepository = executionRepository;
        this.tradeRepository = tradeRepository;
        this.tradeService = tradeService;
        this.messageService = messageService;
    }

    @Transactional
    public void regenerateAllTrades() {
        log.info("BEGIN trade regeneration");

        long tradeCount = tradeRepository.count();
        int numExec = executionRepository.disassociateAllExecutions();

        log.info("disassociated " + numExec + " executions, deleting " + tradeCount + " trades");
        tradeRepository.deleteAll();

        List<Execution> executions = executionRepository.findAllByOrderByFillDateAsc();

        if (executions.isEmpty()) {
            log.info("END trade regeneration, no executions, skipping");
            return;
        }
        List<Trade> regeneratedTades = generateTrades(executions);
        saveRegeneratedTrades(regeneratedTades);

        log.info("END trade regeneration");
        messageService.sendWsReloadRequestMessage(WsTopic.TRADE);
    }

    @Transactional
    public void deleteExecution(long executionId) {
        log.info("received request to delete execution " + executionId);

        Execution execution = executionRepository.findById(executionId).orElse(null);
        if (execution == null) {
            log.warn("execution " + executionId + " does not exists, cannot delete");
            return;
        }

        ExecutionContract ec = ExecutionContract.forExecution(execution);
        List<Trade> tradesAffected = deleteTradesAffected(ec);

        log.info("deleting execution " + execution);
        executionRepository.delete(execution);

        Trade firstTradeAffected = tradesAffected.get(0);
        Execution firstExecution = firstTradeAffected.getExecutions().get(0);

        List<Execution> executionsToAnalyzeAgain = executionRepository
                .findExecutionsToAnalyzeAgain(ec.symbol(), ec.currency(), ec.secType(), ec.multiplier(), firstExecution.getFillDate());

        List<Trade> regeneratedTrades = generateTradesSingleContract(executionsToAnalyzeAgain);
        saveRegeneratedTrades(regeneratedTrades);
    }

    @Transactional
    public void addExecution(Execution execution) {
        log.info("received request to add new execution for symbol " + execution.getSymbol());
        adjustFillDate(execution);

        ExecutionContract ec = ExecutionContract.forExecution(execution);
        List<Trade> tradesAffected = deleteTradesAffected(ec);

        log.info("saving new execution " + execution);
        execution = executionRepository.save(execution);

        if (tradesAffected.isEmpty()) {
            List<Trade> regeneratedTrades = generateTradesSingleContract(Collections.singletonList(execution));
            saveRegeneratedTrades(regeneratedTrades);
            return;
        }

        Trade firstTradeAffected = tradesAffected.get(0);
        Execution firstExecution = firstTradeAffected.getExecutions().get(0);

        LocalDateTime cutoffDate = Stream.of(firstExecution, execution)
                .map(Execution::getFillDate)
                .min(LocalDateTime::compareTo)
                .get();

        List<Execution> executionsToAnalyzeAgain = executionRepository
                .findExecutionsToAnalyzeAgain(ec.symbol(), ec.currency(), ec.secType(), ec.multiplier(), cutoffDate);

        List<Trade> regeneratedTrades = generateTradesSingleContract(executionsToAnalyzeAgain);
        saveRegeneratedTrades(regeneratedTrades);
    }

    private List<Trade> deleteTradesAffected(ExecutionContract ec) {
        List<Trade> tradesAffected = tradeRepository
                .findTradesAffectedByExecution(ec.symbol(), ec.currency(), ec.secType(), ec.multiplier(), ec.execution().getFillDate());

        if (tradesAffected.isEmpty()) {
            log.info("no trades affected");
            return tradesAffected;
        }

        StringBuilder sb = new StringBuilder("trades affected:\n");
        tradesAffected.forEach(trade -> sb.append(trade).append("\n"));
        log.info(sb.toString());

        for (Trade trade : tradesAffected) {
            List<Execution> tradeExecutions = trade.getExecutions();

            for (Execution te : tradeExecutions) {
                te.setTrade(null); // save handled by transaction
                log.info("disassociated execution " + te.getId());
            }
        }
        log.info("deleting " + tradesAffected.size() + " trades");
        tradeRepository.deleteAll(tradesAffected);

        return tradesAffected;
    }
    
    private void adjustFillDate(Execution execution) {
        LocalDateTime fillDate = execution.getFillDate();

        while (executionRepository.existsByFillDate(fillDate)) {
            fillDate = fillDate.plus(1, ChronoUnit.MICROS);
        }
        if (fillDate.isAfter(execution.getFillDate())) {
            log.info("adjusting fill date to " + fillDate);
            execution.setFillDate(fillDate);
        }
    }

    private void saveRegeneratedTrades(List<Trade> trades) {
        log.info("saving " + trades.size() + " regenerated trades");

        if (!trades.isEmpty()) {
            tradeRepository.saveAll(trades); // executions update handled by transaction
        }
        messageService.sendWsReloadRequestMessage(WsTopic.EXECUTION);
        messageService.sendWsReloadRequestMessage(WsTopic.TRADE);
    }
    
    private List<Trade> generateTrades(List<Execution> executions) {
        List<Trade> trades = new ArrayList<>();
        Set<String> cids = executions.stream().map(ExecutionContract::cid).collect(Collectors.toSet());
        Map<String, List<Execution>> executionsPerCidMap = new HashMap<>(); // contract identifier -> list of executions

        cids.forEach(cid -> executionsPerCidMap.put(cid, new ArrayList<>()));

        for (Execution execution : executions) {
            String cid = ExecutionContract.cid(execution);
            executionsPerCidMap.get(cid).add(execution);
        }

        for (String cid : cids) {
            List<Execution> executionsPerCid = executionsPerCidMap.get(cid);

            log.info("generating trades for " + cid);
            List<Trade> generatedTradesPerCid = generateTradesSingleContract(executionsPerCid);

            trades.addAll(generatedTradesPerCid);
        }

        return trades;
    }
    
    private List<Trade> generateTradesSingleContract(List<Execution> executions) {
        List<Trade> trades = new ArrayList<>();

        int currentPos = 0;
        Set<Execution> singleContractSet = new LinkedHashSet<>(executions);

        while (!singleContractSet.isEmpty()) {
            Trade trade = new Trade();

            for (Execution execution : singleContractSet) {
                trade.getExecutions().add(execution);
                execution.setTrade(trade);

                int oldPos = currentPos;
                currentPos += (execution.getAction() == Types.Action.BUY ? execution.getQuantity() : -execution.getQuantity());

                log.info("associated " + execution + ", currentPos=" + currentPos);

                if (detectReversal(oldPos, currentPos)) {
                    throw new IllegalStateException("execution resulting in reversal trade not permitted " + execution);
                }
                if (currentPos == 0) {
                    break;
                }
            }

            tradeService.calculateFields(trade);
            log.info("generated trade " + trade);
            trades.add(trade);
            singleContractSet.removeAll(trade.getExecutions());
        }
        return trades;
    }

    private boolean detectReversal(int oldPos, int currentPos) {
        return oldPos > 0 && currentPos < 0 || oldPos < 0 && currentPos > 0;
    }
}
