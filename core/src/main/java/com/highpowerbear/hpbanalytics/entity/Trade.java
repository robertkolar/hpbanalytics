package com.highpowerbear.hpbanalytics.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.highpowerbear.hpbanalytics.common.HanSettings;
import com.highpowerbear.hpbanalytics.common.HanUtil;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.enums.TradeStatus;
import com.highpowerbear.hpbanalytics.enums.TradeType;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Created by robertk on 5/29/2017.
 */
@Entity
@Table(name = "trade", schema = "hpbanalytics", catalog = "hpbanalytics")
public class Trade implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name="trade_generator", sequenceName = "trade_seq", schema = "hpbanalytics", catalog = "hpbanalytics", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trade_generator")
    private Long id;
    @Enumerated(EnumType.STRING)
    private TradeType type;
    private String symbol;
    private String underlying;
    @Enumerated(EnumType.STRING)
    private Currency currency;
    @Enumerated(EnumType.STRING)
    private SecType secType;
    private Integer cumulativeQuantity;
    @Enumerated(EnumType.STRING)
    private TradeStatus status;
    private Integer openPosition;
    private BigDecimal avgOpenPrice;
    @JsonFormat(pattern = HanSettings.JSON_DATE_FORMAT)
    private LocalDateTime openDate;
    private BigDecimal avgClosePrice;
    @JsonFormat(pattern = HanSettings.JSON_DATE_FORMAT)
    private LocalDateTime closeDate;
    private BigDecimal profitLoss;
    @ManyToOne
    @JsonIgnore
    private Report report;
    @OneToMany(mappedBy = "trade", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("fillDate ASC")
    private List<SplitExecution> splitExecutions;

    @JsonProperty
    public Integer getReportId() {
        return this.report.getId();
    }

    @JsonProperty
    public String getDuration() {
        return closeDate != null ? HanUtil.toDurationString(Duration.between(openDate, closeDate).getSeconds()) : "";
    }

    public String print() {
        return (id + ", " + type + ", " + status + ", " + symbol + ", " + secType + ", " + (openDate != null ? HanUtil.formatLogDate(openDate) : "-") + ", " + (closeDate != null ? HanUtil.formatLogDate(closeDate) : "-") + ", " + profitLoss);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Trade trade = (Trade) o;

        return Objects.equals(id, trade.id);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
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

    public SecType getSecType() {
        return secType;
    }

    public void setSecType(SecType secType) {
        this.secType = secType;
    }

    public Integer getOpenPosition() {
        return openPosition;
    }

    public void setOpenPosition(Integer openPosition) {
        this.openPosition = openPosition;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TradeType getType() {
        return type;
    }

    public void setType(TradeType type) {
        this.type = type;
    }

    public Integer getCumulativeQuantity() {
        return cumulativeQuantity;
    }

    public void setCumulativeQuantity(Integer cummulativeQuantity) {
        this.cumulativeQuantity = cummulativeQuantity;
    }

    public TradeStatus getStatus() {
        return status;
    }

    public void setStatus(TradeStatus status) {
        this.status = status;
    }

    public LocalDateTime getOpenDate() {
        return openDate;
    }

    public void setOpenDate(LocalDateTime openDate) {
        this.openDate = openDate;
    }

    public LocalDateTime getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(LocalDateTime closeDate) {
        this.closeDate = closeDate;
    }

    public BigDecimal getAvgOpenPrice() {
        return avgOpenPrice;
    }

    public void setAvgOpenPrice(BigDecimal avgOpenPrice) {
        this.avgOpenPrice = avgOpenPrice;
    }

    public BigDecimal getAvgClosePrice() {
        return avgClosePrice;
    }

    public void setAvgClosePrice(BigDecimal avgClosePrice) {
        this.avgClosePrice = avgClosePrice;
    }

    public BigDecimal getProfitLoss() {
        return profitLoss;
    }

    public void setProfitLoss(BigDecimal profitLoss) {
        this.profitLoss = profitLoss;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report source) {
        this.report = source;
    }

    public List<SplitExecution> getSplitExecutions() {
        return splitExecutions;
    }

    public void setSplitExecutions(List<SplitExecution> splitExecutions) {
        for (SplitExecution se : splitExecutions) {
            se.setTrade(this);
        }
        this.splitExecutions = splitExecutions;
    }

    public SplitExecution getLastSplitExecution() {
        return getSplitExecutions().get(getSplitExecutions().size() - 1);
    }
    
    public Boolean getOpen() {
        return (status == TradeStatus.OPEN);
    }

    @Override
    public String toString() {
        return "Trade{" +
                "id=" + id +
                ", type=" + type +
                ", symbol='" + symbol + '\'' +
                ", underlying='" + underlying + '\'' +
                ", currency=" + currency +
                ", secType=" + secType +
                ", cumulativeQuantity=" + cumulativeQuantity +
                ", status=" + status +
                ", openPosition=" + openPosition +
                ", avgOpenPrice=" + avgOpenPrice +
                ", openDate=" + openDate +
                ", avgClosePrice=" + avgClosePrice +
                ", closeDate=" + closeDate +
                ", profitLoss=" + profitLoss +
                '}';
    }
}
