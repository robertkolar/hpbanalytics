package com.highpowerbear.hpbanalytics.dao;

import com.highpowerbear.hpbanalytics.common.CoreSettings;
import com.highpowerbear.hpbanalytics.dao.filter.ExecutionFilter;
import com.highpowerbear.hpbanalytics.dao.filter.TradeFilter;
import com.highpowerbear.hpbanalytics.entity.ExchangeRate;
import com.highpowerbear.hpbanalytics.entity.Execution;
import com.highpowerbear.hpbanalytics.entity.IbOrder;
import com.highpowerbear.hpbanalytics.entity.Report;
import com.highpowerbear.hpbanalytics.entity.Trade;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.enums.TradeStatus;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import com.highpowerbear.hpbanalytics.report.ReportInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Calendar;
import java.util.List;

/**
 *
 * Created by robertk on 12/16/2017.
 */
@Repository
@Transactional(propagation = Propagation.SUPPORTS)
public class ReportDaoImpl implements ReportDao {

    @PersistenceContext
    private EntityManager em;

    @Autowired private QueryBuilder queryBuilder;

    @Override
    public IbOrder findIbOrder(long ibOrderId) {
        return em.find(IbOrder.class, ibOrderId);
    }

    @Override
    public Report getReportByOriginAndSecType(String origin, SecType secType) {
        boolean stk = SecType.STK.equals(secType);
        boolean opt = SecType.OPT.equals(secType);
        boolean fut = SecType.FUT.equals(secType);
        boolean fx =  SecType.CASH.equals(secType);
        boolean cfd = SecType.CFD.equals(secType);

        TypedQuery<Report> q = em.createQuery("SELECT r FROM Report r WHERE r.origin = :origin" +
                (stk ? " AND r.stk IS TRUE" : "") +
                (opt ? " AND r.opt IS TRUE" : "") +
                (fut ? " AND r.fut IS TRUE" : "") +
                (fx  ? " AND r.fx  IS TRUE" : "") +
                (cfd ? " AND r.cfd IS TRUE" : ""), Report.class);

        q.setParameter("origin", origin);
        List<Report> list = q.getResultList();

        return (!list.isEmpty() ? list.get(0) : null);
    }

    @Override
    public Report findReport(int reportId) {
        return em.find(Report.class, reportId);
    }

    @Transactional
    @Override
    public Report updateReport(Report report) {
        return em.merge(report);
    }

    @Transactional
    @Override
    public void deleteReport(int reportId) {
        Report report = em.find(Report.class, reportId);
        deleteAllTrades(reportId);
        getExecutions(reportId).forEach(em::remove);

        em.remove(report);
    }

    @Override
    public List<Report> getReports() {
        TypedQuery<Report> q = em.createQuery("SELECT r FROM Report r ORDER BY r.id", Report.class);
        return q.getResultList();
    }

