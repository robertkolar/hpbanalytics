package com.highpowerbear.hpbanalytics.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar fillDate;
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
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
        return (id + ", " + execution.getAction() + ", " + execution.getSymbol() + ", " + splitQuantity + " (" + execution.getQuantity() + ")"+ ", "+ currentPosition + ", " + df.format(execution.getFillDate().getTime()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SplitExecution that = (SplitExecution) o;

        return (id != null ? id.equals(that.id) : that.id == null) && (currentPosition != null ? currentPosition.equals(that.currentPosition) : that.currentPosition == null) && !(execution != null ? !execution.equals(that.execution) : that.execution != null);

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

    public Calendar getFillDate() {
        return fillDate;
    }

    public void setFillDate(Calendar fillDate) {
        this.fillDate = fillDate;
    }
}
