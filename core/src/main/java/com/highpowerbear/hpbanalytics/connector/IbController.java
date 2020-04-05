package com.highpowerbear.hpbanalytics.connector;

import com.highpowerbear.hpbanalytics.common.HanUtil;
import com.highpowerbear.hpbanalytics.repository.OrdTrackDao;
import com.highpowerbear.hpbanalytics.entity.IbAccount;
import com.ib.client.EClientSocket;
import com.ib.client.EJavaSignal;
import com.ib.client.EReader;
import com.ib.client.EReaderSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * Created by robertk on 4/6/2015.
 */
@Component
public class IbController {
    private static final Logger log = LoggerFactory.getLogger(IbController.class);

    private final Map<String, IbAccount> ibAccountMap = new HashMap<>(); // accountId -> ibAccount
    private final Map<String, IbConnection> ibConnectionMap = new HashMap<>(); // accountId --> ibConnection
    private final List<ConnectionListener> connectionListeners = new ArrayList<>();

    private AtomicBoolean initialized = new AtomicBoolean(false);

    @Autowired
    public IbController(OrdTrackDao ordTrackDao) {
        ordTrackDao.getIbAccounts().forEach(ibAccount -> ibAccountMap.put(ibAccount.getAccountId(), ibAccount));
    }

    void initialize(List<IbListener> ibListeners) {
        if (!initialized.get()) {
            initialized.set(true);

            for (IbListener ibListener : ibListeners) {
                EReaderSignal eReaderSignal = new EJavaSignal();
                EClientSocket eClientSocket = new EClientSocket(ibListener, eReaderSignal);

                IbConnection ibConnection = new IbConnection(eClientSocket, eReaderSignal);
                ibConnectionMap.put(ibListener.getAccountId(), ibConnection);
            }
        }
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

    public void addConnectionListener(ConnectionListener connectionListener) {
        connectionListeners.add(connectionListener);
    }

    public void connect(String accountId) {
        connectionListeners.forEach(connectionListener -> connectionListener.preConnect(accountId));

        IbAccount a = ibAccountMap.get(accountId);
        IbConnection c = ibConnectionMap.get(accountId);
        c.markConnected = true;

        if (!isConnected(accountId)) {
            log.info("connecting " + getInfo(accountId));
            c.eClientSocket.eConnect(a.getHost(), a.getPort(), a.getClientId());
            HanUtil.waitMilliseconds(1000);

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
                connectionListeners.forEach(connectionListener -> connectionListener.postConnect(accountId));
            }
        }
    }

    public void disconnect(String accountId) {
        connectionListeners.forEach(connectionListener -> connectionListener.preDisconnect(accountId));

        IbConnection c = ibConnectionMap.get(accountId);
        c.markConnected = false;

        if (isConnected(accountId)) {
            log.info("disconnecting " + getInfo(accountId));
            c.eClientSocket.eDisconnect();
            HanUtil.waitMilliseconds(1000);

            if (!isConnected(accountId)) {
                log.info("successfully disconnected " + getInfo(accountId));
                connectionListeners.forEach(connectionListener -> connectionListener.postDisconnect(accountId));
            }
        }
    }

    @Scheduled(fixedRate = 5000)
    private void reconnect() {
        ibConnectionMap.keySet().forEach(accountId -> {
            if (!isConnected(accountId) && ibConnectionMap.get(accountId).markConnected) {
                connect(accountId);
            }
        });
    }

    public boolean isConnected(String accountId) {
        IbConnection c = ibConnectionMap.get(accountId);
        return c.eClientSocket != null && c.eClientSocket.isConnected();
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
