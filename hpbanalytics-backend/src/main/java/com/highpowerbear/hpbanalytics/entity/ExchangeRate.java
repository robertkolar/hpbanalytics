package com.highpowerbear.hpbanalytics.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by robertk on 5/29/2017.
 */
@Entity
@Table(name = "exchangerate", schema = "report", catalog = "hpbanalytics")
public class ExchangeRate implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String date; // yyyy-MM-dd
    @Column(name = "eurusd")
    private Double eurUsd;
    @Column(name = "gbpusd")
    private Double gbpUsd;
    @Column(name = "audusd")
    private Double audUsd;
    @Column(name = "nzdusd")
    private Double nzdUsd;
    @Column(name = "usdchf")
    private Double usdChf;
    @Column(name = "usdjpy")
    private Double usdJpy;
    @Column(name = "usdcad")
    private Double usdCad;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExchangeRate that = (ExchangeRate) o;

        return date != null ? date.equals(that.date) : that.date == null;

    }

    @Override
    public int hashCode() {
        return date != null ? date.hashCode() : 0;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Double getEurUsd() {
        return eurUsd;
    }

    public void setEurUsd(Double eurUsd) {
        this.eurUsd = eurUsd;
    }

    public Double getGbpUsd() {
        return gbpUsd;
    }

    public void setGbpUsd(Double gbpUsd) {
        this.gbpUsd = gbpUsd;
    }

    public Double getAudUsd() {
        return audUsd;
    }

    public void setAudUsd(Double audUsd) {
        this.audUsd = audUsd;
    }

    public Double getNzdUsd() {
        return nzdUsd;
    }

    public void setNzdUsd(Double nzdUsd) {
        this.nzdUsd = nzdUsd;
    }

    public Double getUsdChf() {
        return usdChf;
    }

    public void setUsdChf(Double usdChf) {
        this.usdChf = usdChf;
    }

    public Double getUsdJpy() {
        return usdJpy;
    }

    public void setUsdJpy(Double usdJpy) {
        this.usdJpy = usdJpy;
    }

    public Double getUsdCad() {
        return usdCad;
    }

    public void setUsdCad(Double usdCad) {
        this.usdCad = usdCad;
    }
}
