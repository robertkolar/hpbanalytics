package com.highpowerbear.hpbanalytics.iblogger;

import com.highpowerbear.hpbanalytics.common.HanUtil;
import com.highpowerbear.hpbanalytics.dao.IbLoggerDao;
import com.highpowerbear.hpbanalytics.entity.IbAccount;
import com.highpowerbear.hpbanalytics.entity.IbOrder;
import com.highpowerbear.hpbanalytics.enums.OrderStatus;
import com.highpowerbear.hpbanalytics.enums.OrderType;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.ib.client.Contract;
import com.ib.client.Order;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by robertk on 4/6/2015.
 */
@Service
public class OpenOrderHandler {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(OpenOrderHandler.class);

    @Autowired private IbLoggerDao ibLoggerDao;
    @Autowired private HeartbeatControl heartbeatControl;

    public void handleOpenOrder(IbAccount ibAccount, int orderId, Contract contract, Order order) {
        if (!checkListenIb(ibAccount)) {
            log.info("IB listening disabled, order will be ignored");
            return;
        }
        if (!(ibAccount.mayProcessAccount(order.m_account))) {
            log.info("Account filter active, account=" + order.m_account + ", permitted=" + ibAccount.getPermittedAccounts() + ", order will be ignored");
            return;
        }
        if (!(ibAccount.mayProcessClient(order.m_clientId))) {
            log.info("Account filter active, clientId=" + order.m_clientId + ", permitted=" + ibAccount.getPermittedClients() + ", order will be ignored");
            return;
        }
        if (!checkOrderType(order.m_orderType)) {
            log.info("Unsupported order type=" + order.m_orderType + ", order will be ignored");
            return;
        }
        if (!checkSecType(contract.m_secType)) {
            log.info("Unsupported security type=" + contract.m_secType + ", order will be ignored");
            return;
        }
        if (!checkInstrumentFilter(ibAccount, contract.m_secType)) {
            log.info("Processing disabled, security type=" + contract.m_secType + ", order will be ignored");
            return;
        }

        String underlying = contract.m_symbol;
        String symbol = contract.m_localSymbol;

        if (symbol.split(" ").length > 1) {
            symbol = HanUtil.removeSpace(symbol);
        }

        IbOrder ibOrderDb = ibLoggerDao.getIbOrderByPermId(ibAccount, order.m_permId);
        if (ibOrderDb != null) {
            updateExistingOrder(ibOrderDb, order);
        } else {
            createNewOrder(ibAccount, orderId, contract, order, underlying, symbol);
        }
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
                SecType.CFD.name().equalsIgnoreCase(secType);
    }

    private boolean checkInstrumentFilter(IbAccount ibAccount, String secType) {
        return  (SecType.STK.name().equalsIgnoreCase(secType) && ibAccount.isStk()) ||
                (SecType.FUT.name().equalsIgnoreCase(secType) && ibAccount.isFut()) ||
                (SecType.OPT.name().equalsIgnoreCase(secType) && ibAccount.isOpt()) ||
                (SecType.CASH.name().equalsIgnoreCase(secType) && ibAccount.isFx()) ||
                (SecType.CFD.name().equalsIgnoreCase(secType) && ibAccount.isCfd());
    }

    private void updateExistingOrder(IbOrder ibOrderDb, Order order) {
        if (ibOrderDb.getOrderId() == 0) {
            ibOrderDb.setOrderId(order.m_orderId);
            ibLoggerDao.updateIbOrder(ibOrderDb);
        }
        if (!OrderStatus.SUBMITTED.equals(ibOrderDb.getStatus()) && !OrderStatus.UPDATED.equals(ibOrderDb.getStatus())) {
            return;
        }
        boolean update = true;

        // if order already exists, check if the lmt/stp price has been updated
        if (OrderType.LMT.name().equalsIgnoreCase(order.m_orderType) && ibOrderDb.getOrderPrice() != order.m_lmtPrice) {
            ibOrderDb.setOrderPrice(order.m_lmtPrice);
        } else if (OrderType.STP.name().equalsIgnoreCase(order.m_orderType) && ibOrderDb.getOrderPrice() != order.m_auxPrice) {
            ibOrderDb.setOrderPrice(order.m_auxPrice);
        } else {
            update = false;
        }

        if (update) {
            ibOrderDb.addEvent(OrderStatus.UPDATED, ibOrderDb.getOrderPrice());
            ibLoggerDao.updateIbOrder(ibOrderDb);
        }
    }

    private void createNewOrder(IbAccount ibAccount, int orderId, Contract contract, Order order, String underlying, String symbol) {
        IbOrder ibOrder = new IbOrder();
        ibOrder.setPermId(order.m_permId);
        ibOrder.setOrderId(orderId);
        ibOrder.setClientId(order.m_clientId);
        ibOrder.setIbAccount(ibAccount);
        ibOrder.setAction(order.m_action);
        ibOrder.setQuantity(order.m_totalQuantity);
        ibOrder.setUnderlying(underlying);
        ibOrder.setCurrency(contract.m_currency);
        ibOrder.setSymbol(symbol);
        ibOrder.setSecType(contract.m_secType);
        ibOrder.setOrderType(order.m_orderType);

        if (OrderType.LMT.name().equalsIgnoreCase(order.m_orderType)) {
            ibOrder.setOrderPrice(order.m_lmtPrice);
        } else if (OrderType.STP.name().equalsIgnoreCase(order.m_orderType)) {
            ibOrder.setOrderPrice(order.m_auxPrice);
        }

        ibOrder.setTif(order.m_tif);
        ibOrder.setParentId(order.m_parentId);
        ibOrder.setOcaGroup(order.m_ocaGroup);
        ibOrder.addEvent(OrderStatus.SUBMITTED, ibOrder.getOrderPrice());
        ibLoggerDao.newIbOrder(ibOrder);
        heartbeatControl.initHeartbeat(ibOrder);
    }
}
