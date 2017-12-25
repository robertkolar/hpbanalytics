package com.highpowerbear.hpbanalytics.iblogger;

import com.highpowerbear.hpbanalytics.common.CoreSettings;
import com.highpowerbear.hpbanalytics.dao.IbLoggerDao;
import com.highpowerbear.hpbanalytics.entity.IbAccount;
import com.highpowerbear.hpbanalytics.enums.IbConnectionType;
import com.ib.client.EClientSocket;
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
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(IbController.class);

    @Autowired private IbLoggerDao ibLoggerDao;
    @Autowired private Provider<IbListener> ibListeners;

    private Map<IbAccount, IbConnection> ibConnectionMap = new HashMap<>(); // ibAccount --> ibConnection

    public IbConnection getIbConnection(IbAccount ibAccount) {
        return ibConnectionMap.get(ibAccount);
    }

    @PostConstruct
    private void init() {
        ibLoggerDao.getIbAccounts().forEach(ibAccount -> {
            EClientSocket eClientSocket = new EClientSocket(ibListeners.get().configure(ibAccount));
            IbConnection ibConnection = new IbConnection(IbConnectionType.IBLOGGER, ibAccount.getHost(), ibAccount.getPort(), CoreSettings.IB_CONNECT_CLIENT_ID, eClientSocket);
            ibConnectionMap.put(ibAccount, ibConnection);
        });
    }

    @PreDestroy
    private void finish() {
        ibConnectionMap.keySet().forEach(this::disconnect);
    }

    public void connect(IbAccount ibAccount) {
        ibConnectionMap.get(ibAccount).connect();
    }

    public void disconnect(IbAccount ibAccount) {
        ibConnectionMap.get(ibAccount).disconnect();
    }

    public void requestOpenOrders(IbAccount ibAccount) {
        log.info("Requesting open orders for ibAccount " + ibAccount.print());
        IbConnection c = ibConnectionMap.get(ibAccount);
        c.getClientSocket().reqOpenOrders();
        c.getClientSocket().reqAllOpenOrders();
        c.getClientSocket().reqAutoOpenOrders(true);
    }
}
