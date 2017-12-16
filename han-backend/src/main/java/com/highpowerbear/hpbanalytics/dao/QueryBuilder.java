package com.highpowerbear.hpbanalytics.dao;

import com.highpowerbear.hpbanalytics.common.HanDefinitions;
import com.highpowerbear.hpbanalytics.dao.filter.ExecutionFilter;
import com.highpowerbear.hpbanalytics.dao.filter.IbOrderFilter;
import com.highpowerbear.hpbanalytics.dao.filter.TradeFilter;
import com.highpowerbear.hpbanalytics.entity.Execution;
import com.highpowerbear.hpbanalytics.entity.IbAccount;
import com.highpowerbear.hpbanalytics.entity.IbOrder;
import com.highpowerbear.hpbanalytics.entity.Report;
import com.highpowerbear.hpbanalytics.entity.Trade;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Calendar;

/**
 * Created by robertk on 10/19/2015.
 */
@Component
public class QueryBuilder {

    public Query buildFilteredIbOrdersQuery(EntityManager em, IbAccount ibAccount, IbOrderFilter filter, boolean isCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append(isCount ? "COUNT(io)" : "io").append(" FROM IbOrder io WHERE io.ibAccount = :ibAccount");

        for (HanDefinitions.FilterOperatorString op : filter.getSymbolFilterMap().keySet()) {
            sb.append(" AND io.symbol ").append(op.getSql()).append(" :").append(op.name()).append("_").append(HanDefinitions.IbOrderFilterField.SYMBOL.getVarName());
        }
        for (HanDefinitions.FilterOperatorEnum op : filter.getSecTypeFilterMap().keySet()) {
            sb.append(" AND io.secType ").append(op.getSql()).append(" :").append(op.name()).append("_").append(HanDefinitions.IbOrderFilterField.SEC_TYPE.getVarName());
        }
        for (HanDefinitions.FilterOperatorCalendar op : filter.getSubmitDateFilterMap().keySet()) {
            String varName = HanDefinitions.IbOrderFilterField.SUBMIT_DATE.getVarName();
            if (HanDefinitions.FilterOperatorCalendar.EQ.equals(op)) {
                sb.append(" AND io.submitDate > :from_").append(varName).append(" AND io.submitDate < :to_").append(varName);
            } else {
                sb.append(" AND io.submitDate ").append(op.getSql()).append(" :").append(op.name()).append("_").append(varName);
            }
        }
        for (HanDefinitions.FilterOperatorEnum op : filter.getStatusFilterMap().keySet()) {
            sb.append(" AND io.status ").append(op.getSql()).append(" :").append(op.name()).append("_").append(HanDefinitions.IbOrderFilterField.STATUS.getVarName());
        }

        sb.append(isCount ? "" : " ORDER BY io.submitDate DESC");
        Query q = (isCount ? em.createQuery(sb.toString()) : em.createQuery(sb.toString(), IbOrder.class));

        q.setParameter("ibAccount", ibAccount);

        for (HanDefinitions.FilterOperatorString op : filter.getSymbolFilterMap().keySet()) {
            boolean isLike = HanDefinitions.FilterOperatorString.LIKE.equals(op);
            q.setParameter(op.name() + "_" + HanDefinitions.IbOrderFilterField.SYMBOL.getVarName(), (isLike ? "%" : "") + filter.getSymbolFilterMap().get(op) + (isLike ? "%" : ""));
        }
        for (HanDefinitions.FilterOperatorEnum op : filter.getSecTypeFilterMap().keySet()) {
            q.setParameter(op.name() + "_" + HanDefinitions.IbOrderFilterField.SEC_TYPE.getVarName(), filter.getSecTypeFilterMap().get(op));
        }
        for (HanDefinitions.FilterOperatorCalendar op : filter.getSubmitDateFilterMap().keySet()) {
            String varName = HanDefinitions.IbOrderFilterField.SUBMIT_DATE.getVarName();
            if (HanDefinitions.FilterOperatorCalendar.EQ.equals(op)) {
                Calendar from = filter.getSubmitDateFilterMap().get(op);
                Calendar to = Calendar.getInstance();
                to.setTimeInMillis(from.getTimeInMillis());
                to.add(Calendar.DATE, 1);
                q.setParameter("from_" + varName, from);
                q.setParameter("to_" + varName, to);
            } else {
                q.setParameter(op.name() + "_" + varName, filter.getSubmitDateFilterMap().get(op));
            }
        }
        for (HanDefinitions.FilterOperatorEnum op : filter.getStatusFilterMap().keySet()) {
            q.setParameter(op.name() + "_" + HanDefinitions.IbOrderFilterField.STATUS.getVarName(), filter.getStatusFilterMap().get(op));
        }

        //l.info("Generated query=" + sb.toString());
        return q;
    }

