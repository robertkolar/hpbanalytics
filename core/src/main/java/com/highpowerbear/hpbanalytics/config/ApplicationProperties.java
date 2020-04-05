package com.highpowerbear.hpbanalytics.config;

import com.ib.client.Types;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Created by robertk on 4/5/2020.
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {

    private final Ibkr ibkr;
    private final Fixer fixer;

    public ApplicationProperties(Ibkr ibkr, Fixer fixer) {
        this.ibkr = ibkr;
        this.fixer = fixer;
    }

    public Ibkr getIbkr() {
        return ibkr;
    }

    public Fixer getFixer() {
        return fixer;
    }

    public static class Ibkr {
        private final String account;
        private final String host;
        private final int port;
        private final int clientId;

        public Ibkr(String account, String host, int port, int clientId, Types.TimeInForce orderTif) {
            this.account = account;
            this.host = host;
            this.port = port;
            this.clientId = clientId;
        }

        public String getAccount() {
            return account;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public int getClientId() {
            return clientId;
        }
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
