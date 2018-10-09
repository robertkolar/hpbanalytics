package com.highpowerbear.hpbanalytics.ibclient;

import com.highpowerbear.hpbanalytics.common.CoreSettings;
import com.highpowerbear.hpbanalytics.common.MessageSender;
import com.highpowerbear.hpbanalytics.dao.OrdTrackDao;
import com.highpowerbear.hpbanalytics.ordtrack.model.Position;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.highpowerbear.hpbanalytics.common.CoreSettings.WS_TOPIC_ORDTRACK;

/**
 *
 * Created by robertk on 4/6/2015.
 */
@Component
public class IbController {
    private static final Logger log = LoggerFactory.getLogger(IbController.class);

    private final OrdTrackDao ordTrackDao;
    private final Provider<IbListener> ibListeners;
    private final MessageSender messageSender;

    private final Map<String, IbConnection> ibConnectionMap = new ConcurrentHashMap<>(); // accountId --> ibConnection
    private final Map<String, List<Position>> positionMap = new ConcurrentHashMap<>(); // accountId --> positions
    private final Map<String, List<Position>> temporaryPositionMap = new ConcurrentHashMap<>(); // accountId --> positions

    @Autowired
    public IbController(OrdTrackDao ordTrackDao, Provider<IbListener> ibListeners, MessageSender messageSender) {
        this.ordTrackDao = ordTrackDao;
        this.ibListeners = ibListeners;
        this.messageSender = messageSender;
    }

    Map<String, IbConnection> getIbConnectionMap() {
        return ibConnectionMap;
    }

    Map<String, List<Position>> getPositionMap() {
        return positionMap;
    }

    Map<String, List<Position>> getTemporaryPositionMap() {
        return temporaryPositionMap;
    }

    public IbConnection getIbConnection(String accountId) {
        return ibConnectionMap.get(accountId);
    }

    public List<Position> getPositions(String accountId) {
        return positionMap.get(accountId);
    }

    @PostConstruct
    private void init() {
        ordTrackDao.getIbAccounts().forEach(ibAccount -> {
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
            log.info("requesting open orders for account " + accountId);

            c.getClientSocket().reqOpenOrders();
            c.getClientSocket().reqAllOpenOrders();
            c.getClientSocket().reqAutoOpenOrders(true);
        }
    }

    public void requestPositions(String accountId) {
        IbConnection c = ibConnectionMap.get(accountId);

        if (c.isConnected()) {
            log.info("requesting positions for account " + accountId);
            temporaryPositionMap.put(accountId, new ArrayList<>());
            c.getClientSocket().reqPositions();
        }
    }

    public void addPosition(Position position) {
        log.debug("adding position " + position);
        temporaryPositionMap.get(position.getAccountId()).add(position);
    }

    public void positionEnd(String accountId) {
        IbConnection c = ibConnectionMap.get(accountId);

        if (c.isConnected()) {
            c.getClientSocket().cancelPositions();
        }

        List<Position> temporaryPositions = temporaryPositionMap.get(accountId);
        if (!temporaryPositions.isEmpty()) {
            temporaryPositions.sort(Comparator.comparing(Position::getAccountId));
            temporaryPositions.sort(Comparator.comparing(Position::getSymbol));
            positionMap.put(accountId, temporaryPositions);
        }

        String msg = "positions updated for account: " + accountId;
        log.info(msg);
        messageSender.sendWsMessage(WS_TOPIC_ORDTRACK, msg);
    }
}
