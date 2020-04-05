package com.highpowerbear.hpbanalytics.connector;

import com.highpowerbear.hpbanalytics.config.WsTopic;
import com.highpowerbear.hpbanalytics.service.MessageService;
import com.highpowerbear.hpbanalytics.ordtrack.OrdTrackService;
import com.ib.client.Contract;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.SocketException;

/**
 *
 * Created by robertk on 4/6/2015.
 */
@Component
@Scope("prototype")
public class IbListener extends GenericIbListener {

    private final IbController ibController;
    private final OrdTrackService ordTrackService;
    private final MessageService messageService;

    private String accountId;

    @Autowired
    public IbListener(IbController ibController, OrdTrackService ordTrackService, MessageService messageService) {
        this.ibController = ibController;
        this.ordTrackService = ordTrackService;
        this.messageService = messageService;
    }

    public String getAccountId() {
        return accountId;
    }

    void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
        super.openOrder(orderId, contract, order, orderState);
        ordTrackService.openOrderReceived(accountId, orderId, contract, order);
    }

    @Override
    public void orderStatus(int orderId, String status, double filled, double remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
        super.orderStatus(orderId, status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld, mktCapPrice);
        ordTrackService.orderStatusReceived(accountId, status, remaining, avgFillPrice, permId);
    }

    @Override
    public void error(Exception e) {
        super.error(e);
        if (e instanceof SocketException && e.getMessage().equals("Socket closed")) {
            messageService.sendWsMessage(WsTopic.ORDTRACK, "ibConnection disconnected");
        }
    }

    @Override
    public void error(int id, int errorCode, String errorMsg) {
        super.error(id, errorCode, errorMsg);
        if (errorCode == 507) {
            ibController.connectionBroken(accountId);
            messageService.sendWsMessage(WsTopic.ORDTRACK, "ibConnection disconnected");
        }
    }

    @Override
    public void connectionClosed() {
        super.connectionClosed();
        messageService.sendWsMessage(WsTopic.ORDTRACK, "ibConnection disconnected");
    }

    @Override
    public void connectAck() {
        super.connectAck();
        messageService.sendWsMessage(WsTopic.ORDTRACK, "ibConnection connected");
    }

    @Override
    public void position(String account, Contract contract, double pos, double avgCost) {
        super.position(account, contract, pos, avgCost);
        ordTrackService.positionReceived(accountId, contract, pos);
    }

    @Override
    public void execDetails(int reqId, Contract contract, Execution execution) {
        super.execDetails(reqId, contract, execution);
        ordTrackService.execDetailsReceived(accountId, contract, execution);
    }
}
