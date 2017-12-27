package com.highpowerbear.hpbanalytics.iblogger.dto;

import com.highpowerbear.hpbanalytics.enums.Action;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.SecType;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by robertk on 4/6/2015.
 */
public class IbExecution implements Serializable {
    private static final long serialVersionUID = -5761622659478582498L;

    private String origin; // IB:ibAccountId
    private String referenceId; // permId
    private Action action;
    private Integer quantity;
    private String underlying;
    private Currency currency;
    private String symbol;
    private SecType secType;
    private Calendar fillDate;
    private Double fillPrice;

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getUnderlying() {
        return underlying;
    }

    public void setUnderlying(String underlying) {
        this.underlying = underlying;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public SecType getSecType() {
        return secType;
    }

    public void setSecType(SecType secType) {
        this.secType = secType;
    }

    public Calendar getFillDate() {
        return fillDate;
    }

    public void setFillDate(Calendar fillDate) {
        this.fillDate = fillDate;
    }

    public Double getFillPrice() {
        return fillPrice;
    }

    public void setFillPrice(Double fillPrice) {
        this.fillPrice = fillPrice;
    }
}
