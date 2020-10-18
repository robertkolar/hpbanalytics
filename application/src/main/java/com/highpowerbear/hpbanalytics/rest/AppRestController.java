package com.highpowerbear.hpbanalytics.rest;

import com.highpowerbear.hpbanalytics.common.HanUtil;
import com.highpowerbear.hpbanalytics.database.*;
import com.highpowerbear.hpbanalytics.enums.TradeStatus;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import com.highpowerbear.hpbanalytics.model.DataFilterItem;
import com.highpowerbear.hpbanalytics.model.Statistics;
import com.highpowerbear.hpbanalytics.rest.model.CalculateStatisticsRequest;
import com.highpowerbear.hpbanalytics.rest.model.CloseTradeRequest;
import com.highpowerbear.hpbanalytics.rest.model.GenericList;
import com.highpowerbear.hpbanalytics.service.AnalyticsService;
import com.highpowerbear.hpbanalytics.service.IfiCsvGeneratorService;
import com.highpowerbear.hpbanalytics.service.StatisticsCalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by robertk on 12/21/2017.
 */
@RestController
@RequestMapping("/")
public class AppRestController {

    private final ExecutionRepository executionRepository;
    private final TradeRepository tradeRepository;
    private final StatisticsCalculatorService statisticsCalculatorService;
    private final AnalyticsService analyticsService;
    private final IfiCsvGeneratorService ifiCsvGeneratorService;

    @Autowired
    public AppRestController(ExecutionRepository executionRepository,
                             TradeRepository tradeRepository,
                             StatisticsCalculatorService statisticsCalculatorService,
                             AnalyticsService analyticsService,
                             IfiCsvGeneratorService ifiCsvGeneratorService) {

        this.executionRepository = executionRepository;
        this.tradeRepository = tradeRepository;
        this.statisticsCalculatorService = statisticsCalculatorService;
        this.analyticsService = analyticsService;
        this.ifiCsvGeneratorService = ifiCsvGeneratorService;
    }