    @Override
    public ReportInfoVO getReportInfo(int reportId) {
        TypedQuery<Long> query = em.createQuery("SELECT COUNT(e) FROM Execution e WHERE e.report.id = :reportId", Long.class);
        query.setParameter("reportId", reportId);

        long numExecutions = query.getSingleResult();

        query = em.createQuery("SELECT COUNT(t) FROM Trade t WHERE t.report.id = :reportId", Long.class);
        query.setParameter("reportId", reportId);

        long numTrades = query.getSingleResult();

        query = em.createQuery("SELECT COUNT(t) FROM Trade t WHERE t.report.id = :reportId AND t.status = :tradeStatus", Long.class);
        query.setParameter("reportId", reportId);
        query.setParameter("tradeStatus", TradeStatus.OPEN);

        long numOpenTrades = query.getSingleResult();

        query = em.createQuery("SELECT COUNT(DISTINCT e.underlying) FROM Execution e WHERE e.report.id = :reportId", Long.class);
        query.setParameter("reportId", reportId);

        long numUnderlyings = query.getSingleResult();

        query = em.createQuery("SELECT COUNT(DISTINCT se.execution.underlying) FROM SplitExecution se WHERE se.execution.report.id = :reportId AND se.trade.status = :tradeStatus", Long.class);

        query.setParameter("reportId", reportId);
        query.setParameter("tradeStatus", TradeStatus.OPEN);

        long numOpenUnderlyings = query.getSingleResult();

        TypedQuery<Calendar> calQuery = em.createQuery("SELECT MIN(e.fillDate) FROM Execution e WHERE e.report.id = :reportId", Calendar.class);
        calQuery.setParameter("reportId", reportId);

        Calendar firstExecutionDate = calQuery.getResultList().get(0);

        calQuery = em.createQuery("SELECT MAX(e.fillDate) FROM Execution e WHERE e.report.id = :reportId", Calendar.class);
        calQuery.setParameter("reportId", reportId);

        Calendar lastExecutionDate = calQuery.getResultList().get(0);

        return new ReportInfoVO(numExecutions, numTrades, numOpenTrades, numUnderlyings, numOpenUnderlyings, firstExecutionDate, lastExecutionDate);
    }

    @Override
    public List<Execution> getExecutions(int reportId) {
        TypedQuery<Execution> q = em.createQuery("SELECT e FROM Execution e WHERE e.report.id = :reportId ORDER BY e.fillDate ASC", Execution.class);
        q.setParameter("reportId", reportId);

        return q.getResultList();
    }

    @Override
    public List<Execution> getExecutionsAfterDate(int reportId, Calendar date, String symbol) {
        TypedQuery<Execution> q = em.createQuery("SELECT e FROM Execution e WHERE e.report.id = :reportId AND e.fillDate > :date AND e.symbol = :symbol ORDER BY e.fillDate ASC", Execution.class);

        q.setParameter("reportId", reportId);
        q.setParameter("date", date);
        q.setParameter("symbol", symbol);

        return q.getResultList();
    }

    @Override
    public List<Execution> getExecutionsAfterDateInclusive(int reportId, Calendar date, String symbol) {
        TypedQuery<Execution> q = em.createQuery("SELECT e FROM Execution e WHERE e.report.id = :reportId AND e.fillDate >= :date AND e.symbol = :symbol ORDER BY e.fillDate ASC", Execution.class);

        q.setParameter("reportId", reportId);
        q.setParameter("date", date);
        q.setParameter("symbol", symbol);

        return q.getResultList();
    }

    @Override
    public boolean existsExecution(long executionId) {
        Execution execution = em.find(Execution.class, executionId);
        return (execution != null);
    }

    @Override
    public Execution findExecution(long executionId) {
        return em.find(Execution.class, executionId);
    }

    @Transactional
    @Override
    public void createExecution(Execution execution) {
        em.persist(execution);
    }

    @Transactional
    @Override
    public void deleteExecution(long executionId) {
        Execution execution = em.find(Execution.class, executionId);
        em.remove(execution);
    }

    @Override
    public List<Execution> getFilteredExecutions(int reportId, ExecutionFilter filter, int start, int limit) {
        Report report = findReport(reportId);
        TypedQuery<Execution> q = queryBuilder.buildFilteredExecutionsQuery(em , report, filter);

        q.setFirstResult(start);
        q.setMaxResults(limit);

        return q.getResultList();
    }

    @Override
    public long getNumFilteredExecutions(int reportId, ExecutionFilter filter) {
        Report report = findReport(reportId);
        TypedQuery<Long> q = queryBuilder.buildFilteredExecutionsCountQuery(em , report, filter);

        return q.getSingleResult();
    }

