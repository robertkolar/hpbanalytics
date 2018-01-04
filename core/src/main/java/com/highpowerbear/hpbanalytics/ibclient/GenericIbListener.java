package com.highpowerbear.hpbanalytics.ibclient;

import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.DeltaNeutralContract;
import com.ib.client.EWrapper;
import com.ib.client.EWrapperMsgGenerator;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.SoftDollarTier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 *
 * Created by robertk on 4/6/2015.
 */
public class GenericIbListener implements EWrapper {
    private static final Logger log = LoggerFactory.getLogger(GenericIbListener.class);

    @Override
    public void bondContractDetails(int reqId, ContractDetails contractDetails) {
        log.info(EWrapperMsgGenerator.bondContractDetails(reqId, contractDetails));
    }

    @Override
    public void contractDetails(int reqId, ContractDetails contractDetails) {
        log.info(EWrapperMsgGenerator.contractDetails(reqId, contractDetails));
    }

    @Override
    public void	contractDetailsEnd(int reqId) {
        log.info(EWrapperMsgGenerator.contractDetailsEnd(reqId));
    }

    @Override
    public void fundamentalData(int reqId, String data) {
        log.info(EWrapperMsgGenerator.fundamentalData(reqId, data));
    }

    @Override
    public void currentTime(long time) {
        log.info(EWrapperMsgGenerator.currentTime(time));
    }

    @Override
    public void execDetails(int reqId, Contract contract, Execution execution) {
        log.info(EWrapperMsgGenerator.execDetails(reqId, contract, execution));
    }

    @Override
    public void execDetailsEnd( int reqId){
        log.info(EWrapperMsgGenerator.execDetailsEnd(reqId));
    }

    @Override
    public void historicalData(int reqId, String date, double open, double high, double low, double close, int volume, int count, double WAP, boolean hasGaps) {
        log.info(EWrapperMsgGenerator.historicalData(reqId, date, open, high, low, close, volume, count, WAP, hasGaps));
    }

    @Override
    public void managedAccounts(String accountsList) {
        log.info(EWrapperMsgGenerator.managedAccounts(accountsList));
    }

    @Override
    public void nextValidId(int orderId) {
        log.info(EWrapperMsgGenerator.nextValidId(orderId));
    }

