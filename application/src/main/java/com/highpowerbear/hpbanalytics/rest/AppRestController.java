package com.highpowerbear.hpbanalytics.rest;

import com.highpowerbear.hpbanalytics.config.WsTopic;
import com.highpowerbear.hpbanalytics.service.MessageService;
import com.highpowerbear.hpbanalytics.rest.model.CloseTradeRequest;
import com.highpowerbear.hpbanalytics.repository.ReportDao;
import com.highpowerbear.hpbanalytics.repository.filter.ExecutionFilter;
import com.highpowerbear.hpbanalytics.repository.filter.FilterParser;
import com.highpowerbear.hpbanalytics.repository.filter.TradeFilter;
import com.highpowerbear.hpbanalytics.database.Execution;
import com.highpowerbear.hpbanalytics.database.Trade;
import com.highpowerbear.hpbanalytics.enums.StatisticsInterval;
import com.highpowerbear.hpbanalytics.enums.TradeStatus;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import com.highpowerbear.hpbanalytics.service.IfiCsvGeneratorService;
import com.highpowerbear.hpbanalytics.service.ReportService;
import com.highpowerbear.hpbanalytics.service.StatisticsCalculatorService;
import com.highpowerbear.hpbanalytics.model.Statistics;
import com.highpowerbear.hpbanalytics.rest.model.GenericList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by robertk on 12/21/2017.
 */
@RestController
@RequestMapping("/")
public class AppRestController {

    private final ReportDao reportDao;
    private final StatisticsCalculatorService statisticsCalculatorService;
    private final ReportService reportService;
    private final FilterParser filterParser;
    private final IfiCsvGeneratorService ifiCsvGeneratorService;
    private final MessageService messageService;

    @Autowired
    public AppRestController(ReportDao reportDao,
                             StatisticsCalculatorService statisticsCalculatorService,
                             ReportService reportService,
                             FilterParser filterParser,
                             IfiCsvGeneratorService ifiCsvGeneratorService,
                             MessageService messageService) {

        this.reportDao = reportDao;
        this.statisticsCalculatorService = statisticsCalculatorService;
        this.reportService = reportService;
        this.filterParser = filterParser;
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
            @RequestParam(required = false, value = "filter") String jsonFilter,
            @RequestParam("start") int start,
            @RequestParam("limit") int limit) {

        ExecutionFilter filter = filterParser.parseExecutionFilter(jsonFilter);
        List<Execution> executions = reportDao.getFilteredExecutions(reportId, filter, start, limit);
        long numExecutions = reportDao.getNumFilteredExecutions(reportId, filter);

        return ResponseEntity.ok(new GenericList<>(executions, (int) numExecutions));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/reports/{id}/executions")
    public ResponseEntity<?> createExecution(
            @PathVariable("id") int reportId,
            @RequestBody Execution execution) {

        execution.setId(null);
        execution.setReportId(reportId);

        reportService.newExecution(execution);
        messageService.sendWsMessage(WsTopic.EXECUTION, "new execution processed");
        messageService.sendWsMessage(WsTopic.TRADE, "new execution processed");

        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/reports/{id}/executions/{executionId}")
    public ResponseEntity<?> deleteExecution(
            @PathVariable("id") int reportId,
            @PathVariable("executionId") long executionId) {

        Execution execution = reportDao.findExecution(executionId);
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
            @RequestParam(required = false, value = "filter") String jsonFilter,
            @RequestParam("start") int start,
            @RequestParam("limit") int limit) {

        TradeFilter filter = filterParser.parseTradeFilter(jsonFilter);
        List<Trade> trades = reportDao.getFilteredTrades(reportId, filter, start, limit);
        long numTrades = reportDao.getNumFilteredTrades(reportId, filter);

        return ResponseEntity.ok(new GenericList<>(trades, (int) numTrades));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/trades/{tradeId}/close")
    public ResponseEntity<?> closeTrade(
            @PathVariable("tradeId") long tradeId,
            @RequestBody CloseTradeRequest r) {

        Trade trade = reportDao.findTrade(tradeId);

        if (trade == null) {
            return ResponseEntity.notFound().build();
        }

        if (!TradeStatus.OPEN.equals(trade.getStatus())) {
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
            @RequestParam(required = false, value = "tradeType") String tradeType,
            @RequestParam(required = false, value = "secType") String secType,
            @RequestParam(required = false, value = "currency") String currency,
            @RequestParam(required = false, value = "underlying") String underlying,
            @RequestParam("start") int start,
            @RequestParam("limit") int limit) {

        List<Statistics> statistics = statisticsCalculatorService.getStatistics(reportId, interval, tradeType, secType, currency, underlying, null);
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
            @RequestParam(required = false, value = "tradeType") String tradeType,
            @RequestParam(required = false, value = "secType") String secType,
            @RequestParam(required = false, value = "currency") String currency,
            @RequestParam(required = false, value = "underlying") String underlying) {

        List<Statistics> statistics = statisticsCalculatorService.getStatistics(reportId, interval, tradeType, secType, currency, underlying, 120);

        return ResponseEntity.ok(new GenericList<>(statistics, statistics.size()));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/reports/{id}/statistics/{interval}")
    public ResponseEntity<?> calculateStatistics(
            @PathVariable("id") int reportId,
            @PathVariable("interval") StatisticsInterval interval,
            @RequestParam(required = false, value = "tradeType") String tradeType,
            @RequestParam(required = false, value = "secType") String secType,
            @RequestParam(required = false, value = "currency") String currency,
            @RequestParam(required = false, value = "underlying") String underlying) {

        statisticsCalculatorService.calculateStatistics(reportId, interval, tradeType, secType, currency, underlying);

        return ResponseEntity.ok().build();
    }

    @RequestMapping("/reports/{id}/underlyings")
    public ResponseEntity<?> getUnderlyings(
            @PathVariable("id") int reportId,
            @RequestParam(required = false, value = "openOnly") String openOnly) {

        return ResponseEntity.ok(reportDao.getUnderlyings(reportId, Boolean.parseBoolean(openOnly)));
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
