package com.highpowerbear.hpbanalytics.ibclient;

import com.ib.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * Created by robertk on 11/5/2018.
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
    public void historicalData(int reqId, Bar bar) {
        log.info(EWrapperMsgGenerator.historicalData(reqId, bar.time(), bar.open(), bar.high(), bar.low(), bar.close(), bar.volume(), bar.count(), bar.wap()));
    }

    @Override
    public void historicalDataEnd(int reqId, String startDateStr, String endDateStr) {
        log.info(EWrapperMsgGenerator.historicalDataEnd(reqId, startDateStr, endDateStr));
    }

    @Override
    public void historicalDataUpdate(int reqId, Bar bar) {
        log.info(EWrapperMsgGenerator.historicalData(reqId, bar.time(), bar.open(), bar.high(), bar.low(), bar.close(), bar.volume(), bar.count(), bar.wap()));
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
    public void orderStatus(int orderId, String status, double filled, double remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
        log.info(EWrapperMsgGenerator.orderStatus(orderId, status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld, mktCapPrice));
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
        //log.info(EWrapperMsgGenerator.tickEFP(tickerId, tickType, basisPoints, formattedBasisPoints, impliedFuture, holdDays, futureExpiry, dividendImpact, dividendsToExpiry));
    }

    @Override
    public void tickGeneric(int tickerId, int tickType, double value) {
        //log.info(EWrapperMsgGenerator.tickGeneric(tickerId, tickType, value));
    }

    @Override
    public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double optPrice, double pvDividend, double gamma, double vega, double theta, double undPrice) {
        //log.info(EWrapperMsgGenerator.tickOptionComputation(tickerId, field, impliedVol, delta, optPrice, pvDividend, gamma, vega, theta, undPrice));
    }

    @Override
    public void tickPrice(int tickerId, int field, double price, TickAttrib attrib) {
        //log.info(EWrapperMsgGenerator.tickPrice(tickerId, field, price, attrib));
    }

    @Override
    public void tickSize(int tickerId, int field, int size) {
        //log.info(EWrapperMsgGenerator.tickSize(tickerId, field, size));
    }

    @Override
    public void tickString(int tickerId, int tickType, String value) {
        //log.info(EWrapperMsgGenerator.tickString(tickerId, tickType, value));
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
    public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price, int size, boolean isSmartDepth) {
        log.info(EWrapperMsgGenerator.updateMktDepthL2(tickerId, position, marketMaker, operation, side, price, size, isSmartDepth));
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
        log.info("verifyMessageAPI, apiData= " + apiData);
    }

    @Override
    public void verifyCompleted(boolean isSuccessful, String errorText) {
        log.info("verifyCompleted, isSuccessful=" + isSuccessful + ", errorText=" + errorText);
    }

    @Override
    public void displayGroupList(int reqId, String groups) {
        log.info("displayGroupList, reqId=" + reqId + ", groups=" + groups);
    }

    @Override
    public void displayGroupUpdated(int reqId, String contractInfo) {
        log.info("displayGroupUpdated, reqId=" + reqId + ", contractInfo=" + contractInfo);
    }

    @Override
    public void verifyAndAuthCompleted(boolean isSuccessful, String errorText) {
        log.info("verifyAndAuthCompleted, isSuccessful=" + isSuccessful + ", errorText=" + errorText);
    }

    @Override
    public void positionMulti(int reqId, String account, String modelCode, Contract contract, double pos, double avgCost) {
        log.info(EWrapperMsgGenerator.positionMulti(reqId, account, modelCode, contract, pos, avgCost));
    }

    @Override
    public void positionMultiEnd(int reqId) {
        log.info(EWrapperMsgGenerator.positionMultiEnd(reqId));
    }

    @Override
    public void verifyAndAuthMessageAPI(String apiData, String xyzChallenge) {
        log.info("verifyAndAuthMessageAPI, apiData=" + apiData + ", xyzChallenge=" + xyzChallenge);
    }

    @Override
    public void connectAck() {
        log.info("connectAck");
    }

    @Override
    public void accountUpdateMulti(int reqId, String account, String modelCode, String key, String value, String currency) {
        log.info(EWrapperMsgGenerator.accountUpdateMulti(reqId, account, modelCode, key, value, currency));
    }

    @Override
    public void accountUpdateMultiEnd(int reqId) {
        log.info(EWrapperMsgGenerator.accountUpdateMultiEnd(reqId));
    }

    @Override
    public void securityDefinitionOptionalParameter(int reqId, String exchange, int underlyingConId, String tradingClass, String multiplier, Set<String> expirations, Set<Double> strikes) {
        log.info(EWrapperMsgGenerator.securityDefinitionOptionalParameter(reqId, exchange, underlyingConId, tradingClass, multiplier, expirations, strikes));
    }

    @Override
    public void securityDefinitionOptionalParameterEnd(int reqId) {
        log.info(EWrapperMsgGenerator.securityDefinitionOptionalParameterEnd(reqId));
    }

    @Override
    public void softDollarTiers(int reqId, SoftDollarTier[] tiers) {
        log.info(EWrapperMsgGenerator.softDollarTiers(tiers));
    }

    @Override
    public void familyCodes(FamilyCode[] familyCodes) {
        log.info(EWrapperMsgGenerator.familyCodes(familyCodes));
    }

    @Override
    public void symbolSamples(int reqId, ContractDescription[] contractDescriptions) {
        log.info(EWrapperMsgGenerator.symbolSamples(reqId, contractDescriptions));
    }

    @Override
    public void mktDepthExchanges(DepthMktDataDescription[] depthMktDataDescriptions) {
        log.info(EWrapperMsgGenerator.mktDepthExchanges(depthMktDataDescriptions));
    }

    @Override
    public void tickNews(int tickerId, long timeStamp, String providerCode, String articleId, String headline, String extraData) {
        log.info(EWrapperMsgGenerator.tickNews(tickerId, timeStamp, providerCode, articleId, headline, extraData));
    }

    @Override
    public void smartComponents(int reqId, Map<Integer, Map.Entry<String, Character>> theMap) {
        log.info(EWrapperMsgGenerator.smartComponents(reqId, theMap));
    }

    @Override
    public void tickReqParams(int tickerId, double minTick, String bboExchange, int snapshotPermissions) {
        log.info(EWrapperMsgGenerator.tickReqParams(tickerId, minTick, bboExchange, snapshotPermissions));
    }

    @Override
    public void newsProviders(NewsProvider[] newsProviders) {
        log.info(EWrapperMsgGenerator.newsProviders(newsProviders));
    }

    @Override
    public void newsArticle(int requestId, int articleType, String articleText) {
        log.info(EWrapperMsgGenerator.newsArticle(requestId, articleType, articleText));
    }

    @Override
    public void historicalNews(int requestId, String time, String providerCode, String articleId, String headline) {
        log.info(EWrapperMsgGenerator.historicalNews(requestId, time, providerCode, articleId, headline));
    }

    @Override
    public void historicalNewsEnd(int requestId, boolean hasMore) {
        log.info(EWrapperMsgGenerator.historicalNewsEnd(requestId, hasMore));
    }

    @Override
    public void headTimestamp(int reqId, String headTimestamp) {
        log.info(EWrapperMsgGenerator.headTimestamp(reqId, headTimestamp));
    }

    @Override
    public void histogramData(int reqId, List<HistogramEntry> items) {
        log.info(EWrapperMsgGenerator.histogramData(reqId, items));
    }

    @Override
    public void rerouteMktDataReq(int reqId, int conId, String exchange) {
        log.info(EWrapperMsgGenerator.rerouteMktDataReq(reqId, conId, exchange));
    }

    @Override
    public void rerouteMktDepthReq(int reqId, int conId, String exchange) {
        log.info(EWrapperMsgGenerator.rerouteMktDepthReq(reqId, conId, exchange));
    }

    @Override
    public void marketRule(int marketRuleId, PriceIncrement[] priceIncrements) {
        log.info(EWrapperMsgGenerator.marketRule(marketRuleId, priceIncrements));
    }

    @Override
    public void pnl(int reqId, double dailyPnL, double unrealizedPnL, double realizedPnL) {
        log.info(EWrapperMsgGenerator.pnl(reqId, dailyPnL, unrealizedPnL, realizedPnL));
    }

    @Override
    public void pnlSingle(int reqId, int pos, double dailyPnL, double unrealizedPnL, double realizedPnL, double value) {
        log.info(EWrapperMsgGenerator.pnlSingle(reqId, pos, dailyPnL, unrealizedPnL, realizedPnL, value));
    }

    @Override
    public void historicalTicks(int reqId, List<HistoricalTick> ticks, boolean done) {
        //
    }

    @Override
    public void historicalTicksBidAsk(int reqId, List<HistoricalTickBidAsk> ticks, boolean done) {
        //
    }

    @Override
    public void historicalTicksLast(int reqId, List<HistoricalTickLast> ticks, boolean done) {
        //
    }

    @Override
    public void tickByTickAllLast(int reqId, int tickType, long time, double price, int size, TickAttribLast tickAttribLast, String exchange, String specialConditions) {
        //
    }

    @Override
    public void tickByTickBidAsk(int reqId, long time, double bidPrice, double askPrice, int bidSize, int askSize, TickAttribBidAsk tickAttribBidAsk) {
        //
    }

    @Override
    public void tickByTickMidPoint(int reqId, long time, double midPoint) {
        //
    }

    @Override
    public void orderBound(long orderId, int apiClientId, int apiOrderId) {
        log.info(EWrapperMsgGenerator.orderBound(orderId, apiClientId, apiOrderId));
    }
}
