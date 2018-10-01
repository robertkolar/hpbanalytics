package com.highpowerbear.hpbanalytics.ibclient;

import com.highpowerbear.hpbanalytics.common.model.ExecutionDto;
import com.highpowerbear.hpbanalytics.common.MessageSender;
import com.highpowerbear.hpbanalytics.dao.OrdTrackDao;
import com.highpowerbear.hpbanalytics.entity.IbOrder;
import com.highpowerbear.hpbanalytics.enums.OrderStatus;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.ordtrack.HeartbeatControl;
import com.highpowerbear.hpbanalytics.ordtrack.OpenOrderHandler;
import com.highpowerbear.hpbanalytics.ordtrack.model.Position;
import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.highpowerbear.hpbanalytics.common.CoreSettings.JMS_DEST_EXECUTION_RECEIVED;
import static com.highpowerbear.hpbanalytics.common.CoreSettings.JMS_DEST_ORDER_FILLED;
import static com.highpowerbear.hpbanalytics.common.CoreSettings.WS_TOPIC_ORDTRACK;

/**
 *
 * Created by robertk on 4/6/2015.
 */
@Component
@Scope("prototype")
public class IbListener extends GenericIbListener {

    private final OrdTrackDao ordTrackDao;
    private final OpenOrderHandler openOrderHandler;
    private final IbController ibController;
    private final HeartbeatControl heartbeatControl;
    private final MessageSender messageSender;

    private final Map<Integer, Double> lastPriceMap = new ConcurrentHashMap<>();

    private String accountId;

    @Autowired
    public IbListener(OrdTrackDao ordTrackDao, OpenOrderHandler openOrderHandler, IbController ibController, HeartbeatControl heartbeatControl, MessageSender messageSender) {
        this.ordTrackDao = ordTrackDao;
        this.openOrderHandler = openOrderHandler;
        this.ibController = ibController;
        this.heartbeatControl = heartbeatControl;
        this.messageSender = messageSender;
    }

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
            if (!ibOrder.getSecType().equals(SecType.BAG.name())) {
                messageSender.sendJmsMesage(JMS_DEST_ORDER_FILLED, String.valueOf(ibOrder.getId()));
            }

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
    public void execDetails(int reqId, Contract c, com.ib.client.Execution e) {
        super.execDetails(reqId, c, e);

        long permId = (long) e.permId();
        IbOrder ibOrder = ordTrackDao.getIbOrderByPermId(accountId, permId);
        if (ibOrder == null) {
            return;
        }

        if (ibOrder.getSecType().equalsIgnoreCase(SecType.BAG.name()) && !c.getSecType().equalsIgnoreCase(SecType.BAG.name())) {

            ExecutionDto executionDto = new ExecutionDto(e.acctNumber(), permId, e.side(), (int) e.shares(), c.symbol(), c.localSymbol(), c.currency(), c.getSecType(), e.price());
            messageSender.sendJmsMesage(JMS_DEST_EXECUTION_RECEIVED, executionDto);
        }
    }
}
