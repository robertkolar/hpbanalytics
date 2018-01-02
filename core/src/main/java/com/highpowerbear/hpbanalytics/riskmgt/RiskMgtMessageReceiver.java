package com.highpowerbear.hpbanalytics.riskmgt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import static com.highpowerbear.hpbanalytics.common.CoreSettings.JMS_DEST_IBLOGGER_TO_RISKMGT;

/**
 * Created by robertk on 12/28/2017.
 */
@Component
public class RiskMgtMessageReceiver {
    private static final Logger log = LoggerFactory.getLogger(RiskMgtMessageReceiver.class);

    @JmsListener(destination = JMS_DEST_IBLOGGER_TO_RISKMGT)
    public void receiveJmsMessage(String message) {
        log.info("receiveJmsMessage " + JMS_DEST_IBLOGGER_TO_RISKMGT + ": " + message);

        performRiskMgt();
    }

    private void performRiskMgt() {
        // TODO risk management logic
    }
}