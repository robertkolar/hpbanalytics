package com.highpowerbear.hpbanalytics.dao.filter;

import com.highpowerbear.hpbanalytics.enums.FilterEnums;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.enums.TradeStatus;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by robertk on 10/21/2015.
 */
public class TradeFilter {

    private final Map<FilterEnums.FilterOperatorString, String> symbolFilterMap = new HashMap<>();
    private final Map<FilterEnums.FilterOperatorEnum, Set<SecType>> secTypeFilterMap = new HashMap<>();
    private final Map<FilterEnums.FilterOperatorCalendar, Calendar> openDateFilterMap = new HashMap<>();
    private final Map<FilterEnums.FilterOperatorEnum, Set<TradeStatus>> statusFilterMap = new HashMap<>();

    public Map<FilterEnums.FilterOperatorString, String> getSymbolFilterMap() {
        return symbolFilterMap;
    }

    public Map<FilterEnums.FilterOperatorEnum, Set<SecType>> getSecTypeFilterMap() {
        return secTypeFilterMap;
    }

    public Map<FilterEnums.FilterOperatorCalendar, Calendar> getOpenDateFilterMap() {
        return openDateFilterMap;
    }

    public Map<FilterEnums.FilterOperatorEnum, Set<TradeStatus>> getStatusFilterMap() {
        return statusFilterMap;
    }
}
