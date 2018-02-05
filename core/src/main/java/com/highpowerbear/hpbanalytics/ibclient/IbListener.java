package com.highpowerbear.hpbanalytics.ibclient;

import com.highpowerbear.hpbanalytics.common.MessageSender;
import com.highpowerbear.hpbanalytics.dao.OrdTrackDao;
import com.highpowerbear.hpbanalytics.entity.IbOrder;
import com.highpowerbear.hpbanalytics.enums.OrderStatus;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.ordtrack.HeartbeatControl;
import com.highpowerbear.hpbanalytics.ordtrack.OpenOrderHandler;
import com.highpowerbear.hpbanalytics.ordtrack.Position;
import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.highpowerbear.hpbanalytics.common.CoreSettings.JMS_DEST_ORDTRACK_TO_REPORT;
import static com.highpowerbear.hpbanalytics.common.CoreSettings.WS_TOPIC_ORDTRACK;

/**
 *
 * Created by robertk on 4/6/2015.
 */
@Component
@Scope("prototype")
public class IbListener extends GenericIbListener {

    @Autowired private OrdTrackDao ordTrackDao;
    @Autowired private OpenOrderHandler openOrderHandler;
    @Autowired private IbController ibController;
    @Autowired private HeartbeatControl heartbeatControl;
    @Autowired private MessageSender messageSender;

    private final Map<Integer, Double> lastPriceMap = new ConcurrentHashMap<>();

    private String accountId;

    public IbListener configure(String accountId) {
        this.accountId = accountId;
        return this;
    }

    @Override
    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
        super.openOrder(orderId, contract, order, orderState);
        openOrderHandler.handleOpenOrder(accountId, orderId, contract, order);
    }

    @Override
    public void orderStatus(int orderId, String status, double filled, double remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {
        super.orderStatus(orderId, status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld);

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
            heartbeatControl.initHeartbeat(ibOrder);

        } else if (OrderStatus.FILLED.getIbStatus().equals(status) && remaining == 0 && !OrderStatus.FILLED.equals(ibOrder.getStatus())) {
            ibOrder.addEvent(OrderStatus.FILLED, avgFillPrice);
            ordTrackDao.updateIbOrder(ibOrder);
            heartbeatControl.removeHeartbeat(ibOrder);
            messageSender.sendJmsMesage(JMS_DEST_ORDTRACK_TO_REPORT, String.valueOf(ibOrder.getId()));

        } else if (OrderStatus.CANCELLED.getIbStatus().equals(status) && !OrderStatus.CANCELLED.equals(ibOrder.getStatus())) {
            ibOrder.addEvent(OrderStatus.CANCELLED, null);
            ordTrackDao.updateIbOrder(ibOrder);
            heartbeatControl.removeHeartbeat(ibOrder);
        }
        messageSender.sendWsMessage(WS_TOPIC_ORDTRACK, "order status changed");
    }

    @Override
    public void managedAccounts(String accountsList) {
        super.managedAccounts(accountsList);
        ibController.getIbConnection(accountId).setAccounts(accountsList);
    }

    @Override
    public void position(String account, Contract contract, double pos, double avgCost) {
        //super.position(account, contract, pos, avgCost);

        if (pos != 0 && !contract.getSecType().equals(SecType.CMDTY.name())) {
            ibController.addPosition(new Position(accountId, contract, pos, avgCost));
        }
    }

    @Override
    public void positionEnd() {
        super.positionEnd();

        ibController.positionEnd(accountId);
    }

    @Override
    public void historicalData(int reqId, String date, double open, double high, double low, double close, int volume, int count, double WAP, boolean hasGaps) {
        //super.historicalData(reqId, date, open, high, low, close, volume, count, WAP, hasGaps);

        if (date.startsWith("finish")) {
            ibController.updateLastPrice(accountId, reqId, lastPriceMap.get(reqId));
            lastPriceMap.remove(reqId);

        } else {
            lastPriceMap.put(reqId, close);
        }
    }
}
