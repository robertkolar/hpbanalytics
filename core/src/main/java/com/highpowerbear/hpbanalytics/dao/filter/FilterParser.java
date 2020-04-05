package com.highpowerbear.hpbanalytics.dao.filter;

import com.highpowerbear.hpbanalytics.config.HanSettings;
import com.highpowerbear.hpbanalytics.enums.FilterEnums;
import com.highpowerbear.hpbanalytics.enums.OrderStatus;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.enums.TradeStatus;
import org.springframework.stereotype.Component;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.json.JsonString;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by robertk on 10/19/2015.
 */
@Component
public class FilterParser {

    public IbOrderFilter parseIbOrderFilter(String jsonFilter) {
        IbOrderFilter filter = new IbOrderFilter();

        if (jsonFilter != null) {
            JsonReader jsonReader = Json.createReader(new StringReader(jsonFilter));
            JsonArray array = jsonReader.readArray();
            for (int i = 0; i < array.size(); i++) {
                String property = array.getJsonObject(i).getJsonString(FilterEnums.FilterKey.PROPERTY.toString()).getString();

                if (FilterEnums.IbOrderFilterField.SYMBOL.getVarName().equals(property)) {
                    filter.getSymbolFilterMap().put(parseOperatorString(array, i), parseString(array, i));

                } else if (FilterEnums.IbOrderFilterField.SEC_TYPE.getVarName().equals(property)) {
                    FilterEnums.FilterOperatorEnum operator = parseOperatorEnum(array, i);
                    filter.getSecTypeFilterMap().put(operator, new HashSet<>());
                    for (String v : parseValues(array, i)) {
                        filter.getSecTypeFilterMap().get(operator).add(v.toUpperCase());
                    }

                } else if (FilterEnums.IbOrderFilterField.SUBMIT_DATE.getVarName().equals(property)) {
                    LocalDateTime localDateTime = LocalDateTime.parse(parseString(array, i), HanSettings.JSON_DATE_FORMATTER);
                    filter.getSubmitDateFilterMap().put(parseOperatorDate(array, i), localDateTime);

                } else if (FilterEnums.IbOrderFilterField.STATUS.getVarName().equals(property)) {
                    FilterEnums.FilterOperatorEnum operator = parseOperatorEnum(array, i);
                    filter.getStatusFilterMap().put(operator, new HashSet<>());
                    for (String v : parseValues(array, i)) {
                        filter.getStatusFilterMap().get(operator).add(OrderStatus.valueOf(v.toUpperCase()));
                    }
                }
            }
            jsonReader.close();
        }
        return filter;
    }

    public ExecutionFilter parseExecutionFilter(String jsonFilter) {
        ExecutionFilter filter = new ExecutionFilter();

        if (jsonFilter != null) {
            JsonReader jsonReader = Json.createReader(new StringReader(jsonFilter));
            JsonArray array = jsonReader.readArray();
            for (int i = 0; i < array.size(); i++) {
                String property = array.getJsonObject(i).getJsonString(FilterEnums.FilterKey.PROPERTY.toString()).getString();

                if (FilterEnums.ExecutionFilterField.SYMBOL.getVarName().equals(property)) {
                    filter.getSymbolFilterMap().put(parseOperatorString(array, i), parseString(array, i));

                } else if (FilterEnums.ExecutionFilterField.SEC_TYPE.getVarName().equals(property)) {
                    FilterEnums.FilterOperatorEnum operator = parseOperatorEnum(array, i);
                    filter.getSecTypeFilterMap().put(operator, new HashSet<>());
                    for (String v : parseValues(array, i)) {
                        filter.getSecTypeFilterMap().get(operator).add(SecType.valueOf(v.toUpperCase()));
                    }

                } else if (FilterEnums.ExecutionFilterField.FILL_DATE.getVarName().equals(property)) {
                    LocalDateTime localDateTime = LocalDateTime.parse(parseString(array, i), HanSettings.JSON_DATE_FORMATTER);
                    filter.getFillDateFilterMap().put(parseOperatorDate(array, i), localDateTime);
                }
            }
            jsonReader.close();
        }
        return filter;
    }

