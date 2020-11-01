package com.highpowerbear.shared;

import java.io.Serializable;

/**
 * Created by robertk on 11/1/2020.
 */
public class ExchangeRateDTO implements Serializable {
    private static final long serialVersionUID = 2177784790473590136L;

    private String date; // yyyy-MM-dd
    private Double eurUsd;
    private Double eurGbp;
    private Double eurChf;
    private Double eurAud;
    private Double eurJpy;
    private Double eurKrw;
    private Double eurHkd;
    private Double eurSgd;

    public double getRate(String baseCurrency, String transactionCurrency) {

        if ("EUR".equals(baseCurrency)) {
            switch (transactionCurrency) {
                case "EUR":
                    return 1d;
                case "USD":
                    return eurUsd;
                case "GBP":
                    return eurGbp;
                case "CHF":
                    return eurChf;
                case "AUD":
                    return eurAud;
                case "JPY":
                    return eurJpy;
                case "KRW":
                    return eurKrw;
                case "HKD":
                    return eurHkd;
                case "SGD":
                    return eurSgd;
            }
        }
        throw new IllegalStateException("exchange rate not available for " + baseCurrency + "/" + transactionCurrency);
    }

    public String getDate() {
        return date;
    }

    public ExchangeRateDTO setDate(String date) {
        this.date = date;
        return this;
    }

    public Double getEurUsd() {
        return eurUsd;
    }

    public ExchangeRateDTO setEurUsd(Double eurUsd) {
        this.eurUsd = eurUsd;
        return this;
    }

    public Double getEurGbp() {
        return eurGbp;
    }

    public ExchangeRateDTO setEurGbp(Double eurGbp) {
        this.eurGbp = eurGbp;
        return this;
    }

    public Double getEurChf() {
        return eurChf;
    }

    public ExchangeRateDTO setEurChf(Double eurChf) {
        this.eurChf = eurChf;
        return this;
    }

    public Double getEurAud() {
        return eurAud;
    }

    public ExchangeRateDTO setEurAud(Double eurAud) {
        this.eurAud = eurAud;
        return this;
    }

    public Double getEurJpy() {
        return eurJpy;
    }

    public ExchangeRateDTO setEurJpy(Double eurJpy) {
        this.eurJpy = eurJpy;
        return this;
    }

    public Double getEurKrw() {
        return eurKrw;
    }

    public ExchangeRateDTO setEurKrw(Double eurKrw) {
        this.eurKrw = eurKrw;
        return this;
    }

    public Double getEurHkd() {
        return eurHkd;
    }

    public ExchangeRateDTO setEurHkd(Double eurHkd) {
        this.eurHkd = eurHkd;
        return this;
    }

    public Double getEurSgd() {
        return eurSgd;
    }

    public ExchangeRateDTO setEurSgd(Double eurSgd) {
        this.eurSgd = eurSgd;
        return this;
    }

    @Override
    public String toString() {
        return "ExchangeRateDTO{" +
                "date='" + date + '\'' +
                ", eurUsd=" + eurUsd +
                ", eurGbp=" + eurGbp +
                ", eurChf=" + eurChf +
                ", eurAud=" + eurAud +
                ", eurJpy=" + eurJpy +
                ", eurKrw=" + eurKrw +
                ", eurHkd=" + eurHkd +
                ", eurSgd=" + eurSgd +
                '}';
    }
}
