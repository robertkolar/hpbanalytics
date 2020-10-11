package com.highpowerbear.hpbanalytics.service;

import com.highpowerbear.dto.ExecutionDTO;
import com.highpowerbear.hpbanalytics.common.ExecutionMapper;
import com.highpowerbear.hpbanalytics.config.WsTopic;
import com.highpowerbear.hpbanalytics.database.Execution;
import com.highpowerbear.hpbanalytics.database.ExecutionRepository;
import com.highpowerbear.hpbanalytics.database.Trade;
import com.highpowerbear.hpbanalytics.database.TradeRepository;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import com.ib.client.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * Created by robertk on 4/26/2015.
 */
@Service
public class AnalyticsService implements ExecutionListener {
    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final ExecutionRepository executionRepository;
    private final TradeRepository tradeRepository;
    private final TradeCalculatorService tradeCalculatorService;
    private final MessageService messageService;
    private final ExecutionMapper executionMapper;

    @Autowired
    public AnalyticsService(ExecutionRepository executionRepository,
                            TradeRepository tradeRepository,
                            TradeCalculatorService tradeCalculatorService,
                            MessageService messageService,
                            ExecutionMapper executionMapper) {

        this.executionRepository = executionRepository;
        this.tradeRepository = tradeRepository;
        this.tradeCalculatorService = tradeCalculatorService;
        this.messageService = messageService;
        this.executionMapper = executionMapper;

        messageService.registerExecutionListener(this);
    }

    @Override
    public void executionReceived(ExecutionDTO dto) {
        newExecution(executionMapper.dtoToEntity(dto));
    }

    @Transactional
    public void regenerateAllTrades() {
        log.info("BEGIN trade regeneration");

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
        Execution execution = executionRepository.findById(executionId).orElse(null);
        if (execution == null) {
            log.warn("execution " + executionId + " does not exists, cannot delete");
            return;
        }
        int conid = execution.getConid();

        List<Trade> tradesAffected = tradeRepository.findTradesAffectedByExecution(execution.getFillDate(), conid);
        logTradesAffected(execution, tradesAffected);

        Trade firstTradeAffected = tradesAffected.get(0);
        Long firstExecutionId = firstTradeAffected.getExecutionIds().get(0);
        Execution firstExecution = Objects.requireNonNull(executionRepository.findById(firstExecutionId).orElse(null));

        log.info("deleting " + tradesAffected.size() + " trades");
        tradeRepository.deleteAll(tradesAffected);

        log.info("deleting execution " + execution);
        executionRepository.deleteById(executionId); // need to delete before the next query

        List<Execution> executionsToAnalyzeAgain = executionRepository.findByConidAndFillDateGreaterThanEqualOrderByFillDateAsc(conid, firstExecution.getFillDate());
        List<Trade> regeneratedTrades = generateTradesSingleConid(executionsToAnalyzeAgain);

        saveRegeneratedTrades(regeneratedTrades);
    }

    @Transactional
    public void newExecution(Execution execution) {
        // TODO prevent that new execution fill date is not equal to any existing execution

        execution.setReceivedDate(LocalDateTime.now());
        int conid = execution.getConid();

        List<Trade> tradesAffected = tradeRepository.findTradesAffectedByExecution(execution.getFillDate(), conid);
        logTradesAffected(execution, tradesAffected);

        log.info("deleting " + tradesAffected.size() + " trades");
        tradeRepository.deleteAll(tradesAffected);

        log.info("saving new execution " + execution);
        execution = executionRepository.save(execution); // need to save before the next query

        if (tradesAffected.isEmpty()) {
            saveRegeneratedTrades(generateTradesSingleConid(Collections.singletonList(execution)));
            return;
        }

        Trade firstTradeAffected = tradesAffected.get(0);

        Long firstExecutionId = firstTradeAffected.getExecutionIds().get(0);
        Execution firstExecution = Objects.requireNonNull(executionRepository.findById(firstExecutionId).orElse(null));

        LocalDateTime cutoffDate = Stream.of(firstExecution, execution)
                .map(Execution::getFillDate)
                .min(LocalDateTime::compareTo)
                .get();

        List<Execution> executionsToAnalyzeAgain = executionRepository.findByConidAndFillDateGreaterThanEqualOrderByFillDateAsc(conid, cutoffDate);
        List<Trade> regeneratedTrades = generateTradesSingleConid(executionsToAnalyzeAgain);

        saveRegeneratedTrades(regeneratedTrades);
    }

