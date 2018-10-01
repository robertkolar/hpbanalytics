package com.highpowerbear.hpbanalytics.report.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    public String getDate() {
        return date;
    }

    public Rates getRates() {
        return rates;
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

        @Override
        public String toString() {
            return "ExchangeRates{" +
                    "usd=" + usd +
                    ", gbp=" + gbp +
                    ", chf=" + chf +
                    ", aud=" + aud +
                    ", jpy=" + jpy +
                    ", krw=" + krw +
                    ", hkd=" + hkd +
                    '}';
        }
    }
}
