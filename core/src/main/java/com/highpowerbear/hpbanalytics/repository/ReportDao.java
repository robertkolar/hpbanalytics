package com.highpowerbear.hpbanalytics.repository;

import com.highpowerbear.hpbanalytics.database.Execution;
import com.highpowerbear.hpbanalytics.database.Trade;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import com.highpowerbear.hpbanalytics.repository.filter.ExecutionFilter;
import com.highpowerbear.hpbanalytics.repository.filter.TradeFilter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by robertk on 12/16/2017.
 */
public interface ReportDao {

    List<Execution> getExecutions(int reportId);
    List<Execution> getExecutionsAfterDate(int reportId, LocalDateTime date, String symbol);
    List<Execution> getExecutionsAfterDateInclusive(int reportId, LocalDateTime date, String symbol);
    boolean existsExecution(long executionId);
    Execution findExecution(long executionId);
    void createExecution(Execution execution);
    void deleteExecution(long executionId);
    List<Execution> getFilteredExecutions(int reportId, ExecutionFilter filter, int start, int limit);
    long getNumFilteredExecutions(int reportId, ExecutionFilter filter);

    List<Trade> getTrades(int reportId, String tradeType, String secType, String currency, String underlying);
    List<Trade> getTradesAffectedByExecution(int reportId, LocalDateTime fillDate, String symbol);
    void createTrades(List<Trade> trades);
    void deleteAllTrades(int reportId);
    void deleteTrades(List<Trade> trades);
    Trade findTrade(long tradeId);
    List<Trade> getFilteredTrades(int reportId, TradeFilter filter, int start, int limit);
    long getNumFilteredTrades(int reportId, TradeFilter filter);
    List<Trade> getTradesBetweenDates(int reportId, LocalDateTime beginDate, LocalDateTime endDate, TradeType tradeType);

    List<String> getUnderlyings(int reportId, boolean openOnly);
}
