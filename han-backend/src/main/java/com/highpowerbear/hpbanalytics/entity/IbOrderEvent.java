package com.highpowerbear.hpbanalytics.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.highpowerbear.hpbanalytics.enums.OrderStatus;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by robertk on 5/29/2017.
 */
@Entity
@Table(name = "iborderevent", schema = "iblogger", catalog = "hpbanalytics")
public class IbOrderEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar eventDate;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private Double price;
    @XmlTransient
    @ManyToOne
    private IbOrder ibOrder;

    @JsonProperty
    public Long getIbOrderDbId() {
        return ibOrder.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IbOrderEvent that = (IbOrderEvent) o;

        return !(id != null ? !id.equals(that.id) : that.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Calendar getEventDate() {
        return eventDate;
    }

    public void setEventDate(Calendar eventDate) {
        this.eventDate = eventDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public IbOrder getIbOrder() {
        return ibOrder;
    }

    public void setIbOrder(IbOrder ibOrder) {
        this.ibOrder = ibOrder;
    }
}
