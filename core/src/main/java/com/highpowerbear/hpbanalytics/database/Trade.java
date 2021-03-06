package com.highpowerbear.hpbanalytics.database;

import com.highpowerbear.hpbanalytics.common.HanUtil;
import com.highpowerbear.hpbanalytics.config.HanSettings;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.TradeStatus;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import com.ib.client.Types;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by robertk on 5/29/2017.
 */
@Entity
@Table(name = "trade", schema = HanSettings.DB_SCHEMA, catalog = HanSettings.DB_DATABASE)
public class Trade implements Serializable {
    private static final long serialVersionUID = 3978501428965359313L;

    @Id
    @SequenceGenerator(name="trade_generator", sequenceName = "trade_seq", schema = HanSettings.DB_SCHEMA, catalog = HanSettings.DB_DATABASE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trade_generator")
    private Long id;
    @Enumerated(EnumType.STRING)
    private TradeType type;
    private String symbol;
    private String underlying;
    @Enumerated(EnumType.STRING)
    private Currency currency;
    @Enumerated(EnumType.STRING)
    private Types.SecType secType;
    private Double multiplier;
    private Integer cumulativeQuantity;
    @Enumerated(EnumType.STRING)
    private TradeStatus status;
    private Integer openPosition;
    private BigDecimal avgOpenPrice;
    private LocalDateTime openDate;
    private BigDecimal avgClosePrice;
    private LocalDateTime closeDate;
    private BigDecimal profitLoss;
    @OneToMany(mappedBy = "trade", fetch = FetchType.EAGER)
    @OrderBy("fillDate ASC")
    private List<Execution> executions = new ArrayList<>();

    public String getDuration() {
        return closeDate != null ? HanUtil.toDurationString(Duration.between(openDate, closeDate).getSeconds()) : "";
    }

    public boolean isOpen() {
        return TradeStatus.OPEN.equals(status);
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

    public Long getId() {
        return id;
    }

    public Trade setId(Long id) {
        this.id = id;
        return this;
    }

    public TradeType getType() {
        return type;
    }

    public Trade setType(TradeType type) {
        this.type = type;
        return this;
    }

    public String getSymbol() {
        return symbol;
    }

    public Trade setSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public String getUnderlying() {
        return underlying;
    }

    public Trade setUnderlying(String underlying) {
        this.underlying = underlying;
        return this;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Trade setCurrency(Currency currency) {
        this.currency = currency;
        return this;
    }

    public Types.SecType getSecType() {
        return secType;
    }

    public Trade setSecType(Types.SecType secType) {
        this.secType = secType;
        return this;
    }

    public Double getMultiplier() {
        return multiplier;
    }

    public Trade setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
        return this;
    }

    public Integer getCumulativeQuantity() {
        return cumulativeQuantity;
    }

    public Trade setCumulativeQuantity(Integer cumulativeQuantity) {
        this.cumulativeQuantity = cumulativeQuantity;
        return this;
    }

    public TradeStatus getStatus() {
        return status;
    }

    public Trade setStatus(TradeStatus status) {
        this.status = status;
        return this;
    }

    public Integer getOpenPosition() {
        return openPosition;
    }

    public Trade setOpenPosition(Integer openPosition) {
        this.openPosition = openPosition;
        return this;
    }

    public BigDecimal getAvgOpenPrice() {
        return avgOpenPrice;
    }

    public Trade setAvgOpenPrice(BigDecimal avgOpenPrice) {
        this.avgOpenPrice = avgOpenPrice;
        return this;
    }

    public LocalDateTime getOpenDate() {
        return openDate;
    }

    public Trade setOpenDate(LocalDateTime openDate) {
        this.openDate = openDate;
        return this;
    }

    public BigDecimal getAvgClosePrice() {
        return avgClosePrice;
    }

    public Trade setAvgClosePrice(BigDecimal avgClosePrice) {
        this.avgClosePrice = avgClosePrice;
        return this;
    }

    public LocalDateTime getCloseDate() {
        return closeDate;
    }

    public Trade setCloseDate(LocalDateTime closeDate) {
        this.closeDate = closeDate;
        return this;
    }

    public BigDecimal getProfitLoss() {
        return profitLoss;
    }

    public Trade setProfitLoss(BigDecimal profitLoss) {
        this.profitLoss = profitLoss;
        return this;
    }

    public List<Execution> getExecutions() {
        return executions;
    }

    public Trade setExecutions(List<Execution> executions) {
        this.executions = executions;
        return this;
    }

    public String getExecutionIds() {
        return executions.stream()
                .map(Execution::getId)
                .map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        return "Trade{" +
                "id=" + id +
                ", type=" + type +
                ", symbol='" + symbol + '\'' +
                ", secType=" + secType +
                ", openDate=" + openDate +
                ", closeDate=" + closeDate +
                ", profitLoss=" + profitLoss +
                ", executionIds=" + getExecutionIds() +
                '}';
    }
}