    public TradeFilter parseTradeFilter(String jsonFilter) {
        TradeFilter filter = new TradeFilter();

        if (jsonFilter != null) {
            JsonReader jsonReader = Json.createReader(new StringReader(jsonFilter));
            JsonArray array = jsonReader.readArray();
            for (int i = 0; i < array.size(); i++) {
                String property = array.getJsonObject(i).getJsonString(FilterEnums.FilterKey.PROPERTY.toString()).getString();

                if (FilterEnums.TradeFilterField.SYMBOL.getVarName().equals(property)) {
                    filter.getSymbolFilterMap().put(parseOperatorString(array, i), parseString(array, i));

                } else if (FilterEnums.TradeFilterField.SEC_TYPE.getVarName().equals(property)) {
                    FilterEnums.FilterOperatorEnum operator = parseOperatorEnum(array, i);
                    filter.getSecTypeFilterMap().put(operator, new HashSet<>());
                    for (String v : parseValues(array, i)) {
                        filter.getSecTypeFilterMap().get(operator).add(SecType.valueOf(v.toUpperCase()));
                    }

                } else if (FilterEnums.TradeFilterField.OPEN_DATE.getVarName().equals(property)) {
                    LocalDateTime localDateTime = LocalDateTime.parse(parseString(array, i), HanSettings.JSON_DATE_FORMATTER);
                    filter.getOpenDateFilterMap().put(parseOperatorDate(array, i), localDateTime);

                }  else if (FilterEnums.TradeFilterField.STATUS.getVarName().equals(property)) {
                    FilterEnums.FilterOperatorEnum operator = parseOperatorEnum(array, i);
                    filter.getStatusFilterMap().put(operator, new HashSet<>());
                    for (String v : parseValues(array, i)) {
                        filter.getStatusFilterMap().get(operator).add(TradeStatus.valueOf(v.toUpperCase()));
                    }
                }
            }
            jsonReader.close();
        }
        return filter;
    }

    private String parseString(JsonArray array, int i) {
        return array.getJsonObject(i).getJsonString(FilterEnums.FilterKey.VALUE.toString()).getString();
    }

    private Long parseLong(JsonArray array, int i) {
        return array.getJsonObject(i).getJsonNumber(FilterEnums.FilterKey.VALUE.toString()).longValue();
    }

    private Set<String> parseValues(JsonArray array, int i) {
        JsonArray a = array.getJsonObject(i).getJsonArray(FilterEnums.FilterKey.VALUE.toString());
        Set<String> values = new HashSet<>();
        for (int j = 0; j < a.size(); j++) {
            values.add(a.getString(j));
        }
        return values;
    }

    private FilterEnums.FilterOperatorString parseOperatorString(JsonArray array, int i) {
        JsonString json = array.getJsonObject(i).getJsonString(FilterEnums.FilterKey.OPERATOR.toString());
        FilterEnums.FilterOperatorString operator;
        try {
            operator = (json != null ? FilterEnums.FilterOperatorString.valueOf(json.getString().toUpperCase()) : FilterEnums.FilterOperatorString.LIKE);
        } catch (Exception e) {
            operator = FilterEnums.FilterOperatorString.LIKE;
        }
        return operator;
    }

    private FilterEnums.FilterOperatorNumber parseOperatorNumber(JsonArray array, int i) {
        JsonString json = array.getJsonObject(i).getJsonString(FilterEnums.FilterKey.OPERATOR.toString());
        FilterEnums.FilterOperatorNumber operator;
        try {
            operator = (json != null ? FilterEnums.FilterOperatorNumber.valueOf(json.getString().toUpperCase()) : FilterEnums.FilterOperatorNumber.EQ);
        } catch (Exception e) {
            operator = FilterEnums.FilterOperatorNumber.EQ;
        }
        return operator;
    }

    private FilterEnums.FilterOperatorDate parseOperatorDate(JsonArray array, int i) {
        JsonString json = array.getJsonObject(i).getJsonString(FilterEnums.FilterKey.OPERATOR.toString());
        FilterEnums.FilterOperatorDate operator;
        try {
            operator = (json != null ? FilterEnums.FilterOperatorDate.valueOf(json.getString().toUpperCase()) : FilterEnums.FilterOperatorDate.EQ);
        } catch (Exception e) {
            operator = FilterEnums.FilterOperatorDate.EQ;
        }
        return operator;
    }

    private FilterEnums.FilterOperatorEnum parseOperatorEnum(JsonArray array, int i) {
        JsonString json = array.getJsonObject(i).getJsonString(FilterEnums.FilterKey.OPERATOR.toString());
        FilterEnums.FilterOperatorEnum operator;
        try {
            operator = (json != null ? FilterEnums.FilterOperatorEnum.valueOf(json.getString().toUpperCase()) : FilterEnums.FilterOperatorEnum.IN);
        } catch (Exception e) {
            operator = FilterEnums.FilterOperatorEnum.IN;
        }
        return operator;
    }
}
