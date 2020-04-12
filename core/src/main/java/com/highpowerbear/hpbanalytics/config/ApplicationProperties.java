package com.highpowerbear.hpbanalytics.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Created by robertk on 4/5/2020.
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {

    private final Fixer fixer;

    public ApplicationProperties(Fixer fixer) {
        this.fixer = fixer;
    }

    public Fixer getFixer() {
        return fixer;
    }

    public static class Fixer {
        private final String url;
        private final String accessKey;
        private final String symbols;
        private final int daysBack;

        public Fixer(String url, String accessKey, String symbols, int daysBack) {
            this.url = url;
            this.accessKey = accessKey;
            this.symbols = symbols;
            this.daysBack = daysBack;
        }

        public String getUrl() {
            return url;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public String getSymbols() {
            return symbols;
        }

        public int getDaysBack() {
            return daysBack;
        }
    }
}
