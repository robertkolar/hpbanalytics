package com.highpowerbear.hpbanalytics.service;

import com.highpowerbear.dto.ExecutionDTO;
import com.highpowerbear.hpbanalytics.common.ExecutionMapper;
import com.highpowerbear.hpbanalytics.common.HanUtil;
import com.highpowerbear.hpbanalytics.config.WsTopic;
import com.highpowerbear.hpbanalytics.database.Execution;
import com.highpowerbear.hpbanalytics.database.ExecutionRepository;
import com.highpowerbear.hpbanalytics.database.Trade;
import com.highpowerbear.hpbanalytics.database.TradeRepository;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import com.ib.client.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        Execution execution = executionMapper.dtoToEntity(dto);
        execution.setSymbol(HanUtil.removeWhiteSpaces(execution.getSymbol()));

        newExecution(execution);
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
        Execution execution = executionRepository.findById(executionId).orElse(null);
        if (execution == null) {
            log.warn("execution " + executionId + " does not exists, cannot delete");
            return;
        }
        String symbol = execution.getSymbol();
        Currency currency = execution.getCurrency();
        double multiplier = execution.getMultiplier();

        List<Trade> tradesAffected = tradeRepository.findTradesAffectedByExecution(symbol, currency, multiplier, execution.getFillDate());
        logTradesAffected(execution, tradesAffected);

        Trade firstTradeAffected = tradesAffected.get(0);
        Long firstExecutionId = firstTradeAffected.getExecutions().get(0).getId();
        Execution firstExecution = Objects.requireNonNull(executionRepository.findById(firstExecutionId).orElse(null));

        log.info("deleting " + tradesAffected.size() + " trades");
        tradeRepository.deleteAll(tradesAffected);

        log.info("deleting execution " + execution);
        executionRepository.deleteById(executionId); // need to delete before the next query

        List<Execution> executionsToAnalyzeAgain = executionRepository.findBySymbolAndCurrencyAndMultiplierAndFillDateGreaterThanEqualOrderByFillDateAsc(symbol, currency, multiplier, firstExecution.getFillDate());
        List<Trade> regeneratedTrades = generateTradesSingleCid(executionsToAnalyzeAgain);

        saveRegeneratedTrades(regeneratedTrades);
    }

    @Transactional
    public void newExecution(Execution execution) {
        adjustFillDate(execution);

        String symbol = execution.getSymbol();
        Currency currency = execution.getCurrency();
        double multiplier = execution.getMultiplier();

        List<Trade> tradesAffected = tradeRepository.findTradesAffectedByExecution(symbol, currency, multiplier, execution.getFillDate());
        logTradesAffected(execution, tradesAffected);

        log.info("deleting " + tradesAffected.size() + " trades");
        tradeRepository.deleteAll(tradesAffected);

        log.info("saving new execution " + execution);
        execution = executionRepository.save(execution); // need to save before the next query

        if (tradesAffected.isEmpty()) {
            saveRegeneratedTrades(generateTradesSingleCid(Collections.singletonList(execution)));
            return;
        }

        Trade firstTradeAffected = tradesAffected.get(0);
        Long firstExecutionId = firstTradeAffected.getExecutions().get(0).getId();
        Execution firstExecution = Objects.requireNonNull(executionRepository.findById(firstExecutionId).orElse(null));

        LocalDateTime cutoffDate = Stream.of(firstExecution, execution)
                .map(Execution::getFillDate)
                .min(LocalDateTime::compareTo)
                .get();

        List<Execution> executionsToAnalyzeAgain = executionRepository.findBySymbolAndCurrencyAndMultiplierAndFillDateGreaterThanEqualOrderByFillDateAsc(symbol, currency, multiplier, cutoffDate);
        List<Trade> regeneratedTrades = generateTradesSingleCid(executionsToAnalyzeAgain);

        saveRegeneratedTrades(regeneratedTrades);
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
            tradeRepository.saveAll(trades);
            trades.forEach(trade -> executionRepository.saveAll(trade.getExecutions()));
        }
        messageService.sendWsReloadRequestMessage(WsTopic.EXECUTION);
        messageService.sendWsReloadRequestMessage(WsTopic.TRADE);
    }

    public void manualCloseTrade(Trade trade, String executionReference, LocalDateTime closeDate, BigDecimal closePrice) {
        log.info("manually closing trade " + trade);
        newExecution(new Execution()
                .setReference(executionReference)
                .setAction(trade.getType() == TradeType.LONG ? Types.Action.SELL : Types.Action.BUY)
                .setQuantity(Math.abs(trade.getOpenPosition()))
                .setSymbol(trade.getSymbol())
                .setUnderlying(trade.getUnderlying())
                .setCurrency(trade.getCurrency())
                .setSecType(trade.getSecType())
                .setMultiplier(trade.getMultiplier())
                .setFillDate(closeDate)
                .setFillPrice(closePrice));

        messageService.sendWsReloadRequestMessage(WsTopic.EXECUTION);
        messageService.sendWsReloadRequestMessage(WsTopic.TRADE);
    }
    
    private List<Trade> generateTrades(List<Execution> executions) {
        List<Trade> trades = new ArrayList<>();
        Set<String> cids = executions.stream().map(Execution::getContractIdentifier).collect(Collectors.toSet());
        Map<String, List<Execution>> executionsPerCidMap = new HashMap<>(); // contract identifier -> list of executions

        cids.forEach(cid -> executionsPerCidMap.put(cid, new ArrayList<>()));

        for (Execution execution : executions) {
            String cid = execution.getContractIdentifier();
            executionsPerCidMap.get(cid).add(execution);
        }

        for (String cid : cids) {
            List<Execution> executionsPerCid = executionsPerCidMap.get(cid);

            log.info("generating trades for " + cid);
            List<Trade> generatedTradesPerCid = generateTradesSingleCid(executionsPerCid);

            trades.addAll(generatedTradesPerCid);
        }

        return trades;
    }
    
    private List<Trade> generateTradesSingleCid(List<Execution> executions) {
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

            tradeCalculatorService.calculateFields(trade);
            log.info("generated trade " + trade);
            trades.add(trade);
            singleContractSet.removeAll(trade.getExecutions());
        }
        return trades;
    }

    private void logTradesAffected(Execution execution, List<Trade> tradesAffected) {
        StringBuilder sb = new StringBuilder();

        if (tradesAffected.isEmpty()) {
            sb.append("no ");
        }
        sb.append("trades affected by execution: ").append(execution).append("\n");
        if (!tradesAffected.isEmpty()) {
            tradesAffected.forEach(trade -> sb.append(trade).append("\n"));
        }
        log.info(sb.toString());
    }

    private boolean detectReversal(int oldPos, int currentPos) {
        return oldPos > 0 && currentPos < 0 || oldPos < 0 && currentPos > 0;
    }
}
