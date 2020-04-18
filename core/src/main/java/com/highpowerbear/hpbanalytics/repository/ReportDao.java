package com.highpowerbear.hpbanalytics.repository;

import com.highpowerbear.hpbanalytics.database.Execution;
import com.highpowerbear.hpbanalytics.database.Trade;
import com.highpowerbear.hpbanalytics.repository.filter.ExecutionFilter;
import com.highpowerbear.hpbanalytics.repository.filter.TradeFilter;

import java.util.List;

/**
 * Created by robertk on 12/16/2017.
 */
public interface ReportDao {

    List<Execution> getFilteredExecutions(int reportId, ExecutionFilter filter, int start, int limit);
    long getNumFilteredExecutions(int reportId, ExecutionFilter filter);

    List<Trade> getFilteredTrades(int reportId, TradeFilter filter, int start, int limit);
    long getNumFilteredTrades(int reportId, TradeFilter filter);
}
