package com.highpowerbear.hpbanalytics.report;

import com.highpowerbear.hpbanalytics.common.MessageSender;
import com.highpowerbear.hpbanalytics.dao.ReportDao;
import com.highpowerbear.hpbanalytics.entity.Execution;
import com.highpowerbear.hpbanalytics.entity.IbOrder;
import com.highpowerbear.hpbanalytics.entity.Report;
import com.highpowerbear.hpbanalytics.enums.Action;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.OrderStatus;
import com.highpowerbear.hpbanalytics.enums.SecType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Calendar;

import static com.highpowerbear.hpbanalytics.common.CoreSettings.JMS_DEST_ORDTRACK_TO_REPORT;
import static com.highpowerbear.hpbanalytics.common.CoreSettings.WS_TOPIC_REPORT;

/**
 * Created by robertk on 12/21/2017.
 */
@Component
public class ReportMessageReceiver {
    private static final Logger log = LoggerFactory.getLogger(ReportMessageReceiver.class);

    @Autowired private ReportDao reportDao;
    @Autowired private ReportProcessor reportProcessor;
    @Autowired private MessageSender messageSender;

    @JmsListener(destination = JMS_DEST_ORDTRACK_TO_REPORT)
    public void receiveJmsMessage(String message) {
        handleExecution(Long.valueOf(message));
    }

    private void handleExecution(long ibOrderId) {
        log.info("handling execution for order " + ibOrderId);

        IbOrder ibOrder = reportDao.findIbOrder(ibOrderId);

        if (ibOrder.getStatus() != OrderStatus.FILLED) {
            log.error("cannot create execution, ibOrder " + ibOrderId + " not filled");
            return;
        }

        Execution execution = new Execution();

        execution.setOrigin("IB:" + ibOrder.getIbAccountId());
        execution.setReferenceId(String.valueOf(ibOrder.getPermId()));
        execution.setAction(Action.valueOf(ibOrder.getAction()));
        execution.setQuantity(ibOrder.getQuantity());
        execution.setUnderlying(ibOrder.getUnderlying());
        execution.setCurrency(Currency.valueOf(ibOrder.getCurrency()));
        execution.setSymbol(ibOrder.getSymbol());
        execution.setSecType(SecType.valueOf(ibOrder.getSecType()));
        execution.setFillDate(ibOrder.getStatusDate());
        execution.setFillPrice(BigDecimal.valueOf(ibOrder.getFillPrice()));

        Calendar now = Calendar.getInstance();
        execution.setReceivedDate(now);

        Report report = reportDao.getReportByOriginAndSecType(execution.getOrigin(), execution.getSecType());

        if (report == null) {
            log.warn("no report for origin=" + execution.getOrigin() + " and secType=" + execution.getSecType() + ", skipping");
            return;
        }
        execution.setReport(report);

        reportProcessor.newExecution(execution);
        messageSender.sendWsMessage(WS_TOPIC_REPORT,  "new execution processed");
    }
}