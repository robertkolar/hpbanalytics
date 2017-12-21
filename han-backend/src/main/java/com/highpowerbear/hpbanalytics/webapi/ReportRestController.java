package com.highpowerbear.hpbanalytics.webapi;

import com.highpowerbear.hpbanalytics.common.HanSettings;
import com.highpowerbear.hpbanalytics.common.OptionParseResult;
import com.highpowerbear.hpbanalytics.common.OptionUtil;
import com.highpowerbear.hpbanalytics.dao.ReportDao;
import com.highpowerbear.hpbanalytics.dao.filter.ExecutionFilter;
import com.highpowerbear.hpbanalytics.dao.filter.FilterParser;
import com.highpowerbear.hpbanalytics.dao.filter.TradeFilter;
import com.highpowerbear.hpbanalytics.entity.Execution;
import com.highpowerbear.hpbanalytics.entity.Report;
import com.highpowerbear.hpbanalytics.entity.Trade;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.enums.StatisticsInterval;
import com.highpowerbear.hpbanalytics.enums.TradeStatus;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import com.highpowerbear.hpbanalytics.report.IfiCsvGenerator;
import com.highpowerbear.hpbanalytics.report.ReportProcessor;
import com.highpowerbear.hpbanalytics.report.Statistics;
import com.highpowerbear.hpbanalytics.report.StatisticsCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by robertk on 12/21/2017.
 */
@RestController
@RequestMapping("/report")
public class ReportRestController {

    @Autowired ReportDao reportDao;
    @Autowired private StatisticsCalculator statisticsCalculator;
    @Autowired private ReportProcessor reportProcessor;
    @Autowired private FilterParser filterParser;
    @Autowired private IfiCsvGenerator ifiCsvGenerator;