    @RequestMapping("execution")
    public ResponseEntity<?> getFilteredExecutions(
            @RequestParam("page") int page,
            @RequestParam("limit") int limit,
            @RequestParam(required = false, value = "filter") String jsonFilter) {

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "fillDate"));

        List<Execution> executions;
        long numExecutions;
        List<DataFilterItem> dataFilterItems = HanUtil.mapDataFilterFromJson(jsonFilter);

        if (dataFilterItems != null) {
            Specification<Execution> specification = DataFilters.executionFilterSpecification(dataFilterItems);
            executions = executionRepository.findAll(specification, pageable).getContent();
            numExecutions = executionRepository.count(specification);

        } else {
            executions = executionRepository.findAll(pageable).getContent();
            numExecutions = executionRepository.count();
        }
        return ResponseEntity.ok(new GenericList<>(executions, (int) numExecutions));
    }

    @RequestMapping(method = RequestMethod.POST, value = "execution")
    public ResponseEntity<?> createExecution(
            @RequestBody Execution execution) {

        execution.setId(null);
        analyticsService.newExecution(execution);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "execution/{executionId}")
    public ResponseEntity<?> deleteExecution(
            @PathVariable("executionId") long executionId) {

        Execution execution = executionRepository.findById(executionId).orElse(null);
        if (execution == null ) {
            return ResponseEntity.notFound().build();
        }

        analyticsService.deleteExecution(executionId);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.POST, value = "trade/regenerate-all")
    public ResponseEntity<?> regenerateAllTrades() {

        analyticsService.regenerateAllTrades();
        return ResponseEntity.ok().build();
    }

    @RequestMapping("trade")
    public ResponseEntity<?> getFilteredTrades(
            @RequestParam("page") int page,
            @RequestParam("limit") int limit,
            @RequestParam(required = false, value = "filter") String jsonFilter) {

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "openDate"));
        List<Trade> trades;
        long numTrades;
        List<DataFilterItem> dataFilterItems = HanUtil.mapDataFilterFromJson(jsonFilter);

        if (dataFilterItems != null) {
            Specification<Trade> specification = DataFilters.tradeFilterSpecification(dataFilterItems);
            trades = tradeRepository.findAll(specification, pageable).getContent();
            numTrades = tradeRepository.count(specification);

        } else {
            trades = tradeRepository.findAll(pageable).getContent();
            numTrades = tradeRepository.count();
        }
        return ResponseEntity.ok(new GenericList<>(trades, (int) numTrades));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "trade/{tradeId}/close")
    public ResponseEntity<?> manualCloseTrade(
            @PathVariable("tradeId") long tradeId,
            @RequestBody CloseTradeRequest r) {

        Trade trade = tradeRepository.findById(tradeId).orElse(null);

        if (trade == null) {
            return ResponseEntity.notFound().build();

        } else if (!TradeStatus.OPEN.equals(trade.getStatus())) {
            return ResponseEntity.badRequest().build();
        }

        analyticsService.manualCloseTrade(trade, r.getExecutionReference(), r.getCloseDate(), r.getClosePrice());
        return ResponseEntity.ok().build();
    }

    @RequestMapping("statistics")
    public ResponseEntity<?> getStatistics(
            @RequestParam("interval") ChronoUnit interval,
            @RequestParam(required = false, value = "tradeType") String tradeType,
            @RequestParam(required = false, value = "secType") String secType,
            @RequestParam(required = false, value = "currency") String currency,
            @RequestParam(required = false, value = "underlying") String underlying,
            @RequestParam("start") int start,
            @RequestParam("limit") int limit) {

        List<Statistics> statistics = statisticsCalculatorService.getStatistics(interval, tradeType, secType, currency, underlying, null);
        Collections.reverse(statistics);
        List<Statistics> statisticsPage = new ArrayList<>();

        for (int i = 0; i < statistics.size(); i++) {
            if (i >= start && i < (start + limit)) {
                statisticsPage.add(statistics.get(i));
            }
        }
        return ResponseEntity.ok(new GenericList<>(statisticsPage, statistics.size()));
    }

    @RequestMapping("statistics/underlyings")
    public ResponseEntity<?> getUnderlyings(
            @RequestParam(required = false, value = "openOnly") boolean openOnly) {

        List<String> underlyings;
        if (openOnly) {
            underlyings = tradeRepository.findOpenUnderlyings();
        } else {
            underlyings = tradeRepository.findAllUnderlyings();
        }
        return ResponseEntity.ok(underlyings);
    }

    @RequestMapping(method = RequestMethod.POST, value = "statistics")
    public ResponseEntity<?> calculateStatistics(@RequestBody CalculateStatisticsRequest r) {

        statisticsCalculatorService.calculateStatistics(r.getInterval(), r.getTradeType(), r.getSecType(), r.getCurrency(), r.getUnderlying());
        return ResponseEntity.ok().build();
    }

    @RequestMapping("statistics/charts")
    public ResponseEntity<?> getCharts(
            @RequestParam("interval") ChronoUnit interval,
            @RequestParam(required = false, value = "tradeType") String tradeType,
            @RequestParam(required = false, value = "secType") String secType,
            @RequestParam(required = false, value = "currency") String currency,
            @RequestParam(required = false, value = "underlying") String underlying) {

        List<Statistics> statistics = statisticsCalculatorService.getStatistics(interval, tradeType, secType, currency, underlying, 120);

        return ResponseEntity.ok(new GenericList<>(statistics, statistics.size()));
    }

    @RequestMapping("statistics/ifi/years")
    public ResponseEntity<?> getIfiYears() {
        return ResponseEntity.ok(ifiCsvGeneratorService.getIfiYears());
    }

    @RequestMapping("statistics/ifi/csv")
    public ResponseEntity<?> getIfiCsv(
            @RequestParam("year") int year,
            @RequestParam("endMonth") int endMonth,
            @RequestParam("tradeType") TradeType tradeType) {

        return ResponseEntity.ok(ifiCsvGeneratorService.generate(year, endMonth, tradeType));
    }
}