    public Query buildFilteredExecutionsQuery(EntityManager em, Report report, ExecutionFilter filter, boolean isCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append(isCount ? "COUNT(e)" : "e").append(" FROM Execution e WHERE e.report = :report");

        for (HanDefinitions.FilterOperatorString op : filter.getSymbolFilterMap().keySet()) {
            sb.append(" AND e.symbol ").append(op.getSql()).append(" :").append(op.name()).append("_").append(HanDefinitions.ExecutionFilterField.SYMBOL.getVarName());
        }
        for (HanDefinitions.FilterOperatorEnum op : filter.getSecTypeFilterMap().keySet()) {
            sb.append(" AND e.secType ").append(op.getSql()).append(" :").append(op.name()).append("_").append(HanDefinitions.ExecutionFilterField.SEC_TYPE.getVarName());
        }
        for (HanDefinitions.FilterOperatorCalendar op : filter.getFillDateFilterMap().keySet()) {
            String varName = HanDefinitions.ExecutionFilterField.FILL_DATE.getVarName();
            if (HanDefinitions.FilterOperatorCalendar.EQ.equals(op)) {
                sb.append(" AND e.fillDate > :from_").append(varName).append(" AND e.fillDate < :to_").append(varName);
            } else {
                sb.append(" AND e.fillDate ").append(op.getSql()).append(" :").append(op.name()).append("_").append(varName);
            }
        }

        sb.append(isCount ? "" : " ORDER BY e.fillDate DESC");
        Query q = (isCount ? em.createQuery(sb.toString()) : em.createQuery(sb.toString(), Execution.class));

        q.setParameter("report", report);

        for (HanDefinitions.FilterOperatorString op : filter.getSymbolFilterMap().keySet()) {
            boolean isLike = HanDefinitions.FilterOperatorString.LIKE.equals(op);
            q.setParameter(op.name() + "_" + HanDefinitions.ExecutionFilterField.SYMBOL.getVarName(), (isLike ? "%" : "") + filter.getSymbolFilterMap().get(op) + (isLike ? "%" : ""));
        }
        for (HanDefinitions.FilterOperatorEnum op : filter.getSecTypeFilterMap().keySet()) {
            q.setParameter(op.name() + "_" + HanDefinitions.ExecutionFilterField.SEC_TYPE.getVarName(), filter.getSecTypeFilterMap().get(op));
        }
        for (HanDefinitions.FilterOperatorCalendar op : filter.getFillDateFilterMap().keySet()) {
            String varName = HanDefinitions.ExecutionFilterField.FILL_DATE.getVarName();
            if (HanDefinitions.FilterOperatorCalendar.EQ.equals(op)) {
                Calendar from = filter.getFillDateFilterMap().get(op);
                Calendar to = Calendar.getInstance();
                to.setTimeInMillis(from.getTimeInMillis());
                to.add(Calendar.DATE, 1);
                q.setParameter("from_" + varName, from);
                q.setParameter("to_" + varName, to);
            } else {
                q.setParameter(op.name() + "_" + varName, filter.getFillDateFilterMap().get(op));
            }
        }

        //l.info("Generated query=" + sb.toString());
        return q;
    }

