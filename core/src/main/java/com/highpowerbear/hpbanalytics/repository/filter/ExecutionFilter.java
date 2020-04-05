package com.highpowerbear.hpbanalytics.repository.filter;

import com.highpowerbear.hpbanalytics.enums.FilterEnums;
import com.highpowerbear.hpbanalytics.enums.SecType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by robertk on 10/21/2015.
 */
public class ExecutionFilter {

    private final Map<FilterEnums.FilterOperatorString, String> symbolFilterMap = new HashMap<>();
    private final Map<FilterEnums.FilterOperatorEnum, Set<SecType>> secTypeFilterMap = new HashMap<>();
    private final Map<FilterEnums.FilterOperatorDate, LocalDateTime> fillDateFilterMap = new HashMap<>();

    public Map<FilterEnums.FilterOperatorString, String> getSymbolFilterMap() {
        return symbolFilterMap;
    }

    public Map<FilterEnums.FilterOperatorEnum, Set<SecType>> getSecTypeFilterMap() {
        return secTypeFilterMap;
    }

    public Map<FilterEnums.FilterOperatorDate, LocalDateTime> getFillDateFilterMap() {
        return fillDateFilterMap;
    }
}
