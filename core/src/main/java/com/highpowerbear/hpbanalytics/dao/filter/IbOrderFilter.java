package com.highpowerbear.hpbanalytics.dao.filter;

import com.highpowerbear.hpbanalytics.enums.FilterEnums;
import com.highpowerbear.hpbanalytics.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by robertk on 10/19/2015.
 */
public class IbOrderFilter {

    private final Map<FilterEnums.FilterOperatorString, String> symbolFilterMap = new HashMap<>();
    private final Map<FilterEnums.FilterOperatorEnum, Set<String>> secTypeFilterMap = new HashMap<>();
    private final Map<FilterEnums.FilterOperatorDate, LocalDateTime> submitDateFilterMap = new HashMap<>();
    private final Map<FilterEnums.FilterOperatorEnum, Set<OrderStatus>> statusFilterMap = new HashMap<>();

    public Map<FilterEnums.FilterOperatorString, String> getSymbolFilterMap() {
        return symbolFilterMap;
    }

    public Map<FilterEnums.FilterOperatorEnum, Set<String>> getSecTypeFilterMap() {
        return secTypeFilterMap;
    }

    public Map<FilterEnums.FilterOperatorDate, LocalDateTime> getSubmitDateFilterMap() {
        return submitDateFilterMap;
    }

    public Map<FilterEnums.FilterOperatorEnum, Set<OrderStatus>> getStatusFilterMap() {
        return statusFilterMap;
    }
}
