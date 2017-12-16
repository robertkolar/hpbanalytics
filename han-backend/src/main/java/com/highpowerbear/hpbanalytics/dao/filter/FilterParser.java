package com.highpowerbear.hpbanalytics.dao.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.highpowerbear.hpbanalytics.common.HanDefinitions;
import com.highpowerbear.hpbanalytics.enums.OrderStatus;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.enums.TradeStatus;
import org.springframework.stereotype.Component;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.json.JsonString;
import java.io.StringReader;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by robertk on 10/19/2015.
 */
@Component
public class FilterParser {

    private ObjectMapper mapper = new ObjectMapper();

    public IbOrderFilter parseIbOrderFilter(String jsonFilter) {
        IbOrderFilter filter = new IbOrderFilter();

        if (jsonFilter != null) {
            JsonReader jsonReader = Json.createReader(new StringReader(jsonFilter));
            JsonArray array = jsonReader.readArray();
            for (int i = 0; i < array.size(); i++) {
                String property = array.getJsonObject(i).getJsonString(HanDefinitions.FilterKey.PROPERTY.toString()).getString();

                if (HanDefinitions.IbOrderFilterField.SYMBOL.getVarName().equals(property)) {
                    filter.getSymbolFilterMap().put(parseOperatorString(array, i), parseString(array, i));

                } else if (HanDefinitions.IbOrderFilterField.SEC_TYPE.getVarName().equals(property)) {
                    HanDefinitions.FilterOperatorEnum operator = parseOperatorEnum(array, i);
                    filter.getSecTypeFilterMap().put(operator, new HashSet<>());
                    for (String v : parseValues(array, i)) {
                        filter.getSecTypeFilterMap().get(operator).add(v.toUpperCase());
                    }

                } else if (HanDefinitions.IbOrderFilterField.SUBMIT_DATE.getVarName().equals(property)) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(Long.valueOf(parseString(array, i)));
                    filter.getSubmitDateFilterMap().put(parseOperatorCalendar(array, i), cal);

                } else if (HanDefinitions.IbOrderFilterField.STATUS.getVarName().equals(property)) {
                    HanDefinitions.FilterOperatorEnum operator = parseOperatorEnum(array, i);
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
                String property = array.getJsonObject(i).getJsonString(HanDefinitions.FilterKey.PROPERTY.toString()).getString();

                if (HanDefinitions.ExecutionFilterField.SYMBOL.getVarName().equals(property)) {
                    filter.getSymbolFilterMap().put(parseOperatorString(array, i), parseString(array, i));

                } else if (HanDefinitions.ExecutionFilterField.SEC_TYPE.getVarName().equals(property)) {
                    HanDefinitions.FilterOperatorEnum operator = parseOperatorEnum(array, i);
                    filter.getSecTypeFilterMap().put(operator, new HashSet<>());
                    for (String v : parseValues(array, i)) {
                        filter.getSecTypeFilterMap().get(operator).add(SecType.valueOf(v.toUpperCase()));
                    }

                } else if (HanDefinitions.ExecutionFilterField.FILL_DATE.getVarName().equals(property)) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(Long.valueOf(parseString(array, i)));
                    filter.getFillDateFilterMap().put(parseOperatorCalendar(array, i), cal);
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
                String property = array.getJsonObject(i).getJsonString(HanDefinitions.FilterKey.PROPERTY.toString()).getString();

                if (HanDefinitions.TradeFilterField.SYMBOL.getVarName().equals(property)) {
                    filter.getSymbolFilterMap().put(parseOperatorString(array, i), parseString(array, i));

                } else if (HanDefinitions.TradeFilterField.SEC_TYPE.getVarName().equals(property)) {
                    HanDefinitions.FilterOperatorEnum operator = parseOperatorEnum(array, i);
                    filter.getSecTypeFilterMap().put(operator, new HashSet<>());
                    for (String v : parseValues(array, i)) {
                        filter.getSecTypeFilterMap().get(operator).add(SecType.valueOf(v.toUpperCase()));
                    }

                } else if (HanDefinitions.TradeFilterField.OPEN_DATE.getVarName().equals(property)) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(Long.valueOf(parseString(array, i)));
                    filter.getOpenDateFilterMap().put(parseOperatorCalendar(array, i), cal);

                }  else if (HanDefinitions.TradeFilterField.STATUS.getVarName().equals(property)) {
                    HanDefinitions.FilterOperatorEnum operator = parseOperatorEnum(array, i);
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
        return array.getJsonObject(i).getJsonString(HanDefinitions.FilterKey.VALUE.toString()).getString();
    }

    private Long parseLong(JsonArray array, int i) {
        return array.getJsonObject(i).getJsonNumber(HanDefinitions.FilterKey.VALUE.toString()).longValue();
    }

    private Set<String> parseValues(JsonArray array, int i) {
        JsonArray a = array.getJsonObject(i).getJsonArray(HanDefinitions.FilterKey.VALUE.toString());
        Set<String> values = new HashSet<>();
        for (int j = 0; j < a.size(); j++) {
            values.add(a.getString(j));
        }
        return values;
    }

    private HanDefinitions.FilterOperatorString parseOperatorString(JsonArray array, int i) {
        JsonString json = array.getJsonObject(i).getJsonString(HanDefinitions.FilterKey.OPERATOR.toString());
        HanDefinitions.FilterOperatorString operator;
        try {
            operator = (json != null ? HanDefinitions.FilterOperatorString.valueOf(json.getString().toUpperCase()) : HanDefinitions.FilterOperatorString.LIKE);
        } catch (Exception e) {
            operator = HanDefinitions.FilterOperatorString.LIKE;
        }
        return operator;
    }

    private HanDefinitions.FilterOperatorNumber parseOperatorNumber(JsonArray array, int i) {
        JsonString json = array.getJsonObject(i).getJsonString(HanDefinitions.FilterKey.OPERATOR.toString());
        HanDefinitions.FilterOperatorNumber operator;
        try {
            operator = (json != null ? HanDefinitions.FilterOperatorNumber.valueOf(json.getString().toUpperCase()) : HanDefinitions.FilterOperatorNumber.EQ);
        } catch (Exception e) {
            operator = HanDefinitions.FilterOperatorNumber.EQ;
        }
        return operator;
    }

    private HanDefinitions.FilterOperatorCalendar parseOperatorCalendar(JsonArray array, int i) {
        JsonString json = array.getJsonObject(i).getJsonString(HanDefinitions.FilterKey.OPERATOR.toString());
        HanDefinitions.FilterOperatorCalendar operator;
        try {
            operator = (json != null ? HanDefinitions.FilterOperatorCalendar.valueOf(json.getString().toUpperCase()) : HanDefinitions.FilterOperatorCalendar.EQ);
        } catch (Exception e) {
            operator = HanDefinitions.FilterOperatorCalendar.EQ;
        }
        return operator;
    }

    private HanDefinitions.FilterOperatorEnum parseOperatorEnum(JsonArray array, int i) {
        JsonString json = array.getJsonObject(i).getJsonString(HanDefinitions.FilterKey.OPERATOR.toString());
        HanDefinitions.FilterOperatorEnum operator;
        try {
            operator = (json != null ? HanDefinitions.FilterOperatorEnum.valueOf(json.getString().toUpperCase()) : HanDefinitions.FilterOperatorEnum.IN);
        } catch (Exception e) {
            operator = HanDefinitions.FilterOperatorEnum.IN;
        }
        return operator;
    }
}
