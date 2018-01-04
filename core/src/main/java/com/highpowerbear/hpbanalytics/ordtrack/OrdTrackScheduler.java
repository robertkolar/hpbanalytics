package com.highpowerbear.hpbanalytics.ordtrack;

import com.highpowerbear.hpbanalytics.dao.OrdTrackDao;
import com.highpowerbear.hpbanalytics.ibclient.IbConnection;
import com.highpowerbear.hpbanalytics.ibclient.IbController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by robertk on 3/29/2015.
 */
@Component
public class OrdTrackScheduler {

    @Autowired private OrdTrackDao ordTrackDao;
    @Autowired private IbController ibController;
    @Autowired private HeartbeatControl heartbeatControl;

    @Scheduled(fixedRate = 5000)
    private void reconnect() {
        ordTrackDao.getIbAccounts().forEach(ibAccount -> {
            IbConnection c = ibController.getIbConnection(ibAccount.getAccountId());

            if (!c.isConnected() && c.isMarkConnected()) {
                ibController.connect(ibAccount.getAccountId());
            }
        });
    }

    @Scheduled(fixedRate = 300000, initialDelay = 60000)
    private void performPeriodicTasks() {
        ordTrackDao.getIbAccounts().forEach(ibAccount -> {

            String accountId = ibAccount.getAccountId();
            IbConnection c = ibController.getIbConnection(accountId);

            if (c.isConnected()) {
                heartbeatControl.updateHeartbeats(accountId);
                ibController.requestOpenOrders(accountId);
                ibController.requestPositions(accountId);
            }
        });
    }
}