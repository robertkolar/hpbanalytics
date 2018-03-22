package com.highpowerbear.hpbanalytics.report;

import com.highpowerbear.hpbanalytics.common.CoreUtil;
import com.highpowerbear.hpbanalytics.common.ExecutionVO;
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

import static com.highpowerbear.hpbanalytics.common.CoreSettings.JMS_DEST_EXECUTION_RECEIVED;
import static com.highpowerbear.hpbanalytics.common.CoreSettings.JMS_DEST_ORDER_FILLED;
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

    @JmsListener(destination = JMS_DEST_ORDER_FILLED)
    public void receiveJmsMessage(String ibOrderId) {
        handleOrderFilled(Long.valueOf(ibOrderId));
    }

    @JmsListener(destination = JMS_DEST_EXECUTION_RECEIVED)
    public void receiveJmsMessage(ExecutionVO executionVO) {
        handleExecutionReceived(executionVO);
    }

    private void handleOrderFilled(long ibOrderId) {
        log.info("handling execution for order " + ibOrderId);

        IbOrder ibOrder = reportDao.findIbOrder(ibOrderId);

        if (ibOrder.getStatus() != OrderStatus.FILLED) {
            log.error("cannot create execution, ibOrder " + ibOrderId + " not filled");
            return;
        }

        Execution e = new Execution();

        e.setOrigin("IB:" + ibOrder.getIbAccountId());
        e.setReferenceId(String.valueOf(ibOrder.getPermId()));
        e.setAction(Action.valueOf(ibOrder.getAction()));
        e.setQuantity(ibOrder.getQuantity());
        e.setUnderlying(ibOrder.getUnderlying());
        e.setCurrency(Currency.valueOf(ibOrder.getCurrency()));
        e.setSymbol(ibOrder.getSymbol());
        e.setSecType(SecType.valueOf(ibOrder.getSecType()));
        e.setFillDate(ibOrder.getStatusDate());
        e.setFillPrice(BigDecimal.valueOf(ibOrder.getFillPrice()));

        processExecution(e);
    }

    private void handleExecutionReceived(ExecutionVO evo) {
        Execution e = new Execution();

        String symbol = evo.getLocalSymbol();

        if (symbol.split(" ").length > 1) {
            symbol = CoreUtil.removeSpace(symbol);
        }

        e.setOrigin("IB:" + evo.getAcctNumber());
        e.setReferenceId(String.valueOf(evo.getPermId()));
        e.setAction(Action.getByExecSide(evo.getSide()));
        e.setQuantity(evo.getCumQty());
        e.setUnderlying(evo.getSymbol());
        e.setCurrency(Currency.valueOf(evo.getCurrency()));
        e.setSymbol(symbol);
        e.setSecType(SecType.valueOf(evo.getSecType()));
        e.setFillPrice(BigDecimal.valueOf(evo.getPrice()));

        processExecution(e);
    }

    private void processExecution(Execution execution) {
        Calendar now = Calendar.getInstance();
        execution.setReceivedDate(now);

        if (execution.getFillDate() == null) {
            execution.setFillDate(execution.getReceivedDate());
        }

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