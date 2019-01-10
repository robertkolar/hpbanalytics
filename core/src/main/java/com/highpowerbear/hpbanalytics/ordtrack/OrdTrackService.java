package com.highpowerbear.hpbanalytics.ordtrack;

import com.highpowerbear.hpbanalytics.common.CoreSettings;
import com.highpowerbear.hpbanalytics.common.CoreUtil;
import com.highpowerbear.hpbanalytics.common.MessageService;
import com.highpowerbear.hpbanalytics.report.model.ExecutionDto;
import com.highpowerbear.hpbanalytics.connector.ConnectionListener;
import com.highpowerbear.hpbanalytics.connector.IbController;
import com.highpowerbear.hpbanalytics.dao.OrdTrackDao;
import com.highpowerbear.hpbanalytics.entity.IbAccount;
import com.highpowerbear.hpbanalytics.entity.IbOrder;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.OrderStatus;
import com.highpowerbear.hpbanalytics.enums.OrderType;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.ordtrack.model.Position;
import com.ib.client.Contract;
import com.ib.client.Execution;
import com.ib.client.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.highpowerbear.hpbanalytics.common.CoreSettings.*;

/**
 * Created by robertk on 12/11/2018.
 */
@Service
public class OrdTrackService implements ConnectionListener {
    private static final Logger log = LoggerFactory.getLogger(OrdTrackService.class);

    private final IbController ibController;
    private final OrdTrackDao ordTrackDao;
    private final MessageService messageService;

    private final Map<String, Map<Integer, Position>> positionMap = new HashMap<>(); // accountId -> (conid -> position)
    private final Map<String, Map<IbOrder, Integer>> openOrderHeartbeatMap = new HashMap<>(); // accountId --> (ibOrder --> number of failed heartbeats left before UNKNOWN)

    @Autowired
    public OrdTrackService(IbController ibController, OrdTrackDao ordTrackDao, MessageService messageService) {
        this.ibController = ibController;
        this.ordTrackDao = ordTrackDao;
        this.messageService = messageService;

        ibController.addConnectionListener(this);

        ordTrackDao.getIbAccounts().forEach(ibAccount -> openOrderHeartbeatMap.put(ibAccount.getAccountId(), new HashMap<>()));
        ordTrackDao.getIbAccounts().stream()
                .flatMap(ibAccount -> ordTrackDao.getOpenIbOrders(ibAccount.getAccountId()).stream())
                .forEach(this::initHeartbeat);
    }

    @Override
    public void postConnect(String accountId) {
        ibController.requestOpenOrders(accountId);
        ibController.requestPositions(accountId);
    }

    @Override
    public void preDisconnect(String accountId) {
        ibController.cancelPositions(accountId);
    }

    @Scheduled(fixedRate = 300000, initialDelay = 60000)
    private void performPeriodicTasks() {
        ordTrackDao.getIbAccounts().stream()
                .map(IbAccount::getAccountId)
                .filter(ibController::isConnected).forEach(accountId -> {

            updateHeartbeats(accountId);
            ibController.requestOpenOrders(accountId);
        });
    }

    private void updateHeartbeats(String accountId) {
        Map<IbOrder, Integer> hm = openOrderHeartbeatMap.get(accountId);
        Set<IbOrder> keyset = new HashSet<>(hm.keySet());

        for (IbOrder ibOrder : keyset) {
            Integer failedHeartbeatsLeft = hm.get(ibOrder);

            if (failedHeartbeatsLeft <= 0) {
                if (!OrderStatus.UNKNOWN.equals(ibOrder.getStatus())) {
                    ibOrder.addEvent(OrderStatus.UNKNOWN, null);
                    ordTrackDao.updateIbOrder(ibOrder);
                }
                hm.remove(ibOrder);
            } else {
                hm.put(ibOrder, failedHeartbeatsLeft - 1);
            }
        }
    }

    private void initHeartbeat(IbOrder ibOrder) {
        openOrderHeartbeatMap.get(ibOrder.getAccountId()).put(ibOrder, CoreSettings.MAX_ORDER_HEARTBEAT_FAILS);
    }

    private void removeHeartbeat(IbOrder ibOrder) {
        openOrderHeartbeatMap.get(ibOrder.getAccountId()).remove(ibOrder);
    }

    public Integer getHeartbeatCount(String accountId, IbOrder ibOrder) {
        return openOrderHeartbeatMap.get(accountId).get(ibOrder);
    }

