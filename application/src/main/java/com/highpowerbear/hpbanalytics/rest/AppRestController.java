package com.highpowerbear.hpbanalytics.rest;

import com.highpowerbear.hpbanalytics.config.WsTopic;
import com.highpowerbear.hpbanalytics.database.*;
import com.highpowerbear.hpbanalytics.enums.*;
import com.highpowerbear.hpbanalytics.model.DataFilter;
import com.highpowerbear.hpbanalytics.model.Statistics;
import com.highpowerbear.hpbanalytics.rest.model.CloseTradeRequest;
import com.highpowerbear.hpbanalytics.rest.model.GenericList;
import com.highpowerbear.hpbanalytics.service.IfiCsvGeneratorService;
import com.highpowerbear.hpbanalytics.service.MessageService;
import com.highpowerbear.hpbanalytics.service.ReportService;
import com.highpowerbear.hpbanalytics.service.StatisticsCalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private final ReportService reportService;
    private final IfiCsvGeneratorService ifiCsvGeneratorService;
    private final MessageService messageService;

    @Autowired
    public AppRestController(ExecutionRepository executionRepository,
                             TradeRepository tradeRepository,
                             StatisticsCalculatorService statisticsCalculatorService,
                             ReportService reportService,
                             IfiCsvGeneratorService ifiCsvGeneratorService,
                             MessageService messageService) {

        this.executionRepository = executionRepository;
        this.tradeRepository = tradeRepository;
        this.statisticsCalculatorService = statisticsCalculatorService;
        this.reportService = reportService;
        this.ifiCsvGeneratorService = ifiCsvGeneratorService;
        this.messageService = messageService;
    }

    @RequestMapping("/ifiyears")
    public ResponseEntity<?> getIfiYears() {
        return ResponseEntity.ok(ifiCsvGeneratorService.getIfiYears());
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/reports/{id}")
    public ResponseEntity<?> analyzeReport(
            @PathVariable("id") int reportId) {

        reportService.analyzeAll(reportId);
        messageService.sendWsMessage(WsTopic.TRADE, "report " + reportId + " analyzed");

        return ResponseEntity.ok().build();
    }

    @RequestMapping("/reports/{id}/executions")
    public ResponseEntity<?> getFilteredExecutions(
            @PathVariable("id") int reportId,
            @RequestParam("page") int page,
            @RequestParam("limit") int limit,
            @RequestParam(required = false, value = "filter") DataFilter filter) {

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "fillDate"));
        Specification<Execution> specification = DataFilters.executionFilterSpecification(filter);

        List<Execution> executions = executionRepository.findAll(specification, pageable).getContent();
        long numExecutions = executionRepository.count(specification);

        return ResponseEntity.ok(new GenericList<>(executions, (int) numExecutions));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/reports/{id}/executions")
    public ResponseEntity<?> createExecution(
            @PathVariable("id") int reportId,
            @RequestBody Execution execution) {

        execution
                .setId(null)
                .setReportId(reportId);

        reportService.newExecution(execution);
        messageService.sendWsMessage(WsTopic.EXECUTION, "new execution processed");
        messageService.sendWsMessage(WsTopic.TRADE, "new execution processed");

        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/reports/{id}/executions/{executionId}")
    public ResponseEntity<?> deleteExecution(
            @PathVariable("id") int reportId,
            @PathVariable("executionId") long executionId) {

        Execution execution = executionRepository.findById(executionId).orElse(null);
        if (execution == null || reportId != execution.getReportId()) {
            return ResponseEntity.notFound().build();
        }
        reportService.deleteExecution(executionId);
        messageService.sendWsMessage(WsTopic.EXECUTION, "execution " + executionId + " deleted");
        messageService.sendWsMessage(WsTopic.TRADE, "execution " + executionId + " deleted");

        return ResponseEntity.ok().build();
    }

    @RequestMapping("/reports/{id}/trades")
    public ResponseEntity<?> getFilteredTrades(
            @PathVariable("id") int reportId,
            @RequestParam("page") int page,
            @RequestParam("limit") int limit,
            @RequestParam(required = false, value = "filter") DataFilter filter) {

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "openDate"));
        Specification<Trade> specification = DataFilters.tradeFilterSpecification(filter);

        List<Trade> trades = tradeRepository.findAll(specification, pageable).getContent();
        long numTrades = tradeRepository.count(specification);

        return ResponseEntity.ok(new GenericList<>(trades, (int) numTrades));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/trades/{tradeId}/close")
    public ResponseEntity<?> closeTrade(
            @PathVariable("tradeId") long tradeId,
            @RequestBody CloseTradeRequest r) {

        Trade trade = tradeRepository.findById(tradeId).orElse(null);

        if (trade == null) {
            return ResponseEntity.notFound().build();

        } else if (!TradeStatus.OPEN.equals(trade.getStatus())) {
            return ResponseEntity.badRequest().build();
        }

        reportService.closeTrade(trade, r.getCloseDate(), r.getClosePrice());
        messageService.sendWsMessage(WsTopic.EXECUTION, "trade " + tradeId + " closed");
        messageService.sendWsMessage(WsTopic.TRADE, "trade " + tradeId + " closed");

        return ResponseEntity.ok().build();
    }

    @RequestMapping("/reports/{id}/statistics/{interval}")
    public ResponseEntity<?> getStatistics(
            @PathVariable("id") int reportId,
            @PathVariable("interval") StatisticsInterval interval,
            @RequestParam(required = false, value = "tradeType") TradeType tradeType,
            @RequestParam(required = false, value = "secType") SecType secType,
            @RequestParam(required = false, value = "currency") Currency currency,
            @RequestParam(required = false, value = "underlying") String underlying,
            @RequestParam("start") int start,
            @RequestParam("limit") int limit) {

        List<Statistics> statistics = statisticsCalculatorService.getStatistics(interval, reportId, tradeType, secType, currency, underlying, null);
        Collections.reverse(statistics);
        List<Statistics> statisticsPage = new ArrayList<>();

        for (int i = 0; i < statistics.size(); i++) {
            if (i >= start && i < (start + limit)) {
                statisticsPage.add(statistics.get(i));
            }
        }
        return ResponseEntity.ok(new GenericList<>(statisticsPage, statistics.size()));
    }

    @RequestMapping("reports/{id}/charts/{interval}")
    public ResponseEntity<?> getCharts(
            @PathVariable("id") int reportId,
            @PathVariable("interval") StatisticsInterval interval,
            @RequestParam(required = false, value = "tradeType") TradeType tradeType,
            @RequestParam(required = false, value = "secType") SecType secType,
            @RequestParam(required = false, value = "currency") Currency currency,
            @RequestParam(required = false, value = "underlying") String underlying) {

        List<Statistics> statistics = statisticsCalculatorService.getStatistics(interval, reportId, tradeType, secType, currency, underlying, 120);

        return ResponseEntity.ok(new GenericList<>(statistics, statistics.size()));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/reports/{id}/statistics/{interval}")
    public ResponseEntity<?> calculateStatistics(
            @PathVariable("id") int reportId,
            @PathVariable("interval") StatisticsInterval interval,
            @RequestParam(required = false, value = "tradeType") TradeType tradeType,
            @RequestParam(required = false, value = "secType") SecType secType,
            @RequestParam(required = false, value = "currency") Currency currency,
            @RequestParam(required = false, value = "underlying") String underlying) {

        statisticsCalculatorService.calculateStatistics(interval, reportId, tradeType, secType, currency, underlying);

        return ResponseEntity.ok().build();
    }

    @RequestMapping("/reports/{id}/underlyings")
    public ResponseEntity<?> getUnderlyings(
            @PathVariable("id") int reportId,
            @RequestParam(required = false, value = "openOnly") boolean openOnly) {

        List<String> underlyings;
        if (openOnly) {
            underlyings = tradeRepository.getOpenUnderlyings(reportId);
        } else {
            underlyings = tradeRepository.getAllUnderlyings(reportId);
        }
        return ResponseEntity.ok(underlyings);
    }

    @RequestMapping("/reports/{id}/ificsv/{year}/{endMonth}/{tradeType}")
    public ResponseEntity<?> getIfiCsv(
            @PathVariable("id") int reportId,
            @PathVariable("year") int year,
            @PathVariable("endMonth") int endMonth,
            @PathVariable("tradeType") TradeType tradeType) {

        return ResponseEntity.ok(ifiCsvGeneratorService.generate(reportId, year, endMonth, tradeType));
    }
}
