package com.highpowerbear.hpbanalytics.dao;

import com.highpowerbear.hpbanalytics.dao.filter.ExecutionFilter;
import com.highpowerbear.hpbanalytics.dao.filter.TradeFilter;
import com.highpowerbear.hpbanalytics.entity.ExchangeRate;
import com.highpowerbear.hpbanalytics.entity.Execution;
import com.highpowerbear.hpbanalytics.entity.IbOrder;
import com.highpowerbear.hpbanalytics.entity.Report;
import com.highpowerbear.hpbanalytics.entity.Trade;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import com.highpowerbear.hpbanalytics.report.model.ReportInfo;

import java.util.Calendar;
import java.util.List;

/**
 * Created by robertk on 12/16/2017.
 */
public interface ReportDao {

    IbOrder findIbOrder(long ibOrderId);

    Report getReportByOriginAndSecType(String origin, SecType secType);
    Report findReport(int reportId);
    Report updateReport(Report report);
    void deleteReport(int reportId);
    List<Report> getReports();
    ReportInfo getReportInfo(int reportId);

    List<Execution> getExecutions(int reportId);
    List<Execution> getExecutionsAfterDate(int reportId, Calendar date, String symbol);
    List<Execution> getExecutionsAfterDateInclusive(int reportId, Calendar date, String symbol);
    boolean existsExecution(long executionId);
    Execution findExecution(long executionId);
    void createExecution(Execution execution);
    void deleteExecution(long executionId);
    List<Execution> getFilteredExecutions(int reportId, ExecutionFilter filter, int start, int limit);
    long getNumFilteredExecutions(int reportId, ExecutionFilter filter);

    List<Trade> getTradesByUnderlying(int reportId, String underlying);
    List<Trade> getTradesAffectedByExecution(int reportId, Calendar fillDate, String symbol);
    void createTrades(List<Trade> trades);
    void deleteAllTrades(int reportId);
    void deleteTrades(List<Trade> trades);
    Trade findTrade(long tradeId);
    List<Trade> getFilteredTrades(int reportId, TradeFilter filter, int start, int limit);
    long getNumFilteredTrades(int reportId, TradeFilter filter);
    List<Trade> getTradesBetweenDates(int reportId, Calendar beginDate, Calendar endDate, TradeType tradeType);

    List<String> getUnderlyings(int reportId);

    ExchangeRate getExchangeRate(String date);
    List<ExchangeRate> getAllExchangeRates();
    void createOrUpdateExchangeRate(ExchangeRate exchangeRate);
}
