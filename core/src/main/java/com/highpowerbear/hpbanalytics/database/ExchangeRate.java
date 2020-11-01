package com.highpowerbear.hpbanalytics.database;

import com.highpowerbear.hpbanalytics.config.HanSettings;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/**
 * Created by robertk on 5/29/2017.
 */
@Entity
@Table(name = "exchange_rate", schema = HanSettings.DB_SCHEMA, catalog = HanSettings.DB_DATABASE)
public class ExchangeRate implements Serializable {
    private static final long serialVersionUID = 539031756808205732L;

    @Id
    private String date; // yyyy-MM-dd
    private Double eurUsd;
    private Double eurGbp;
    private Double eurChf;
    private Double eurAud;
    private Double eurJpy;
    private Double eurKrw;
    private Double eurHkd;
    private Double eurSgd;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExchangeRate that = (ExchangeRate) o;

        return Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return date != null ? date.hashCode() : 0;
    }

    public String getDate() {
        return date;
    }

    public ExchangeRate setDate(String date) {
        this.date = date;
        return this;
    }

    public Double getEurUsd() {
        return eurUsd;
    }

    public ExchangeRate setEurUsd(Double eurUsd) {
        this.eurUsd = eurUsd;
        return this;
    }

    public Double getEurGbp() {
        return eurGbp;
    }

    public ExchangeRate setEurGbp(Double eurGbp) {
        this.eurGbp = eurGbp;
        return this;
    }

    public Double getEurChf() {
        return eurChf;
    }

    public ExchangeRate setEurChf(Double eurChf) {
        this.eurChf = eurChf;
        return this;
    }

    public Double getEurAud() {
        return eurAud;
    }

    public ExchangeRate setEurAud(Double eurAud) {
        this.eurAud = eurAud;
        return this;
    }

    public Double getEurJpy() {
        return eurJpy;
    }

    public ExchangeRate setEurJpy(Double eurJpy) {
        this.eurJpy = eurJpy;
        return this;
    }

    public Double getEurKrw() {
        return eurKrw;
    }

    public ExchangeRate setEurKrw(Double eurKrw) {
        this.eurKrw = eurKrw;
        return this;
    }

    public Double getEurHkd() {
        return eurHkd;
    }

    public ExchangeRate setEurHkd(Double eurHkd) {
        this.eurHkd = eurHkd;
        return this;
    }

    public Double getEurSgd() {
        return eurSgd;
    }

    public ExchangeRate setEurSgd(Double eurSgd) {
        this.eurSgd = eurSgd;
        return this;
    }
}
