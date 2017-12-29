package com.highpowerbear.hpbanalytics.iblogger;

import com.highpowerbear.hpbanalytics.common.CoreSettings;
import com.highpowerbear.hpbanalytics.common.CoreUtil;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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
    private Map<String, List<PositionVO>> positionMap = new HashMap<>(); // accountId --> positions

    public IbConnection getIbConnection(String accountId) {
        return ibConnectionMap.get(accountId);
    }

    public List<PositionVO> getPositions(String accountId) {
        return positionMap.get(accountId);
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
        IbConnection c = ibConnectionMap.get(accountId);
        c.connect();

        CoreUtil.waitMilliseconds(1000);
        requestOpenOrders(accountId);
        requestPositions(accountId);
    }

    public void disconnect(String accountId) {
        IbConnection c = ibConnectionMap.get(accountId);

        if (c.isConnected()) {
            c.disconnect();
        }
    }

    public void requestOpenOrders(String accountId) {
        IbConnection c = ibConnectionMap.get(accountId);

        if (c.isConnected()) {
            log.info("requesting open orders for ibAccount " + accountId);

            c.getClientSocket().reqOpenOrders();
            c.getClientSocket().reqAllOpenOrders();
            c.getClientSocket().reqAutoOpenOrders(true);
        }
    }

    public void requestPositions(String accountId) {
        IbConnection c = ibConnectionMap.get(accountId);

        if (c.isConnected()) {
            log.info("requesting positions for ibAccount " + accountId);
            positionMap.put(accountId, new ArrayList<>());

            c.getClientSocket().reqPositions();
        }
    }

    public void addPosition(PositionVO position) {
        positionMap.get(position.getAccountId()).add(position);
    }

    public void positionEnd(String accountId) {
        IbConnection c = ibConnectionMap.get(accountId);

        if (c.isConnected()) {
            c.getClientSocket().cancelPositions();
        }

        List<PositionVO> positions = positionMap.get(accountId);
        positions.sort(Comparator.comparing(PositionVO::getAccountId));
        positions.sort(Comparator.comparing(PositionVO::getSymbol));
    }
}
