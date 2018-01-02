package com.highpowerbear.hpbanalytics.iblogger;

import com.highpowerbear.hpbanalytics.common.CoreSettings;
import com.highpowerbear.hpbanalytics.common.CoreUtil;
import com.highpowerbear.hpbanalytics.common.MessageSender;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.highpowerbear.hpbanalytics.common.CoreSettings.JMS_DEST_IBLOGGER_TO_RISKMGT;
import static com.highpowerbear.hpbanalytics.common.CoreSettings.WS_TOPIC_IBLOGGER;

/**
 *
 * Created by robertk on 4/6/2015.
 */
@Component
public class IbController {
    private static final Logger log = LoggerFactory.getLogger(IbController.class);

    @Autowired private IbLoggerDao ibLoggerDao;
    @Autowired private Provider<IbListener> ibListeners;
    @Autowired private MessageSender messageSender;

    private final Map<String, IbConnection> ibConnectionMap = new HashMap<>(); // accountId --> ibConnection
    private final Map<String, List<Position>> positionMap = new HashMap<>(); // accountId --> positions
    private final Map<Integer, Position> historicalDataRequestMap = new HashMap<>(); // ib request id -> position

    private final AtomicInteger requestIdGenerator = new AtomicInteger();
    private final DateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

    public IbConnection getIbConnection(String accountId) {
        return ibConnectionMap.get(accountId);
    }

    public List<Position> getPositions(String accountId) {
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
        CoreUtil.waitMilliseconds(1000);
        requestPositionsHistoricalData(accountId);
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
            positionMap.put(accountId, new ArrayList<>());

            c.getClientSocket().reqPositions();
        }
    }

    public void addPosition(Position position) {
        positionMap.get(position.getAccountId()).add(position);
    }

    public void positionEnd(String accountId) {
        IbConnection c = ibConnectionMap.get(accountId);

        if (c.isConnected()) {
            c.getClientSocket().cancelPositions();
        }

        List<Position> positions = positionMap.get(accountId);
        positions.sort(Comparator.comparing(Position::getAccountId));
        positions.sort(Comparator.comparing(Position::getSymbol));
    }

    public void requestPositionsHistoricalData(String accountId) {
        IbConnection c = ibConnectionMap.get(accountId);

        if (c.isConnected()) {
            log.info("requesting positions historical data for account " + accountId);
            String endDate = df.format(Calendar.getInstance().getTime()) + " " + CoreSettings.IB_TIMEZONE;

            positionMap.get(accountId).forEach(p -> historicalDataRequestMap.put(requestIdGenerator.incrementAndGet(), p));

            historicalDataRequestMap.keySet().forEach(reqId -> {
                Position p = historicalDataRequestMap.get(reqId);
                c.getClientSocket().reqHistoricalData(reqId, p.createHistDataContract(), endDate, "60 S", "1 min", p.getSecType().getIbWhatToShow(), 1, 2, null);
            });
        }
    }

    public void updateLastPrice(int reqId, double close) {
        Position position = historicalDataRequestMap.get(reqId);

        if (position != null) {
            position.setLastPrice(close);
            historicalDataRequestMap.remove(reqId);

            if (historicalDataRequestMap.isEmpty()) {
                String msg = "positions updated";
                messageSender.sendWsMessage(WS_TOPIC_IBLOGGER, msg);
                messageSender.sendJmsMesage(JMS_DEST_IBLOGGER_TO_RISKMGT, msg);
            }
        }
    }
}
