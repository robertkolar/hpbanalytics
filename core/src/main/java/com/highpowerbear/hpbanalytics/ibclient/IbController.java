package com.highpowerbear.hpbanalytics.ibclient;

import com.highpowerbear.hpbanalytics.common.CoreUtil;
import com.highpowerbear.hpbanalytics.dao.OrdTrackDao;
import com.highpowerbear.hpbanalytics.entity.IbAccount;
import com.highpowerbear.hpbanalytics.ordtrack.OrdTrackService;
import com.ib.client.EClientSocket;
import com.ib.client.EJavaSignal;
import com.ib.client.EReader;
import com.ib.client.EReaderSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Provider;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by robertk on 4/6/2015.
 */
@Component
public class IbController {
    private static final Logger log = LoggerFactory.getLogger(IbController.class);

    private final OrdTrackDao ordTrackDao;
    private final OrdTrackService ordTrackService;
    private final Provider<IbListener> ibListeners;

    private final Map<String, IbAccount> ibAccountMap = new HashMap<>(); // accountId -> ibAccount
    private final Map<String, IbConnection> ibConnectionMap = new HashMap<>(); // accountId --> ibConnection

    @Autowired
    public IbController(OrdTrackDao ordTrackDao, OrdTrackService ordTrackService, Provider<IbListener> ibListeners) {
        this.ordTrackDao = ordTrackDao;
        this.ordTrackService = ordTrackService;
        this.ibListeners = ibListeners;
    }

    private class IbConnection {
        private boolean markConnected = false;
        private final EClientSocket eClientSocket;
        private final EReaderSignal eReaderSignal;

        private IbConnection(EClientSocket eClientSocket, EReaderSignal eReaderSignal) {
            this.eClientSocket = eClientSocket;
            this.eReaderSignal = eReaderSignal;
        }
    }

    @PostConstruct
    private void init() {
        ordTrackService.setIbController(this);

        for (IbAccount ibAccount : ordTrackDao.getIbAccounts()) {
            ibAccountMap.put(ibAccount.getAccountId(), ibAccount);

            IbListener ibListener = ibListeners.get().configure(ibAccount.getAccountId());
            ibListener.setIbController(this);
            ibListener.setOrdTrackService(ordTrackService);

            EReaderSignal eReaderSignal = new EJavaSignal();
            EClientSocket eClientSocket = new EClientSocket(ibListener, eReaderSignal);

            IbConnection ibConnection = new IbConnection(eClientSocket, eReaderSignal);
            ibConnectionMap.put(ibAccount.getAccountId(), ibConnection);
        }
    }

    public void connect(String accountId) {
        IbAccount a = ibAccountMap.get(accountId);
        IbConnection c = ibConnectionMap.get(accountId);

        c.markConnected = true;

        if (!isConnected(accountId)) {
            log.info("connecting " + getInfo(accountId));
            c.eClientSocket.eConnect(a.getHost(), a.getPort(), a.getClientId());
            CoreUtil.waitMilliseconds(1000);

            if (isConnected(accountId)) {
                log.info("successfully connected " + getInfo(accountId));

                final EReader eReader = new EReader(c.eClientSocket, c.eReaderSignal);

                eReader.start();
                // an additional thread is created in this program design to empty the messaging queue
                new Thread(() -> {
                    while (c.eClientSocket.isConnected()) {
                        c.eReaderSignal.waitForSignal();
                        try {
                            eReader.processMsgs();
                        } catch (Exception e) {
                            log.error("error", e);
                        }
                    }
                }).start();
            }
        }
    }

    public void disconnect(String accountId) {
        IbConnection c = ibConnectionMap.get(accountId);
        c.markConnected = false;

        if (isConnected(accountId)) {
            log.info("disconnecting " + getInfo(accountId));
            c.eClientSocket.eDisconnect();
            CoreUtil.waitMilliseconds(1000);

            if (!isConnected(accountId)) {
                log.info("successfully disconnected " + getInfo(accountId));
            }
        }
    }

    public boolean isConnected(String accountId) {
        IbConnection c = ibConnectionMap.get(accountId);
        return c.eClientSocket != null && c.eClientSocket.isConnected();
    }

    public boolean isMarkConnected(String accountId) {
        return ibConnectionMap.get(accountId).markConnected;
    }

    public void requestOpenOrders(String accountId) {
        log.info("requesting openOrders, allOpenOrders and autoOpenOrders");

        if (checkConnected(accountId)) {
            IbConnection c = ibConnectionMap.get(accountId);

            c.eClientSocket.reqOpenOrders();
            c.eClientSocket.reqAllOpenOrders();
            c.eClientSocket.reqAutoOpenOrders(true);
        }
    }

    public void requestPositions(String accountId) {
        log.info("requesting positions");

        if (checkConnected(accountId)) {
            ibConnectionMap.get(accountId).eClientSocket.reqPositions();
        }
    }

    public void cancelPositions(String accountId) {
        log.info("cancelling positions");

        if (checkConnected(accountId)) {
            ibConnectionMap.get(accountId).eClientSocket.cancelPositions();
        }
    }

    void connectionBroken(String accountId) {
        ibConnectionMap.get(accountId).eClientSocket.eDisconnect();
    }

    private boolean checkConnected(String accountId) {
        if (!isConnected(accountId)) {
            log.info("not connected " + getInfo(accountId));
            return false;
        }
        return true;
    }

    private String getInfo(String accountId) {
        IbAccount a = ibAccountMap.get(accountId);
        return a.getHost() + ":" + a.getPort() + ":" + a.getClientId() + "," + isConnected(accountId);
    }
}
