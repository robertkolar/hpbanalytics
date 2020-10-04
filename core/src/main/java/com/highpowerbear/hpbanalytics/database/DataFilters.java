package com.highpowerbear.hpbanalytics.database;

import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import com.highpowerbear.hpbanalytics.model.DataFilterItem;
import com.ib.client.Types;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.text.MessageFormat;
import java.util.List;

/**
 * Created by robertk on 4/19/2020.
 */
public class DataFilters {

    public static Example<Trade> tradeFilterByExample(TradeType tradeType, Types.SecType secType, Currency currency, String underlying) {
        return Example.of(new Trade()
                .setType(tradeType)
                .setSecType(secType)
                .setCurrency(currency)
                .setUnderlying(underlying));
    }

    public static Specification<Execution> executionFilterSpecification(List<DataFilterItem> dataFilterItems) {
        // TODO support for SecType
        return (root, query, builder) -> builder.and(
                dataFilterItems.stream()
                        .map(item -> builder.like(root.get(item.getProperty()), contains(item.getValue())))
                        .toArray(Predicate[]::new));
    }

    public static Specification<Trade> tradeFilterSpecification(List<DataFilterItem> dataFilterItems) {
        // TODO support for SecType, TradeStatus enums
        return (root, query, builder) -> builder.and(
                dataFilterItems.stream()
                        .map(item -> builder.like(root.get(item.getProperty()), contains(item.getValue())))
                        .toArray(Predicate[]::new));
    }

    private static String contains(String expression) {
        return MessageFormat.format("%{0}%", expression);
    }
}
