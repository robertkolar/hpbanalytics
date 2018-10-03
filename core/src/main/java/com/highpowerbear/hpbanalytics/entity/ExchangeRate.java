package com.highpowerbear.hpbanalytics.entity;

import com.highpowerbear.hpbanalytics.enums.Currency;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by robertk on 5/29/2017.
 */
@Entity
@Table(name = "exchange_rate", schema = "hpbanalytics", catalog = "hpbanalytics")
public class ExchangeRate implements Serializable {
    private static final long serialVersionUID = 1L;

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

    public Double getRate(Currency base, Currency transaction) {
        switch (base) {
            case EUR:
                switch (transaction) {
                    case EUR: return 1d;
                    case USD: return eurUsd;
                    case GBP: return eurGbp;
                    case CHF: return eurChf;
                    case AUD: return eurAud;
                    case JPY: return eurJpy;
                    case KRW: return eurKrw;
                    case HKD: return eurHkd;
                    case SGD: return eurSgd;
                    default: return null;
                }
            default: return null;
        }
    }

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

    public Double getEurGbp() {
        return eurGbp;
    }

    public void setEurGbp(Double eurGbp) {
        this.eurGbp = eurGbp;
    }

    public Double getEurChf() {
        return eurChf;
    }

    public void setEurChf(Double eurChf) {
        this.eurChf = eurChf;
    }

    public Double getEurAud() {
        return eurAud;
    }

    public void setEurAud(Double eurAud) {
        this.eurAud = eurAud;
    }

    public Double getEurJpy() {
        return eurJpy;
    }

    public void setEurJpy(Double eurJpy) {
        this.eurJpy = eurJpy;
    }

    public Double getEurKrw() {
        return eurKrw;
    }

    public void setEurKrw(Double eurKrw) {
        this.eurKrw = eurKrw;
    }

    public Double getEurHkd() {
        return eurHkd;
    }

    public void setEurHkd(Double eurHkd) {
        this.eurHkd = eurHkd;
    }

    public Double getEurSgd() {
        return eurSgd;
    }

    public void setEurSgd(Double eurSgd) {
        this.eurSgd = eurSgd;
    }
}
