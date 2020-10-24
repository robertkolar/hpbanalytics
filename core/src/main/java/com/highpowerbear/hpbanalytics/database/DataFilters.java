package com.highpowerbear.hpbanalytics.database;

import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.DataFilterOperator;
import com.highpowerbear.hpbanalytics.enums.TradeStatus;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import com.highpowerbear.hpbanalytics.model.DataFilterItem;
import com.ib.client.Types;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.MessageFormat;
import java.util.ArrayList;
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

    public static Specification<Execution> filteredExecutions(List<DataFilterItem> dataFilterItems) {
        return (root, query, builder) -> build(root, builder, dataFilterItems);
    }

    public static Specification<Trade> filteredTrades(List<DataFilterItem> dataFilterItems) {
        return (root, query, builder) -> build(root, builder, dataFilterItems);
    }

    private static <T> Predicate build(Root<T> root, CriteriaBuilder builder, List<DataFilterItem> dataFilterItems) {
        List<Predicate> predicates = new ArrayList<>();

        for (DataFilterItem item : dataFilterItems) {
            DataFilterOperator operator = DataFilterOperator.valueOf(item.getOperator().toUpperCase());
            String field = item.getProperty();

            switch (operator) {
                case LIKE:
                    String likeStr = MessageFormat.format("%{0}%", item.getValue());
                    predicates.add(builder.like(root.get(field), likeStr));
                    break;

                case IN:
                    if (field.equals("secType")) {
                        CriteriaBuilder.In<Types.SecType> inPredicate = builder.in(root.get(field));
                        item.getValues().forEach(value -> inPredicate.value(Types.SecType.valueOf(value)));
                        predicates.add(inPredicate);

                    } else if (field.equals("status")) {
                        CriteriaBuilder.In<TradeStatus> inPredicate = builder.in(root.get(field));
                        item.getValues().forEach(value -> inPredicate.value(TradeStatus.valueOf(value)));
                        predicates.add(inPredicate);
                    }
                    break;
            }
        }
        return builder.and(predicates.toArray(new Predicate[0]));
    }
}
