package com.highpowerbear.hpbanalytics.report;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by robertk on 7/3/2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RatesVO {

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
        return "RatesVO{" +
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

        @Override
        public String toString() {
            return "Rates{" +
                    "usd=" + usd +
                    ", gbp=" + gbp +
                    ", chf=" + chf +
                    ", aud=" + aud +
                    ", jpy=" + jpy +
                    '}';
        }
    }
}
