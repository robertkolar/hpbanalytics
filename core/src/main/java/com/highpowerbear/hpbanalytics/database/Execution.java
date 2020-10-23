package com.highpowerbear.hpbanalytics.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.highpowerbear.hpbanalytics.config.HanSettings;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.ib.client.Types;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Created by robertk on 5/29/2017.
 */
@Entity
@Table(name = "execution", schema = HanSettings.DB_SCHEMA, catalog = HanSettings.DB_DATABASE)
public class Execution implements Serializable {
    private static final long serialVersionUID = 2067980957084297540L;

    @Id
    @SequenceGenerator(name="execution_generator", sequenceName = "execution_seq", schema = HanSettings.DB_SCHEMA, catalog = HanSettings.DB_DATABASE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "execution_generator")
    private Long id;
    private String reference;
    @Enumerated(EnumType.STRING)
    private Types.Action action;
    private Integer quantity;
    private String symbol;
    private String underlying;
    @Enumerated(EnumType.STRING)
    private Currency currency;
    @Enumerated(EnumType.STRING)
    private Types.SecType secType;
    private Double multiplier;
    private LocalDateTime fillDate;
    private BigDecimal fillPrice;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Trade trade;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Execution execution = (Execution) o;

        return Objects.equals(id, execution.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public Long getId() {
        return id;
    }

    public Execution setId(Long id) {
        this.id = id;
        return this;
    }

    public String getReference() {
        return reference;
    }

    public Execution setReference(String reference) {
        this.reference = reference;
        return this;
    }

    public Types.Action getAction() {
        return action;
    }

    public Execution setAction(Types.Action action) {
        this.action = action;
        return this;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Execution setQuantity(Integer quantity) {
        this.quantity = quantity;
        return this;
    }

    public String getSymbol() {
        return symbol;
    }

    public Execution setSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public String getUnderlying() {
        return underlying;
    }

    public Execution setUnderlying(String underlying) {
        this.underlying = underlying;
        return this;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Execution setCurrency(Currency currency) {
        this.currency = currency;
        return this;
    }

    public Types.SecType getSecType() {
        return secType;
    }

    public Execution setSecType(Types.SecType secType) {
        this.secType = secType;
        return this;
    }

    public Double getMultiplier() {
        return multiplier;
    }

    public Execution setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
        return this;
    }

    public LocalDateTime getFillDate() {
        return fillDate;
    }
    
    public Execution setFillDate(LocalDateTime fillDate) {
        this.fillDate = fillDate;
        return this;
    }

    public BigDecimal getFillPrice() {
        return fillPrice;
    }

    public Execution setFillPrice(BigDecimal fillPrice) {
        this.fillPrice = fillPrice;
        return this;
    }

    public Trade getTrade() {
        return trade;
    }

    public Execution setTrade(Trade trade) {
        this.trade = trade;
        return this;
    }

    public Long getTradeId() {
        return trade != null ? trade.getId() : null;
    }

    public String getTradeExecutionIds() {
        return trade != null ? trade.getExecutionIds() : null;
    }

    @Override
    public String toString() {
        return "Execution{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", action=" + action +
                ", quantity=" + quantity +
                ", symbol='" + symbol + '\'' +
                ", underlying='" + underlying + '\'' +
                ", currency=" + currency +
                ", secType=" + secType +
                ", multiplier=" + multiplier +
                ", fillDate=" + fillDate +
                ", fillPrice=" + fillPrice +
                ", tradeId=" + getTradeId() +
                ", tradeExecutionIds=" + getTradeExecutionIds() +
                '}';
    }
}
