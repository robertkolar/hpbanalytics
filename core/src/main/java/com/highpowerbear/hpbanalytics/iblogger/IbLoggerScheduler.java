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
    @Autowired private PositionRepository positionRepository;

    @Scheduled(fixedRate = 5000)
    private void reconnect() {
        ibLoggerDao.getIbAccounts().forEach(ibAccount -> {
            IbConnection c = ibController.getIbConnection(ibAccount.getAccountId());

            if (!c.isConnected() && c.isMarkConnected()) {
                c.connect();
            }
        });
    }

    @Scheduled(fixedRate = 300000)
    private void performPeriodicTasks() {
        ibLoggerDao.getIbAccounts().forEach(ibAccount -> {

            String accountId = ibAccount.getAccountId();
            IbConnection c = ibController.getIbConnection(accountId);

            if (c.isConnected()) {
                heartbeatControl.updateHeartbeats(accountId);
                ibController.requestOpenOrders(accountId);

                positionRepository.initPositions(accountId);
                ibController.requestPositions(accountId);
            }
        });
    }
}