    @Override
    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
        log.info(EWrapperMsgGenerator.openOrder(orderId, contract, order, orderState));
    }

    @Override
    public void openOrderEnd() {
        log.info(EWrapperMsgGenerator.openOrderEnd());
    }

    @Override
    public void orderStatus(int orderId, String status, double filled, double remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {
        log.info(EWrapperMsgGenerator.orderStatus(orderId, status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld));
    }

    @Override
    public void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume, double wap, int count) {
        log.info(EWrapperMsgGenerator.realtimeBar(reqId, time, open, high, low, close, volume, wap, count));
    }

    @Override
    public void receiveFA(int faDataType, String xml) {
        log.info(EWrapperMsgGenerator.receiveFA(faDataType, xml));
    }

    @Override
    public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance, String benchmark, String projection, String legsStr) {
        log.info(EWrapperMsgGenerator.scannerData(reqId, rank, contractDetails, distance, benchmark, projection, legsStr));
    }

    @Override
    public void scannerDataEnd(int reqId) {
        log.info(EWrapperMsgGenerator.scannerDataEnd(reqId));
    }

    @Override
    public void scannerParameters(String xml) {
        log.info(EWrapperMsgGenerator.scannerParameters(xml));
    }

    @Override
    public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints, double impliedFuture, int holdDays, String futureExpiry, double dividendImpact, double dividendsToExpiry) {
        log.info(EWrapperMsgGenerator.tickEFP(tickerId, tickType, basisPoints, formattedBasisPoints, impliedFuture, holdDays, futureExpiry, dividendImpact, dividendsToExpiry));
    }

    @Override
    public void tickGeneric(int tickerId, int tickType, double value) {
        log.info(EWrapperMsgGenerator.tickGeneric(tickerId, tickType, value));
    }

    @Override
    public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double optPrice, double pvDividend, double gamma, double vega, double theta, double undPrice) {
        log.info(EWrapperMsgGenerator.tickOptionComputation(tickerId, field, impliedVol, delta, optPrice, pvDividend, gamma, vega, theta, undPrice));
    }

    @Override
    public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
        log.info(EWrapperMsgGenerator.tickPrice(tickerId, field, price, canAutoExecute));
    }

    @Override
    public void tickSize(int tickerId, int field, int size) {
        log.info(EWrapperMsgGenerator.tickSize(tickerId, field, size));
    }

    @Override
    public void tickString(int tickerId, int tickType, String value) {
        log.info(EWrapperMsgGenerator.tickString(tickerId, tickType, value));
    }

    @Override
    public void updateAccountTime(String timeStamp) {
        log.info(EWrapperMsgGenerator.updateAccountTime(timeStamp));
    }

    @Override
    public void updateAccountValue(String key, String value, String currency, String accountName) {
        log.info(EWrapperMsgGenerator.updateAccountValue(key, value, currency, accountName));
    }

    @Override
    public void accountDownloadEnd(String accountName) {
        log.info(EWrapperMsgGenerator.accountDownloadEnd(accountName));
    }

    @Override
    public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {
        log.info(EWrapperMsgGenerator.updateMktDepth(tickerId, position, operation, side, price, size));
    }

    @Override
    public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price, int size) {
        log.info(EWrapperMsgGenerator.updateMktDepthL2(tickerId, position, marketMaker, operation, side, price, size));
    }

    @Override
    public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
        log.info(EWrapperMsgGenerator.updateNewsBulletin(msgId, msgType, message, origExchange));
    }

    @Override
    public void updatePortfolio(Contract contract, double position, double marketPrice, double marketValue, double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
        log.info(EWrapperMsgGenerator.updatePortfolio(contract, (int) position, marketPrice, marketValue, averageCost, unrealizedPNL, realizedPNL, accountName));
    }

    @Override
    public void connectionClosed() {
        log.info(EWrapperMsgGenerator.connectionClosed());
    }

    @Override
    public void error(Exception e) {
        log.info(EWrapperMsgGenerator.error(e));
    }

    @Override
    public void error(String str) {
        log.info(EWrapperMsgGenerator.error(str));
    }

    @Override
    public void error(int id, int errorCode, String errorMsg) {
        log.info(EWrapperMsgGenerator.error(id, errorCode, errorMsg));
    }

    @Override
    public void deltaNeutralValidation(int reqId, DeltaNeutralContract underComp) {
        log.info(EWrapperMsgGenerator.deltaNeutralValidation(reqId, underComp));
    }

    @Override
    public void tickSnapshotEnd(int reqId) {
        log.info(EWrapperMsgGenerator.tickSnapshotEnd(reqId));
    }
    
    @Override
    public void marketDataType(int reqId, int marketDataType) {
        log.info(EWrapperMsgGenerator.marketDataType(reqId, marketDataType));
    }
    
    @Override
    public void commissionReport(CommissionReport commissionReport) {
        log.info(EWrapperMsgGenerator.commissionReport(commissionReport));
    }

    @Override
    public void position(String account, Contract contract, double pos, double avgCost) {
        log.info(EWrapperMsgGenerator.position(account, contract, pos, avgCost));
    }

    @Override
    public void positionEnd() {
        log.info(EWrapperMsgGenerator.positionEnd());
    }

    @Override
    public void accountSummary(int reqId, String account, String tag, String value, String currency) {
        log.info(EWrapperMsgGenerator.accountSummary(reqId, account, tag, value, currency));
    }

    @Override
    public void accountSummaryEnd(int reqId) {
        log.info(EWrapperMsgGenerator.accountSummaryEnd(reqId));
    }

    @Override
    public void verifyMessageAPI(String apiData) {
    }

    @Override
    public void verifyCompleted(boolean isSuccessful, String errorText) {
    }

    @Override
    public void displayGroupList(int reqId, String groups) {
    }

    @Override
    public void displayGroupUpdated(int reqId, String contractInfo) {
    }

    // API 9.72
    @Override
    public void verifyAndAuthCompleted(boolean b, String s) {
    }

    @Override
    public void positionMulti(int i, String s, String s1, Contract contract, double v, double v1) {
    }

    @Override
    public void positionMultiEnd(int i) {
    }

    @Override
    public void verifyAndAuthMessageAPI(String s, String s1) {
    }

    @Override
    public void connectAck() {
    }

    @Override
    public void accountUpdateMulti(int i, String s, String s1, String s2, String s3, String s4) {
    }

    @Override
    public void accountUpdateMultiEnd(int i) {
    }

    @Override
    public void securityDefinitionOptionalParameter(int i, String s, int i1, String s1, String s2, Set<String> set, Set<Double> set1) {
    }

    @Override
    public void securityDefinitionOptionalParameterEnd(int i) {
    }

    @Override
    public void softDollarTiers(int i, SoftDollarTier[] softDollarTiers) {
    }
}
