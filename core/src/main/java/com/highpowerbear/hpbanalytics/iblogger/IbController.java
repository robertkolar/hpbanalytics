package com.highpowerbear.hpbanalytics.iblogger;

import com.highpowerbear.hpbanalytics.common.CoreSettings;
import com.highpowerbear.hpbanalytics.dao.IbLoggerDao;
import com.ib.client.EClientSocket;
import com.ib.client.EJavaSignal;
import com.ib.client.EReaderSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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

    @Autowired private IbLoggerDao ibLoggerDao;
    @Autowired private Provider<IbListener> ibListeners;

    private Map<String, IbConnection> ibConnectionMap = new HashMap<>(); // accountId --> ibConnection

    public IbConnection getIbConnection(String accountId) {
        return ibConnectionMap.get(accountId);
    }

    @PostConstruct
    private void init() {
        ibLoggerDao.getIbAccounts().forEach(ibAccount -> {
            EReaderSignal eReaderSignal = new EJavaSignal();
            EClientSocket eClientSocket = new EClientSocket(ibListeners.get().configure(ibAccount.getAccountId()), eReaderSignal);

            IbConnection ibConnection = new IbConnection(ibAccount.getHost(), ibAccount.getPort(), CoreSettings.IB_CONNECT_CLIENT_ID, eClientSocket, eReaderSignal);
            ibConnectionMap.put(ibAccount.getAccountId(), ibConnection);
        });
    }

    @PreDestroy
    private void finish() {
        ibConnectionMap.keySet().forEach(this::disconnect);
    }

    public void connect(String accountId) {
        ibConnectionMap.get(accountId).connect();
    }

    public void disconnect(String accountId) {
        ibConnectionMap.get(accountId).disconnect();
    }

    public void requestOpenOrders(String accountId) {
        log.info("requesting open orders for ibAccount " + accountId);

        IbConnection c = ibConnectionMap.get(accountId);
        c.getClientSocket().reqOpenOrders();
        c.getClientSocket().reqAllOpenOrders();
        c.getClientSocket().reqAutoOpenOrders(true);
    }

    public void requestPositions(String accountId) {
        log.info("requesting positions for ibAccount " + accountId);

        IbConnection c = ibConnectionMap.get(accountId);
        c.getClientSocket().reqPositions();
    }
}
