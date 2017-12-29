package com.highpowerbear.hpbanalytics.iblogger;

import com.highpowerbear.hpbanalytics.common.MessageSender;
import com.highpowerbear.hpbanalytics.dao.IbLoggerDao;
import com.highpowerbear.hpbanalytics.entity.IbOrder;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.OrderStatus;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.highpowerbear.hpbanalytics.common.CoreSettings.JMS_DEST_IBLOGGER_TO_REPORT;
import static com.highpowerbear.hpbanalytics.common.CoreSettings.JMS_DEST_IBLOGGER_TO_RISKMGT;
import static com.highpowerbear.hpbanalytics.common.CoreSettings.WS_TOPIC_IBLOGGER;

/**
 *
 * Created by robertk on 4/6/2015.
 */
@Component
@Scope("prototype")
public class IbListener extends GenericIbListener {

    @Autowired private IbLoggerDao ibLoggerDao;
    @Autowired private OpenOrderHandler openOrderHandler;
    @Autowired private IbController ibController;
    @Autowired private HeartbeatControl heartbeatControl;
    @Autowired private MessageSender messageSender;
    @Autowired private PositionRepository positionRepository;

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

        IbOrder ibOrder = ibLoggerDao.getIbOrderByPermId(accountId, (long) permId);
        if (ibOrder == null) {
            return;
        }

        if ((OrderStatus.SUBMITTED.getIbStatus().equals(status) || OrderStatus.PRESUBMITTED.getIbStatus().equals(status)) && OrderStatus.SUBMITTED.equals(ibOrder.getStatus())) {
            heartbeatControl.initHeartbeat(ibOrder);

        } else if (OrderStatus.FILLED.getIbStatus().equals(status) && remaining == 0 && !OrderStatus.FILLED.equals(ibOrder.getStatus())) {
            ibOrder.addEvent(OrderStatus.FILLED, avgFillPrice);
            ibLoggerDao.updateIbOrder(ibOrder);
            heartbeatControl.removeHeartbeat(ibOrder);
            messageSender.sendJmsMesage(JMS_DEST_IBLOGGER_TO_REPORT, String.valueOf(ibOrder.getId()));

        } else if (OrderStatus.CANCELLED.getIbStatus().equals(status) && !OrderStatus.CANCELLED.equals(ibOrder.getStatus())) {
            ibOrder.addEvent(OrderStatus.CANCELLED, null);
            ibLoggerDao.updateIbOrder(ibOrder);
            heartbeatControl.removeHeartbeat(ibOrder);
        }
        messageSender.sendWsMessage(WS_TOPIC_IBLOGGER, "order status changed");
    }

    @Override
    public void managedAccounts(String accountsList) {
        super.managedAccounts(accountsList);
        ibController.getIbConnection(accountId).setAccounts(accountsList);
    }

    @Override
    public void position(String account, Contract contract, double pos, double avgCost) {
        super.position(account, contract, pos, avgCost);

        PositionVO position = new PositionVO(
                contract.localSymbol(),
                contract.symbol(),
                Currency.valueOf(contract.currency()),
                SecType.valueOf(contract.getSecType()),
                pos,
                avgCost
        );

        positionRepository.addPosition(account, position);
    }

    @Override
    public void positionEnd() {
        super.positionEnd();
        messageSender.sendJmsMesage(JMS_DEST_IBLOGGER_TO_RISKMGT, String.valueOf("position end"));
    }
}
