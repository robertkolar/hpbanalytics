package com.highpowerbear.hpbanalytics.dao;

import com.highpowerbear.hpbanalytics.dao.filter.ExecutionFilter;
import com.highpowerbear.hpbanalytics.dao.filter.IbOrderFilter;
import com.highpowerbear.hpbanalytics.dao.filter.TradeFilter;
import com.highpowerbear.hpbanalytics.entity.Execution;
import com.highpowerbear.hpbanalytics.entity.IbAccount;
import com.highpowerbear.hpbanalytics.entity.IbOrder;
import com.highpowerbear.hpbanalytics.entity.Report;
import com.highpowerbear.hpbanalytics.entity.Trade;
import com.highpowerbear.hpbanalytics.enums.FilterEnums;
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

        for (FilterEnums.FilterOperatorString op : filter.getSymbolFilterMap().keySet()) {
            sb.append(" AND io.symbol ").append(op.getSql()).append(" :").append(op.name()).append("_").append(FilterEnums.IbOrderFilterField.SYMBOL.getVarName());
        }
        for (FilterEnums.FilterOperatorEnum op : filter.getSecTypeFilterMap().keySet()) {
            sb.append(" AND io.secType ").append(op.getSql()).append(" :").append(op.name()).append("_").append(FilterEnums.IbOrderFilterField.SEC_TYPE.getVarName());
        }
        for (FilterEnums.FilterOperatorCalendar op : filter.getSubmitDateFilterMap().keySet()) {
            String varName = FilterEnums.IbOrderFilterField.SUBMIT_DATE.getVarName();
            if (FilterEnums.FilterOperatorCalendar.EQ.equals(op)) {
                sb.append(" AND io.submitDate > :from_").append(varName).append(" AND io.submitDate < :to_").append(varName);
            } else {
                sb.append(" AND io.submitDate ").append(op.getSql()).append(" :").append(op.name()).append("_").append(varName);
            }
        }
        for (FilterEnums.FilterOperatorEnum op : filter.getStatusFilterMap().keySet()) {
            sb.append(" AND io.status ").append(op.getSql()).append(" :").append(op.name()).append("_").append(FilterEnums.IbOrderFilterField.STATUS.getVarName());
        }

        sb.append(isCount ? "" : " ORDER BY io.submitDate DESC");
        Query q = (isCount ? em.createQuery(sb.toString()) : em.createQuery(sb.toString(), IbOrder.class));

        q.setParameter("ibAccount", ibAccount);

        for (FilterEnums.FilterOperatorString op : filter.getSymbolFilterMap().keySet()) {
            boolean isLike = FilterEnums.FilterOperatorString.LIKE.equals(op);
            q.setParameter(op.name() + "_" + FilterEnums.IbOrderFilterField.SYMBOL.getVarName(), (isLike ? "%" : "") + filter.getSymbolFilterMap().get(op) + (isLike ? "%" : ""));
        }
        for (FilterEnums.FilterOperatorEnum op : filter.getSecTypeFilterMap().keySet()) {
            q.setParameter(op.name() + "_" + FilterEnums.IbOrderFilterField.SEC_TYPE.getVarName(), filter.getSecTypeFilterMap().get(op));
        }
        for (FilterEnums.FilterOperatorCalendar op : filter.getSubmitDateFilterMap().keySet()) {
            String varName = FilterEnums.IbOrderFilterField.SUBMIT_DATE.getVarName();
            if (FilterEnums.FilterOperatorCalendar.EQ.equals(op)) {
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
        for (FilterEnums.FilterOperatorEnum op : filter.getStatusFilterMap().keySet()) {
            q.setParameter(op.name() + "_" + FilterEnums.IbOrderFilterField.STATUS.getVarName(), filter.getStatusFilterMap().get(op));
        }

        //l.info("Generated query=" + sb.toString());
        return q;
    }

    public Query buildFilteredExecutionsQuery(EntityManager em, Report report, ExecutionFilter filter, boolean isCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append(isCount ? "COUNT(e)" : "e").append(" FROM Execution e WHERE e.report = :report");

        for (FilterEnums.FilterOperatorString op : filter.getSymbolFilterMap().keySet()) {
            sb.append(" AND e.symbol ").append(op.getSql()).append(" :").append(op.name()).append("_").append(FilterEnums.ExecutionFilterField.SYMBOL.getVarName());
        }
        for (FilterEnums.FilterOperatorEnum op : filter.getSecTypeFilterMap().keySet()) {
            sb.append(" AND e.secType ").append(op.getSql()).append(" :").append(op.name()).append("_").append(FilterEnums.ExecutionFilterField.SEC_TYPE.getVarName());
        }
        for (FilterEnums.FilterOperatorCalendar op : filter.getFillDateFilterMap().keySet()) {
            String varName = FilterEnums.ExecutionFilterField.FILL_DATE.getVarName();
            if (FilterEnums.FilterOperatorCalendar.EQ.equals(op)) {
                sb.append(" AND e.fillDate > :from_").append(varName).append(" AND e.fillDate < :to_").append(varName);
            } else {
                sb.append(" AND e.fillDate ").append(op.getSql()).append(" :").append(op.name()).append("_").append(varName);
            }
        }

        sb.append(isCount ? "" : " ORDER BY e.fillDate DESC");
        Query q = (isCount ? em.createQuery(sb.toString()) : em.createQuery(sb.toString(), Execution.class));

        q.setParameter("report", report);

        for (FilterEnums.FilterOperatorString op : filter.getSymbolFilterMap().keySet()) {
            boolean isLike = FilterEnums.FilterOperatorString.LIKE.equals(op);
            q.setParameter(op.name() + "_" + FilterEnums.ExecutionFilterField.SYMBOL.getVarName(), (isLike ? "%" : "") + filter.getSymbolFilterMap().get(op) + (isLike ? "%" : ""));
        }
        for (FilterEnums.FilterOperatorEnum op : filter.getSecTypeFilterMap().keySet()) {
            q.setParameter(op.name() + "_" + FilterEnums.ExecutionFilterField.SEC_TYPE.getVarName(), filter.getSecTypeFilterMap().get(op));
        }
        for (FilterEnums.FilterOperatorCalendar op : filter.getFillDateFilterMap().keySet()) {
            String varName = FilterEnums.ExecutionFilterField.FILL_DATE.getVarName();
            if (FilterEnums.FilterOperatorCalendar.EQ.equals(op)) {
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

        for (FilterEnums.FilterOperatorString op : filter.getSymbolFilterMap().keySet()) {
            sb.append(" AND t.symbol ").append(op.getSql()).append(" :").append(op.name()).append("_").append(FilterEnums.TradeFilterField.SYMBOL.getVarName());
        }
        for (FilterEnums.FilterOperatorEnum op : filter.getSecTypeFilterMap().keySet()) {
            sb.append(" AND t.secType ").append(op.getSql()).append(" :").append(op.name()).append("_").append(FilterEnums.TradeFilterField.SEC_TYPE.getVarName());
        }
        for (FilterEnums.FilterOperatorCalendar op : filter.getOpenDateFilterMap().keySet()) {
            String varName = FilterEnums.TradeFilterField.OPEN_DATE.getVarName();
            if (FilterEnums.FilterOperatorCalendar.EQ.equals(op)) {
                sb.append(" AND t.openDate > :from_").append(varName).append(" AND t.openDate < :to_").append(varName);
            } else {
                sb.append(" AND t.openDate ").append(op.getSql()).append(" :").append(op.name()).append("_").append(varName);
            }
        }
        for (FilterEnums.FilterOperatorEnum op : filter.getStatusFilterMap().keySet()) {
            sb.append(" AND t.status ").append(op.getSql()).append(" :").append(op.name()).append("_").append(FilterEnums.TradeFilterField.STATUS.getVarName());
        }

        sb.append(isCount ? "" : " ORDER BY t.openDate DESC");
        Query q = (isCount ? em.createQuery(sb.toString()) : em.createQuery(sb.toString(), Trade.class));

        q.setParameter("report", report);

        for (FilterEnums.FilterOperatorString op : filter.getSymbolFilterMap().keySet()) {
            boolean isLike = FilterEnums.FilterOperatorString.LIKE.equals(op);
            q.setParameter(op.name() + "_" + FilterEnums.TradeFilterField.SYMBOL.getVarName(), (isLike ? "%" : "") + filter.getSymbolFilterMap().get(op) + (isLike ? "%" : ""));
        }
        for (FilterEnums.FilterOperatorEnum op : filter.getSecTypeFilterMap().keySet()) {
            q.setParameter(op.name() + "_" + FilterEnums.TradeFilterField.SEC_TYPE.getVarName(), filter.getSecTypeFilterMap().get(op));
        }
        for (FilterEnums.FilterOperatorCalendar op : filter.getOpenDateFilterMap().keySet()) {
            String varName = FilterEnums.TradeFilterField.OPEN_DATE.getVarName();
            if (FilterEnums.FilterOperatorCalendar.EQ.equals(op)) {
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
        for (FilterEnums.FilterOperatorEnum op : filter.getStatusFilterMap().keySet()) {
            q.setParameter(op.name() + "_" + FilterEnums.TradeFilterField.STATUS.getVarName(), filter.getStatusFilterMap().get(op));
        }

        //l.info("Generated query=" + sb.toString());
        return q;
    }
}
