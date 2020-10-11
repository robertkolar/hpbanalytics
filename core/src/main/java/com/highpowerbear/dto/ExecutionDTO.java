package com.highpowerbear.dto;

import com.ib.client.Types;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by robertk on 10/3/2020.
 */
public class ExecutionDTO implements Serializable {
    private static final long serialVersionUID = -7595159006402413521L;

    private String reference;
    private Types.Action action;
    private Integer quantity;
    private String symbol;
    private String underlying;
    private String currency;
    private Types.SecType secType;
    private Double multiplier;
    private LocalDateTime fillDate;
    private BigDecimal fillPrice;

    public String getReference() {
        return reference;
    }

    public ExecutionDTO setReference(String reference) {
        this.reference = reference;
        return this;
    }

    public Types.Action getAction() {
        return action;
    }

    public ExecutionDTO setAction(Types.Action action) {
        this.action = action;
        return this;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public ExecutionDTO setQuantity(Integer quantity) {
        this.quantity = quantity;
        return this;
    }

    public String getSymbol() {
        return symbol;
    }

    public ExecutionDTO setSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public String getUnderlying() {
        return underlying;
    }

    public ExecutionDTO setUnderlying(String underlying) {
        this.underlying = underlying;
        return this;
    }

    public String getCurrency() {
        return currency;
    }

    public ExecutionDTO setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public Types.SecType getSecType() {
        return secType;
    }

    public ExecutionDTO setSecType(Types.SecType secType) {
        this.secType = secType;
        return this;
    }

    public Double getMultiplier() {
        return multiplier;
    }

    public ExecutionDTO setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
        return this;
    }

    public LocalDateTime getFillDate() {
        return fillDate;
    }

    public ExecutionDTO setFillDate(LocalDateTime fillDate) {
        this.fillDate = fillDate;
        return this;
    }

    public BigDecimal getFillPrice() {
        return fillPrice;
    }

    public ExecutionDTO setFillPrice(BigDecimal fillPrice) {
        this.fillPrice = fillPrice;
        return this;
    }

    @Override
    public String toString() {
        return "ExecutionDTO{" +
                "reference='" + reference + '\'' +
                ", action=" + action +
                ", quantity=" + quantity +
                ", symbol='" + symbol + '\'' +
                ", underlying='" + underlying + '\'' +
                ", currency='" + currency + '\'' +
                ", secType=" + secType +
                ", multiplier=" + multiplier +
                ", fillDate=" + fillDate +
                ", fillPrice=" + fillPrice +
                '}';
    }
}
