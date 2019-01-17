package com.highpowerbear.hpbanalytics.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.highpowerbear.hpbanalytics.common.HanSettings;
import com.highpowerbear.hpbanalytics.common.HanUtil;
import com.highpowerbear.hpbanalytics.enums.Action;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.SecType;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
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
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name="execution_generator", sequenceName = "execution_seq", schema = "hpbanalytics", catalog = "hpbanalytics", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "execution_generator")
    private Long id;
    @JsonFormat(pattern = HanSettings.JSON_DATE_FORMAT)
    private LocalDateTime receivedDate;
    @ManyToOne
    @JsonIgnore
    private Report report;
    private String comment;
    private String origin; // in case of IB origin --> IB:ibAccountId, in case of manual addition --> manual
    private String referenceId; // in case of IB origin --> permId
    @Enumerated(EnumType.STRING)
    private Action action;
    private Integer quantity;
    private String symbol;
    private String underlying;
    @Enumerated(EnumType.STRING)
    private Currency currency;
    @Enumerated(EnumType.STRING)
    private SecType secType;
    @JsonFormat(pattern = HanSettings.JSON_DATE_FORMAT)
    private LocalDateTime fillDate;
    private BigDecimal fillPrice;

    @JsonProperty
    public Integer getReportId() {
        return (report != null ? report.getId() : null);
    }

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
        return (id + ", " + action + ", " + quantity + ", " + symbol + ", " + HanUtil.formatLogDate(fillDate) + ", " + fillPrice);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDateTime receivedDate) {
        this.receivedDate = receivedDate;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

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

    public SecType getSecType() {
        return secType;
    }

    public void setSecType(SecType secType) {
        this.secType = secType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
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

    public LocalDateTime getFillDate() {
        return fillDate;
    }
    
    public void setFillDate(LocalDateTime fillDate) {
        this.fillDate = fillDate;
    }

    public BigDecimal getFillPrice() {
        return fillPrice;
    }

    public void setFillPrice(BigDecimal fillPrice) {
        this.fillPrice = fillPrice;
    }
}
