package com.highpowerbear.hpbanalytics.dao.filter;

import com.highpowerbear.hpbanalytics.enums.FilterEnums;
import com.highpowerbear.hpbanalytics.enums.OrderStatus;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by robertk on 10/19/2015.
 */
public class IbOrderFilter {

    private Map<FilterEnums.FilterOperatorString, String> symbolFilterMap = new HashMap<>();
    private Map<FilterEnums.FilterOperatorEnum, Set<String>> secTypeFilterMap = new HashMap<>();
    private Map<FilterEnums.FilterOperatorCalendar, Calendar> submitDateFilterMap = new HashMap<>();
    private Map<FilterEnums.FilterOperatorEnum, Set<OrderStatus>> statusFilterMap = new HashMap<>();

    public Map<FilterEnums.FilterOperatorString, String> getSymbolFilterMap() {
        return symbolFilterMap;
    }

    public Map<FilterEnums.FilterOperatorEnum, Set<String>> getSecTypeFilterMap() {
        return secTypeFilterMap;
    }

    public Map<FilterEnums.FilterOperatorCalendar, Calendar> getSubmitDateFilterMap() {
        return submitDateFilterMap;
    }

    public Map<FilterEnums.FilterOperatorEnum, Set<OrderStatus>> getStatusFilterMap() {
        return statusFilterMap;
    }
}
