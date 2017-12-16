package com.highpowerbear.hpbanalytics.dao.filter;

import com.highpowerbear.hpbanalytics.common.HanDefinitions;
import com.highpowerbear.hpbanalytics.enums.OrderStatus;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by robertk on 10/19/2015.
 */
public class IbOrderFilter {

    private Map<HanDefinitions.FilterOperatorString, String> symbolFilterMap = new HashMap<>();
    private Map<HanDefinitions.FilterOperatorEnum, Set<String>> secTypeFilterMap = new HashMap<>();
    private Map<HanDefinitions.FilterOperatorCalendar, Calendar> submitDateFilterMap = new HashMap<>();
    private Map<HanDefinitions.FilterOperatorEnum, Set<OrderStatus>> statusFilterMap = new HashMap<>();

    public Map<HanDefinitions.FilterOperatorString, String> getSymbolFilterMap() {
        return symbolFilterMap;
    }

    public Map<HanDefinitions.FilterOperatorEnum, Set<String>> getSecTypeFilterMap() {
        return secTypeFilterMap;
    }

    public Map<HanDefinitions.FilterOperatorCalendar, Calendar> getSubmitDateFilterMap() {
        return submitDateFilterMap;
    }

    public Map<HanDefinitions.FilterOperatorEnum, Set<OrderStatus>> getStatusFilterMap() {
        return statusFilterMap;
    }
}
