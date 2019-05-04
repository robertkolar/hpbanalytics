package com.highpowerbear.hpbanalytics.rest;

import com.highpowerbear.hpbanalytics.common.MessageService;
import com.highpowerbear.hpbanalytics.rest.model.CloseTrade;
import com.highpowerbear.hpbanalytics.dao.ReportDao;
import com.highpowerbear.hpbanalytics.dao.filter.ExecutionFilter;
import com.highpowerbear.hpbanalytics.dao.filter.FilterParser;
import com.highpowerbear.hpbanalytics.dao.filter.TradeFilter;
import com.highpowerbear.hpbanalytics.entity.Execution;
import com.highpowerbear.hpbanalytics.entity.Report;
import com.highpowerbear.hpbanalytics.entity.Trade;
import com.highpowerbear.hpbanalytics.enums.StatisticsInterval;
import com.highpowerbear.hpbanalytics.enums.TradeStatus;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import com.highpowerbear.hpbanalytics.report.IfiCsvGenerator;
import com.highpowerbear.hpbanalytics.report.ReportService;
import com.highpowerbear.hpbanalytics.report.StatisticsCalculator;
import com.highpowerbear.hpbanalytics.report.model.Statistics;
import com.highpowerbear.hpbanalytics.rest.model.RestList;
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

import static com.highpowerbear.hpbanalytics.common.HanSettings.WS_TOPIC_REPORT;

/**
 * Created by robertk on 12/21/2017.
 */
@RestController
@RequestMapping("/report")
public class ReportRestController {

    private final ReportDao reportDao;
    private final StatisticsCalculator statisticsCalculator;
    private final ReportService reportService;
    private final FilterParser filterParser;
    private final IfiCsvGenerator ifiCsvGenerator;
    private final MessageService messageService;

    @Autowired
    public ReportRestController(ReportDao reportDao, StatisticsCalculator statisticsCalculator, ReportService reportService, FilterParser filterParser, IfiCsvGenerator ifiCsvGenerator, MessageService messageService) {
        this.reportDao = reportDao;
        this.statisticsCalculator = statisticsCalculator;
        this.reportService = reportService;
        this.filterParser = filterParser;
        this.ifiCsvGenerator = ifiCsvGenerator;
        this.messageService = messageService;
    }

    @RequestMapping("/ifiyears")
    public ResponseEntity<?> getIfiYears() {
        return ResponseEntity.ok(ifiCsvGenerator.getIfiYears());
    }

