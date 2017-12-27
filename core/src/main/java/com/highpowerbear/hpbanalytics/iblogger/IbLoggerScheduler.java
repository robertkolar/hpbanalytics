package com.highpowerbear.hpbanalytics.iblogger;

import com.highpowerbear.hpbanalytics.dao.IbLoggerDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by robertk on 3/29/2015.
 */
@Component
public class IbLoggerScheduler {

    @Autowired private IbLoggerDao ibLoggerDao;
    @Autowired private IbController ibController;
    @Autowired private HeartbeatControl heartbeatControl;

    @Scheduled(fixedRate = 5000)
    private void reconnect() {
        ibLoggerDao.getIbAccounts().forEach(ibAccount -> {
            IbConnection c = ibController.getIbConnection(ibAccount);

            if (!c.isConnected() && c.isMarkConnected()) {
                c.connect();
            }
        });
    }

    @Scheduled(fixedRate = 300000)
    private void requestOpenOrders() {
        ibLoggerDao.getIbAccounts().forEach(ibAccount -> {
            IbConnection c = ibController.getIbConnection(ibAccount);

            if (c.isConnected()) {
                heartbeatControl.updateHeartbeats(ibAccount);
                ibController.requestOpenOrders(ibAccount);
            }
        });
    }
}