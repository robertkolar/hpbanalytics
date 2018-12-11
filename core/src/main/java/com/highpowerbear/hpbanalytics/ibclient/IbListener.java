package com.highpowerbear.hpbanalytics.ibclient;

import com.highpowerbear.hpbanalytics.common.MessageSender;
import com.highpowerbear.hpbanalytics.common.model.ExecutionDto;
import com.highpowerbear.hpbanalytics.dao.OrdTrackDao;
import com.highpowerbear.hpbanalytics.entity.IbOrder;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.OrderStatus;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.ordtrack.HeartbeatControl;
import com.highpowerbear.hpbanalytics.ordtrack.OpenOrderHandler;
import com.highpowerbear.hpbanalytics.ordtrack.OrdTrackService;
import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.SocketException;

import static com.highpowerbear.hpbanalytics.common.CoreSettings.*;

/**
 *
 * Created by robertk on 4/6/2015.
 */
@Component
@Scope("prototype")
public class IbListener extends GenericIbListener {

    private final OrdTrackDao ordTrackDao;
    private final OpenOrderHandler openOrderHandler;
    private final HeartbeatControl heartbeatControl;
    private final MessageSender messageSender;

    private IbController ibController; // prevent circular dependency
    private OrdTrackService ordTrackService; // prevent circular dependency

    private String accountId;

    @Autowired
    public IbListener(OrdTrackDao ordTrackDao, OpenOrderHandler openOrderHandler, HeartbeatControl heartbeatControl, MessageSender messageSender) {
        this.ordTrackDao = ordTrackDao;
        this.openOrderHandler = openOrderHandler;
        this.heartbeatControl = heartbeatControl;
        this.messageSender = messageSender;
    }

    void setIbController(IbController ibController) {
        this.ibController = ibController;
    }

    void setOrdTrackService(OrdTrackService ordTrackService) {
        this.ordTrackService = ordTrackService;
    }

    IbListener configure(String accountId) {
        this.accountId = accountId;
        return this;
    }

    @Override
    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
        super.openOrder(orderId, contract, order, orderState);
        openOrderHandler.handleOpenOrder(accountId, orderId, contract, order);
    }

    @Override
    public void orderStatus(int orderId, String status, double filled, double remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
        super.orderStatus(orderId, status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld, mktCapPrice);

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
    public void error(Exception e) {
        super.error(e);
        if (e instanceof SocketException && e.getMessage().equals("Socket closed")) {
            // TODO send WS message
        }
    }

    @Override
    public void error(int id, int errorCode, String errorMsg) {
        super.error(id, errorCode, errorMsg);
        if (errorCode == 507) {
            ibController.connectionBroken(accountId);
            // TODO send WS message
        }
    }

    @Override
    public void connectionClosed() {
        super.connectionClosed();
        // TODO send WS message
    }

    @Override
    public void connectAck() {
        super.connectAck();
        // TODO send WS message
    }

    @Override
    public void position(String account, Contract contract, double pos, double avgCost) {
        super.position(account, contract, pos, avgCost);

        int conid = contract.conid();
        String symbol = contract.localSymbol();
        String underlying = contract.symbol();
        Currency currency = Currency.valueOf(contract.currency());
        SecType secType = SecType.valueOf(contract.getSecType());
        String exchange = contract.exchange() != null ? contract.exchange() : secType.getDefaultExchange();

        ordTrackService.positionChanged(accountId, conid, secType, underlying, symbol, currency, exchange, pos);
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
