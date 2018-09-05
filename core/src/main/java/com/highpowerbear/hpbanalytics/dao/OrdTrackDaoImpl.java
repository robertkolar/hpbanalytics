package com.highpowerbear.hpbanalytics.dao;

import com.highpowerbear.hpbanalytics.dao.filter.IbOrderFilter;
import com.highpowerbear.hpbanalytics.entity.IbAccount;
import com.highpowerbear.hpbanalytics.entity.IbOrder;
import com.highpowerbear.hpbanalytics.enums.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by robertk on 11/18/2017.
 */
@Repository
@Transactional(propagation = Propagation.SUPPORTS)
public class OrdTrackDaoImpl implements OrdTrackDao {

    @PersistenceContext
    private EntityManager em;

    private final QueryBuilder queryBuilder;

    @Autowired
    public OrdTrackDaoImpl(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    @Override
    public IbAccount findIbAccount(String accountId) {
        return em.find(IbAccount.class, accountId);
    }

    @Override
    public List<IbAccount> getIbAccounts() {
        TypedQuery<IbAccount> q = em.createQuery("SELECT ia FROM IbAccount ia ORDER BY ia.port", IbAccount.class);
        return q.getResultList();
    }

    @Transactional
    @Override
    public IbAccount updateIbAccount(IbAccount ibAccount) {
        return em.merge(ibAccount);
    }

    @Override
    public List<IbOrder> getFilteredIbOrders(String accountId, IbOrderFilter filter, int start, int limit) {
        IbAccount ibAccount = findIbAccount(accountId);
        TypedQuery<IbOrder> q = queryBuilder.buildFilteredIbOrdersQuery(em, ibAccount, filter);

        q.setFirstResult(start);
        q.setMaxResults(limit);

        return q.getResultList();
    }

    @Override
    public long getNumFilteredIbOrders(String accountId, IbOrderFilter filter) {
        IbAccount ibAccount = findIbAccount(accountId);
        TypedQuery<Long> q = queryBuilder.buildFilteredIbOrdersCountQuery(em, ibAccount, filter);

        return q.getSingleResult();
    }

    @Override
    public List<IbOrder> getOpenIbOrders(String accountId) {
        TypedQuery<IbOrder> q = em.createQuery("SELECT io FROM IbOrder io WHERE io.ibAccount.accountId = :accountId AND io.status IN :statuses", IbOrder.class);

        q.setParameter("accountId", accountId);
        Set<OrderStatus> statuses = new HashSet<>();
        statuses.add(OrderStatus.SUBMITTED);
        statuses.add(OrderStatus.UPDATED);
        q.setParameter("statuses", statuses);

        return q.getResultList();
    }

    @Transactional
    @Override
    public void newIbOrder(IbOrder ibOrder) {
        em.persist(ibOrder);
    }

    @Transactional
    @Override
    public void updateIbOrder(IbOrder ibOrder) {
        em.merge(ibOrder);
    }

    @Override
    public IbOrder getIbOrderByPermId(String accountId, long permId) {
        TypedQuery<IbOrder> q = em.createQuery("SELECT io FROM IbOrder io WHERE io.ibAccount.accountId = :accountId AND io.permId = :permId", IbOrder.class);

        q.setParameter("accountId", accountId);
        q.setParameter("permId", permId);
        List<IbOrder> ibOrders = q.getResultList();

        return !ibOrders.isEmpty() ? ibOrders.get(0) : null;
    }
}
