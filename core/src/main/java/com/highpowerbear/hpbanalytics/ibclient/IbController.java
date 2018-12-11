package com.highpowerbear.hpbanalytics.ibclient;

import com.highpowerbear.hpbanalytics.common.CoreSettings;
import com.highpowerbear.hpbanalytics.dao.OrdTrackDao;
import com.highpowerbear.hpbanalytics.ordtrack.OrdTrackService;
import com.ib.client.EClientSocket;
import com.ib.client.EJavaSignal;
import com.ib.client.EReaderSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.inject.Provider;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    private final Map<String, IbConnection> ibConnectionMap = new ConcurrentHashMap<>(); // accountId --> ibConnection

    @Autowired
    public IbController(OrdTrackDao ordTrackDao, OrdTrackService ordTrackService, Provider<IbListener> ibListeners) {
        this.ordTrackDao = ordTrackDao;
        this.ordTrackService = ordTrackService;
        this.ibListeners = ibListeners;
    }

    @PostConstruct
    private void init() {
        ordTrackService.setIbController(this);

        ordTrackDao.getIbAccounts().forEach(ibAccount -> {
            IbListener ibListener = ibListeners.get().configure(ibAccount.getAccountId());
            ibListener.setIbController(this);
            ibListener.setOrdTrackService(ordTrackService);

            EReaderSignal eReaderSignal = new EJavaSignal();
            EClientSocket eClientSocket = new EClientSocket(ibListener, eReaderSignal);

            IbConnection ibConnection = new IbConnection(ibAccount.getHost(), ibAccount.getPort(), CoreSettings.IB_CONNECT_CLIENT_ID, eClientSocket, eReaderSignal);
            ibConnectionMap.put(ibAccount.getAccountId(), ibConnection);
        });
    }

    public void connect(String accountId) {
        ibConnectionMap.get(accountId).connect();
    }

    public void disconnect(String accountId) {
        ibConnectionMap.get(accountId).disconnect();
    }

    public boolean isConnected(String accountId) {
        return ibConnectionMap.get(accountId).isConnected();
    }

    public boolean isMarkConnected(String accountId) {
        return ibConnectionMap.get(accountId).isMarkConnected();
    }

    public void requestOpenOrders(String accountId) {
        log.info("requesting openOrders, allOpenOrders and autoOpenOrders");

        if (checkConnected(accountId)) {
            IbConnection c = ibConnectionMap.get(accountId);

            c.getClientSocket().reqOpenOrders();
            c.getClientSocket().reqAllOpenOrders();
            c.getClientSocket().reqAutoOpenOrders(true);
        }
    }

    public void requestPositions(String accountId) {
        log.info("requesting positions");

        if (checkConnected(accountId)) {
            ibConnectionMap.get(accountId).getClientSocket().reqPositions();
        }
    }

    public void cancelPositions(String accountId) {
        log.info("cancelling positions");

        if (checkConnected(accountId)) {
            ibConnectionMap.get(accountId).getClientSocket().cancelPositions();
        }
    }

    void connectionBroken(String accountId) {
        ibConnectionMap.get(accountId).getClientSocket().eDisconnect();
    }

    private boolean checkConnected(String accountId) {
        if (!isConnected(accountId)) {
            log.info("not connected " + ibConnectionMap.get(accountId).getInfo());
            return false;
        }
        return true;
    }
}