    public void openOrderReceived(String accountId, int orderId, Contract contract, Order order) {
        IbAccount ibAccount = ordTrackDao.findIbAccount(accountId);

        if (!checkListenIb(ibAccount)) {
            log.info("IB listening disabled, order will be ignored");
            return;
        }
        if (!checkOrderType(order.getOrderType())) {
            log.info("unsupported order type=" + order.getOrderType() + ", order will be ignored");
            return;
        }
        if (!checkSecType(contract.getSecType())) {
            log.info("unsupported security type=" + contract.getSecType() + ", order will be ignored");
            return;
        }
        if (!checkInstrumentFilter(ibAccount, contract.getSecType())) {
            log.info("processing disabled, security type=" + contract.getSecType() + ", order will be ignored");
            return;
        }

        String underlying = contract.symbol();
        String symbol = contract.localSymbol();

        if (symbol.split(" ").length > 1) {
            symbol = CoreUtil.removeSpace(symbol);
        }

        IbOrder ibOrderDb = ordTrackDao.getIbOrderByPermId(accountId, order.permId());
        if (ibOrderDb != null) {
            updateExistingOrder(ibOrderDb, order);
        } else {
            createNewOrder(ibAccount, orderId, contract, order, underlying, symbol);
        }
    }

    public void orderStatusReceived(String accountId, String status, double remaining, double avgFillPrice, int permId) {
        if (!(  OrderStatus.SUBMITTED.getIbStatus().equals(status) ||
                OrderStatus.PRESUBMITTED.getIbStatus().equals(status) ||
                OrderStatus.CANCELLED.getIbStatus().equals(status) ||
                OrderStatus.FILLED.getIbStatus().equals(status))) {
            return;
        }

        IbOrder ibOrder = ordTrackDao.getIbOrderByPermId(accountId, (long) permId);
        if (ibOrder == null) {
            return;
        }

        if ((OrderStatus.SUBMITTED.getIbStatus().equals(status) || OrderStatus.PRESUBMITTED.getIbStatus().equals(status)) && OrderStatus.SUBMITTED.equals(ibOrder.getStatus())) {
            initHeartbeat(ibOrder);

        } else if (OrderStatus.FILLED.getIbStatus().equals(status) && remaining == 0 && !OrderStatus.FILLED.equals(ibOrder.getStatus())) {
            ibOrder.addEvent(OrderStatus.FILLED, avgFillPrice);
            ordTrackDao.updateIbOrder(ibOrder);
            removeHeartbeat(ibOrder);
            if (!ibOrder.getSecType().equals(SecType.BAG.name())) {
                messageService.sendJmsMesage(JMS_DEST_ORDER_FILLED, String.valueOf(ibOrder.getId()));
            }

        } else if (OrderStatus.CANCELLED.getIbStatus().equals(status) && !OrderStatus.CANCELLED.equals(ibOrder.getStatus())) {
            ibOrder.addEvent(OrderStatus.CANCELLED, null);
            ordTrackDao.updateIbOrder(ibOrder);
            removeHeartbeat(ibOrder);
        }
        messageService.sendWsMessage(WS_TOPIC_ORDTRACK, "order status changed");
    }

    private boolean checkListenIb(IbAccount ibAccount) {
        return ibAccount.isListen();
    }

    private boolean checkOrderType(String orderType) {
        return  OrderType.LMT.name().equalsIgnoreCase(orderType) ||
                OrderType.STP.name().equalsIgnoreCase(orderType) ||
                OrderType.MKT.name().equalsIgnoreCase(orderType);
    }

    private boolean checkSecType(String secType) {
        return  SecType.STK.name().equalsIgnoreCase(secType) ||
                SecType.FUT.name().equalsIgnoreCase(secType) ||
                SecType.OPT.name().equalsIgnoreCase(secType) ||
                SecType.CASH.name().equalsIgnoreCase(secType) ||
                SecType.CFD.name().equalsIgnoreCase(secType) ||
                SecType.BAG.name().equalsIgnoreCase(secType);
    }

    private boolean checkInstrumentFilter(IbAccount ibAccount, String secType) {
        return  (SecType.STK.name().equalsIgnoreCase(secType) && ibAccount.isStk()) ||
                (SecType.FUT.name().equalsIgnoreCase(secType) && ibAccount.isFut()) ||
                (SecType.OPT.name().equalsIgnoreCase(secType) && ibAccount.isOpt()) ||
                (SecType.CASH.name().equalsIgnoreCase(secType) && ibAccount.isFx()) ||
                (SecType.CFD.name().equalsIgnoreCase(secType) && ibAccount.isCfd()) ||
                SecType.BAG.name().equalsIgnoreCase(secType);
    }