    @RequestMapping("/reports")
    public ResponseEntity<?> getReports() {
        List<Report> reports = reportDao.getReports();

        for (Report r : reports) {
            r.setNumExecutions(reportDao.getNumExecutions(r));
            r.setNumTrades(reportDao.getNumTrades(r));
            r.setNumOpenTrades(reportDao.getNumOpenTrades(r));
            r.setNumUnderlyings(reportDao.getNumUnderlyings(r));
            r.setNumOpenUnderlyings(reportDao.getNumOpenUnderlyings(r));
            r.setFirstExecutionDate(reportDao.getFirstExecutionDate(r));
            r.setLastExecutionDate(reportDao.getLastExecutionDate(r));
        }
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
            @PathVariable("id") Integer id) {

        Report report = reportDao.findReport(id);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        reportProcessor.analyzeAll(report);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/reports/{id}")
    public ResponseEntity<?> deleteReport(
            @PathVariable("id") Integer id) {

        Report report = reportDao.findReport(id);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        reportProcessor.deleteReport(report);
        return ResponseEntity.ok().build();
    }

    @RequestMapping("/reports/{id}/executions")
    public ResponseEntity<?> getFilteredExecutions(
            @PathVariable("id") Integer id,
            @RequestParam("filter") String jsonFilter,
            @RequestParam("start") Integer start,
            @RequestParam("limit") Integer limit) {

        start = (start == null ? 0 : start);
        limit = (limit == null ? HanSettings.JPA_MAX_RESULTS : limit);

        Report report = reportDao.findReport(id);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        ExecutionFilter filter = filterParser.parseExecutionFilter(jsonFilter);
        List<Execution> executions = reportDao.getFilteredExecutions(report, filter, start, limit);
        Long numExecutions = reportDao.getNumFilteredExecutions(report, filter);
        return ResponseEntity.ok(new RestList<>(executions, numExecutions));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/reports/{id}/executions")
    public ResponseEntity<?> createExecution(
            @PathVariable("id") Integer reportId, Execution execution) {

        Report report = reportDao.findReport(reportId);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        execution.setId(null);
        execution.setReport(report);
        // fix fill date timezone, JAXB JSON converter sets it to UTC
        execution.getFillDate().setTimeZone(TimeZone.getTimeZone(HanSettings.TIMEZONE));
        execution.setReceivedDate(Calendar.getInstance());
        Long executionId = reportProcessor.newExecution(execution);
        execution.setId(executionId);

        return (executionId != null ? ResponseEntity.ok(execution) : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/reports/{id}/executions/{executionid}")
    public ResponseEntity<?> deleteExecution(
            @PathVariable("id") Integer reportId,
            @PathVariable("executionid") Long executionId) {

        Execution execution = reportDao.findExecution(executionId);
        if (execution == null || !reportId.equals(execution.getReportId())) {
            return ResponseEntity.notFound().build();
        }
        reportProcessor.deleteExecution(execution);
        return ResponseEntity.ok().build();
    }

    @RequestMapping("/reports/{id}/trades")
    public ResponseEntity<?> getFilteredTrades(
            @PathVariable("id") Integer id,
            @RequestParam("filter") String jsonFilter,
            @RequestParam("start") Integer start,
            @RequestParam("limit") Integer limit) {

        start = (start == null ? 0 : start);
        limit = (limit == null ? HanSettings.JPA_MAX_RESULTS : limit);

        Report report = reportDao.findReport(id);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        TradeFilter filter = filterParser.parseTradeFilter(jsonFilter);
        List<Trade> trades = reportDao.getFilteredTrades(report, filter, start, limit);
        Long numTrades = reportDao.getNumFilteredTrades(report, filter);

        return ResponseEntity.ok(new RestList<>(trades, numTrades));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/reports/{id}/trades/{tradeid}/close")
    public ResponseEntity<?> closeTrade(
            @PathVariable("id") Integer id,
            @PathVariable("tradeid") Long tradeId,
            @RequestBody CloseTradeDto closeTradeDto) {

        Report report = reportDao.findReport(id);
        Trade trade = reportDao.findTrade(tradeId);

        if (report == null || trade == null) {
            return ResponseEntity.notFound().build();
        }

        if (!TradeStatus.OPEN.equals(trade.getStatus())) {
            return ResponseEntity.badRequest().build();
        }

        // fix fill date timezone, JAXB JSON converter sets it to UTC
        closeTradeDto.getCloseDate().setTimeZone(TimeZone.getTimeZone(HanSettings.TIMEZONE));

        return ResponseEntity.ok(reportProcessor.closeTrade(trade, closeTradeDto.getCloseDate(), closeTradeDto.getClosePrice()));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/reports/{id}/trades/{tradeid}/expire")
    public ResponseEntity<?> expireTrade(
            @PathVariable("id") Integer id,
            @PathVariable("tradeid") Long tradeId) {

        Report report = reportDao.findReport(id);
        Trade trade = reportDao.findTrade(tradeId);

        if (report == null || trade == null) {
            return ResponseEntity.notFound().build();
        }

        if (!SecType.OPT.equals(trade.getSecType()) || !TradeStatus.OPEN.equals(trade.getStatus())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(reportProcessor.expireTrade(trade));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/reports/{id}/trades/{tradeid}/assign")
    public ResponseEntity<?> assignTrade(
            @PathVariable("id") Integer id,
            @PathVariable("tradeid") Long tradeId) {

        Report report = reportDao.findReport(id);
        Trade trade = reportDao.findTrade(tradeId);

        if (report == null || trade == null) {
            return ResponseEntity.notFound().build();
        }

        if (!SecType.OPT.equals(trade.getSecType()) || !TradeStatus.OPEN.equals(trade.getStatus())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(reportProcessor.assignTrade(trade));
    }

    @RequestMapping("/reports/{id}/statistics/{interval}")
    public ResponseEntity<?> getStatistics(
            @PathVariable("id") Integer id,
            @PathVariable("interval") StatisticsInterval interval,
            @RequestParam("underlying") String underlying,
            @RequestParam("start") Integer start,
            @RequestParam("limit") Integer limit) {

        Report report = reportDao.findReport(id);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        List<Statistics> statistics = statisticsCalculator.getStatistics(report, interval, underlying, HanSettings.MAX_STATS_RETURNED);
        Collections.reverse(statistics);
        List<Statistics> statisticsPage = new ArrayList<>();

        for (int i = 0; i < statistics.size(); i++) {
            if (i >= start && i < (start + limit)) {
                statisticsPage.add(statistics.get(i));
            }
        }
        return ResponseEntity.ok(new RestList<>(statisticsPage, (long) statistics.size()));
    }

    @RequestMapping("reports/{id}/charts/{interval}")
    public ResponseEntity<?> getCharts(
            @PathVariable("id") Integer id,
            @PathVariable("interval") StatisticsInterval interval,
            @RequestParam("underlying") String underlying) {

        Report report = reportDao.findReport(id);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }
        List<Statistics> statistics = statisticsCalculator.getStatistics(report, interval, underlying, HanSettings.MAX_STATS_RETURNED);

        return ResponseEntity.ok(new RestList<>(statistics, (long) statistics.size()));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/reports/{id}/statistics/{interval}")
    public ResponseEntity<?> recalculateStatistics(
            @PathVariable("id") Integer id,
            @PathVariable("interval") StatisticsInterval interval,
            @RequestParam("underlying") String underlying) {

        Report report = reportDao.findReport(id);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }
        statisticsCalculator.calculateStatistics(report, interval, underlying);

        return ResponseEntity.ok().build();
    }

    @RequestMapping("/reports/{id}/underlyings")
    public ResponseEntity<?> getUnderlyings(
            @PathVariable("id") Integer id) {

        Report report = reportDao.findReport(id);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reportDao.getUnderlyings(report));
    }

    @RequestMapping("/optionutil/parse")
    public ResponseEntity<?> optionUtilParse(
            @RequestParam("optionsymbol") String optionSymbol) {

        OptionParseResult optionParseResult;
        try {
            optionParseResult = OptionUtil.parse(optionSymbol);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(optionParseResult);
    }

    @RequestMapping("/reports/{id}/ificsv/{year}/{tradetype}")
    public ResponseEntity<?> getIfiCsv(
            @PathVariable("id") Integer id,
            @PathVariable("year") Integer year,
            @PathVariable("tradetype") TradeType tradeType) {

        Report report = reportDao.findReport(id);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ifiCsvGenerator.generate(report, year, tradeType));
    }
}
