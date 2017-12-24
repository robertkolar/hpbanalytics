package com.highpowerbear.hpbanalytics.report;

import com.highpowerbear.hpbanalytics.dao.ReportDao;
import com.highpowerbear.hpbanalytics.entity.Execution;
import com.highpowerbear.hpbanalytics.entity.Report;
import com.highpowerbear.hpbanalytics.iblogger.IbExecution;
import com.highpowerbear.hpbanalytics.webapi.WebsocketController;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Calendar;

/**
 * Created by robertk on 12/21/2017.
 */
@Component
public class ExecutionReceiver {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ExecutionReceiver.class);

    @Autowired private ReportDao reportDao;
    @Autowired private ReportProcessor reportProcessor;
    @Autowired private WebsocketController websocketController;

    @JmsListener(destination = "ibLoggerToReport", containerFactory = "jmsFactory")
    public void receiveExecution(IbExecution ibExecution) {

        Execution execution = new Execution(ibExecution);

        Calendar now = Calendar.getInstance();
        execution.setReceivedDate(now);

        Report report = reportDao.getReportByOriginAndSecType(execution.getOrigin(), execution.getSecType());

        if (report == null) {
            log.warn("No report for origin=" + execution.getOrigin() + " and secType=" + execution.getSecType() + ", skipping");
            return;
        }
        execution.setReport(report);

        reportProcessor.newExecution(execution);
        websocketController.sendReportMessage("new execution processed");
    }
}