    @Override
    public List<Trade> getTradesByUnderlying(int reportId, String underlying) {
        TypedQuery<Trade> q = em.createQuery("SELECT t FROM Trade t WHERE t.report.id = :reportId" +  (underlying != null ? " AND t.underlying = :underlying" : "") + " ORDER BY t.openDate ASC", Trade.class);
        q.setParameter("reportId", reportId);

        if (underlying != null) {
            q.setParameter("underlying", underlying);
        }
        return q.getResultList();
    }

    @Override
    public List<Trade> getTradesAffectedByExecution(int reportId, Calendar fillDate, String symbol) {
        TypedQuery<Trade> q = em.createQuery("SELECT t FROM Trade t WHERE t.report.id = :reportId AND (t.closeDate >= :fillDate OR t.status = :tradeStatus) AND t.symbol = :symbol ORDER BY t.openDate ASC", Trade.class);

        q.setParameter("tradeStatus", TradeStatus.OPEN);
        q.setParameter("reportId", reportId);
        q.setParameter("fillDate", fillDate);
        q.setParameter("symbol", symbol);

        return q.getResultList();
    }

    @Transactional
    @Override
    public void createTrades(List<Trade> trades) {
        trades.forEach(em::persist);
    }

    @Transactional
    @Override
    public void deleteAllTrades(int reportId) {
        for (Trade trade : this.getTradesByUnderlying(reportId, null)) {
            // it is managed, since trade is managed
            trade.getSplitExecutions().forEach(em::remove);
            em.remove(trade);
        }
    }

    @Transactional
    @Override
    public void deleteTrades(List<Trade> trades) {
        if (trades == null) {
            return;
        }
        for (Trade trade : trades) {
            Trade tradeDb = em.find(Trade.class, trade.getId()); // make sure it is managed by entitymanager
            // it is managed, since trade is managed
            tradeDb.getSplitExecutions().forEach(em::remove);
            em.remove(tradeDb);
        }
    }

    @Override
    public Trade findTrade(long tradeId) {
        return em.find(Trade.class, tradeId);
    }

    @Override
    public List<Trade> getFilteredTrades(int reportId, TradeFilter filter, int start, int limit) {
        Report report = findReport(reportId);
        TypedQuery<Trade> q = queryBuilder.buildFilteredTradesQuery(em, report, filter);

        q.setFirstResult(start);
        q.setMaxResults(limit);

        return q.getResultList();
    }

    @Override
    public long getNumFilteredTrades(int reportId, TradeFilter filter) {
        Report report = findReport(reportId);
        TypedQuery<Long> q = queryBuilder.buildFilteredTradesCountQuery(em, report, filter);

        return q.getSingleResult();
    }

    @Override
    public List<Trade> getTradesBetweenDates(int reportId, Calendar beginDate, Calendar endDate, TradeType tradeType) {
        TypedQuery<Trade> q = em.createQuery("SELECT t FROM Trade t WHERE t.report.id = :reportId AND t.closeDate >= :beginDate AND t.closeDate < :endDate AND t.type = :tradeType ORDER BY t.openDate ASC", Trade.class);

        q.setParameter("reportId", reportId);
        q.setParameter("beginDate", beginDate);
        q.setParameter("endDate", endDate);
        q.setParameter("tradeType", tradeType);

        return q.getResultList();
    }

    @Override
    public List<String> getUnderlyings(int reportId) {
        TypedQuery<String> query = em.createQuery("SELECT DISTINCT e.underlying FROM Execution e WHERE e.report.id = :reportId", String.class);
        query.setParameter("reportId", reportId);

        return query.getResultList();
    }

    @Override
    public ExchangeRate getExchangeRate(String date) {
        return em.find(ExchangeRate.class, date);
    }

    @Override
    public List<ExchangeRate> getAllExchangeRates() {
        TypedQuery<ExchangeRate> q = em.createQuery("SELECT er FROM ExchangeRate er", ExchangeRate.class);
        return q.getResultList();
    }

    @Transactional
    @Override
    public void createOrUpdateExchangeRate(ExchangeRate exchangeRate) {
        em.merge(exchangeRate);
    }
}