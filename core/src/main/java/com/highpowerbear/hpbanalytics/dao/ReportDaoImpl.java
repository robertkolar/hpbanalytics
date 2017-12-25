package com.highpowerbear.hpbanalytics.dao;

import com.highpowerbear.hpbanalytics.common.CoreSettings;
import com.highpowerbear.hpbanalytics.dao.filter.ExecutionFilter;
import com.highpowerbear.hpbanalytics.dao.filter.TradeFilter;
import com.highpowerbear.hpbanalytics.entity.ExchangeRate;
import com.highpowerbear.hpbanalytics.entity.Execution;
import com.highpowerbear.hpbanalytics.entity.Report;
import com.highpowerbear.hpbanalytics.entity.Trade;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.enums.TradeStatus;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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
    public Report findReport(Integer id) {
        return em.find(Report.class, id);
    }

    @Transactional
    @Override
    public Report updateReport(Report report) {
        return em.merge(report);
    }

    @Transactional
    @Override
    public void deleteReport(Report report) {
        Report reportDb = em.find(Report.class, report.getId());
        this.deleteAllTrades(report);
        this.getExecutions(report).forEach(em::remove);
        em.remove(reportDb);
    }

    @Override
    public List<Report> getReports() {
        TypedQuery<Report> q = em.createQuery("SELECT r FROM Report r ORDER BY r.id", Report.class);
        return q.getResultList();
    }

    @Override
    public Calendar getFirstExecutionDate(Report report) {
        Query q = em.createQuery("SELECT MIN(e.fillDate) FROM Execution e WHERE e.report = :report");
        q.setParameter("report", report);
        return (Calendar) q.getResultList().get(0);
    }

    @Override
    public Calendar getLastExecutionDate(Report report) {
        Query q = em.createQuery("SELECT MAX(e.fillDate) FROM Execution e WHERE e.report = :report");
        q.setParameter("report", report);
        return  (Calendar) q.getResultList().get(0);
    }

    @Override
    public Long getNumExecutions(Report report) {
        Query query = em.createQuery("SELECT COUNT(e) FROM Execution e WHERE e.report = :report");
        query.setParameter("report", report);
        return (Long) query.getSingleResult();
    }

    @Override
    public List<Execution> getExecutions(Report report) {
        TypedQuery<Execution> q = em.createQuery("SELECT e FROM Execution e WHERE e.report = :report ORDER BY e.fillDate ASC", Execution.class);
        q.setParameter("report", report);
        return q.getResultList();
    }

    @Override
    public List<Execution> getExecutionsAfterExecution(Execution e) {
        TypedQuery<Execution> q = em.createQuery("SELECT e FROM Execution e WHERE e.report = :report AND e.fillDate > :eDate AND e.symbol = :eSymbol ORDER BY e.fillDate ASC", Execution.class);
        q.setParameter("report", e.getReport());
        q.setParameter("eDate", e.getFillDate());
        q.setParameter("eSymbol", e.getSymbol());
        return q.getResultList();
    }

    @Override
    public List<Execution> getExecutionsAfterExecutionInclusive(Execution e) {
        TypedQuery<Execution> q = em.createQuery("SELECT e FROM Execution e WHERE e.report= :report AND e.fillDate >= :eDate AND e.symbol = :eSymbol ORDER BY e.fillDate ASC", Execution.class);
        q.setParameter("report", e.getReport());
        q.setParameter("eDate", e.getFillDate());
        q.setParameter("eSymbol", e.getSymbol());
        return q.getResultList();
    }

    @Override
    public boolean existsExecution(Execution e) {
        Execution eDb = em.find(Execution.class, e.getId());
        return (eDb != null);
    }

    @Override
    public Execution findExecution(Long id) {
        return em.find(Execution.class, id);
    }

    @Transactional
    @Override
    public void createExecution(Execution execution) {
        em.persist(execution);
    }

    @Transactional
    @Override
    public void deleteExecution(Execution execution) {
        Execution executionDb = em.find(Execution.class, execution.getId());
        em.remove(executionDb);
    }

    @Override
    public List<Execution> getFilteredExecutions(Report report, ExecutionFilter filter, Integer start, Integer limit) {
        Query q = queryBuilder.buildFilteredExecutionsQuery(em , report, filter, false);

        q.setFirstResult(start != null ? start : 0);
        q.setMaxResults(limit != null ? limit : CoreSettings.JPA_MAX_RESULTS);

        return q.getResultList();
    }

    @Override
    public Long getNumFilteredExecutions(Report report, ExecutionFilter filter) {
        Query q = queryBuilder.buildFilteredExecutionsQuery(em , report, filter, true);
        return (Long) q.getSingleResult();
    }

    @Override
    public Long getNumTrades(Report report) {
        Query query = em.createQuery("SELECT COUNT(t) FROM Trade t WHERE t.report = :report");
        query.setParameter("report", report);
        return (Long) query.getSingleResult();
    }

    @Override
    public Long getNumOpenTrades(Report report) {
        Query query = em.createQuery("SELECT COUNT(t) FROM Trade t WHERE t.report = :report AND t.status = :tradeStatus");
        query.setParameter("report", report);
        query.setParameter("tradeStatus", TradeStatus.OPEN);
        return (Long) query.getSingleResult();
    }

    @Override
    public List<Trade> getTradesByUnderlying(Report report, String underlying) {
        if ("ALLUNDLS".equals(underlying)) {
            underlying = null;
        }
        TypedQuery<Trade> q = em.createQuery("SELECT t FROM Trade t WHERE t.report = :report" +  (underlying != null ? " AND t.underlying = :underlying" : "") + " ORDER BY t.openDate ASC", Trade.class);
        q.setParameter("report", report);
        if (underlying != null) {
            q.setParameter("underlying", underlying);
        }
        return q.getResultList();
    }

    @Override
    public List<Trade> getTradesAffectedByExecution(Execution e) {
        TypedQuery<Trade> q = em.createQuery("SELECT t FROM Trade t WHERE t.report = :report AND (t.closeDate >= :eDate OR t.status = :tradeStatus) AND t.symbol = :eSymbol ORDER BY t.openDate ASC", Trade.class);
        q.setParameter("tradeStatus", TradeStatus.OPEN);
        q.setParameter("report", e.getReport());
        q.setParameter("eDate", e.getFillDate());
        q.setParameter("eSymbol", e.getSymbol());
        return q.getResultList();
    }

    @Transactional
    @Override
    public void createTrades(List<Trade> trades) {
        trades.forEach(em::persist);
    }

    @Transactional
    @Override
    public void deleteAllTrades(Report report) {
        for (Trade trade : this.getTradesByUnderlying(report, null)) {
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
    public Trade findTrade(Long id) {
        return em.find(Trade.class, id);
    }

    @Override
    public List<Trade> getFilteredTrades(Report report, TradeFilter filter, Integer start, Integer limit) {
        Query q = queryBuilder.buildFilteredTradesQuery(em, report, filter, false);

        q.setFirstResult(start != null ? start : 0);
        q.setMaxResults(limit != null ? limit : CoreSettings.JPA_MAX_RESULTS);

        return (List<Trade>) q.getResultList();
    }

    @Override
    public Long getNumFilteredTrades(Report report, TradeFilter filter) {
        Query q = queryBuilder.buildFilteredTradesQuery(em, report, filter, true);
        return (Long) q.getSingleResult();
    }

    @Override
    public List<Trade> getTradesBetweenDates(Report report, Calendar beginDate, Calendar endDate, TradeType tradeType) {
        TypedQuery<Trade> q = em.createQuery("SELECT t FROM Trade t WHERE t.report = :report AND t.closeDate >= :beginDate AND t.closeDate < :endDate AND t.type = :tradeType ORDER BY t.openDate ASC", Trade.class);
        q.setParameter("report", report);
        q.setParameter("beginDate", beginDate);
        q.setParameter("endDate", endDate);
        q.setParameter("tradeType", tradeType);
        return q.getResultList();
    }

    @Override
    public List<String> getUnderlyings(Report report) {
        TypedQuery<String> query = em.createQuery("SELECT DISTINCT e.underlying FROM Execution e WHERE e.report = :report", String.class);
        query.setParameter("report", report);
        return query.getResultList();
    }

    @Override
    public Long getNumUnderlyings(Report report) {
        Query query = em.createQuery("SELECT COUNT(DISTINCT e.underlying) FROM Execution e WHERE e.report = :report");
        query.setParameter("report", report);
        return (Long) query.getSingleResult();
    }

    @Override
    public Long getNumOpenUnderlyings(Report report) {
        Query query = em.createQuery("SELECT COUNT(DISTINCT se.execution.underlying) FROM SplitExecution se WHERE se.execution.report = :report AND se.trade.status = :tradeStatus");
        query.setParameter("report", report);
        query.setParameter("tradeStatus", TradeStatus.OPEN);
        return (Long) query.getSingleResult();
    }

    @Override
    public ExchangeRate getExchangeRate(String date) {
        return em.find(ExchangeRate.class, date);
    }

    @Transactional
    @Override
    public void createOrUpdateExchangeRate(ExchangeRate exchangeRate) {
        em.merge(exchangeRate);
    }
}