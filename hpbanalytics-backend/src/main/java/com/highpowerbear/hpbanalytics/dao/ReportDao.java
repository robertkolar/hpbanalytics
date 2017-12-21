package com.highpowerbear.hpbanalytics.dao;

import com.highpowerbear.hpbanalytics.dao.filter.ExecutionFilter;
import com.highpowerbear.hpbanalytics.dao.filter.TradeFilter;
import com.highpowerbear.hpbanalytics.entity.ExchangeRate;
import com.highpowerbear.hpbanalytics.entity.Execution;
import com.highpowerbear.hpbanalytics.entity.Report;
import com.highpowerbear.hpbanalytics.entity.Trade;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.enums.TradeType;

import java.util.Calendar;
import java.util.List;

/**
 * Created by robertk on 12/16/2017.
 */
public interface ReportDao {

    Report getReportByOriginAndSecType(String origin, SecType secType);
    Report findReport(Integer id);
    Report updateReport(Report report);
    void deleteReport(Report report);
    List<Report> getReports();

    Calendar getFirstExecutionDate(Report report);
    Calendar getLastExecutionDate(Report report);
    List<Execution> getExecutions(Report report);
    Long getNumExecutions(Report report);
    List<Execution> getExecutionsAfterExecution(Execution e);
    List<Execution> getExecutionsAfterExecutionInclusive(Execution e);
    boolean existsExecution(Execution e);
    Execution findExecution(Long id);
    void createExecution(Execution execution);
    void deleteExecution(Execution execution);
    List<Execution> getFilteredExecutions(Report report, ExecutionFilter filter, Integer start, Integer limit);
    Long getNumFilteredExecutions(Report report, ExecutionFilter filter);

    Long getNumTrades(Report report);
    Long getNumOpenTrades(Report report);
    List<Trade> getTradesByUnderlying(Report report, String underlying);
    List<Trade> getTradesAffectedByExecution(Execution e);
    void createTrades(List<Trade> trades);
    void deleteAllTrades(Report report);
    void deleteTrades(List<Trade> trades);
    Trade findTrade(Long id);
    List<Trade> getFilteredTrades(Report report, TradeFilter filter, Integer start, Integer limit);
    Long getNumFilteredTrades(Report report, TradeFilter filter);
    List<Trade> getTradesBetweenDates(Report report, Calendar beginDate, Calendar endDate, TradeType tradeType);

    List<String> getUnderlyings(Report report);
    Long getNumUnderlyings(Report report);
    Long getNumOpenUnderlyings(Report report);

    ExchangeRate getExchangeRate(String date);
    void createOrUpdateExchangeRate(ExchangeRate exchangeRate);
}
