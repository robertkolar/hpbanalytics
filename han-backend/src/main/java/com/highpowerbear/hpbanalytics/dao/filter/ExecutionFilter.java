package com.highpowerbear.hpbanalytics.dao.filter;

import com.highpowerbear.hpbanalytics.enums.FilterEnums;
import com.highpowerbear.hpbanalytics.enums.SecType;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by robertk on 10/21/2015.
 */
public class ExecutionFilter {

    private Map<FilterEnums.FilterOperatorString, String> symbolFilterMap = new HashMap<>();
    private Map<FilterEnums.FilterOperatorEnum, Set<SecType>> secTypeFilterMap = new HashMap<>();
    private Map<FilterEnums.FilterOperatorCalendar, Calendar> fillDateFilterMap = new HashMap<>();

    public Map<FilterEnums.FilterOperatorString, String> getSymbolFilterMap() {
        return symbolFilterMap;
    }

    public Map<FilterEnums.FilterOperatorEnum, Set<SecType>> getSecTypeFilterMap() {
        return secTypeFilterMap;
    }

    public Map<FilterEnums.FilterOperatorCalendar, Calendar> getFillDateFilterMap() {
        return fillDateFilterMap;
    }
}
