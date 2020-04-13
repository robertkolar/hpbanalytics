package com.highpowerbear.hpbanalytics.repository;

import com.highpowerbear.hpbanalytics.database.Execution;
import com.highpowerbear.hpbanalytics.database.Trade;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.enums.TradeStatus;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import com.highpowerbear.hpbanalytics.repository.filter.ExecutionFilter;
import com.highpowerbear.hpbanalytics.repository.filter.TradeFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
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

    private final QueryBuilder queryBuilder;

    @Autowired
    public ReportDaoImpl(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    @Override
    public List<Execution> getExecutions(int reportId) {
        TypedQuery<Execution> q = em.createQuery("SELECT e FROM Execution e WHERE e.reportId = :reportId ORDER BY e.fillDate ASC", Execution.class);
        q.setParameter("reportId", reportId);

        return q.getResultList();
    }

    @Override
    public List<Execution> getExecutionsAfterDate(int reportId, LocalDateTime date, String symbol) {
        TypedQuery<Execution> q = em.createQuery("SELECT e FROM Execution e WHERE e.reportId = :reportId AND e.fillDate > :date AND e.symbol = :symbol ORDER BY e.fillDate ASC", Execution.class);

        q.setParameter("reportId", reportId);
        q.setParameter("date", date);
        q.setParameter("symbol", symbol);

        return q.getResultList();
    }

    @Override
    public List<Execution> getExecutionsAfterDateInclusive(int reportId, LocalDateTime date, String symbol) {
        TypedQuery<Execution> q = em.createQuery("SELECT e FROM Execution e WHERE e.reportId = :reportId AND e.fillDate >= :date AND e.symbol = :symbol ORDER BY e.fillDate ASC", Execution.class);

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
        TypedQuery<Execution> q = queryBuilder.buildFilteredExecutionsQuery(em , reportId, filter);

        q.setFirstResult(start);
        q.setMaxResults(limit);

        return q.getResultList();
    }

    @Override
    public long getNumFilteredExecutions(int reportId, ExecutionFilter filter) {
        TypedQuery<Long> q = queryBuilder.buildFilteredExecutionsCountQuery(em , reportId, filter);

        return q.getSingleResult();
    }

    @Override
    public List<Trade> getTrades(int reportId, String tradeType, String secType, String currency, String underlying) {

        String tradeTypeQuery = (tradeType != null ? " AND t.type = :tradeType" : "");
        String secTypeQuery = (secType != null ? " AND t.secType = :secType" : "");
        String currencyQuery = (currency != null ? " AND t.currency = :currency" : "");
        String underlyingQuery = (underlying != null ? " AND t.underlying = :underlying" : "");

        TypedQuery<Trade> q = em.createQuery("SELECT t FROM Trade t WHERE t.reportId = :reportId" + tradeTypeQuery + secTypeQuery + currencyQuery + underlyingQuery + " ORDER BY t.openDate ASC", Trade.class);
        q.setParameter("reportId", reportId);

        if (tradeType != null) {
            q.setParameter("tradeType", TradeType.valueOf(tradeType));
        }
        if (secType != null) {
            q.setParameter("secType", SecType.valueOf(secType));
        }
        if (currency != null) {
            q.setParameter("currency", Currency.valueOf(currency));
        }
        if (underlying != null) {
            q.setParameter("underlying", underlying);
        }
        return q.getResultList();
    }

    @Override
    public List<Trade> getTradesAffectedByExecution(int reportId, LocalDateTime fillDate, String symbol) {
        TypedQuery<Trade> q = em.createQuery("SELECT t FROM Trade t WHERE t.reportId = :reportId AND (t.closeDate >= :fillDate OR t.status = :tradeStatus) AND t.symbol = :symbol ORDER BY t.openDate ASC", Trade.class);

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
        for (Trade trade : this.getTrades(reportId, null, null, null, null)) {
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
        TypedQuery<Trade> q = queryBuilder.buildFilteredTradesQuery(em, reportId, filter);

        q.setFirstResult(start);
        q.setMaxResults(limit);

        return q.getResultList();
    }

    @Override
    public long getNumFilteredTrades(int reportId, TradeFilter filter) {
        TypedQuery<Long> q = queryBuilder.buildFilteredTradesCountQuery(em, reportId, filter);

        return q.getSingleResult();
    }

    @Override
    public List<Trade> getTradesBetweenDates(int reportId, LocalDateTime beginDate, LocalDateTime endDate, TradeType tradeType) {
        TypedQuery<Trade> q = em.createQuery("SELECT t FROM Trade t WHERE t.reportId = :reportId AND t.closeDate >= :beginDate AND t.closeDate < :endDate AND t.type = :tradeType ORDER BY t.openDate ASC", Trade.class);

        q.setParameter("reportId", reportId);
        q.setParameter("beginDate", beginDate);
        q.setParameter("endDate", endDate);
        q.setParameter("tradeType", tradeType);

        return q.getResultList();
    }

    @Override
    public List<String> getUnderlyings(int reportId, boolean openOnly) {
        TypedQuery<String> query = em.createQuery("SELECT DISTINCT se.execution.underlying AS u FROM SplitExecution se WHERE se.execution.reportId = :reportId" + (openOnly ? " AND se.trade.status = :tradeStatus" : "") + " ORDER BY u", String.class);

        query.setParameter("reportId", reportId);
        if (openOnly) {
            query.setParameter("tradeStatus", TradeStatus.OPEN);
        }

        return query.getResultList();
    }
}
