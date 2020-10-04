package com.highpowerbear.hpbanalytics.database;

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
@Table(name = "execution", schema = "hpbanalytics", catalog = "hpbanalytics")
public class Execution implements Serializable, Comparable<Execution> {
    private static final long serialVersionUID = 2067980957084297540L;

    @Id
    @SequenceGenerator(name="execution_generator", sequenceName = "execution_seq", schema = "hpbanalytics", catalog = "hpbanalytics", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "execution_generator")
    private Long id;
    private LocalDateTime receivedDate;
    private String comment;
    private String origin;
    private String referenceId;
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

    @Override
    public int compareTo(Execution other) {
        return fillDate.compareTo(other.fillDate);
    }

    public String print() {
        return (id + ", " + action + ", " + quantity + ", " + symbol + ", " + fillDate + ", " + fillPrice);
    }

    public Long getId() {
        return id;
    }

    public Execution setId(Long id) {
        this.id = id;
        return this;
    }

    public LocalDateTime getReceivedDate() {
        return receivedDate;
    }

    public Execution setReceivedDate(LocalDateTime receivedDate) {
        this.receivedDate = receivedDate;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public Execution setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public String getOrigin() {
        return origin;
    }

    public Execution setOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public Execution setReferenceId(String referenceId) {
        this.referenceId = referenceId;
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
}
