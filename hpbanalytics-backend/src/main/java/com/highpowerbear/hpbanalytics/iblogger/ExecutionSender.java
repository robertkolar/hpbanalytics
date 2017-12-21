package com.highpowerbear.hpbanalytics.iblogger;

import com.highpowerbear.hpbanalytics.common.HanSettings;
import com.highpowerbear.hpbanalytics.entity.IbOrder;
import com.highpowerbear.hpbanalytics.enums.Action;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.SecType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by robertk on 3/29/2015.
 */
@Component
public class ExecutionSender {

    @Autowired private JmsTemplate jmsTemplate;

    public void sendExecution(IbOrder ibOrder) {
        IbExecution ie = new IbExecution();

        ie.setOrigin(HanSettings.CONVERSION_ORIGIN_PREFIX_IB + ibOrder.getIbAccountId());
        ie.setReferenceId(String.valueOf(ibOrder.getPermId()));
        ie.setAction(Action.valueOf(ibOrder.getAction()));
        ie.setQuantity(ibOrder.getQuantity());
        ie.setUnderlying(ibOrder.getUnderlying());
        ie.setCurrency(Currency.valueOf(ibOrder.getCurrency()));
        ie.setSymbol(ibOrder.getSymbol());
        ie.setSecType(SecType.valueOf(ibOrder.getSecType()));
        ie.setFillDate(ibOrder.getStatusDate());
        ie.setFillPrice(ibOrder.getFillPrice());

        jmsTemplate.convertAndSend("ibLoggerToReport", ie);
    }
}