    private void updateExistingOrder(IbOrder ibOrderDb, Order order) {
        if (ibOrderDb.getOrderId() == 0) {
            ibOrderDb.setOrderId(order.orderId());
            ordTrackDao.updateIbOrder(ibOrderDb);
        }
        if (!OrderStatus.SUBMITTED.equals(ibOrderDb.getStatus()) && !OrderStatus.UPDATED.equals(ibOrderDb.getStatus())) {
            return;
        }
        boolean update = true;

        // if order already exists, check if the lmt/stp price has been updated
        if (OrderType.LMT.name().equalsIgnoreCase(order.getOrderType()) && ibOrderDb.getOrderPrice() != order.lmtPrice()) {
            ibOrderDb.setOrderPrice(order.lmtPrice());
        } else if (OrderType.STP.name().equalsIgnoreCase(order.getOrderType()) && ibOrderDb.getOrderPrice() != order.auxPrice()) {
            ibOrderDb.setOrderPrice(order.auxPrice());
        } else {
            update = false;
        }

        if (update) {
            ibOrderDb.addEvent(OrderStatus.UPDATED, ibOrderDb.getOrderPrice());
            ordTrackDao.updateIbOrder(ibOrderDb);
        }
    }

    private void createNewOrder(IbAccount ibAccount, int orderId, Contract contract, Order order, String underlying, String symbol) {
        IbOrder ibOrder = new IbOrder();
        ibOrder.setPermId((long) order.permId());
        ibOrder.setOrderId(orderId);
        ibOrder.setClientId(order.clientId());
        ibOrder.setAccountId(ibAccount.getAccountId());
        ibOrder.setAction(order.getAction());
        ibOrder.setQuantity((int) order.totalQuantity());
        ibOrder.setUnderlying(underlying);
        ibOrder.setCurrency(contract.currency());
        ibOrder.setSymbol(SecType.BAG.name().equals(contract.getSecType()) ? underlying : symbol);
        ibOrder.setSecType(contract.getSecType());
        ibOrder.setOrderType(order.getOrderType());

        if (OrderType.LMT.name().equalsIgnoreCase(order.getOrderType())) {
            ibOrder.setOrderPrice(order.lmtPrice());
        } else if (OrderType.STP.name().equalsIgnoreCase(order.getOrderType())) {
            ibOrder.setOrderPrice(order.auxPrice());
        }

        ibOrder.setTif(order.getTif());
        ibOrder.setParentId(order.parentId());
        ibOrder.setOcaGroup(order.ocaGroup());
        ibOrder.addEvent(OrderStatus.SUBMITTED, ibOrder.getOrderPrice());
        ordTrackDao.newIbOrder(ibOrder);
        initHeartbeat(ibOrder);
    }

    public void positionReceived(String accountId, Contract contract, double positionSize) {
        int conid = contract.conid();
        String symbol = contract.localSymbol();
        String underlyingSymbol = contract.symbol();
        Currency currency = Currency.valueOf(contract.currency());
        SecType secType = SecType.valueOf(contract.getSecType());
        String exchange = contract.exchange() != null ? contract.exchange() : secType.getDefaultExchange();

        positionMap.computeIfAbsent(accountId, k -> new HashMap<>());
        Position position = positionMap.get(accountId).get(conid);
        boolean send = true;

        if (position == null && positionSize != 0) {
            positionMap.get(accountId).put(conid, new Position(accountId, conid, secType, underlyingSymbol, symbol, currency, exchange, positionSize));
        } else if (positionSize != 0){
            position.setSize(positionSize);
        } else if (positionMap.get(accountId).containsKey(conid)) {
            positionMap.get(accountId).remove(conid);
        } else {
            send = false;
        }
        if (send) {
            messageService.sendWsMessage(WS_TOPIC_ORDTRACK, "position changed " + symbol);
        }
    }

    public void execDetailsReceived(String accountId, Contract contract, Execution execution) {
        long permId = (long) execution.permId();

        IbOrder ibOrder = ordTrackDao.getIbOrderByPermId(accountId, permId);
        if (ibOrder == null) {
            return;
        }

        if (ibOrder.getSecType().equalsIgnoreCase(SecType.BAG.name()) && !contract.getSecType().equalsIgnoreCase(SecType.BAG.name())) {
            ExecutionDto executionDto = new ExecutionDto(
                    execution.acctNumber(),
                    permId, execution.side(),
                    (int) execution.shares(),
                    contract.symbol(),
                    contract.localSymbol(),
                    contract.currency(),
                    contract.getSecType(),
                    execution.price());

            messageService.sendJmsMesage(JMS_DEST_EXECUTION_RECEIVED, executionDto);
        }
    }

    public List<Position> getPositions(String accountId) {
        if (positionMap.get(accountId) == null) {
            return null;
        }
        List<Position> positions = new ArrayList<>(positionMap.get(accountId).values());
        positions.sort(Comparator.comparing(Position::getSymbol));
        return positions;
    }
}