    public Query buildFilteredTradesQuery(EntityManager em, Report report, TradeFilter filter, boolean isCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append(isCount ? "COUNT(t)" : "t").append(" FROM Trade t WHERE t.report = :report");

        for (HanDefinitions.FilterOperatorString op : filter.getSymbolFilterMap().keySet()) {
            sb.append(" AND t.symbol ").append(op.getSql()).append(" :").append(op.name()).append("_").append(HanDefinitions.TradeFilterField.SYMBOL.getVarName());
        }
        for (HanDefinitions.FilterOperatorEnum op : filter.getSecTypeFilterMap().keySet()) {
            sb.append(" AND t.secType ").append(op.getSql()).append(" :").append(op.name()).append("_").append(HanDefinitions.TradeFilterField.SEC_TYPE.getVarName());
        }
        for (HanDefinitions.FilterOperatorCalendar op : filter.getOpenDateFilterMap().keySet()) {
            String varName = HanDefinitions.TradeFilterField.OPEN_DATE.getVarName();
            if (HanDefinitions.FilterOperatorCalendar.EQ.equals(op)) {
                sb.append(" AND t.openDate > :from_").append(varName).append(" AND t.openDate < :to_").append(varName);
            } else {
                sb.append(" AND t.openDate ").append(op.getSql()).append(" :").append(op.name()).append("_").append(varName);
            }
        }
        for (HanDefinitions.FilterOperatorEnum op : filter.getStatusFilterMap().keySet()) {
            sb.append(" AND t.status ").append(op.getSql()).append(" :").append(op.name()).append("_").append(HanDefinitions.TradeFilterField.STATUS.getVarName());
        }

        sb.append(isCount ? "" : " ORDER BY t.openDate DESC");
        Query q = (isCount ? em.createQuery(sb.toString()) : em.createQuery(sb.toString(), Trade.class));

        q.setParameter("report", report);

        for (HanDefinitions.FilterOperatorString op : filter.getSymbolFilterMap().keySet()) {
            boolean isLike = HanDefinitions.FilterOperatorString.LIKE.equals(op);
            q.setParameter(op.name() + "_" + HanDefinitions.TradeFilterField.SYMBOL.getVarName(), (isLike ? "%" : "") + filter.getSymbolFilterMap().get(op) + (isLike ? "%" : ""));
        }
        for (HanDefinitions.FilterOperatorEnum op : filter.getSecTypeFilterMap().keySet()) {
            q.setParameter(op.name() + "_" + HanDefinitions.TradeFilterField.SEC_TYPE.getVarName(), filter.getSecTypeFilterMap().get(op));
        }
        for (HanDefinitions.FilterOperatorCalendar op : filter.getOpenDateFilterMap().keySet()) {
            String varName = HanDefinitions.TradeFilterField.OPEN_DATE.getVarName();
            if (HanDefinitions.FilterOperatorCalendar.EQ.equals(op)) {
                Calendar from = filter.getOpenDateFilterMap().get(op);
                Calendar to = Calendar.getInstance();
                to.setTimeInMillis(from.getTimeInMillis());
                to.add(Calendar.DATE, 1);
                q.setParameter("from_" + varName, from);
                q.setParameter("to_" + varName, to);
            } else {
                q.setParameter(op.name() + "_" + varName, filter.getOpenDateFilterMap().get(op));
            }
        }
        for (HanDefinitions.FilterOperatorEnum op : filter.getStatusFilterMap().keySet()) {
            q.setParameter(op.name() + "_" + HanDefinitions.TradeFilterField.STATUS.getVarName(), filter.getStatusFilterMap().get(op));
        }

        //l.info("Generated query=" + sb.toString());
        return q;
    }
}
