package com.highpowerbear.hpbanalytics.dao;

import com.highpowerbear.hpbanalytics.entity.ExchangeRate;
import com.highpowerbear.hpbanalytics.entity.Trade;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Created by robertk on 11/18/2017.
 */
@Repository
public class HanDaoImpl {

    @PersistenceContext
    private EntityManager em;

    public List<ExchangeRate> getAllExchangeRates() {
        TypedQuery<ExchangeRate> q = em.createQuery("SELECT er FROM ExchangeRate er", ExchangeRate.class);

        return q.getResultList();
    }

    public List<Trade> getAllTrades() {
        TypedQuery<Trade> q = em.createQuery("SELECT t FROM Trade t", Trade.class);

        return q.getResultList();
    }
}
