package com.highpowerbear.hpbanalytics.ibclient;

import com.highpowerbear.hpbanalytics.common.CoreSettings;
import com.highpowerbear.hpbanalytics.common.CoreUtil;
import com.highpowerbear.hpbanalytics.common.MessageSender;
import com.highpowerbear.hpbanalytics.dao.OrdTrackDao;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.ordtrack.Position;
import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.EJavaSignal;
import com.ib.client.EReaderSignal;
import com.ib.client.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.highpowerbear.hpbanalytics.common.CoreSettings.JMS_DEST_ORDTRACK_TO_RISKMGT;
import static com.highpowerbear.hpbanalytics.common.CoreSettings.WS_TOPIC_ORDTRACK;

/**
 *
 * Created by robertk on 4/6/2015.
 */
@Component
public class IbController {
    private static final Logger log = LoggerFactory.getLogger(IbController.class);

    @Autowired private OrdTrackDao ordTrackDao;
    @Autowired private Provider<IbListener> ibListeners;
    @Autowired TaskExecutor taskExecutor;
    @Autowired private MessageSender messageSender;

    private final Map<String, IbConnection> ibConnectionMap = new ConcurrentHashMap<>(); // accountId --> ibConnection
    private final Map<String, List<Position>> positionMap = new ConcurrentHashMap<>(); // accountId --> positions
    private final Map<String, Map<Integer, Position>> ibRequestPositionMap = new ConcurrentHashMap<>(); // accountId -> (ib request id -> position)
    private final Map<Integer, String> ibRequestUnderlyingMap = new ConcurrentHashMap<>(); // ib request id -> underlying
    private final Map<String, Double> underlyingPriceMap = new ConcurrentHashMap<>(); // underlying -> price

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
            taskExecutor.execute(() -> {
                log.info("requesting positions for account " + accountId);
                positionMap.put(accountId, new ArrayList<>());
                ibRequestPositionMap.put(accountId, new HashMap<>());

                c.getClientSocket().reqPositions();

                CoreUtil.waitMilliseconds(3000);
                log.info("requesting positions historical data for account " + accountId);

                String endDate = df.format(Calendar.getInstance().getTime()) + " " + CoreSettings.IB_TIMEZONE;

                positionMap.get(accountId).forEach(p -> {
                    ibRequestPositionMap.get(accountId).put(requestIdGenerator.incrementAndGet(), p);

                    if (p.getSecType() == SecType.OPT) {
                        String underlying = p.getUnderlying();

                        if (!ibRequestUnderlyingMap.values().contains(underlying)) {
                            ibRequestUnderlyingMap.put(requestIdGenerator.incrementAndGet(), underlying);
                        }
                    }
                });

                ibRequestUnderlyingMap.keySet().forEach(reqId -> {
                    String undl = ibRequestUnderlyingMap.get(reqId);
                    c.getClientSocket().reqHistoricalData(reqId, createStockContract(undl), endDate, "60 S", "1 min", SecType.STK.getIbWhatToShow(), 1, 2, null);
                });

                CoreUtil.waitMilliseconds(2000);

                ibRequestPositionMap.get(accountId).keySet().forEach(reqId -> {
                    Position p = ibRequestPositionMap.get(accountId).get(reqId);
                    Contract contract = createHistDataContract(p);
                    String whatToShow = SecType.valueOf(contract.getSecType()).getIbWhatToShow();
                    c.getClientSocket().reqHistoricalData(reqId, contract, endDate, "60 S", "1 min", whatToShow, 1, 2, null);
                });

                CoreUtil.waitMilliseconds(5000);

                positionMap.get(accountId).stream().filter(p -> p.getSecType() == SecType.OPT).forEach(p -> p.setUnderlyingPrice(underlyingPriceMap.get(p.getUnderlying())));

                String msg = "positions updated for account: " + accountId;
                messageSender.sendWsMessage(WS_TOPIC_ORDTRACK, msg);
                messageSender.sendJmsMesage(JMS_DEST_ORDTRACK_TO_RISKMGT, msg);
            });
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

    public void updateLastPrice(String accountId, int reqId, double close) {

        if (ibRequestUnderlyingMap.containsKey(reqId)) {
            underlyingPriceMap.put(ibRequestUnderlyingMap.get(reqId), close);
            ibRequestUnderlyingMap.remove(reqId);

        } else if (ibRequestPositionMap.get(accountId).containsKey(reqId)) {
            ibRequestPositionMap.get(accountId).get(reqId).setLastPrice(close);
            ibRequestPositionMap.get(accountId).remove(reqId);
        }
    }

    private Contract createHistDataContract(Position p) {
        Contract contract = new Contract();

        contract.symbol(p.getUnderlying());
        contract.localSymbol(p.getSymbol());
        contract.currency(p.getCurrency().name());
        contract.exchange(p.getExchange());
        contract.secType(p.getSecType().name());

        if (p.getSecType() == SecType.CFD) {
            if (p.getSymbol().endsWith("n")) {
                contract.localSymbol(p.getSymbol().substring(0, p.getSymbol().length() - 1));
            }
            if (p.getCurrency() == Currency.USD) {
                contract.secType(SecType.STK.name());
            }
        }
        return contract;
    }

    private Contract createStockContract(String symbol) {
        Contract contract = new Contract();

        contract.symbol(symbol);
        contract.localSymbol(symbol);
        contract.currency(Currency.USD.name());
        contract.exchange(SecType.STK.getDefaultExchange());
        contract.primaryExch("ARCA");
        contract.secType(Types.SecType.STK);

        return contract;
    }
}
