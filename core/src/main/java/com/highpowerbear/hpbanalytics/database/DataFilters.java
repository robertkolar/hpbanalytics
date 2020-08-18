package com.highpowerbear.hpbanalytics.database;

import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import com.highpowerbear.hpbanalytics.model.DataFilter;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.text.MessageFormat;

/**
 * Created by robertk on 4/19/2020.
 */
public class DataFilters {

    public static Example<Trade> tradeFilterByExample(TradeType tradeType, SecType secType, Currency currency, String underlying) {
        return Example.of(new Trade()
                .setType(tradeType)
                .setSecType(secType)
                .setCurrency(currency)
                .setUnderlying(underlying));
    }

    public static Specification<Execution> executionFilterSpecification(DataFilter filter) {
        return (root, query, builder) -> builder.and(
                filter.getItems().stream()
                        .map(item -> builder.like(root.get(item.getProperty()), contains(item.getValue())))
                        .toArray(Predicate[]::new));
    }

    public static Specification<Trade> tradeFilterSpecification(DataFilter filter) {
        return (root, query, builder) -> builder.and(
                filter.getItems().stream()
                        .map(item -> builder.like(root.get(item.getProperty()), contains(item.getValue())))
                        .toArray(Predicate[]::new));
    }

    private static String contains(String expression) {
        return MessageFormat.format("%{0}%", expression);
    }
}
