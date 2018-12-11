package com.highpowerbear.hpbanalytics.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.highpowerbear.hpbanalytics.common.CoreSettings;
import com.highpowerbear.hpbanalytics.common.CoreUtil;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Created by robertk on 5/29/2017.
 */
@Entity
@Table(name = "split_execution", schema = "hpbanalytics", catalog = "hpbanalytics")
public class SplitExecution implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name="split_execution_generator", sequenceName = "split_execution_seq", schema = "hpbanalytics", catalog = "hpbanalytics", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "split_execution_generator")
    private Long id;
    private Integer splitQuantity;
    private Integer currentPosition;
    @JsonFormat(pattern = CoreSettings.JSON_DATE_FORMAT)
    private LocalDateTime fillDate;
    @ManyToOne
    @JsonIgnore
    private Execution execution;
    @ManyToOne
    @JsonIgnore
    private Trade trade;

    @JsonProperty
    private String getExecutionDisplay() {
        return execution.print();
    }

    @JsonProperty
    public Long getTradeId() {
        return this.trade.getId();
    }

    public String print() {
        return (id + ", " + execution.getAction() + ", " + execution.getSymbol() + ", " + splitQuantity + " (" + execution.getQuantity() + ")"+ ", "+ currentPosition + ", " + CoreUtil.formatLogDate(execution.getFillDate()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SplitExecution that = (SplitExecution) o;

        return (Objects.equals(id, that.id)) && (Objects.equals(currentPosition, that.currentPosition)) && Objects.equals(execution, that.execution);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (currentPosition != null ? currentPosition.hashCode() : 0);
        result = 31 * result + (execution != null ? execution.hashCode() : 0);
        return result;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Execution getExecution() {
        return execution;
    }

    public void setExecution(Execution execution) {
        this.execution = execution;
    }

    public Integer getSplitQuantity() {
        return splitQuantity;
    }

    public void setSplitQuantity(Integer splitQuantity) {
        this.splitQuantity = splitQuantity;
    }

    public Trade getTrade() {
        return trade;
    }

    public void setTrade(Trade trade) {
        this.trade = trade;
    }

    public Integer getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(Integer currentPosition) {
        this.currentPosition = currentPosition;
    }

    public LocalDateTime getFillDate() {
        return fillDate;
    }

    public void setFillDate(LocalDateTime fillDate) {
        this.fillDate = fillDate;
    }
}
