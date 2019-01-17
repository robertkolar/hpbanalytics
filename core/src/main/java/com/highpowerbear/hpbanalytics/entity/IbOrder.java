package com.highpowerbear.hpbanalytics.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.highpowerbear.hpbanalytics.common.HanSettings;
import com.highpowerbear.hpbanalytics.enums.OrderStatus;
import net.minidev.json.annotate.JsonIgnore;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by robertk on 5/29/2017.
 */
@Entity
@Table(name = "ib_order", schema = "hpbanalytics", catalog = "hpbanalytics")
public class IbOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name="ib_order_generator", sequenceName = "ib_order_seq", schema = "hpbanalytics", catalog = "hpbanalytics", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ib_order_generator")
    private Long id;
    private Long permId;
    private Integer orderId;
    private Integer clientId;
    @JsonIgnore
    private String accountId;
    private String action;
    private Integer quantity;
    private String underlying;
    private String currency;
    private String symbol;
    private String secType;
    private String orderType;
    @JsonFormat(pattern = HanSettings.JSON_DATE_FORMAT)
    private LocalDateTime submitDate;
    private Double orderPrice;
    private String tif;
    private Integer parentId;
    private String ocaGroup;
    @JsonFormat(pattern = HanSettings.JSON_DATE_FORMAT)
    private LocalDateTime statusDate;
    private Double fillPrice;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @OneToMany(mappedBy = "ibOrder", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("eventDate DESC, id DESC")
    private List<IbOrderEvent> ibOrderEvents;
    @Transient
    private Integer heartbeatCount;

    public void addEvent(OrderStatus status, Double price) {
        this.status = status;
        this.statusDate = LocalDateTime.now();
        IbOrderEvent e = new IbOrderEvent();
        e.setIbOrder(this);
        e.setEventDate(this.statusDate );
        e.setStatus(this.status);
        e.setPrice(price);
        if (OrderStatus.SUBMITTED.equals(status)) {
            this.submitDate = this.statusDate;
        }
        if (OrderStatus.FILLED.equals(status)) {
            this.fillPrice = price;
        }
        if (ibOrderEvents == null) {
            ibOrderEvents = new ArrayList<>();
        }
        ibOrderEvents.add(e);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IbOrder ibOrder = (IbOrder) o;

        return Objects.equals(id, ibOrder.id);

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

    public Long getPermId() {
        return permId;
    }

    public void setPermId(Long permId) {
        this.permId = permId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getUnderlying() {
        return underlying;
    }

    public void setUnderlying(String underlying) {
        this.underlying = underlying;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSecType() {
        return secType;
    }

    public void setSecType(String secType) {
        this.secType = secType;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public LocalDateTime getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(LocalDateTime submitDate) {
        this.submitDate = submitDate;
    }

    public Double getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(Double orderPrice) {
        this.orderPrice = orderPrice;
    }

    public String getTif() {
        return tif;
    }

    public void setTif(String tif) {
        this.tif = tif;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getOcaGroup() {
        return ocaGroup;
    }

    public void setOcaGroup(String ocaGroup) {
        this.ocaGroup = ocaGroup;
    }

    public LocalDateTime getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(LocalDateTime fillDate) {
        this.statusDate = fillDate;
    }

    public Double getFillPrice() {
        return fillPrice;
    }

    public void setFillPrice(Double fillPrice) {
        this.fillPrice = fillPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public List<IbOrderEvent> getIbOrderEvents() {
        return ibOrderEvents;
    }

    public void setIbOrderEvents(List<IbOrderEvent> events) {
        this.ibOrderEvents = events;
    }

    public Integer getHeartbeatCount() {
        return heartbeatCount;
    }

    public void setHeartbeatCount(Integer heartbeatCount) {
        this.heartbeatCount = heartbeatCount;
    }
}
