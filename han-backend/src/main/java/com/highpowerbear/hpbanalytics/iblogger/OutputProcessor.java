package com.highpowerbear.hpbanalytics.iblogger;

import com.highpowerbear.hpbanalytics.common.HanSettings;
import com.highpowerbear.hpbanalytics.common.HanUtil;
import com.highpowerbear.hpbanalytics.entity.IbOrder;
import com.highpowerbear.hpbanalytics.enums.Action;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.SecType;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Queue;

/**
 * Created by robertk on 3/29/2015.
 */
@Component
public class OutputProcessor {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(OutputProcessor.class);

    @Autowired private JmsTemplate jmsTemplate;
    @Autowired private Queue queue;

    public void processExecution(IbOrder ibOrder) {
        if (ibOrder.getIbAccount().isAnalytics()) {
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
            try {
                jmsTemplate.convertAndSend(queue, HanUtil.toXml(ie));
            } catch (Exception e) {
                log.error("Error", e);
            }
        } else {
            log.info("Analytics processing is disabled, ibExecution won't be sent");
        }
    }
}