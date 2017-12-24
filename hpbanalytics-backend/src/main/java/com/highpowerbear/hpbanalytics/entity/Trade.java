package com.highpowerbear.hpbanalytics.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.highpowerbear.hpbanalytics.common.HanUtil;
import com.highpowerbear.hpbanalytics.common.OptionUtil;
import com.highpowerbear.hpbanalytics.enums.Action;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.FuturePlMultiplier;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

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
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar openDate;
    private BigDecimal avgClosePrice;
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar closeDate;
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
        return (closeDate != null ? HanUtil.toDurationString(closeDate.getTimeInMillis() - openDate.getTimeInMillis()) : "");
    }

    public void calculate() {
        MathContext mc = new MathContext(7);
        report = splitExecutions.iterator().next().execution.getReport();
        type = (splitExecutions.iterator().next().getCurrentPosition() > 0 ? TradeType.LONG : TradeType.SHORT);
        symbol = (splitExecutions == null || splitExecutions.isEmpty() ? null : splitExecutions.iterator().next().execution.getSymbol());
        underlying = (splitExecutions == null || splitExecutions.isEmpty() ? null : splitExecutions.iterator().next().execution.getUnderlying());
        currency = (splitExecutions == null || splitExecutions.isEmpty() ? null : splitExecutions.iterator().next().execution.getCurrency());
        secType = (splitExecutions == null || splitExecutions.isEmpty() ? null : splitExecutions.iterator().next().execution.getSecType());
        openPosition = (splitExecutions == null || splitExecutions.isEmpty() ? null : splitExecutions.get(splitExecutions.size() - 1).getCurrentPosition());
        BigDecimal cumulativeOpenPrice = new BigDecimal(0.0);
        BigDecimal cumulativeClosePrice = new BigDecimal(0.0);
        cumulativeQuantity = 0;

        for (SplitExecution se : splitExecutions) {
            if ((type == TradeType.LONG && se.execution.getAction() == Action.BUY) || (type == TradeType.SHORT && se.execution.getAction() == Action.SELL)) {
                cumulativeQuantity += se.getSplitQuantity();
                cumulativeOpenPrice = cumulativeOpenPrice.add(new BigDecimal(se.getSplitQuantity()).multiply(se.execution.getFillPrice(), mc));
            }
            if (status == TradeStatus.CLOSED) {
                if ((type == TradeType.LONG && se.execution.getAction() == Action.SELL) || (type == TradeType.SHORT && se.execution.getAction() == Action.BUY)) {
                    cumulativeClosePrice = cumulativeClosePrice.add(new BigDecimal(se.getSplitQuantity()).multiply(se.execution.getFillPrice(), mc));
                }
            }
        }

        avgOpenPrice = cumulativeOpenPrice.divide(new BigDecimal(cumulativeQuantity), mc);
        openDate = getSplitExecutions().get(0).getExecution().getFillDate();

        if (status == TradeStatus.CLOSED) {
            avgClosePrice = cumulativeClosePrice.divide(new BigDecimal(cumulativeQuantity), mc);
            closeDate = getSplitExecutions().get(getSplitExecutions().size() - 1).getExecution().getFillDate();
            profitLoss = (TradeType.LONG.equals(type) ? cumulativeClosePrice.subtract(cumulativeOpenPrice, mc) : cumulativeOpenPrice.subtract(cumulativeClosePrice, mc));
            if (SecType.OPT.equals(getSecType())) {
                profitLoss = profitLoss.multiply((OptionUtil.isMini(symbol) ? new BigDecimal(10) : new BigDecimal(100)), mc);
            }
            if (SecType.FUT.equals(getSecType())) {
                profitLoss = profitLoss.multiply(new BigDecimal(FuturePlMultiplier.getMultiplierByUnderlying(underlying)), mc);
            }
        }
    }

    public String print() {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
        return (id + ", " + type + ", " + status + ", " + symbol + ", " + secType + ", " + (openDate != null ? df.format(openDate.getTime()) : "-") + ", " + (closeDate != null ? df.format(closeDate.getTime()) : "-") + ", " + profitLoss);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Trade trade = (Trade) o;

        return !(id != null ? !id.equals(trade.id) : trade.id != null);

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

    public Calendar getOpenDate() {
        return openDate;
    }

    public void setOpenDate(Calendar openDate) {
        this.openDate = openDate;
    }

    public Calendar getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Calendar closeDate) {
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