    private void saveRegeneratedTrades(List<Trade> trades) {
        log.info("saving " + trades.size() + " regenerated trades");

        if (!trades.isEmpty()) {
            tradeRepository.saveAll(trades);
        }
        messageService.sendWsReloadRequestMessage(WsTopic.EXECUTION);
        messageService.sendWsReloadRequestMessage(WsTopic.TRADE);
    }

    public void closeTrade(Trade trade, LocalDateTime closeDate, BigDecimal closePrice) {
        newExecution(new Execution()
                .setComment(trade.getSecType() == Types.SecType.OPT && closePrice.compareTo(BigDecimal.ZERO) == 0 ? "EXPIRE" : "CLOSE")
                .setOrigin("INTERNAL")
                .setReferenceId("N/A")
                .setAction(trade.getType() == TradeType.LONG ? Types.Action.SELL : Types.Action.BUY)
                .setQuantity(Math.abs(trade.getOpenPosition()))
                .setSymbol(trade.getSymbol())
                .setUnderlying(trade.getUnderlying())
                .setCurrency(trade.getCurrency())
                .setSecType(trade.getSecType())
                .setFillDate(closeDate)
                .setFillPrice(closePrice));

        messageService.sendWsReloadRequestMessage(WsTopic.EXECUTION);
        messageService.sendWsReloadRequestMessage(WsTopic.TRADE);
    }
    
    private List<Trade> generateTrades(List<Execution> executions) {
        List<Trade> trades = new ArrayList<>();
        Set<Integer> conids = executions.stream().map(Execution::getConid).collect(Collectors.toSet());
        Map<Integer, List<Execution>> executionsPerConidMap = new HashMap<>(); // conid -> list of executions

        conids.forEach(conid -> executionsPerConidMap.put(conid, new ArrayList<>()));
        executions.forEach(execution -> executionsPerConidMap.get(execution.getConid()).add(execution));
        conids.forEach(conid -> trades.addAll(generateTradesSingleConid(executionsPerConidMap.get(conid))));

        return trades;
    }
    
    private List<Trade> generateTradesSingleConid(List<Execution> executions) {
        List<Trade> trades = new ArrayList<>();

        int currentPos = 0;
        Set<Execution> singleConidSet = new LinkedHashSet<>(executions);

        while (!singleConidSet.isEmpty()) {
            List<Execution> tradeExecutions = new ArrayList<>();
            Trade trade = new Trade();

            for (Execution execution : singleConidSet) {
                tradeExecutions.add(execution);

                currentPos += (execution.getAction() == Types.Action.BUY ? execution.getQuantity() : -execution.getQuantity());
                if (currentPos== 0) {
                    break;
                }
            }
            trade.setExecutionIds(tradeExecutions.stream()
                    .map(Execution::getId)
                    .collect(Collectors.toList()));

            tradeCalculatorService.calculateFields(trade);
            trades.add(trade);
            singleConidSet.removeAll(tradeExecutions);
        }
        return trades;
    }

    private void logTradesAffected(Execution execution, List<Trade> tradesAffected) {
        StringBuilder sb = new StringBuilder();

        sb.append("trades affected by execution: ").append(execution).append("\n");
        tradesAffected.forEach(trade -> sb.append(trade).append("\n"));

        log.info(sb.toString());
    }
}
