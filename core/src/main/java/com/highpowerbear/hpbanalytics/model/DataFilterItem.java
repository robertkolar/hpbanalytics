package com.highpowerbear.hpbanalytics.model;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by robertk on 10/4/2020.
 */
public class DataFilterItem {

    private String property;
    private String operator;

    private String value;
    private Double doubleValue;
    private final List<String> values = new ArrayList<>();

    @JsonSetter("value")
    public void setValueNode(JsonNode valueNode) {
        if (valueNode.isTextual()) {
            value = valueNode.asText();

        } else if (valueNode.isNumber()) {
            doubleValue = valueNode.asDouble();

        } else if (valueNode.isArray()) {
            valueNode.forEach(item -> values.add(item.asText()));
        }
    }

    public String getProperty() {
        return property;
    }

    public String getOperator() {
        return operator;
    }

    public String getValue() {
        return value;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }

    public List<String> getValues() {
        return values;
    }
}
