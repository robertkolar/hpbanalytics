package com.highpowerbear.hpbanalytics.dao.filter;

import com.highpowerbear.hpbanalytics.common.HanDefinitions;
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

    private Map<HanDefinitions.FilterOperatorString, String> symbolFilterMap = new HashMap<>();
    private Map<HanDefinitions.FilterOperatorEnum, Set<SecType>> secTypeFilterMap = new HashMap<>();
    private Map<HanDefinitions.FilterOperatorCalendar, Calendar> openDateFilterMap = new HashMap<>();
    private Map<HanDefinitions.FilterOperatorEnum, Set<TradeStatus>> statusFilterMap = new HashMap<>();

    public Map<HanDefinitions.FilterOperatorString, String> getSymbolFilterMap() {
        return symbolFilterMap;
    }

    public Map<HanDefinitions.FilterOperatorEnum, Set<SecType>> getSecTypeFilterMap() {
        return secTypeFilterMap;
    }

    public Map<HanDefinitions.FilterOperatorCalendar, Calendar> getOpenDateFilterMap() {
        return openDateFilterMap;
    }

    public Map<HanDefinitions.FilterOperatorEnum, Set<TradeStatus>> getStatusFilterMap() {
        return statusFilterMap;
    }
}
