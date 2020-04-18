package com.highpowerbear.hpbanalytics.repository;

import com.highpowerbear.hpbanalytics.database.Execution;
import com.highpowerbear.hpbanalytics.database.Trade;
import com.highpowerbear.hpbanalytics.repository.filter.ExecutionFilter;
import com.highpowerbear.hpbanalytics.repository.filter.TradeFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
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
}
