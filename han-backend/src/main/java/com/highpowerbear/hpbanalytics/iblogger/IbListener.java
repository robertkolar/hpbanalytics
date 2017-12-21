package com.highpowerbear.hpbanalytics.iblogger;

import com.highpowerbear.hpbanalytics.dao.IbLoggerDao;
import com.highpowerbear.hpbanalytics.entity.IbAccount;
import com.highpowerbear.hpbanalytics.entity.IbOrder;
import com.highpowerbear.hpbanalytics.enums.IbOrderStatus;
import com.highpowerbear.hpbanalytics.enums.OrderStatus;
import com.highpowerbear.hpbanalytics.web.WebsocketController;
import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * Created by robertk on 4/6/2015.
 */
@Component
@Scope("prototype")
public class IbListener extends GenericIbListener {

    @Autowired private IbLoggerDao ibLoggerDao;
    @Autowired private OpenOrderHandler openOrderHandler;
    @Autowired private OutputProcessor outputProcessor;
    @Autowired private IbController ibController;
    @Autowired private HeartbeatControl heartbeatControl;
    @Autowired private WebsocketController websocketController;

    private IbAccount ibAccount;

    public IbListener configure(IbAccount ibAccount) {
        this.ibAccount = ibAccount;
        return this;
    }

    @Override
    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
        super.openOrder(orderId, contract, order, orderState);
        openOrderHandler.handle(ibAccount, orderId, contract, order, orderState);
    }

    @Override
    public void orderStatus(int orderId, String status, int filled, int remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {
        super.orderStatus(orderId, status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld);
        if (!(  IbOrderStatus.SUBMITTED.getValue().equalsIgnoreCase(status) ||
                IbOrderStatus.PRESUBMITTED.getValue().equalsIgnoreCase(status) ||
                IbOrderStatus.CANCELLED.getValue().equalsIgnoreCase(status) ||
                IbOrderStatus.FILLED.getValue().equalsIgnoreCase(status))) {
            return;
        }

        IbOrder ibOrder = ibLoggerDao.getIbOrderByPermId(ibAccount, permId);
        if (ibOrder == null) {
            return;
        }

        if ((IbOrderStatus.SUBMITTED.getValue().equalsIgnoreCase(status) || IbOrderStatus.PRESUBMITTED.getValue().equalsIgnoreCase(status)) && OrderStatus.SUBMITTED.equals(ibOrder.getStatus())) {
            heartbeatControl.initHeartbeat(ibOrder);

        } else if (IbOrderStatus.FILLED.getValue().equalsIgnoreCase(status) && remaining == 0 && !OrderStatus.FILLED.equals(ibOrder.getStatus())) {
            ibOrder.addEvent(OrderStatus.FILLED, avgFillPrice);
            ibLoggerDao.updateIbOrder(ibOrder);
            heartbeatControl.removeHeartbeat(ibOrder);
            outputProcessor.processExecution(ibOrder);

        } else if (IbOrderStatus.CANCELLED.getValue().equalsIgnoreCase(status) && !OrderStatus.CANCELLED.equals(ibOrder.getStatus())) {
            ibOrder.addEvent(OrderStatus.CANCELLED, null);
            ibLoggerDao.updateIbOrder(ibOrder);
            heartbeatControl.removeHeartbeat(ibOrder);
        }
        websocketController.broadcastIbLoggerMessage("order status changed");
    }

    @Override
    public void managedAccounts(String accountsList) {
        super.managedAccounts(accountsList);
        ibController.getIbConnection(ibAccount).setAccounts(accountsList);
    }
}
