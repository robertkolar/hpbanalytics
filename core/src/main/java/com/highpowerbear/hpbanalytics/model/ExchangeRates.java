package com.highpowerbear.hpbanalytics.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.highpowerbear.hpbanalytics.enums.Currency;

/**
 * Created by robertk on 7/3/2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeRates {

    private boolean success;
    private long timestamp;
    private String base;
    private String date;

    private Rates rates;

    public boolean isSuccess() {
        return success;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getBase() {
        return base;
    }

    @JsonIgnore
    public Currency getBaseCurrency() {
        return base != null ? Currency.valueOf(base) : null;
    }

    public String getDate() {
        return date;
    }

    public Rates getRates() {
        return rates;
    }

    @JsonIgnore
    public Double getRate(Currency transactionCurrency) {
        switch (transactionCurrency) {
            case EUR: return rates.eur;
            case USD: return rates.usd;
            case GBP: return rates.gbp;
            case CHF: return rates.chf;
            case AUD: return rates.aud;
            case JPY: return rates.jpy;
            case KRW: return rates.krw;
            case HKD: return rates.hkd;
            case SGD: return rates.sgd;
            default: return null;
        }
    }

    @Override
    public String toString() {
        return "ExchangeRates{" +
                "success=" + success +
                ", timestamp=" + timestamp +
                ", base='" + base + '\'' +
                ", date='" + date + '\'' +
                ", rates=" + rates +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Rates {
        @JsonProperty("EUR")
        private double eur;
        @JsonProperty("USD")
        private double usd;
        @JsonProperty("GBP")
        private double gbp;
        @JsonProperty("CHF")
        private double chf;
        @JsonProperty("AUD")
        private double aud;
        @JsonProperty("JPY")
        private double jpy;
        @JsonProperty("KRW")
        private double krw;
        @JsonProperty("HKD")
        private double hkd;
        @JsonProperty("SGD")
        private double sgd;

        public double getEur() {
            return eur;
        }

        public double getUsd() {
            return usd;
        }

        public double getGbp() {
            return gbp;
        }

        public double getChf() {
            return chf;
        }

        public double getAud() {
            return aud;
        }

        public double getJpy() {
            return jpy;
        }

        public double getKrw() {
            return krw;
        }

        public double getHkd() {
            return hkd;
        }

        public double getSgd() {
            return sgd;
        }

        @Override
        public String toString() {
            return "Rates{" +
                    "eur=" + eur +
                    ", usd=" + usd +
                    ", gbp=" + gbp +
                    ", chf=" + chf +
                    ", aud=" + aud +
                    ", jpy=" + jpy +
                    ", krw=" + krw +
                    ", hkd=" + hkd +
                    ", sgd=" + sgd +
                    '}';
        }
    }
}