    @RequestMapping("/reports")
    public ResponseEntity<?> getReports() {
        List<Report> reports = reportDao.getReports();

        reports.forEach(report -> report.setReportInfo(reportDao.getReportInfo(report.getId())));

        return ResponseEntity.ok(reports);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/reports")
    public ResponseEntity<?> updateReport(
            @RequestBody Report report) {

        Report reportDb = reportDao.findReport(report.getId());
        if (reportDb == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reportDao.updateReport(report));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/reports/{id}")
    public ResponseEntity<?> analyzeReport(
            @PathVariable("id") int id) {

        Report report = reportDao.findReport(id);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        reportService.analyzeAll(id);
        messageService.sendWsMessage(WS_TOPIC_REPORT, "report " + id + " analyzed");

        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/reports/{id}")
    public ResponseEntity<?> deleteReport(
            @PathVariable("id") int id) {

        Report report = reportDao.findReport(id);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        reportService.deleteReport(id);
        messageService.sendWsMessage(WS_TOPIC_REPORT, "report " + id + " deleted");

        return ResponseEntity.ok().build();
    }

    @RequestMapping("/reports/{id}/executions")
    public ResponseEntity<?> getFilteredExecutions(
            @PathVariable("id") int id,
            @RequestParam(required = false, value = "filter") String jsonFilter,
            @RequestParam("start") int start,
            @RequestParam("limit") int limit) {

        Report report = reportDao.findReport(id);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        ExecutionFilter filter = filterParser.parseExecutionFilter(jsonFilter);
        List<Execution> executions = reportDao.getFilteredExecutions(id, filter, start, limit);
        long numExecutions = reportDao.getNumFilteredExecutions(id, filter);

        return ResponseEntity.ok(new RestList<>(executions, (int) numExecutions));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/reports/{id}/executions")
    public ResponseEntity<?> createExecution(
            @PathVariable("id") int reportId,
            @RequestBody Execution execution) {

        Report report = reportDao.findReport(reportId);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }
        execution.setId(null);
        execution.setReport(report);

        reportService.newExecution(execution);
        messageService.sendWsMessage(WS_TOPIC_REPORT, "new execution processed");

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
        messageService.sendWsMessage(WS_TOPIC_REPORT, "execution " + executionId + " deleted");

        return ResponseEntity.ok().build();
    }

    @RequestMapping("/reports/{id}/trades")
    public ResponseEntity<?> getFilteredTrades(
            @PathVariable("id") int id,
            @RequestParam(required = false, value = "filter") String jsonFilter,
            @RequestParam("start") int start,
            @RequestParam("limit") int limit) {

        Report report = reportDao.findReport(id);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        TradeFilter filter = filterParser.parseTradeFilter(jsonFilter);
        List<Trade> trades = reportDao.getFilteredTrades(id, filter, start, limit);
        long numTrades = reportDao.getNumFilteredTrades(id, filter);

        return ResponseEntity.ok(new RestList<>(trades, (int) numTrades));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/reports/{id}/trades/{tradeId}/close")
    public ResponseEntity<?> closeTrade(
            @PathVariable("id") int id,
            @PathVariable("tradeId") long tradeId,
            @RequestBody CloseTrade closeTrade) {

        Report report = reportDao.findReport(id);
        Trade trade = reportDao.findTrade(tradeId);

        if (report == null || trade == null) {
            return ResponseEntity.notFound().build();
        }

        if (!TradeStatus.OPEN.equals(trade.getStatus())) {
            return ResponseEntity.badRequest().build();
        }

        reportService.closeTrade(trade, closeTrade.getCloseDate(), closeTrade.getClosePrice());
        messageService.sendWsMessage(WS_TOPIC_REPORT, "trade " + tradeId + " closed");

        return ResponseEntity.ok().build();
    }

    @RequestMapping("/reports/{id}/statistics/{interval}")
    public ResponseEntity<?> getStatistics(
            @PathVariable("id") int id,
            @PathVariable("interval") StatisticsInterval interval,
            @RequestParam(required = false, value = "tradeType") String tradeType,
            @RequestParam(required = false, value = "secType") String secType,
            @RequestParam(required = false, value = "currency") String currency,
            @RequestParam(required = false, value = "underlying") String underlying,
            @RequestParam("start") int start,
            @RequestParam("limit") int limit) {

        Report report = reportDao.findReport(id);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        List<Statistics> statistics = statisticsCalculator.getStatistics(report, interval, tradeType, secType, currency, underlying, null);
        Collections.reverse(statistics);
        List<Statistics> statisticsPage = new ArrayList<>();

        for (int i = 0; i < statistics.size(); i++) {
            if (i >= start && i < (start + limit)) {
                statisticsPage.add(statistics.get(i));
            }
        }
        return ResponseEntity.ok(new RestList<>(statisticsPage, statistics.size()));
    }

    @RequestMapping("reports/{id}/charts/{interval}")
    public ResponseEntity<?> getCharts(
            @PathVariable("id") int id,
            @PathVariable("interval") StatisticsInterval interval,
            @RequestParam(required = false, value = "tradeType") String tradeType,
            @RequestParam(required = false, value = "secType") String secType,
            @RequestParam(required = false, value = "currency") String currency,
            @RequestParam(required = false, value = "underlying") String underlying) {

        Report report = reportDao.findReport(id);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }
        List<Statistics> statistics = statisticsCalculator.getStatistics(report, interval, tradeType, secType, currency, underlying, 120);

        return ResponseEntity.ok(new RestList<>(statistics, statistics.size()));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/reports/{id}/statistics/{interval}")
    public ResponseEntity<?> calculateStatistics(
            @PathVariable("id") int id,
            @PathVariable("interval") StatisticsInterval interval,
            @RequestParam(required = false, value = "tradeType") String tradeType,
            @RequestParam(required = false, value = "secType") String secType,
            @RequestParam(required = false, value = "currency") String currency,
            @RequestParam(required = false, value = "underlying") String underlying) {

        Report report = reportDao.findReport(id);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }
        statisticsCalculator.calculateStatistics(id, interval, tradeType, secType, currency, underlying);

        return ResponseEntity.ok().build();
    }

    @RequestMapping("/reports/{id}/underlyings")
    public ResponseEntity<?> getUnderlyings(
            @PathVariable("id") int reportId,
            @RequestParam(required = false, value = "openOnly") String openOnly) {

        Report report = reportDao.findReport(reportId);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reportDao.getUnderlyings(reportId, Boolean.parseBoolean(openOnly)));
    }

    @RequestMapping("/reports/{id}/ificsv/{year}/{endMonth}/{tradeType}")
    public ResponseEntity<?> getIfiCsv(
            @PathVariable("id") int id,
            @PathVariable("year") int year,
            @PathVariable("endMonth") int endMonth,
            @PathVariable("tradeType") TradeType tradeType) {

        Report report = reportDao.findReport(id);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ifiCsvGenerator.generate(id, year, endMonth, tradeType));
    }
}
